package com.example.discover.category

import android.app.Application
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.SparseIntArray
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.discover.DiscoverApplication
import com.example.discover.category.dao.MediaCategoryDao
import com.example.discover.datamodel.media.Media
import com.example.discover.datamodel.media.MediaUnderTheTypes
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

class CategoryViewModel(private val mApplication: Application) : AndroidViewModel(mApplication) {

    companion object {
        const val TAG = "CategoryViewModel"
        const val TRENDING = 1
        const val TOP_RATED = 2
        const val NOW_PLAYING = 3
        const val POPULAR = 4
        const val UPCOMING = 5
        const val ON_THE_AIR = 6
        const val AIRING_TODAY = 7
    }

    var movies = hashMapOf<Int, List<MoviePreview>>()
    var shows = hashMapOf<Int, List<ShowPreview>>()

    var moviesList = mutableListOf<MoviePreview>()
    var showsList = mutableListOf<ShowPreview>()

    var isLinear = true
    var position = 0

    var page = 1

    var positions = SparseIntArray()

    private val mediaApiCall: MediaCategoryApiCall =
        (mApplication as DiscoverApplication).mediaCategoryApiCall


    private val categoryDao: MediaCategoryDao =
        DiscoverDatabase.getDatabase(mApplication).mediaCategoryDao()

    val handlerThread = HandlerThread("insert").apply {
        start()
    }
    val handler = Handler(handlerThread.looper)

    fun databaseTrendingMedia() = categoryDao.getTrendingMedia()
    fun databaseTopRated() = categoryDao.getTopRatedMedia()
    fun databaseUpcoming() = categoryDao.getUpcomingMedia()
    fun databasePopular() = categoryDao.getPopularMedia()
    fun databaseNowPlaying() = categoryDao.getNowPlayingMedia()
    fun databaseOnTheAir() = categoryDao.getOnTheAirMedia()
    fun databaseAiringToday() = categoryDao.getAiringTodayMedia()

    private fun insertType(api_id: Int, type: Int, isMovie: Boolean) {
        categoryDao.insertType(api_id, type, isMovie)
    }

    private fun insertMedia(media: Media) {
        categoryDao.insertMedia(media)
    }

    private fun insertMediaGenres(id: Int, genres: List<Int>, isMovie: Boolean) {
        categoryDao.insertMediaGenresIds(id, genres, isMovie)
    }

    fun trendingMoviesData(page: Int = 1): LiveData<List<MoviePreview>> {
        val call = mediaApiCall.getTrendingMovies(page)
        return sendMovieRequestCall(call, page, TRENDING)
    }

    fun nowPlayingMoviesData(page: Int = 1): LiveData<List<MoviePreview>> {
        val call = mediaApiCall.getNowPlayingMovies(page)
        return sendMovieRequestCall(call, page, NOW_PLAYING)
    }

    fun popularMoviesData(page: Int = 1): LiveData<List<MoviePreview>> {
        val call = mediaApiCall.getPopularMovies(page)
        return sendMovieRequestCall(call, page, POPULAR)
    }

    fun topRatedMoviesData(page: Int = 1): LiveData<List<MoviePreview>> {
        val call = mediaApiCall.getTopRatedMovies(page)
        return sendMovieRequestCall(call, page, TOP_RATED)
    }

    fun upcomingMoviesData(page: Int = 1): LiveData<List<MoviePreview>> {
        val call = mediaApiCall.getUpcomingMovies(page)
        return sendMovieRequestCall(call, page, UPCOMING)
    }

    private fun sendMovieRequestCall(
        call: Call<MoviesList>,
        page: Int,
        type: Int
    ): LiveData<List<MoviePreview>> {
        val moviesList = MutableLiveData<List<MoviePreview>>()
        viewModelScope.launch(Dispatchers.IO) {
            call.enqueue(object : Callback<MoviesList> {
                override fun onFailure(call: Call<MoviesList>, t: Throwable) {
                    if (page == 1) {
                        when (type) {
                            TRENDING -> databaseTrendingMedia()
                            TOP_RATED -> databaseTopRated()
                            NOW_PLAYING -> databaseNowPlaying()
                            POPULAR -> databasePopular()
                            else -> databaseUpcoming()
                        }.observeForever {
                            moviesList.value = getDatabaseMovies(it).apply { reverse() }
                        }
                    }
                }

                override fun onResponse(call: Call<MoviesList>, response: Response<MoviesList>) {
                    if (response.isSuccessful) {
                        handler.post {
                            writeMoviesPreviewsToDb(response.body()!!.results, type)
                        }
                        moviesList.value = response.body()!!.results
                    } else {
                        Log.d(
                            TAG,
                            "APIMovies_Error: $type ${(mApplication as DiscoverApplication).fetchErrorMessage(
                                response.errorBody()!!
                            )}"
                        )
                    }
                }
            })
        }
        return moviesList
    }

    fun getDatabaseMovies(list: List<MediaUnderTheTypes>): MutableList<MoviePreview> {
        val movies = mutableListOf<MediaUnderTheTypes>()
        for (i in list)
            if (i.media.isMovie) {
                movies.add(i)
            }
        return formatToMoviePreview(movies)
    }

    private fun formatToMoviePreview(list: List<MediaUnderTheTypes>?): MutableList<MoviePreview> {
        val movies = mutableListOf<MoviePreview>()
        list?.let {
            for (i in list) {
                i.media.apply {
                    movies.add(
                        MoviePreview(
                            adult,
                            backdrop_path,
                            i.genres_id,
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
//        movies.shuffle()
        return movies
    }

    fun writeMoviesPreviewsToDb(list: List<MoviePreview>, type: Int) {
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
                insertType(i.id, type, true)
            }
        }

    }

    fun setTrendingShowsData(page: Int = 1): LiveData<List<ShowPreview>> {
        val call = mediaApiCall.getTrendingShows(page)
        return sendShowRequestCall(call, page, TRENDING)
    }

    fun setOnAirShowsData(page: Int = 1): LiveData<List<ShowPreview>> {
        val call = mediaApiCall.getOnAirShows(page)
        return sendShowRequestCall(call, page, ON_THE_AIR)
    }

    fun setPopularShowsData(page: Int = 1): LiveData<List<ShowPreview>> {
        val call = mediaApiCall.getPopularShows(page)
        return sendShowRequestCall(call, page, POPULAR)
    }

    fun setTopRatedShowsData(page: Int = 1): LiveData<List<ShowPreview>> {
        val call = mediaApiCall.getTopRatedShows(page)
        return sendShowRequestCall(call, page, TOP_RATED)
    }

    fun setAiringTodayShowsData(page: Int = 1): LiveData<List<ShowPreview>> {
        val call = mediaApiCall.getAiringTodayShows(page)
        return sendShowRequestCall(call, page, AIRING_TODAY)

    }

    private fun sendShowRequestCall(
        call: Call<ShowsList>,
        page: Int,
        type: Int
    ): LiveData<List<ShowPreview>> {
        val showsList = MutableLiveData<List<ShowPreview>>()
        viewModelScope.launch(Dispatchers.IO) {
            call.enqueue(object : Callback<ShowsList> {
                override fun onFailure(call: Call<ShowsList>, t: Throwable) {
                    if (page == 1) {
                        when (type) {
                            TRENDING -> databaseTrendingMedia()
                            TOP_RATED -> databaseTopRated()
                            ON_THE_AIR -> databaseOnTheAir()
                            POPULAR -> databasePopular()
                            else -> databaseAiringToday()
                        }.observeForever {
                            showsList.value = getDatabaseShows(it).apply { reverse() }
                        }
                    }
                }

                override fun onResponse(call: Call<ShowsList>, response: Response<ShowsList>) {
                    if (response.isSuccessful) {
                        handler.post {
                            writeShowsPreviewsToDb(response.body()!!.results, type)
                        }
                        showsList.value = response.body()!!.results
                    } else {
                        Log.d(
                            TAG,
                            "APIMovies_Error: $type ${(mApplication as DiscoverApplication).fetchErrorMessage(
                                response.errorBody()!!
                            )}"
                        )
                    }
                }
            })
        }

        return showsList
    }

    fun getDatabaseShows(list: List<MediaUnderTheTypes>): MutableList<ShowPreview> {
        val shows = mutableListOf<MediaUnderTheTypes>()
        for (i in list)
            if (!i.media.isMovie)
                shows.add(i)
        return formatToShowPreview(shows)
    }

    private fun formatToShowPreview(list: List<MediaUnderTheTypes>?): MutableList<ShowPreview> {
        val shows = mutableListOf<ShowPreview>()
        list?.let {
            for (i in list) {
                i.media.apply {
                    shows.add(
                        ShowPreview(
                            api_id,
                            name,
                            vote_count,
                            vote_average,
                            first_air_date,
                            poster_path,
                            i.genres_id,
                            original_language,
                            backdrop_path,
                            overview,
                            origin_country.substringAfter('[').substringBefore(']').split(',')
                        )
                    )
                }
            }
        }
//        shows.shuffle()
        return shows
    }

    private fun writeShowsPreviewsToDb(shows: List<ShowPreview>, type: Int) {
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
                insertType(i.id, type, false)
            }
        }

    }

    fun clearTypeTable() = handler.post {
        categoryDao.deleteTypeTableRecords()
    }

}