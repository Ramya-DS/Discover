package com.example.discover.datamodel.movie.detail

import com.example.discover.datamodel.genre.Genres
import com.google.gson.annotations.SerializedName

data class MovieDetails(

    @SerializedName("adult")
    val adult: Boolean,

    @SerializedName("backdrop_path")
    val backdrop_path: String?,

    @SerializedName("budget")
    val budget: Int,

    @SerializedName("genres")
    val genres: List<Genres>,

    @SerializedName("homepage")
    val homepage: String?,

    @SerializedName("id")
    val id: Int,

    @SerializedName("original_language")
    val original_language: String,

    @SerializedName("overview")
    val overview: String?,

    @SerializedName("poster_path")
    val poster_path: String?,

    @SerializedName("release_date")
    val release_date: String?,

    @SerializedName("revenue")
    val revenue: Long,

    @SerializedName("runtime")
    val runtime: Int,

    @SerializedName("status")
    val status: String,

    @SerializedName("tagline")
    val tagline: String?,

    @SerializedName("title")
    val title: String,

    @SerializedName("vote_average")
    val vote_average: Double,

    @SerializedName("vote_count")
    val vote_count: Int
)

