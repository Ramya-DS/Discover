package com.example.discover.category.dao

import androidx.room.*
import com.example.discover.datamodel.mediaType.MediaType

@Dao
interface MediaTypeDao : MediaUtilsDao {
    //Type
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTypeRecord(type: MediaType): Long

    @Query("SELECT * FROM MediaType WHERE media_id= :movie_id")
    fun checkIfExists(movie_id: Int): List<MediaType>

    @Update
    fun updateType(type: MediaType)

    @Transaction
    fun insertType(api_id: Int, type: Int, value: Boolean) {
        val mediaId = getMediaId(api_id, value)
        val record = checkIfExists(mediaId)
        if (record.isEmpty()) {
            val typeObject = MediaType(mediaId)
            when (type) {
                1 -> typeObject.trending = true
                2 -> typeObject.top_rated = true
                3 -> typeObject.now_playing = true
                4 -> typeObject.popular = true
                5 -> typeObject.upcoming = true
                6 -> typeObject.on_the_air = true
                7 -> typeObject.airing_today = true
            }
            insertTypeRecord(typeObject)
        } else {
            when (type) {
                1 -> record[0].trending = true
                2 -> record[0].top_rated = true
                3 -> record[0].now_playing = true
                4 -> record[0].popular = true
                5 -> record[0].upcoming = true
                6 -> record[0].on_the_air = true
                7 -> record[0].airing_today = true
            }
            updateType(record[0])
        }
    }

    @Query("DELETE FROM MediaType ")
    fun deleteTypeTableRecords()
}