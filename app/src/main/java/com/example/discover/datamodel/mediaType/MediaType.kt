package com.example.discover.datamodel.mediaType

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "MediaType")
data class MediaType(
    var media_id: Int,
    var trending: Boolean = false,
    var top_rated: Boolean = false,
    var now_playing: Boolean = false,
    var popular: Boolean = false,
    var upcoming: Boolean = false,
    var on_the_air: Boolean = false,
    var airing_today: Boolean = false
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}