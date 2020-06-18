package com.example.discover.datamodel.media

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class MediaSimilar(

    @Embedded var media: Media,
    @Relation(
        parentColumn = "id",
        entity = Media::class,
        entityColumn = "api_id",
        associateBy = Junction(
            value = SimilarCrossReference::class,
            parentColumn = "parent_id",
            entityColumn = "similar_id"
        )
    ) var similars: List<Media>
) {
    constructor() : this(
        Media(
            false, 0, null, "", null, null, 0.0, 0
        ), emptyList()
    )
}