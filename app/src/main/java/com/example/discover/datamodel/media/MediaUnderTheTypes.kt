package com.example.discover.datamodel.media

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.discover.datamodel.genre.Genres
import com.example.discover.datamodel.genre.MediaGenreCrossReference
import com.example.discover.datamodel.mediaType.MediaType

data class MediaUnderTheTypes(

    @Embedded var mediaType: MediaType,

    @Relation(
        parentColumn = "media_id",
        entity = Media::class,
        entityColumn = "id"
    ) var media: Media,

    @Relation(
        parentColumn = "media_id",
        entity = Genres::class,
        entityColumn = "id",
        associateBy = Junction(
            value = MediaGenreCrossReference::class,
            parentColumn = "media_id",
            entityColumn = "genre_id"
        ),
        projection = ["id"]
    ) var genres_id: List<Int>
)
