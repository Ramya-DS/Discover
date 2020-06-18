package com.example.discover.datamodel.keyword

import androidx.room.Entity

@Entity(tableName = "MediaKeywordCrossReference", primaryKeys = ["media_id", "keyword_id"])

data class MediaKeywordCrossReference(
    var media_id: Int,
    var keyword_id: Int
)