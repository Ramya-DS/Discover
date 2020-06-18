package com.example.discover.roomDatabase.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.discover.datamodel.Language

@Dao
interface LanguageDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAllLanguages(list: List<Language>)

    @Query("SELECT * FROM Language")
    fun getAllLanguages():List<Language>
}