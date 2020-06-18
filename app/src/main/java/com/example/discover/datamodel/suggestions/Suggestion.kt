package com.example.discover.datamodel.suggestions

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "Suggestion")
data class Suggestion(
    @PrimaryKey
    var query: String
)
