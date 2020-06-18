package com.example.discover.datamodel.multiSearch

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MultiSearch(
    @SerializedName("poster_path") val poster_path: String,
    @SerializedName("release_date") val release_date: String,
    @SerializedName("genre_ids") val genre_ids: List<Int>,
    @SerializedName("id") val id: Int,
    @SerializedName("media_type") val media_type: String,
    @SerializedName("original_language") val original_language: String,
    @SerializedName("backdrop_path") val backdrop_path: String,
    @SerializedName("title") val title: String,
    @SerializedName("vote_average") val vote_average: Double,
    @SerializedName("first_air_date") val first_air_date: String,
    @SerializedName("profile_path") val profile_path: String,
    @SerializedName("name") val name: String
) : Parcelable