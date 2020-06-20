package com.example.discover.searchScreen

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.discover.DiscoverApplication
import com.example.discover.datamodel.movie.MoviesList
import com.example.discover.datamodel.movie.preview.MoviePreview
import com.example.discover.datamodel.multiSearch.MultiSearch
import com.example.discover.datamodel.multiSearch.MultiSearchResult
import com.example.discover.datamodel.suggestions.Suggestion
import com.example.discover.datamodel.tvshow.preview.ShowPreview
import com.example.discover.datamodel.tvshow.preview.ShowsList
import com.example.discover.roomDatabase.DiscoverDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class SearchViewModel(private val mApplication: Application) : AndroidViewModel(mApplication) {

    val queryMap = HashMap<String, String>().apply { put("page", "1") }
    var isMovie: Boolean? = null
    var isLinear: Boolean = true

    var onNetworkLostListener: OnNetworkLostListener? = null

    var focus = true

    lateinit var moviesList: List<MoviePreview>
    lateinit var showsList: List<ShowPreview>
    lateinit var multiList: List<MultiSearch>

    var fragmentExists = false
    var position = 0

    private val suggestionsDao: SuggestionsDao =
        DiscoverDatabase.getDatabase(mApplication).suggestionDao()


    fun insertQuery(query: String) = viewModelScope.launch(Dispatchers.IO) {
        suggestionsDao.insertQuery(Suggestion(query))
    }

    fun getSuggestions(query: String): LiveData<List<String>> {
        val suggestions = MutableLiveData<List<String>>()
        viewModelScope.launch(Dispatchers.IO) {
            suggestions.postValue(suggestionsDao.getSuggestions("%$query%"))
        }
        return suggestions
    }

    fun getSearchedMovies(): LiveData<List<MoviePreview>> {
        val result = MutableLiveData<List<MoviePreview>>()
        val call = getSearchApiCall().movieSearch(queryMap)
        call.enqueue(object : Callback<MoviesList> {
            override fun onFailure(call: Call<MoviesList>, t: Throwable) {
                onNetworkLostListener?.onNetworkLostFragment()
            }

            override fun onResponse(call: Call<MoviesList>, response: Response<MoviesList>) {
                if (response.isSuccessful) {
                    val movieResult = response.body()!!
                    result.value = movieResult.results
                } else
                    Log.d("Search", "Error" + (mApplication as DiscoverApplication).fetchErrorMessage(response.errorBody()!!))
            }
        })
        return result
    }

    fun getSearchedShows(): LiveData<List<ShowPreview>> {
        val result = MutableLiveData<List<ShowPreview>>()
        val call = getSearchApiCall().showsSearch(queryMap)
        call.enqueue(object : Callback<ShowsList> {
            override fun onFailure(call: Call<ShowsList>, t: Throwable) {
                onNetworkLostListener?.onNetworkLostFragment()
            }

            override fun onResponse(call: Call<ShowsList>, response: Response<ShowsList>) {
                if (response.isSuccessful) {
                    val tvResult = response.body()!!
                    result.value = tvResult.results
                } else
                    Log.d("Search", "Error" + (mApplication as DiscoverApplication).fetchErrorMessage(response.errorBody()!!))
            }
        })

        return result
    }


    fun getMultiSearchResult(): LiveData<List<MultiSearch>> {
        val result = MutableLiveData<List<MultiSearch>>()
        val call = getSearchApiCall().multiSearch(queryMap)
        call.enqueue(object : Callback<MultiSearchResult> {
            override fun onFailure(call: Call<MultiSearchResult>, t: Throwable) {
                onNetworkLostListener?.onNetworkLostFragment()
            }

            override fun onResponse(
                call: Call<MultiSearchResult>,
                response: Response<MultiSearchResult>
            ) {
                if (response.isSuccessful) {
                    val multiSearchResult = response.body()!!
                    result.value = multiSearchResult.results
                } else
                    Log.d("Search", "Error" + (mApplication as DiscoverApplication).fetchErrorMessage(response.errorBody()!!))
            }

        })
        return result
    }

    private fun getSearchApiCall() = (mApplication as DiscoverApplication).searchApiCall
}