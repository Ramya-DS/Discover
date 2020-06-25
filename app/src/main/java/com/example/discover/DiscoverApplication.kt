package com.example.discover

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import androidx.collection.LruCache
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.discover.category.MediaCategoryApiCall
import com.example.discover.datamodel.Language
import com.example.discover.datamodel.genre.GenreResult
import com.example.discover.datamodel.genre.Genres
import com.example.discover.filterScreen.FilterApiCall
import com.example.discover.firstScreen.GenreApiCall
import com.example.discover.genreScreen.GenreMediaApiCall
import com.example.discover.mediaScreenUtils.apiCalls.MovieDetailApiCall
import com.example.discover.roomDatabase.DiscoverDatabase
import com.example.discover.roomDatabase.dao.GenreDao
import com.example.discover.roomDatabase.dao.LanguageDao
import com.example.discover.searchScreen.SeachApiCall
import com.example.discover.showsScreen.SeasonApiCall
import com.example.discover.showsScreen.ShowDetailApiCall
import com.example.discover.util.LoadingFragment
import com.jakewharton.disklrucache.DiskLruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.*
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class DiscoverApplication : Application() {

    lateinit var genreApiCall: GenreApiCall
    lateinit var filterApiCall: FilterApiCall
    lateinit var searchApiCall: SeachApiCall
    lateinit var mediaCategoryApiCall: MediaCategoryApiCall
    lateinit var movieDetailApiCall: MovieDetailApiCall
    lateinit var seasonApiCall: SeasonApiCall
    lateinit var showDetailApiCall: ShowDetailApiCall
    lateinit var genreMediaApiCall: GenreMediaApiCall

    lateinit var memoryCache: LruCache<String, Bitmap>

    lateinit var movieGenres: LiveData<List<Genres>>
    lateinit var showsGenres: LiveData<List<Genres>>
    lateinit var languages: LiveData<List<Language>>

    lateinit var genreDao: GenreDao
    lateinit var languageDao: LanguageDao

    var diskLruCache: DiskLruCache? = null
    val diskCacheLock = ReentrantLock()
    private val diskCacheLockCondition: Condition = diskCacheLock.newCondition()
    private var diskCacheStarting = true

    companion object {
        private const val DISK_CACHE_SIZE: Long = 1024 * 1024 * 10 // 10MB
        private const val DISK_CACHE_SUB_DIR = "thumbnails"
    }

    override fun onCreate() {
        super.onCreate()
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val newRequest: Request = chain.request().newBuilder()
                .addHeader(
                    "Authorization",
                    "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI3YWQ1NzE0ZTA2OGVhMmE0ZjJhNmRjMDVjMGViODdiNSIsInN1YiI6IjVlYmU2YTA0YmM4YWJjMDAyMWMzMTdhYiIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.9Ar7uY2Ktj8uEMQUrRHKXrkXGIbqlDtRoFSq_Rbw1vo"
                )
                .build()
            chain.proceed(newRequest)
        }.build()

        val retrofit = Retrofit.Builder()
            .client(client)
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        genreApiCall = retrofit.create(GenreApiCall::class.java)
        filterApiCall = retrofit.create(FilterApiCall::class.java)
        searchApiCall = retrofit.create(SeachApiCall::class.java)
        mediaCategoryApiCall = retrofit.create(MediaCategoryApiCall::class.java)
        movieDetailApiCall = retrofit.create(MovieDetailApiCall::class.java)
        seasonApiCall = retrofit.create(SeasonApiCall::class.java)
        showDetailApiCall = retrofit.create(ShowDetailApiCall::class.java)
        genreMediaApiCall = retrofit.create(GenreMediaApiCall::class.java)

        genreDao = DiscoverDatabase.getDatabase(applicationContext).genresUtilsDao()
        languageDao = DiscoverDatabase.getDatabase(applicationContext).languageDao()
        memoryCache = accessCache()

        movieGenres = getGenres(true)
        showsGenres = getGenres(false)
        languages = getAllLanguages()

        val cacheDir = getDiskCacheDir(this, DISK_CACHE_SUB_DIR)
        InitDiskCacheTask().execute(cacheDir)
    }

    private fun accessCache(): LruCache<String, Bitmap> {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8
        return object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
        }
    }

    internal inner class InitDiskCacheTask : AsyncTask<File, Void, Void>() {
        override fun doInBackground(vararg params: File): Void? {
            diskCacheLock.withLock {
                val cacheDir = params[0]
                diskLruCache = DiskLruCache.open(cacheDir, 1, 1, DISK_CACHE_SIZE)
                diskCacheStarting = false // Finished initialization
                diskCacheLockCondition.signalAll() // Wake any waiting threads
            }
            return null
        }
    }

    private fun getDiskCacheDir(context: Context, uniqueName: String): File {
        val cachePath =
            if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
                || !Environment.isExternalStorageRemovable()
            ) {
                context.externalCacheDir?.path
            } else {
                context.cacheDir.path
            }

        return File(cachePath + File.separator + uniqueName)
    }

    fun containsKey(key: String?): Boolean {
        var contained = false
        var snapshot: DiskLruCache.Snapshot? = null
        try {
            snapshot = diskLruCache?.get(key)
            contained = snapshot != null
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            snapshot?.close()
        }
        return contained
    }

    fun put(key: String, data: Bitmap) {
        var editor: DiskLruCache.Editor? = null
        try {
            editor = diskLruCache?.edit(key)
            if (editor == null) {
                return
            }
            if (writeBitmapToFile(data, editor)) {
                diskLruCache?.flush()
                editor.commit()
                if (BuildConfig.DEBUG) {
                    Log.d("cache_test_DISK_", "image put on disk cache $key")
                }
            } else {
                editor.abort()
                if (BuildConfig.DEBUG) {
                    Log.d("cache_test_DISK_", "ERROR on: image put on disk cache $key")
                }
            }
        } catch (e: IOException) {
            if (BuildConfig.DEBUG) {
                Log.d("cache_test_DISK_", "ERROR on: image put on disk cache $key")
            }
            try {
                editor?.abort()
            } catch (ignored: IOException) {
            }
        }
    }

    @Throws(IOException::class, FileNotFoundException::class)
    private fun writeBitmapToFile(
        bitmap: Bitmap,
        editor: DiskLruCache.Editor
    ): Boolean {
        var out: OutputStream? = null
        return try {
            out = BufferedOutputStream(editor.newOutputStream(0), 8 * 1024)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out)
        } finally {
            out?.close()
        }
    }

    fun getBitmap(key: String): Bitmap? {
        var bitmap: Bitmap? = null
        var snapshot: DiskLruCache.Snapshot? = null
        try {
            snapshot = diskLruCache?.get(key)
            if (snapshot == null) {
                return null
            }
            val `in` = snapshot.getInputStream(0)
            if (`in` != null) {
                val buffIn = BufferedInputStream(`in`, 8 * 1024)
                bitmap = BitmapFactory.decodeStream(buffIn)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            snapshot?.close()
        }
        if (BuildConfig.DEBUG) {
            Log.d(
                "cache_test_DISK_",
                if (bitmap == null) "" else "image read from disk $key"
            )
        }
        return bitmap
    }

    private fun getGenres(isMovie: Boolean): LiveData<List<Genres>> {
        val list = MutableLiveData<List<Genres>>()

        val call =
            if (isMovie) genreApiCall.getAllMovieGenres() else genreApiCall.getAllShowsGenres()
        call.enqueue(object : Callback<GenreResult> {
            override fun onFailure(call: Call<GenreResult>, t: Throwable) {
                runBlocking {
                    withContext(Dispatchers.IO) {
                        list.postValue(genreDao.getMediaGenres(isMovie))
                    }
                }
            }

            override fun onResponse(call: Call<GenreResult>, response: Response<GenreResult>) {
                if (response.isSuccessful) {
                    insertAllGenres(response.body()!!.genres, isMovie)
                    list.postValue(response.body()!!.genres)
                } else Log.d("OnError", fetchErrorMessage(response.errorBody()!!))
            }

        })
        return list
    }

    private fun insertAllGenres(list: List<Genres>, isMovie: Boolean) = Thread {
        for (i in list)
            i.isMovie = isMovie
        genreDao.insertAllGenres(list)

    }.start()


    fun fetchErrorMessage(error: ResponseBody): String {
        val reader: BufferedReader?
        val sb = StringBuilder()
        try {
            reader =
                BufferedReader(InputStreamReader(error.byteStream()))
            var line: String?
            try {
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return sb.toString()
    }

    private fun getAllLanguages(): LiveData<List<Language>> {
        val call = genreApiCall.getLanguages()
        val languages = MutableLiveData<List<Language>>()
        call.enqueue(object : Callback<List<Language>> {
            override fun onFailure(call: Call<List<Language>>, t: Throwable) {
                runBlocking {
                    withContext(Dispatchers.IO) {
                        languages.postValue(languageDao.getAllLanguages())
                    }
                }
            }

            override fun onResponse(
                call: Call<List<Language>>,
                response: Response<List<Language>>
            ) {
                if (response.isSuccessful) {
                    insertAllLanguages(response.body()!!)
                    languages.postValue(response.body()!!)
                } else
                    Log.d("FirstScreenViewModel", "Languages Fetch Error")
            }
        })
        return languages
    }

    private fun insertAllLanguages(list: List<Language>) = Thread {
        languageDao.insertAllLanguages(list)
    }.start()

    fun checkConnectivity(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        if (cm.getNetworkInfo(TYPE_MOBILE)?.state == NetworkInfo.State.CONNECTED
//            || cm.getNetworkInfo(TYPE_WIFI)?.state == NetworkInfo.State.CONNECTED
//        ) {
//            Log.d("Connectivity","true")
//            return true
//
//        }
//        Log.d("Connectivity","false")
//        return false

//        for (network in cm.allNetworks)
//        { try
//            {
//                val info = cm.getNetworkInfo(network);
//
//                if (info == null || !info.isAvailable || info.subtype.HasFla(ConnectivityType.Dummy))
//                    continue;
//
//                if (info.IsConnected)
//                    return true;
//            }
//            catch
//            {
//                //there is a possibility, but don't worry
//            }
//        }
//        return false;

        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true
        Log.d("isConnected", isConnected.toString())
        return isConnected
    }

    fun displayLoadingFragment(fragmentManager: FragmentManager, container: Int) {
        fragmentManager.beginTransaction()
            .replace(container, LoadingFragment(), "${container}_load").commit()
    }

    fun removeLoadingFragment(fragmentManager: FragmentManager, container: Int) {
        fragmentManager.findFragmentByTag("${container}_load")?.let {
            fragmentManager.beginTransaction().remove(it).commit()
        }
    }
}