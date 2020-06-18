package com.example.discover.datamodel.media

import androidx.room.Entity

@Entity(
    tableName = "RecommendationsCrossReference",
    primaryKeys = ["parent_id", "recommendation_id"]
)
data class RecommendationsCrossReference(
    var parent_id: Int,
    var recommendation_id: Int
)