package com.example.discover.category.dao

import androidx.room.*
import com.example.discover.datamodel.genre.Genres
import com.example.discover.datamodel.genre.MediaGenreCrossReference

@Dao
interface GenresUtilsDao : MediaUtilsDao {

    @Transaction
    fun insertMediaGenresObjects(api_id: Int, genres: List<Genres>, value: Boolean) {
        val id = getMediaId(api_id, value)
        for (i in genres)
            insertGenreCrossRef(MediaGenreCrossReference(id, i.id))
    }

    @Transaction
    fun insertMediaGenresIds(api_id: Int, genres: List<Int>, value: Boolean) {
        val id = getMediaId(api_id, value)
        for (i in genres)
            insertGenreCrossRef(MediaGenreCrossReference(id, i))
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertGenreCrossRef(mediaGenreCrossReference: MediaGenreCrossReference)

    @Query("SELECT genre_id FROM MediaGenreCrossReference WHERE media_id=:id")
    fun getGenreIds(id: Int): List<Int>

}