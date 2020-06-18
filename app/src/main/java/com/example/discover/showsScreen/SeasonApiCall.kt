package com.example.discover.showsScreen

import com.example.discover.datamodel.credit.Credit
import com.example.discover.datamodel.images.Images
import com.example.discover.datamodel.tvshow.detail.Season
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface SeasonApiCall {

    @GET("tv/{tv_id}/season/{season_number}")
    fun seasonDetails(@Path("tv_id") showId: Int, @Path("season_number") seasonNumber: Int): Call<Season>

    @GET("tv/{tv_id}/season/{season_number}/credits")
    fun credits(@Path("tv_id") showId: Int, @Path("season_number") seasonNumber: Int): Call<Credit>

    @GET("tv/{tv_id}/season/{season_number}/images")
    fun getImages(@Path("tv_id") id: Int, @Path("season_number") seasonNumber: Int): Call<Images>
}