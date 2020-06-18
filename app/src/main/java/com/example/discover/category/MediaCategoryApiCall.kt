package com.example.discover.category

import com.example.discover.datamodel.movie.MoviesList
import com.example.discover.datamodel.tvshow.preview.ShowsList
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MediaCategoryApiCall {

    @GET("trending/movie/day")
    fun getTrendingMovies(@Query("page") page: Int): Call<MoviesList>

    @GET("movie/now_playing")
    fun getNowPlayingMovies(@Query("page") page: Int): Call<MoviesList>

    @GET("movie/popular")
    fun getPopularMovies(@Query("page") page: Int): Call<MoviesList>

    @GET("movie/top_rated")
    fun getTopRatedMovies(@Query("page") page: Int): Call<MoviesList>

    @GET("movie/upcoming")
    fun getUpcomingMovies(@Query("page") page: Int): Call<MoviesList>

    @GET("trending/tv/day")
    fun getTrendingShows(@Query("page") page: Int = 1): Call<ShowsList>

    @GET("tv/on_the_air")
    fun getOnAirShows(@Query("page") page: Int = 1): Call<ShowsList>

    @GET("tv/popular")
    fun getPopularShows(@Query("page") page: Int = 1): Call<ShowsList>

    @GET("tv/airing_today")
    fun getAiringTodayShows(@Query("page") page: Int = 1): Call<ShowsList>

    @GET("tv/top_rated")
    fun getTopRatedShows(@Query("page") page: Int = 1): Call<ShowsList>
}