package com.example.discover.filterScreen

import com.example.discover.datamodel.movie.MoviesList
import com.example.discover.datamodel.tvshow.preview.ShowsList
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface FilterApiCall {
    @GET("discover/movie")
    fun discoverMovies(@QueryMap parameters: Map<String, String>): Call<MoviesList>

    @GET("discover/tv")
    fun discoverShows(@QueryMap parameters: Map<String, String>): Call<ShowsList>
}