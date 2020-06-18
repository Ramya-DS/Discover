package com.example.discover.searchScreen

import com.example.discover.datamodel.movie.MoviesList
import com.example.discover.datamodel.multiSearch.MultiSearchResult
import com.example.discover.datamodel.tvshow.preview.ShowsList
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface SeachApiCall {

    @GET("search/multi")
    fun multiSearch(@QueryMap parameters: Map<String, String>): Call<MultiSearchResult>

    @GET("search/movie")
    fun movieSearch(@QueryMap parameters: Map<String, String>): Call<MoviesList>

    @GET("search/tv")
    fun showsSearch(@QueryMap parameters: Map<String, String>): Call<ShowsList>

}