package com.example.discover.datamodel.credit.crew

import androidx.room.Entity

@Entity(
    tableName = "MediaCrewCrossReference",
    primaryKeys = ["media_id", "crew_id"]
)
data class MediaCrewCrossReference(
    var media_id: Int,
    var crew_id: Int
)