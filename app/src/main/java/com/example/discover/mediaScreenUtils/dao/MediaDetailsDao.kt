package com.example.discover.mediaScreenUtils.dao

import android.util.Log
import androidx.room.*
import com.example.discover.category.dao.GenresUtilsDao
import com.example.discover.category.dao.MediaTypeDao
import com.example.discover.category.dao.MediaUtilsDao
import com.example.discover.datamodel.credit.Credit
import com.example.discover.datamodel.credit.cast.Cast
import com.example.discover.datamodel.credit.cast.MediaCastCrossReference
import com.example.discover.datamodel.credit.crew.Crew
import com.example.discover.datamodel.credit.crew.MediaCrewCrossReference
import com.example.discover.datamodel.externalId.ExternalID
import com.example.discover.datamodel.images.ImageDetails
import com.example.discover.datamodel.images.Images
import com.example.discover.datamodel.keyword.Keyword
import com.example.discover.datamodel.keyword.KeywordResult
import com.example.discover.datamodel.keyword.MediaKeywordCrossReference
import com.example.discover.datamodel.media.*
import com.example.discover.datamodel.movie.preview.MoviePreview
import com.example.discover.datamodel.review.Review
import com.example.discover.datamodel.review.ReviewList
import com.example.discover.datamodel.tvshow.detail.Episode
import com.example.discover.datamodel.tvshow.detail.Season
import com.example.discover.datamodel.tvshow.preview.ShowPreview

@Dao
interface MediaDetailsDao : MediaUtilsDao, MediaTypeDao, GenresUtilsDao {

    //Credits
    @Transaction
    fun insertCredit(api_id: Int, credit: Credit, value: Boolean) {
        val id = getMediaId(api_id, value)
        for (i in credit.cast) {
            i.media_id = id
            insertCast(i)
            val j = insertCastCrossRef(MediaCastCrossReference(id, i.id))
            Log.d("MovieRepository", "insertCredit $j")
        }

        for (i in credit.crew) {
            i.media_id = id
            insertCrew(i)
            val j = insertCrewCrossRef(MediaCrewCrossReference(id, i.id))
            Log.d("MovieRepository", "insertCredit $j")
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCastCrossRef(movieCastCrossRef: MediaCastCrossReference): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCrewCrossRef(mediaCrewCrossReference: MediaCrewCrossReference): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCast(cast: Cast): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCrew(crew: Crew): Long

    @Transaction
    @Query("SELECT * FROM Media WHERE api_id= :id AND isMovie=:isMovie")
    fun getCredits(id: Int, isMovie: Boolean): Credit


    //Reviews
    @Transaction
    fun insertMediaReviews(api_id: Int, reviews: List<Review>, value: Boolean) {
        val id = getMediaId(api_id, value)

        for (i in reviews)
            i.media_id = id

        insertReview(reviews)
    }

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReview(review: List<Review>)

    @Transaction
    @Query("SELECT * FROM Media WHERE api_id=:id AND isMovie=:value")
    fun getReview(id: Int, value: Boolean): ReviewList


    //External ID
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertExternals(externalId: ExternalID)

    @Transaction
    fun insertExternalIds(externalId: ExternalID, isMovie: Boolean) {
        val id = getMediaId(externalId.media_id, isMovie)
        externalId.media_id = id
        insertExternals(externalId)

    }

    @Query("SELECT * FROM External_ID WHERE media_id=:id")
    fun getExternals(id: Int): ExternalID

    @Transaction
    fun getExternalIDs(id: Int, isMovie: Boolean): ExternalID {
        return getExternals(getMediaId(id, isMovie))
    }

    //Images
    @Transaction
    fun insertImages(api_id: Int, imageDetails: List<ImageDetails>, value: Boolean) {
        val id = getMediaId(api_id, value)
        for (i in imageDetails)
            i.media_id = id

        insertAllImages(imageDetails)

    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllImages(imageDetails: List<ImageDetails>)

    @Transaction
    @Query("SELECT * FROM Media WHERE api_id=:id AND isMovie=:value")
    fun getImage(id: Int, value: Boolean): Images

    //keywords
    @Transaction
    @Query("SELECT * FROM Media WHERE api_id=:id AND isMovie=:value")
    fun getKeywords(id: Int, value: Boolean): KeywordResult

    @Transaction
    fun insertMediaKeywords(api_id: Int, keywords: List<Keyword>?, value: Boolean) {
        val id = getMediaId(api_id, value)
        keywords?.let {
            for (i in keywords)
                insertKeywordCrossRef(MediaKeywordCrossReference(id, i.id))
        }
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertKeywordCrossRef(mediaKeywordCrossReference: MediaKeywordCrossReference)

    //recommendations
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertRecommendationCrossRef(reference: List<RecommendationsCrossReference>)

    @Transaction
    fun insertMoviesRecommendations(api_id: Int, list: List<MoviePreview>, value: Boolean) {
        val id = getMediaId(api_id, value)
        val crossRefs = mutableListOf<RecommendationsCrossReference>()
        for (i in list) {
            insertMedia(
                Media(
                    true,
                    i.id,
                    i.backdrop_path,
                    i.original_language,
                    i.overview,
                    i.poster_path,
                    i.vote_average,
                    i.vote_count,
                    i.adult,
                    release_date = i.release_date,
                    title = i.title
                )
            )
            insertMediaGenresIds(i.id, i.genre_ids, true)
            crossRefs.add(RecommendationsCrossReference(id, i.id))
        }

        insertRecommendationCrossRef(crossRefs)
    }

    @Transaction
    fun insertShowsRecommendations(api_id: Int, list: List<ShowPreview>, value: Boolean) {
        val id = getMediaId(api_id, value)
        val crossRefs = mutableListOf<RecommendationsCrossReference>()
        for (i in list)
            crossRefs.add(RecommendationsCrossReference(id, i.id))

        insertRecommendationCrossRef(crossRefs)
    }


    @Transaction
    @Query("SELECT * FROM Media WHERE api_id=:id AND isMovie=:value")
    fun getRecommendations(id: Int, value: Boolean): MediaRecommendations


    //Similar
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertSimilarCrossRef(reference: List<SimilarCrossReference>)

    @Transaction
    fun insertMoviesSimilar(api_id: Int, list: List<MoviePreview>, value: Boolean) {
        val id = getMediaId(api_id, value)
        val crossRefs = mutableListOf<SimilarCrossReference>()
        for (i in list) {
            insertMedia(
                Media(
                    true,
                    i.id,
                    i.backdrop_path,
                    i.original_language,
                    i.overview,
                    i.poster_path,
                    i.vote_average,
                    i.vote_count,
                    i.adult,
                    release_date = i.release_date,
                    title = i.title
                )
            )
            insertMediaGenresIds(i.id, i.genre_ids, true)
            crossRefs.add(SimilarCrossReference(id, i.id))
        }

        insertSimilarCrossRef(crossRefs)
    }

    @Transaction
    fun insertShowsSimilar(api_id: Int, list: List<ShowPreview>, value: Boolean) {
        val id = getMediaId(api_id, value)
        val crossRefs = mutableListOf<SimilarCrossReference>()
        for (i in list) {
            crossRefs.add(SimilarCrossReference(id, i.id))
        }

        insertSimilarCrossRef(crossRefs)
    }

    @Transaction
    @Query("SELECT * FROM Media WHERE api_id=:id AND isMovie=:value")
    fun getSimilar(id: Int, value: Boolean): MediaSimilar


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSeason(seasons: List<Season>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEpisodes(episodes: List<Episode>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAnEpisode(episodes: Episode)

    @Transaction
    @Query("SELECT * FROM Media WHERE api_id=:showId")
    fun getSeasons(showId: Int): MediaDatabaseDetails

    @Transaction
    @Query("SELECT * FROM Episode WHERE season_id=:seasonId")
    fun getEpisodes(seasonId: Int): List<Episode>

    @Transaction
    @Query("SELECT * FROM Episode WHERE id=:episodeId AND season_id=:showId")
    fun getAnEpisode(showId: Int, episodeId: Int): Episode


}