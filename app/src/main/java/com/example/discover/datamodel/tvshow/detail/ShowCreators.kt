package com.example.discover.datamodel.tvshow.detail

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Show_Creators")
data class ShowCreators(
    var media_id: Int,
    var name: String,
    var profilePath: String?
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0
}