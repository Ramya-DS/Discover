package com.example.discover.datamodel.media

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Media")
data class Media(
    var isMovie: Boolean,
    var api_id: Int,
    var backdrop_path: String?,
    var original_language: String,
    var overview: String?,
    var poster_path: String?,
    var vote_average: Double,
    var vote_count: Int,


    var adult: Boolean = false,
    var homepage: String? = null,
    var release_date: String? = null,
    var revenue: Long = 0L,
    var runtime: Int = 0,
    var tagLine: String? = null,
    var title: String = "",
    val episode_run_time: String = "",
    var first_air_date: String? = null,
    var in_production: Boolean = false,
    var last_air_date: String? = null,
    var name: String = "",
    var number_of_episodes: Int = 0,
    var number_of_seasons: Int = 0,
    var origin_country: String = "",
    var type: String = "",
    var budget: Int = 0,
    var status: String = "",
    var last_episode: Int? = null
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}