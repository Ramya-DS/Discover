package com.example.discover.firstScreen

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.discover.DiscoverApplication
import com.example.discover.datamodel.Language
import com.example.discover.datamodel.genre.GenreResult
import com.example.discover.datamodel.genre.Genres
import com.example.discover.roomDatabase.DiscoverDatabase
import com.example.discover.roomDatabase.dao.GenreDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


//class FirstScreenViewModel(mApplication: Application) : AndroidViewModel(mApplication) {
//
//    private val genreDao: GenreDao =
//        DiscoverDatabase.getDatabase(mApplication).genresUtilsDao()
//
//    private val genreApiCall = (mApplication as DiscoverApplication).genreApiCall
//
//    fun getGenres(isMovie: Boolean): LiveData<List<Genres>> {
//        val list = MutableLiveData<List<Genres>>()
//
//        val call =
//            if (isMovie) genreApiCall.getAllMovieGenres() else genreApiCall.getAllShowsGenres()
//        call.enqueue(object : Callback<GenreResult> {
//            override fun onFailure(call: Call<GenreResult>, t: Throwable) {
//                viewModelScope.launch(Dispatchers.IO) {
//                    list.postValue(genreDao.getMediaGenres(isMovie))
//                }
//            }
//
//            override fun onResponse(call: Call<GenreResult>, response: Response<GenreResult>) {
//                if (response.isSuccessful) {
//                    insertAllGenres(response.body()!!.genres, isMovie)
//                    list.postValue(response.body()!!.genres)
//                } else Log.d("OnError", fetchErrorMessage(response.errorBody()!!))
//            }
//
//        })
//        return list
//    }
//
//    private fun insertAllGenres(list: List<Genres>, isMovie: Boolean) =
//        viewModelScope.launch(Dispatchers.IO) {
//            for (i in list)
//                i.isMovie = isMovie
//            genreDao.insertAllGenres(list)
//
//        }
//
//    private fun fetchErrorMessage(error: ResponseBody): String {
//        val reader: BufferedReader?
//        val sb = StringBuilder()
//        try {
//            reader =
//                BufferedReader(InputStreamReader(error.byteStream()))
//            var line: String?
//            try {
//                while (reader.readLine().also { line = it } != null) {
//                    sb.append(line)
//                }
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//
//        return sb.toString()
//    }
//
//    fun getLanguages(): LiveData<List<Language>> {
//        val call = genreApiCall.getLanguages()
//        val languages = MutableLiveData<List<Language>>()
//        call.enqueue(object : Callback<List<Language>> {
//            override fun onFailure(call: Call<List<Language>>, t: Throwable) {
//                Log.d("FirstScreenViewModel", "Languages Fetch Failure")
//            }
//
//            override fun onResponse(
//                call: Call<List<Language>>,
//                response: Response<List<Language>>
//            ) {
//                if (response.isSuccessful) {
//                    insertAllLanguages(response.body()!!)
//                    languages.postValue(response.body()!!)
//                } else
//                    Log.d("FirstScreenViewModel", "Languages Fetch Error")
//            }
//        })
//        return languages
//    }
//
//    private fun insertAllLanguages(list: List<Language>) = viewModelScope.launch(Dispatchers.IO) {
//        genreDao.insertAllLanguages(list)
//    }
//}