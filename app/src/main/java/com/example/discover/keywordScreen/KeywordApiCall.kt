package com.example.discover.keywordScreen

import com.example.discover.datamodel.movie.MoviesList
import com.example.discover.datamodel.tvshow.preview.ShowsList
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface KeywordApiCall {

    @GET("discover/movie")
    fun getKeywordRelatedMovies(@Query("with_keywords") keyword: Int, @Query("page") page: Int): Call<MoviesList>

    @GET("discover/tv")
    fun getKeywordRelatedShows(@Query("with_keywords") keyword: Int, @Query("page") page: Int): Call<ShowsList>
}