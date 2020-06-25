package com.example.discover.util

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.widget.ImageView
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.example.discover.DiscoverApplication
import com.example.discover.R
import java.io.BufferedInputStream
import java.io.InputStream
import java.lang.ref.WeakReference
import java.net.URL
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import kotlin.concurrent.withLock

class LoadPosterImage(
    private val url: String?,
    private val imageView: ImageView,
    private val activity: WeakReference<Activity>
) {
    companion object {
        val executors: ThreadPoolExecutor = Executors.newFixedThreadPool(20) as ThreadPoolExecutor
        private val handlerThread = HandlerThread("memoryPut").apply {
            start()
        }
        val handler = Handler(handlerThread.looper)
    }

    private var bitmap = MutableLiveData<Bitmap?>().apply { postValue(null) }
    private var thread: Thread? = null

    fun loadImage() {
        executors.execute {
            try {
                thread = Thread.currentThread()
                val inputStream: InputStream?
                if (url != null) {
                    val app = (activity.get()?.application as DiscoverApplication)
                    val key = createKey(url)

                    var mBitmap = checkInMemoryAndDisk(key, app).apply {
                        bitmap.postValue(this)
                    }

                    if (mBitmap == null) {
                        val url = "https://image.tmdb.org/t/p/w154/$url"
                        inputStream = URL(url).openStream()
                        mBitmap = inputStream?.let {
                            createScaledBitmapFromStream(inputStream).apply {
                                bitmap.postValue(this)
                            }
                        }
                        if (mBitmap != null) {
                            handler.post { writeInMemoryAndDiskCache(key, mBitmap, app) }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d("LoadImage", "Error occurred. ${e.message}")
            }
        }
        setPosterImageToView()
    }

    fun loadImage(width: Int, saveToOnlyMemoryCache: Boolean) {
        executors.execute {
            try {
                val height = (width / 1.777777).toInt()
                thread = Thread.currentThread()
                val inputStream: InputStream?
                if (url != null) {
                    val app = (activity.get()?.application as DiscoverApplication)
                    val key = createKey(url)

                    var mBitmap =
                        if (saveToOnlyMemoryCache) checkInMemoryCache(key, app) else checkInMemoryAndDisk(key, app)

                    mBitmap.apply {
                        bitmap.postValue(this)
                    }

                    if (mBitmap == null) {
                        val url = "https://image.tmdb.org/t/p/w780/$url"

                        inputStream = URL(url).openStream()
                        mBitmap = inputStream?.let {
                            createScaledBitmapFromStream(inputStream, width, height).apply {
                                bitmap.postValue(this)
                            }
                        }
                        if (mBitmap != null) {
                            if (saveToOnlyMemoryCache)
                                handler.post { writeToMemoryCache(key, mBitmap, app) }
                            else
                                handler.post { writeInMemoryAndDiskCache(key, mBitmap, app) }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d("LoadImage", "Error occurred. ${e.message}")
            }
        }
        setBackdropImageToView()
    }

    fun loadCreditImage(saveToCache: Boolean) {
        executors.execute {
            try {
                val width = 100
                val height = 100
                thread = Thread.currentThread()
                val inputStream: InputStream?
                if (url != null) {
                    val app = (activity.get()?.application as DiscoverApplication)
                    val key = createKey(url)

                    var mBitmap =
                        if (!saveToCache) checkInMemoryAndDisk(key, app) else checkInMemoryCache(key, app)

                    mBitmap.apply {
                        bitmap.postValue(this)
                    }

                    if (mBitmap == null) {
                        val url = "https://image.tmdb.org/t/p/w780/$url"

                        inputStream = URL(url).openStream()
                        mBitmap = inputStream?.let {
                            createScaledBitmapFromStream(inputStream, width, height).apply {
                                bitmap.postValue(this)
                            }
                        }
                        if (mBitmap != null) {
                            if (saveToCache)
                                handler.post { writeToMemoryCache(key, mBitmap, app) }
                            else
                                handler.post { writeInMemoryAndDiskCache(key, mBitmap, app) }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d("LoadImage", "Error occurred. ${e.message}")
            }
        }
        setCreditImageToView()
    }

    @WorkerThread
    private fun createScaledBitmapFromStream(
        s: InputStream,
        minimumDesiredBitmapWidth: Int = 360,
        minimumDesiredBitmapHeight: Int = 540
    ): Bitmap? {
        val stream = BufferedInputStream(s, 8 * 1024)
        val decodeBitmapOptions = BitmapFactory.Options()
        if (minimumDesiredBitmapWidth > 0 && minimumDesiredBitmapHeight > 0) {
            val decodeBoundsOptions = BitmapFactory.Options()
            decodeBoundsOptions.inJustDecodeBounds = true
            stream.mark(8 * 1024)
            BitmapFactory.decodeStream(stream, null, decodeBoundsOptions)
            stream.reset()
            val originalWidth: Int = decodeBoundsOptions.outWidth
            val originalHeight: Int = decodeBoundsOptions.outHeight

            val scale =
                (originalWidth / minimumDesiredBitmapWidth).coerceAtMost(originalHeight / minimumDesiredBitmapHeight)

            val bitmap: Bitmap?
            if (scale > 1) {
                decodeBitmapOptions.inSampleSize = 1.coerceAtLeast(scale)
                bitmap = BitmapFactory.decodeStream(stream, null, decodeBitmapOptions)

            } else
                bitmap = BitmapFactory.decodeStream(stream)
            stream.close()
            return bitmap
        }
        return null
    }

    private fun createKey(path: String): String {
        return path.substringAfter('/').substringBefore('.').toLowerCase(Locale.ENGLISH)
    }

    private fun getBitmapFromDiskCache(key: String, application: DiscoverApplication): Bitmap? {
        if (application.containsKey(key)) {
            val bitmap = application.getBitmap(key)
            application.memoryCache.put(key, bitmap!!)
            return bitmap
        }

        return null
    }

    private fun checkInMemoryAndDisk(key: String, application: DiscoverApplication): Bitmap? {
        val image = fetchMemoryCacheImage(key, application)
        if (image != null) {
            return image
        }
        return getBitmapFromDiskCache(key, application)
    }

    private fun checkInMemoryCache(key: String, application: DiscoverApplication): Bitmap? {
        val image = fetchMemoryCacheImage(key, application)
        if (image != null) {
            return image
        }
        return null
    }

    private fun fetchMemoryCacheImage(key: String, application: DiscoverApplication): Bitmap? {
        val memoryCache = (application).memoryCache
        return memoryCache[key]
    }

    @WorkerThread
    private fun writeInMemoryAndDiskCache(key: String, bitmap: Bitmap, application: DiscoverApplication) {
        application.apply {
            memoryCache.put(key, bitmap)
            diskCacheLock.withLock {
                if (!containsKey(key)) {
                    put(key, bitmap)
                }
            }
        }
    }

    @WorkerThread
    private fun writeToMemoryCache(key: String, bitmap: Bitmap, application: DiscoverApplication) {
        application.apply {
            memoryCache.put(key, bitmap)
        }
    }

    fun interruptThread() {
        thread?.interrupt()
    }

    @MainThread
    private fun setPosterImageToView() {
        bitmap.observeForever {
            if (it != null) {
                imageView.scaleType = ImageView.ScaleType.FIT_XY
                imageView.setImageBitmap(it)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    activity.get()?.startPostponedEnterTransition()
                }
            } else {
                imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        imageView.context,
                        R.drawable.ic_media_placeholder
                    )
                )
            }
        }
    }

    @MainThread
    private fun setBackdropImageToView() {
        bitmap.observeForever {
            if (it != null) {
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                imageView.setImageBitmap(it)
            } else {
                imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        imageView.context,
                        R.drawable.ic_media_placeholder
                    )
                )
            }
        }
    }

    @MainThread
    private fun setCreditImageToView() {
        bitmap.observeForever {
            if (it != null) {
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                imageView.setImageBitmap(it)
            } else {
                imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        imageView.context,
                        R.drawable.ic_credit_placeholder
                    )
                )
            }
        }
    }
}
