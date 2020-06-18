package com.example.discover.genreScreen

import com.example.discover.datamodel.movie.MoviesList
import com.example.discover.datamodel.tvshow.preview.ShowsList
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GenreMediaApiCall {

    @GET("discover/movie")
    fun getGenreRelatedMovies(@Query("with_genres") id: Int, @Query("page") page: Int): Call<MoviesList>

    @GET("discover/tv")
    fun getGenreRelatedShows(@Query("with_genres") id: Int, @Query("page") page: Int): Call<ShowsList>

}