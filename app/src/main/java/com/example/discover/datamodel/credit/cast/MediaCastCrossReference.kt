package com.example.discover.datamodel.credit.cast

import androidx.room.Entity

@Entity(
    tableName = "MovieCastCrossReference",
    primaryKeys = ["media_id", "cast_id"]
)
data class MediaCastCrossReference(
    var media_id: Int,
    var cast_id: Int
)