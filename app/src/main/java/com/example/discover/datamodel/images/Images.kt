package com.example.discover.datamodel.images

import androidx.room.Embedded
import androidx.room.Relation
import com.example.discover.datamodel.media.Media
import com.google.gson.annotations.SerializedName

data class Images(
    @Embedded var media: Media,

    @Relation(
        parentColumn = "id",
        entity = ImageDetails::class,
        entityColumn = "media_id"
    )
    @SerializedName("backdrops")
    val backdrops: List<ImageDetails>?,

    @Relation(
        parentColumn = "id",
        entity = ImageDetails::class,
        entityColumn = "media_id"
    )
    @SerializedName("posters")
    val posters: List<ImageDetails>?
)