package com.example.discover.datamodel.credit

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.discover.datamodel.credit.cast.Cast
import com.example.discover.datamodel.credit.cast.MediaCastCrossReference
import com.example.discover.datamodel.credit.crew.Crew
import com.example.discover.datamodel.credit.crew.MediaCrewCrossReference
import com.example.discover.datamodel.media.Media
import com.google.gson.annotations.SerializedName

data class Credit(

    @Embedded var media: Media,

    @SerializedName("cast")
    @Relation(
        parentColumn = "id",
        entity = Cast::class,
        entityColumn = "id",
        associateBy = Junction(
            value = MediaCastCrossReference::class,
            parentColumn = "media_id",
            entityColumn = "cast_id"
        )
    )
    val cast: List<Cast>,

    @Relation(
        parentColumn = "id",
        entity = Crew::class,
        entityColumn = "id",
        associateBy = Junction(
            value = MediaCrewCrossReference::class,
            parentColumn = "media_id",
            entityColumn = "crew_id"
        )
    )
    @SerializedName("crew") val crew: List<Crew>
)