package com.example.discover.keywordScreen

import android.app.Application
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.discover.DiscoverApplication
import com.example.discover.datamodel.genre.GenreMedia
import com.example.discover.datamodel.media.Media
import com.example.discover.datamodel.movie.MoviesList
import com.example.discover.datamodel.movie.preview.MoviePreview
import com.example.discover.datamodel.tvshow.preview.ShowPreview
import com.example.discover.datamodel.tvshow.preview.ShowsList
import com.example.discover.roomDatabase.DiscoverDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class KeywordScreenViewModel(private val mApplication: Application) : AndroidViewModel(mApplication){

    var movies: List<MoviePreview>? = null
    var shows: List<ShowPreview>? = null

    var isLinear = true
    var position = 0

    private val handlerThread = HandlerThread("insert").apply {
        start()
    }
    private val handler = Handler(handlerThread.looper)

    private val genreMediaApiCall = (mApplication as DiscoverApplication).keywordApiCall

    private val categoryDao = DiscoverDatabase.getDatabase(mApplication).mediaCategoryDao()

    fun keywordRelatedMovies(genreId: Int, page: Int): LiveData<List<MoviePreview>> {
        val call = genreMediaApiCall.getKeywordRelatedMovies(genreId, page)
        val movies = MutableLiveData<List<MoviePreview>>()
        call.enqueue(object : Callback<MoviesList> {
            override fun onFailure(call: Call<MoviesList>, t: Throwable) {
                if (page == 1)
                    getGenreMovies(genreId).observeForever {
                        movies.postValue(it)
                    }
            }

            override fun onResponse(call: Call<MoviesList>, response: Response<MoviesList>) {
                if (response.isSuccessful) {
                    movies.postValue(response.body()!!.results)
                    writeMoviesPreviewsToDb(response.body()!!.results)
                } else Log.d("KeywordScreenViewModel", (mApplication as DiscoverApplication).fetchErrorMessage(response.errorBody()!!))
            }
        })

        return movies
    }

    fun keywordRelatedShows(genreId: Int, page: Int): LiveData<List<ShowPreview>> {
        val call = genreMediaApiCall.getKeywordRelatedShows(genreId, page)
        val shows = MutableLiveData<List<ShowPreview>>()
        call.enqueue(object : Callback<ShowsList> {
            override fun onFailure(call: Call<ShowsList>, t: Throwable) {
                if (page == 1)
                    getGenreShows(genreId).observeForever {
                        shows.postValue(it)
                    }
            }

            override fun onResponse(call: Call<ShowsList>, response: Response<ShowsList>) {
                if (response.isSuccessful) {
                    shows.postValue(response.body()!!.results)
                    writeShowsPreviewsToDb(response.body()!!.results)
                } else Log.d("KeywordScreenViewModel", (mApplication as DiscoverApplication).fetchErrorMessage(response.errorBody()!!))
            }
        })

        return shows
    }

    private fun writeShowsPreviewsToDb(shows: List<ShowPreview>) {
        handler.post{
            for (i in shows) {
                i.apply {
                    insertMedia(
                        Media(
                            false,
                            id,
                            backdrop_path,
                            original_language,
                            overview,
                            poster_path,
                            vote_average,
                            vote_count,
                            name = name,
                            first_air_date = first_air_date,
                            origin_country = origin_country.toString()
                        )
                    )
                    insertMediaGenres(id, genre_ids, false)
                    insertType(i.id, false)
                }
            }
        }
    }

    fun writeMoviesPreviewsToDb(list: List<MoviePreview>) {
        handler.post{
            for (i in list) {
                i.apply {
                    insertMedia(
                        Media(
                            true,
                            id,
                            backdrop_path,
                            original_language,
                            overview,
                            poster_path,
                            vote_average,
                            vote_count,
                            adult = adult,
                            release_date = release_date,
                            title = title
                        )
                    )
                    insertMediaGenres(id, genre_ids, true)
                    insertType(i.id, true)
                }
            }
        }
    }

    private fun insertMedia(media: Media) {
        categoryDao.insertMedia(media)
    }

    private fun insertMediaGenres(id: Int, genres: List<Int>, isMovie: Boolean) {
        categoryDao.insertMediaGenresIds(id, genres, isMovie)
    }

    private fun insertType(api_id: Int, isMovie: Boolean) {
        categoryDao.insertType(api_id, 0, isMovie)
    }

    private fun getGenreMovies(id: Int): LiveData<List<MoviePreview>> {
        val results = MutableLiveData<List<MoviePreview>>()
        viewModelScope.launch(Dispatchers.IO) {
            results.postValue(formatToMoviePreview(categoryDao.getGenreMedia(id, true)))
        }
        return results
    }

    private fun getGenreShows(id: Int): LiveData<List<ShowPreview>> {
        val results = MutableLiveData<List<ShowPreview>>()
        viewModelScope.launch(Dispatchers.IO) {
            results.postValue(formatToShowPreview(categoryDao.getGenreMedia(id, false)))
        }
        return results
    }

    private fun formatToShowPreview(genreMedia: GenreMedia?): MutableList<ShowPreview> {
        val shows = mutableListOf<ShowPreview>()
        genreMedia?.let {
            for (i in it.medias) {
                i.apply {
                    shows.add(
                        ShowPreview(
                            api_id,
                            name,
                            vote_count,
                            vote_average,
                            first_air_date,
                            poster_path,
                            getGenreIds(id),
                            original_language,
                            backdrop_path,
                            overview,
                            origin_country.substringAfter('[').substringBefore(']').split(',')
                        )
                    )
                }
            }
        }
        shows.shuffle()
        return shows
    }

    private fun formatToMoviePreview(genreMedia: GenreMedia?): MutableList<MoviePreview> {
        val movies = mutableListOf<MoviePreview>()
        genreMedia?.let {
            for (i in it.medias) {
                i.apply {
                    movies.add(
                        MoviePreview(
                            adult,
                            backdrop_path,
                            getGenreIds(id),
                            api_id,
                            original_language,
                            overview,
                            poster_path,
                            release_date,
                            title,
                            vote_average,
                            vote_count
                        )
                    )
                }
            }
        }
        movies.shuffle()
        return movies
    }

    private fun getGenreIds(id: Int): List<Int> {
        return categoryDao.getGenreIds(id)
    }

}