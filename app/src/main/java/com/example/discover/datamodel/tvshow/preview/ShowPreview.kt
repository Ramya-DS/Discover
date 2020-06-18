package com.example.discover.datamodel.tvshow.preview

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class ShowPreview(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("vote_count") val vote_count: Int,
    @SerializedName("vote_average") val vote_average: Double,
    @SerializedName("first_air_date") val first_air_date: String?,
    @SerializedName("poster_path") val poster_path: String?,
    @SerializedName("genre_ids") val genre_ids: List<Int>,
    @SerializedName("original_language") val original_language: String,
    @SerializedName("backdrop_path") val backdrop_path: String?,
    @SerializedName("overview") val overview: String?,
    @SerializedName("origin_country") val origin_country: List<String>
) : Serializable, Parcelable