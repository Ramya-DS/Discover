package com.example.discover.datamodel.tvshow.detail

import androidx.room.*
import com.example.discover.datamodel.credit.cast.Cast
import com.example.discover.datamodel.credit.cast.MediaCastCrossReference
import com.example.discover.datamodel.credit.crew.Crew
import com.example.discover.datamodel.credit.crew.MediaCrewCrossReference
import com.google.gson.annotations.SerializedName

@Entity(tableName = "Episode")
data class Episode(

    @SerializedName("air_date")
    var air_date: String,

    @SerializedName("episode_number")
    var episode_number: Int,

    @PrimaryKey
    @SerializedName("id")
    var id: Int,

    @SerializedName("name")
    var name: String,

    @SerializedName("overview")
    var overview: String,

    @SerializedName("season_number")
    var season_number: Int,

    @SerializedName("still_path")
    var still_path: String?,

    @SerializedName("vote_average")
    var vote_average: Double,

    @SerializedName("vote_count")
    var vote_count: Int,

    @Ignore
    @SerializedName("crew")
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
    var crew: List<Crew>,

    @Ignore
    @SerializedName("guest_stars")
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
    var guest_stars: List<Cast>,

    var season_id: Int
) {
    constructor() : this("", 0, 0, "", "", 0, null, 0.0, 0, emptyList(), emptyList(), 0)
}