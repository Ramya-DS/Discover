package com.example.discover.roomDatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.discover.category.dao.MediaCategoryDao
import com.example.discover.datamodel.Language
import com.example.discover.datamodel.credit.cast.Cast
import com.example.discover.datamodel.credit.cast.MediaCastCrossReference
import com.example.discover.datamodel.credit.crew.Crew
import com.example.discover.datamodel.credit.crew.MediaCrewCrossReference
import com.example.discover.datamodel.externalId.ExternalID
import com.example.discover.datamodel.genre.Genres
import com.example.discover.datamodel.genre.MediaGenreCrossReference
import com.example.discover.datamodel.images.ImageDetails
import com.example.discover.datamodel.keyword.Keyword
import com.example.discover.datamodel.keyword.MediaKeywordCrossReference
import com.example.discover.datamodel.media.Media
import com.example.discover.datamodel.media.RecommendationsCrossReference
import com.example.discover.datamodel.media.SimilarCrossReference
import com.example.discover.datamodel.mediaType.MediaType
import com.example.discover.datamodel.review.Review
import com.example.discover.datamodel.suggestions.Suggestion
import com.example.discover.datamodel.tvshow.detail.Episode
import com.example.discover.datamodel.tvshow.detail.Season
import com.example.discover.datamodel.tvshow.detail.ShowCreators
import com.example.discover.mediaScreenUtils.dao.MediaDetailsDao
import com.example.discover.roomDatabase.dao.GenreDao
import com.example.discover.roomDatabase.dao.LanguageDao
import com.example.discover.searchScreen.SuggestionsDao

@Database(
    entities = [
        Genres::class,
        Language::class,
        Suggestion::class,
        Cast::class,
        Crew::class,
        MediaCastCrossReference::class,
        MediaCrewCrossReference::class,
        MediaGenreCrossReference::class,
        MediaType::class,
        Media::class,
        Season::class,
        Episode::class,
        ShowCreators::class,
        MediaKeywordCrossReference::class,
        ExternalID::class,
        ImageDetails::class,
        Keyword::class,
        Review::class,
        RecommendationsCrossReference::class,
        SimilarCrossReference::class],
    version = 7,
    exportSchema = false
)
abstract class DiscoverDatabase : RoomDatabase() {
    abstract fun genresUtilsDao(): GenreDao
    abstract fun languageDao(): LanguageDao
    abstract fun suggestionDao(): SuggestionsDao
    abstract fun mediaCategoryDao(): MediaCategoryDao
    abstract fun mediaDetailsDao(): MediaDetailsDao

    companion object {
        @Volatile
        private var INSTANCE: DiscoverDatabase? = null

        fun getDatabase(context: Context): DiscoverDatabase {
            val tempInstance =
                INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DiscoverDatabase::class.java,
                    "Discover_Database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                return instance
            }
        }
    }
}