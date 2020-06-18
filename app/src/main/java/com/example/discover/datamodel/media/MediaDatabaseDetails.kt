package com.example.discover.datamodel.media

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.discover.datamodel.genre.Genres
import com.example.discover.datamodel.genre.MediaGenreCrossReference
import com.example.discover.datamodel.tvshow.detail.Season
import com.example.discover.datamodel.tvshow.detail.ShowCreators
import com.google.gson.annotations.SerializedName

data class MediaDatabaseDetails(

    @Embedded var media: Media,

    @SerializedName("genres")
    @Relation(
        parentColumn = "id",
        entity = Genres::class,
        entityColumn = "id",
        associateBy = Junction(
            value = MediaGenreCrossReference::class,
            parentColumn = "media_id",
            entityColumn = "genre_id"
        )
    )
    var genres: List<Genres>,

    @Relation(
        parentColumn = "api_id",
        entity = ShowCreators::class,
        entityColumn = "media_id"
    ) var creators: List<ShowCreators>,

    @Relation(
        parentColumn = "api_id",
        entity = Season::class,
        entityColumn = "show_id"
    ) var seasons: List<Season>
)