package com.example.discover.datamodel.genre

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.discover.datamodel.media.Media

data class GenreMedia(
    @Embedded var genres: Genres,

    @Relation(
        parentColumn = "id",
        entity = Media::class,
        entityColumn = "id",
        associateBy = Junction(
            value = MediaGenreCrossReference::class,
            parentColumn = "genre_id",
            entityColumn = "media_id"
        )
    ) var medias: List<Media>
)