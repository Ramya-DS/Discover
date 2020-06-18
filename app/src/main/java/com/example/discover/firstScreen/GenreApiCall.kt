package com.example.discover.firstScreen

import com.example.discover.datamodel.Language
import com.example.discover.datamodel.genre.GenreResult
import retrofit2.Call
import retrofit2.http.GET

interface GenreApiCall {

    @GET("genre/movie/list")
    fun getAllMovieGenres(): Call<GenreResult>

    @GET("genre/tv/list")
    fun getAllShowsGenres(): Call<GenreResult>

    @GET("configuration/languages")
    fun getLanguages(): Call<List<Language>>

}