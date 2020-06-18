package com.example.discover.filterScreen

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.discover.DiscoverApplication
import com.example.discover.datamodel.movie.MoviesList
import com.example.discover.datamodel.movie.preview.MoviePreview
import com.example.discover.datamodel.tvshow.preview.ShowPreview
import com.example.discover.datamodel.tvshow.preview.ShowsList
import com.example.discover.searchScreen.OnNetworkLostListener
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class FilterViewModel(private val mApplication: Application) : AndroidViewModel(mApplication) {

    var queryMap = HashMap<String, String>().apply {
        put("media", "movie")
    }

    var onNetworkLostListener: OnNetworkLostListener? = null

    var movies: List<MoviePreview>? = null
    var shows: List<ShowPreview>? = null
    var isMovie: Boolean = true
    var isLinear = true
    var size = 0
    var position = 0

    fun discoverMovies(parameter: Map<String, String>): LiveData<List<MoviePreview>> {
        val call = getDiscoverApiCall().discoverMovies(parameter)
        return sendDiscoverMoviesRequestCall(call)
    }

    fun discoverTv(parameter: Map<String, String>): LiveData<List<ShowPreview>> {
        val call = getDiscoverApiCall().discoverShows(parameter)
        return sendDiscoverTvRequestCall(call)
    }

    private fun sendDiscoverMoviesRequestCall(
        call: Call<MoviesList>
    ): LiveData<List<MoviePreview>> {
        val result = MutableLiveData<List<MoviePreview>>()
        call.enqueue(object : Callback<MoviesList> {
            override fun onFailure(call: Call<MoviesList>, t: Throwable) {
                Log.d("MovieMain", "Failure ${t.message}")
                onNetworkLostListener?.onNetworkLostFragment()
            }

            override fun onResponse(call: Call<MoviesList>, response: Response<MoviesList>) {
                if (response.isSuccessful) {
                    onNetworkLostListener?.onNetworkDialogDismiss()
                    Log.d("result", response.body()!!.results.size.toString())
                    result.value = response.body()!!.results
                } else {
                    Log.d("MovieMain", "Error" + fetchErrorMessage(response.errorBody()!!))
                }
            }
        })
        return result
    }

    private fun sendDiscoverTvRequestCall(
        call: Call<ShowsList>
    ): LiveData<List<ShowPreview>> {
        val result = MutableLiveData<List<ShowPreview>>()
        call.enqueue(object : Callback<ShowsList> {
            override fun onFailure(call: Call<ShowsList>, t: Throwable) {
                Log.d("MovieMain", "Failure ${t.message}")
                onNetworkLostListener?.onNetworkLostFragment()
            }

            override fun onResponse(call: Call<ShowsList>, response: Response<ShowsList>) {
                if (response.isSuccessful) {
                    Log.d("result", response.body()!!.results.size.toString())
                    onNetworkLostListener?.onNetworkDialogDismiss()
                    result.value = response.body()!!.results
                } else {
                    Log.d("MovieMain", "Error" + fetchErrorMessage(response.errorBody()!!))
                }
            }
        })
        return result
    }

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

    private fun getDiscoverApiCall(): FilterApiCall {
        return (mApplication as DiscoverApplication).filterApiCall
    }
}