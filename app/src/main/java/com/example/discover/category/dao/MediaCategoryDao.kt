package com.example.discover.category.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.discover.datamodel.genre.GenreMedia
import com.example.discover.datamodel.media.MediaDatabaseDetails
import com.example.discover.datamodel.media.MediaUnderTheTypes

@Dao
interface MediaCategoryDao : MediaUtilsDao, MediaTypeDao, GenresUtilsDao {

    @Transaction
    @Query("SELECT * FROM Media WHERE id=:id AND isMovie=:value")
    fun getMediaDetailsUsingID(id: Int, value: Boolean = true): LiveData<MediaDatabaseDetails>

    @Transaction
    @Query("SELECT * FROM MediaType WHERE trending= :value")
    fun getTrendingMedia(value: Boolean = true): LiveData<List<MediaUnderTheTypes>>

    @Transaction
    @Query("SELECT * FROM MediaType WHERE top_rated= :value")
    fun getTopRatedMedia(value: Boolean = true): LiveData<List<MediaUnderTheTypes>>

    @Transaction
    @Query("SELECT * FROM MediaType WHERE upcoming= :value")
    fun getUpcomingMedia(value: Boolean = true): LiveData<List<MediaUnderTheTypes>>

    @Transaction
    @Query("SELECT * FROM MediaType WHERE popular= :value")
    fun getPopularMedia(value: Boolean = true): LiveData<List<MediaUnderTheTypes>>

    @Transaction
    @Query("SELECT * FROM MediaType WHERE now_playing= :value")
    fun getNowPlayingMedia(value: Boolean = true): LiveData<List<MediaUnderTheTypes>>

    @Transaction
    @Query("SELECT * FROM MediaType WHERE on_the_air= :value")
    fun getOnTheAirMedia(value: Boolean = true): LiveData<List<MediaUnderTheTypes>>

    @Transaction
    @Query("SELECT * FROM MediaType WHERE airing_today=:value")
    fun getAiringTodayMedia(value: Boolean = true): LiveData<List<MediaUnderTheTypes>>

    @Transaction
    @Query("SELECT * FROM Genre WHERE id=:id AND isMovie=:isMovie")
    fun getGenreMedia(id: Int, isMovie: Boolean): GenreMedia
}