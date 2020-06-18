package com.example.discover.showsScreen

import com.example.discover.datamodel.credit.Credit
import com.example.discover.datamodel.externalId.ExternalID
import com.example.discover.datamodel.images.Images
import com.example.discover.datamodel.keyword.KeywordResult
import com.example.discover.datamodel.review.ReviewList
import com.example.discover.datamodel.tvshow.detail.ShowDetails
import com.example.discover.datamodel.tvshow.preview.ShowsList
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ShowDetailApiCall {

    @GET("tv/{tv_id}")
    fun showDetails(@Path("tv_id") id: Int): Call<ShowDetails>

    @GET("tv/{tv_id}/credits")
    fun getCredits(@Path("tv_id") id: Int): Call<Credit>

    @GET("tv/{tv_id}/keywords")
    fun getExternalIds(@Path("tv_id") id: Int): Call<ExternalID>

    @GET("tv/{tv_id}/keywords")
    fun getKeywords(@Path("tv_id") id: Int): Call<KeywordResult>

    @GET("tv/{tv_id}/recommendations")
    fun getRecommendations(@Path("tv_id") id: Int): Call<ShowsList>

    @GET("tv/{tv_id}/reviews")
    fun getReviews(@Path("tv_id") id: Int): Call<ReviewList>

    @GET("tv/{tv_id}/similar")
    fun getSimilarShows(@Path("tv_id") id: Int): Call<ShowsList>

    @GET("tv/{tv_id}/images")
    fun getImages(@Path("tv_id") id: Int): Call<Images>


}