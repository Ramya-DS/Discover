package com.example.discover.datamodel.media

import androidx.room.Entity

@Entity(
    tableName = "SimilarCrossReference",
    primaryKeys = ["parent_id", "similar_id"]
)
data class SimilarCrossReference(
    var parent_id: Int,
    var similar_id: Int
)