package com.example.discover.mediaScreenUtils.apiCalls

import com.example.discover.datamodel.credit.Credit
import com.example.discover.datamodel.externalId.ExternalID
import com.example.discover.datamodel.images.Images
import com.example.discover.datamodel.keyword.KeywordResult
import com.example.discover.datamodel.movie.MoviesList
import com.example.discover.datamodel.movie.detail.MovieDetails
import com.example.discover.datamodel.review.ReviewList
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieDetailApiCall {

    @GET("movie/{movie_id}")
    fun movieDetails(
        @Path("movie_id") id: Int, @Query("append_to_response") value: String = "images", @Query(
            "include_image_language"
        ) values: String = "en,null"
    ): Call<MovieDetails>

    @GET("movie/{movie_id}/credits")
    fun creditDetails(@Path("movie_id") id: Int): Call<Credit>

    @GET("movie/{movie_id}/external_ids")
    fun externalIds(@Path("movie_id") id: Int): Call<ExternalID>

    @GET("movie/{movie_id}/keywords")
    fun getKeywords(@Path("movie_id") id: Int): Call<KeywordResult>

    @GET("movie/{movie_id}/recommendations")
    fun getRecommendations(@Path("movie_id") id: Int): Call<MoviesList>

    @GET("movie/{movie_id}/similar")
    fun getSimilar(@Path("movie_id") id: Int): Call<MoviesList>

    @GET("movie/{movie_id}/reviews")
    fun getReviews(@Path("movie_id") id: Int): Call<ReviewList>

    @GET("movie/{movie_id}/images")
    fun getImages(@Path("movie_id") id: Int): Call<Images>


}