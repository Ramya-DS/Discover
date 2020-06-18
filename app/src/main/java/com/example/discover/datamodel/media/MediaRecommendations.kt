package com.example.discover.datamodel.media

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class MediaRecommendations(

    @Embedded var media: Media,
    @Relation(
        parentColumn = "id",
        entity = Media::class,
        entityColumn = "api_id",
        associateBy = Junction(
            value = RecommendationsCrossReference::class,
            parentColumn = "parent_id",
            entityColumn = "recommendation_id"
        )
    ) var recommendations: List<Media>
) {
    constructor() : this(
        Media(
            false, 0, null, "", null, null, 0.0, 0
        ), emptyList()
    )
}