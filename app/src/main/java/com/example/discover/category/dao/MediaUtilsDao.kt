package com.example.discover.category.dao

import androidx.room.*
import com.example.discover.datamodel.media.Media
import com.example.discover.datamodel.media.MediaDatabaseDetails

@Dao
interface MediaUtilsDao {
    @Query("SELECT id FROM Media WHERE api_id=:id AND isMovie=:value")
    fun getMediaId(id: Int, value: Boolean): Int

    @Query("SELECT id FROM Media WHERE api_id=:id AND isMovie=:value")
    fun checkIfExists(id: Int, value: Boolean): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMediaInDb(media: Media): Long

    @Update
    fun updateMedia(media: Media)

    @Transaction
    @Query("SELECT * FROM Media WHERE api_id=:id AND isMovie=:value")
    fun getMediaDetailsUsingApiID(id: Int, value: Boolean): MediaDatabaseDetails

    @Transaction
    fun insertMedia(media: Media) {
        val id = checkIfExists(media.api_id, media.isMovie)
        if (id == null)
            insertMediaInDb(media)
        else {
            media.id = id
            updateMedia(media)
        }
    }

}