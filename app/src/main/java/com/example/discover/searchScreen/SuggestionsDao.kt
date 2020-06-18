package com.example.discover.searchScreen

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.discover.datamodel.suggestions.Suggestion

@Dao
interface SuggestionsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertQuery(suggestion: Suggestion)

    @Query("SELECT `query` FROM Suggestion WHERE `query` LIKE :query")
    fun getSuggestions(query: String): List<String>
}