package com.example.discover.datamodel.review

import androidx.room.Embedded
import androidx.room.Relation
import com.example.discover.datamodel.media.Media
import com.google.gson.annotations.SerializedName

data class ReviewList(

    @Embedded var media: Media,

    @Relation(
        parentColumn = "id",
        entity = Review::class,
        entityColumn = "media_id"
    )
    @SerializedName("results")
    val results: List<Review>
)