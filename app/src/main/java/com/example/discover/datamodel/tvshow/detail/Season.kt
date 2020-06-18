package com.example.discover.datamodel.tvshow.detail

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.google.gson.annotations.SerializedName

@Entity(tableName = "Season")
data class Season(

    @SerializedName("air_date")
    var air_date: String,

    @SerializedName("episode_count")
    var episode_count: Int,

    @PrimaryKey
    @SerializedName("id")
    var id: Int,

    @SerializedName("name")
    var name: String,

    @SerializedName("overview")
    var overview: String,

    @SerializedName("poster_path")
    var poster_path: String?,

    @SerializedName("season_number")
    var season_number: Int,

    @Ignore
    @SerializedName("episodes")
    @Relation(
        parentColumn = "id",
        entity = Episode::class,
        entityColumn = "season_id"
    )
    var episodes: List<Episode>,

    var show_id: Int
) {
    constructor() : this("", 0, 0, "", "", null, 0, emptyList(), 0)
}