package com.example.discover.datamodel.genre

import androidx.room.Entity

@Entity(
    tableName = "MediaGenreCrossReference",
    primaryKeys = ["media_id", "genre_id"]
)
data class MediaGenreCrossReference(
    var media_id: Int,
    var genre_id: Int
)