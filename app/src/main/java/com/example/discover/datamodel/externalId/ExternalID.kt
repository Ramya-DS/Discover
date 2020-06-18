package com.example.discover.datamodel.externalId

import androidx.room.Entity
import com.google.gson.annotations.SerializedName

@Entity(tableName = "External_ID", primaryKeys = ["media_id", "isMovie"])
data class ExternalID(
    @SerializedName("imdb_id") var imdb_id: String?,
    @SerializedName("facebook_id") var facebook_id: String?,
    @SerializedName("instagram_id") var instagram_id: String?,
    @SerializedName("twitter_id") var twitter_id: String?,
    @SerializedName("id") var media_id: Int,
    var isMovie: Boolean = true
)