package com.example.discover.roomDatabase.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.discover.datamodel.Language
import com.example.discover.datamodel.genre.Genres


@Dao
interface GenreDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAllGenres(list: List<Genres>)

    @Query("SELECT * FROM GENRE WHERE isMovie=:isMovie")
    fun getMediaGenres(isMovie: Boolean): List<Genres>

    @Query("SELECT name FROM Language WHERE isoId=:id")
    fun getMediaGenres(id: String): List<String>
}