package com.example.discover.showsScreen

import android.app.Application
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.discover.DiscoverApplication
import com.example.discover.datamodel.credit.Credit
import com.example.discover.datamodel.genre.Genres
import com.example.discover.datamodel.images.Images
import com.example.discover.datamodel.keyword.Keyword
import com.example.discover.datamodel.keyword.KeywordResult
import com.example.discover.datamodel.media.Media
import com.example.discover.datamodel.media.MediaDatabaseDetails
import com.example.discover.datamodel.review.Review
import com.example.discover.datamodel.review.ReviewList
import com.example.discover.datamodel.tvshow.detail.Season
import com.example.discover.datamodel.tvshow.detail.ShowDetails
import com.example.discover.datamodel.tvshow.preview.ShowPreview
import com.example.discover.datamodel.tvshow.preview.ShowsList
import com.example.discover.mediaScreenUtils.dao.MediaDetailsDao
import com.example.discover.roomDatabase.DiscoverDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class ShowViewModel(mApplication: Application) : AndroidViewModel(mApplication) {

    private val showDetailApiCall: ShowDetailApiCall =
        (mApplication as DiscoverApplication).showDetailApiCall
    private val mediaDetailsDao: MediaDetailsDao =
        DiscoverDatabase.getDatabase(mApplication).mediaDetailsDao()

    var showDetails: ShowDetails? = null
    var credit: Credit? = null
    var review: List<Review>? = null
    var images: Images? = null
    var recommendations: List<ShowPreview>? = null
    var similar: List<ShowPreview>? = null
    var keywords: List<Keyword>? = null

    var recommendedPosition = 0
    var similarPosition = 0
    var seasonPosition = 0
    var castPosition = 0
    var crewPosition = 0
    var reviewPosition = 0

//    private val languageDao = DiscoverDatabase.getDatabase(mApplication).languageDao()

    private val handlerThread = HandlerThread("insert").apply {
        start()
    }
    val handler = Handler(handlerThread.looper)

//    fun getAllLanguages(): LiveData<List<Language>> {
//        val list = MutableLiveData<List<Language>>()
//
//        viewModelScope.launch(Dispatchers.IO) {
//            list.postValue(languageDao.getAllLanguages())
//        }
//        return list
//    }

    private fun insertMovie(media: Media) {
        mediaDetailsDao.insertMedia(media)
    }

    private fun insertType(movie_id: Int, isMovie: Boolean) {
        mediaDetailsDao.insertType(movie_id, 0, isMovie)
    }

    private fun insertMediaGenres(id: Int, genres: List<Genres>, isMovie: Boolean) {
        mediaDetailsDao.insertMediaGenresObjects(id, genres, isMovie)
    }

    private fun getDatabaseMovieDetails(id: Int, isMovie: Boolean): LiveData<MediaDatabaseDetails> {
        val media = MutableLiveData<MediaDatabaseDetails>()
        viewModelScope.launch(Dispatchers.IO) {
            media.postValue(mediaDetailsDao.getMediaDetailsUsingApiID(id, isMovie))
        }
        return media
    }

    //network shows
    fun showDetails(id: Int): LiveData<ShowDetails> {
        val call = showDetailApiCall.showDetails(id)
        return sendDetailsRequest(call, id)
    }

    private fun sendDetailsRequest(call: Call<ShowDetails>, id: Int): LiveData<ShowDetails> {
        val showDetails = MutableLiveData<ShowDetails>()
        call.enqueue(object : Callback<ShowDetails> {
            override fun onFailure(call: Call<ShowDetails>, t: Throwable) {
            }

            override fun onResponse(call: Call<ShowDetails>, response: Response<ShowDetails>) {
                if (response.isSuccessful) {
                    writeShowDetails(response.body()!!)
                    this@ShowViewModel.showDetails = response.body()!!
                    showDetails.value = response.body()!!
                } else Log.d(
                    "MovieRepository",
                    "APIMoviesDetails_Error: $id ${fetchErrorMessage(response.errorBody()!!)}"
                )
            }
        })
        return showDetails
    }

//    fun formatToShowDetails(mediaDatabaseDetails: MediaDatabaseDetails): ShowDetails? {
//
//        if (mediaDatabaseDetails.media.episode_run_time != "") {
//            var show: ShowDetails? = null
//            mediaDatabaseDetails.media.apply {
//                val creators = mediaDatabaseDetails.creators.map {
//                    Crew(it.media_id, it.name, it.profilePath)
//                }
//                val episodeRunTime = episode_run_time.split(',').map { it.toInt() }
//                getLastEpisode(api_id, last_episode).observeForever { mEpisode ->
//                    show = ShowDetails(
//                        backdrop_path,
//                        creators,
//                        episodeRunTime,
//                        first_air_date,
//                        mediaDatabaseDetails.genres,
//                        homepage,
//                        api_id,
//                        in_production,
//                        last_air_date,
//                        mEpisode,
//                        name,
//                        number_of_episodes,
//                        number_of_seasons,
//                        origin_country.substringAfter('[').substringBefore(']').split('.'),
//                        original_language,
//                        overview,
//                        poster_path,
//                        mediaDatabaseDetails.seasons,
//                        status,
//                        type,
//                        vote_average,
//                        vote_count
//                    )
//                }
//            }
//            return show
//        }
//        return null
//    }

    fun writeShowDetails(showDetails: ShowDetails) {
        Log.d("MovieRepository", "writeMoviesDetails")
        handler.post {
            showDetails.apply {
                insertMovie(
                    Media(
                        true,
                        api_id = id,
                        backdrop_path = backdrop_path,
                        original_language = original_language,
                        overview = overview,
                        poster_path = poster_path,
                        vote_average = vote_average,
                        vote_count = vote_count,
                        homepage = homepage,
                        status = status,
                        first_air_date = first_air_date,
                        episode_run_time = episode_run_time.toString(),
                        in_production = in_production,
                        last_air_date = last_air_date,
                        name = name,
                        number_of_episodes = number_of_episodes,
                        number_of_seasons = number_of_seasons,
                        origin_country = origin_country.toString(),
                        type = type
                    )
                )
                insertType(id, false)
                insertMediaGenres(id, genres, false)
                insertSeasons(id, seasons)
            }
        }
    }

    private fun insertSeasons(api_id: Int, seasons: List<Season>) {
        viewModelScope.launch(Dispatchers.IO) {
            for (i in seasons)
                i.show_id = api_id
            mediaDetailsDao.insertSeason(seasons)
        }
    }

//    fun getSeasons(api_id: Int): LiveData<MediaDatabaseDetails> {
//        val shows = MutableLiveData<MediaDatabaseDetails>()
//        runBlocking {
//            withContext(Dispatchers.IO) {
//                shows.postValue(mediaDetailsDao.getSeasons(api_id))
//            }
//        }
//        return shows
//    }

//    fun formatToShowPreview(media: MediaRecommendations): List<ShowPreview> {
//        val shows = mutableListOf<ShowPreview>()
//        for (i in media.recommendations) {
//            getGenreIds(i.id).observeForever {
//                i.apply {
//                    shows.add(
//                        ShowPreview(
//                            api_id,
//                            name,
//                            vote_count,
//                            vote_average,
//                            first_air_date,
//                            poster_path,
//                            it,
//                            original_language,
//                            backdrop_path,
//                            overview,
//                            origin_country.substringAfter('[').substringBefore(']').split(',')
//                        )
//                    )
//                }
//            }
//        }
//        return shows
//    }

//    fun formatToShowPreview(media: MediaSimilar): List<ShowPreview> {
//        val shows = mutableListOf<ShowPreview>()
//        for (i in media.similars) {
//            getGenreIds(i.id).observeForever {
//                i.apply {
//                    shows.add(
//                        ShowPreview(
//                            api_id,
//                            name,
//                            vote_count,
//                            vote_average,
//                            first_air_date,
//                            poster_path,
//                            it,
//                            original_language,
//                            backdrop_path,
//                            overview,
//                            origin_country.substringAfter('[').substringBefore(']').split(',')
//                        )
//                    )
//                }
//            }
//        }
//        return shows
//    }

//    private fun getGenreIds(id: Int): LiveData<List<Int>> {
//        val genreIds = MutableLiveData<List<Int>>()
//        viewModelScope.launch(Dispatchers.IO) {
//            genreIds.postValue(mediaDetailsDao.getGenreIds(id))
//        }
//        return genreIds
//    }

    //database episodes
//    fun insertEpisodes(season_id: Int, episodes: List<Episode>) {
//        viewModelScope.launch(Dispatchers.IO) {
//            for (i in episodes)
//                i.season_id = season_id
//            mediaDetailsDao.insertEpisodes(episodes)
//        }
//    }
//
//    fun insertAnEpisode(show_id: Int, episode: Episode) {
//        viewModelScope.launch(Dispatchers.IO) {
//            episode.season_id = show_id
//            mediaDetailsDao.insertAnEpisode(episode)
//        }
//    }
//
//    fun getEpisodes(season_id: Int): LiveData<List<Episode>> {
//        val episodes = MutableLiveData<List<Episode>>()
//        viewModelScope.launch(Dispatchers.IO) {
//            episodes.postValue(mediaDetailsDao.getEpisodes(season_id))
//        }
//        return episodes
//    }

//    private fun getLastEpisode(show_id: Int, episodeId: Int?): LiveData<Episode> {
//        val episode = MutableLiveData<Episode>()
//        viewModelScope.launch(Dispatchers.IO) {
//            episodeId?.let {
//                episode.postValue(mediaDetailsDao.getAnEpisode(show_id, episodeId))
//            }
//        }
//        return episode
//    }

    //database credits
//    private fun insertCredit(id: Int, credit: Credit, isMovie: Boolean) {
//        mediaDetailsDao.insertCredit(id, credit, isMovie)
//    }

//    private fun getDatabaseCredits(id: Int, isMovie: Boolean): LiveData<Credit> {
//        val credit = MutableLiveData<Credit>()
//        viewModelScope.launch(Dispatchers.IO) {
//            credit.postValue(mediaDetailsDao.getCredits(id, isMovie))
//        }
//        return credit
//    }

    //network credits
    fun fetchCredits(id: Int): LiveData<Credit> {
        val call = showDetailApiCall.getCredits(id)
        return sendCreditsRequest(call, id)
    }

    private fun sendCreditsRequest(call: Call<Credit>, id: Int): LiveData<Credit> {
        val credit = MutableLiveData<Credit>()
        call.enqueue(object : Callback<Credit> {
            override fun onFailure(call: Call<Credit>, t: Throwable) {
//                val creditData = getDatabaseCredits(id, false)
//                creditData.observeForever {
//                    credit.postValue(it)
//                }

            }

            override fun onResponse(call: Call<Credit>, response: Response<Credit>) {
                if (response.isSuccessful) {
//                    handler.post {
//                        insertCredit(id, response.body()!!, false)
//                    }
                    this@ShowViewModel.credit = response.body()!!
                    credit.value = response.body()!!
                } else Log.d(
                    "MovieRepository",
                    "APIMoviesCredits_Error: $id ${fetchErrorMessage(response.errorBody()!!)}"
                )
            }
        })

        return credit
    }

    //database reviews
//    private fun insertMediaReviews(id: Int, reviews: List<Review>, isMovie: Boolean) {
//        mediaDetailsDao.insertMediaReviews(id, reviews, isMovie)
//    }
//
//    private fun getDatabaseReviews(id: Int, isMovie: Boolean): LiveData<ReviewList> {
//        val reviewList = MutableLiveData<ReviewList>()
//        viewModelScope.launch(Dispatchers.IO) {
//            reviewList.postValue(mediaDetailsDao.getReview(id, isMovie))
//        }
//
//        return reviewList
//    }

    //network reviews
    fun fetchReviews(id: Int): LiveData<List<Review>> {
        val call = showDetailApiCall.getReviews(id)
        return sendReviewsRequest(call, id)
    }

    private fun sendReviewsRequest(call: Call<ReviewList>, id: Int): LiveData<List<Review>> {
        val reviews = MutableLiveData<List<Review>>()
        call.enqueue(object : Callback<ReviewList> {
            override fun onFailure(call: Call<ReviewList>, t: Throwable) {
//                val reviewResult = getDatabaseReviews(id, false)
//                reviewResult.observeForever {
//                    reviews.value = it.results
//                }
            }

            override fun onResponse(call: Call<ReviewList>, response: Response<ReviewList>) {
                if (response.isSuccessful) {
                    val review = response.body()!!.results
//                    handler.post { insertMediaReviews(id, review, false) }
                    this@ShowViewModel.review = review
                    reviews.value = review
                } else Log.d(
                    "MovieRepository",
                    "APIMoviesReviews_Error: $id ${fetchErrorMessage(response.errorBody()!!)}"
                )
            }
        })

        return reviews
    }

    //database external ID
//    fun insertExternalIds(externalId: ExternalID, isMovie: Boolean) {
//        mediaDetailsDao.insertExternalIds(externalId, isMovie)
//    }
//
//    //network external ID
//    fun getDatabaseExternalId(id: Int, isMovie: Boolean): LiveData<ExternalID> {
//        val externalID = MutableLiveData<ExternalID>()
//        runBlocking {
//            withContext(Dispatchers.IO) {
//                externalID.postValue(mediaDetailsDao.getExternalIDs(id, isMovie))
//            }
//        }
//        return externalID
//    }

//    fun fetchExternalIds(id: Int): LiveData<ExternalID> {
//        val call = showDetailApiCall.getExternalIds(id)
//        return sendExternalIdRequest(call, id)
//    }
//
//    private fun sendExternalIdRequest(call: Call<ExternalID>, id: Int): LiveData<ExternalID> {
//        val externalID = MutableLiveData<ExternalID>()
//        call.enqueue(object : Callback<ExternalID> {
//            override fun onFailure(call: Call<ExternalID>, t: Throwable) {
//                val externalIDResult = getDatabaseExternalId(id, false)
//                externalIDResult.observeForever {
//                    externalID.value = it
//                }
//            }
//
//            override fun onResponse(call: Call<ExternalID>, response: Response<ExternalID>) {
//                if (response.isSuccessful) {
//                    val externalId = response.body()!!
//                    handler.post {
//                        insertExternalIds(externalId, false)
//                    }
//                    externalID.value = externalId
//                } else Log.d(
//                    "MovieRepository",
//                    "APIMoviesExternalID_Error: $id ${fetchErrorMessage(response.errorBody()!!)}"
//                )
//            }
//        })
//
//        return externalID
//    }

    //database images

//    private fun insertImages(id: Int, imageDetails: List<ImageDetails>, isMovie: Boolean) {
//        mediaDetailsDao.insertImages(id, imageDetails, isMovie)
//    }
//
//    private fun getDatabaseImages(id: Int, isMovie: Boolean): LiveData<Images> {
//        val images = MutableLiveData<Images>()
//        viewModelScope.launch(Dispatchers.IO) {
//            images.postValue(mediaDetailsDao.getImage(id, isMovie))
//        }
//        return images
//    }

    //network Images
    fun fetchImages(id: Int): LiveData<Images> {
        val call = showDetailApiCall.getImages(id)
        return sendImagesRequest(call, id)
    }

    private fun sendImagesRequest(call: Call<Images>, id: Int): LiveData<Images> {
        val images = MutableLiveData<Images>()
        call.enqueue(object : Callback<Images> {
            override fun onFailure(call: Call<Images>, t: Throwable) {
//                val imagesResult = getDatabaseImages(id, false)
//                imagesResult.observeForever {
//                    images.value = it
//                }
            }

            override fun onResponse(call: Call<Images>, response: Response<Images>) {
                if (response.isSuccessful) {
                    val image = response.body()!!
//                    image.backdrops?.let {
//                        handler.post { insertImages(id, it, false) }
//                    }
                    this@ShowViewModel.images = image
                    images.value = image
                } else Log.d(
                    "MovieRepository",
                    "APIMoviesImages_Error: $id ${fetchErrorMessage(response.errorBody()!!)}"
                )
            }
        })

        return images
    }

    //database keywords
//    private fun insertMediaKeywords(id: Int, keywords: List<Keyword>, isMovie: Boolean) {
//        mediaDetailsDao.insertMediaKeywords(id, keywords, isMovie)
//    }
//
//    fun getDatabaseKeywords(id: Int, isMovie: Boolean): LiveData<KeywordResult> {
//        val keywordResult = MutableLiveData<KeywordResult>()
//        viewModelScope.launch(Dispatchers.IO) {
//            keywordResult.postValue(mediaDetailsDao.getKeywords(id, isMovie))
//        }
//        return keywordResult
//    }

    //network Keywords
    fun getKeywords(id: Int): LiveData<List<Keyword>> {
        val call = showDetailApiCall.getKeywords(id)
        return sendKeywordsRequest(call, id)
    }

    private fun sendKeywordsRequest(call: Call<KeywordResult>, id: Int): LiveData<List<Keyword>> {
        val keywords = MutableLiveData<List<Keyword>>()
        call.enqueue(object : Callback<KeywordResult> {
            override fun onFailure(call: Call<KeywordResult>, t: Throwable) {
//                val keywordResult = getDatabaseKeywords(id, false)
//                keywordResult.observeForever {
//                    keywords.postValue(it.keywords)
//                }
            }

            override fun onResponse(call: Call<KeywordResult>, response: Response<KeywordResult>) {
                if (response.isSuccessful) {
//                    handler.post { insertMediaKeywords(id, response.body()!!.keywords, false) }
                    this@ShowViewModel.keywords = response.body()!!.keywords
                    keywords.postValue(response.body()!!.keywords)
                } else Log.d(
                    "MovieRepository",
                    "APIMoviesKeywords_Error: $id ${fetchErrorMessage(response.errorBody()!!)}"
                )
            }

        })
        return keywords
    }

    //database recommended movies
//    private fun insertRecommendations(id: Int, list: List<ShowPreview>, isMovie: Boolean) {
//        mediaDetailsDao.insertShowsRecommendations(id, list, isMovie)
//    }
//
//    private fun getDatabaseRecommendation(
//        id: Int,
//        isMovie: Boolean
//    ): LiveData<MediaRecommendations> {
//        val media = MutableLiveData<MediaRecommendations>()
//        viewModelScope.launch(Dispatchers.IO) {
//            media.postValue(mediaDetailsDao.getRecommendations(id, isMovie))
//        }
//        return media
//    }

    //network recommendations
    fun fetchRecommendations(id: Int): LiveData<List<ShowPreview>> {
        val call = showDetailApiCall.getRecommendations(id)
        return sendRecommendationsRequest(call, id)
    }

    private fun sendRecommendationsRequest(
        call: Call<ShowsList>,
        id: Int
    ): LiveData<List<ShowPreview>> {
        val movies = MutableLiveData<List<ShowPreview>>()
        call.enqueue(object : Callback<ShowsList> {
            override fun onFailure(call: Call<ShowsList>, t: Throwable) {
//                val moviesResult = getDatabaseRecommendation(id, false)
//                moviesResult.observeForever {
//                    movies.postValue(formatToShowPreview(it))
//                }
            }

            override fun onResponse(call: Call<ShowsList>, response: Response<ShowsList>) {
                if (response.isSuccessful) {
//                    handler.post { insertRecommendations(id, response.body()!!.results, false) }
                    this@ShowViewModel.recommendations = response.body()!!.results
                    movies.value = response.body()!!.results
                } else Log.d(
                    "MovieRepository",
                    "APIMoviesRecommendations_Error: $id ${fetchErrorMessage(response.errorBody()!!)}"
                )
            }

        })
        return movies
    }

    //database similar movies
//    private fun insertSimilar(id: Int, list: List<ShowPreview>, isMovie: Boolean) {
//        mediaDetailsDao.insertShowsSimilar(id, list, isMovie)
//    }
//
//    private fun getDatabaseSimilar(id: Int, isMovie: Boolean): LiveData<MediaSimilar> {
//        val similarMoviesList = MutableLiveData<MediaSimilar>()
//        viewModelScope.launch(Dispatchers.IO) {
//            similarMoviesList.postValue(mediaDetailsDao.getSimilar(id, isMovie))
//        }
//        return similarMoviesList
//    }

    //network similar
    fun fetchSimilarShows(id: Int): LiveData<List<ShowPreview>> {
        val call = showDetailApiCall.getSimilarShows(id)
        return sendSimilarRequest(call, id)
    }

    private fun sendSimilarRequest(call: Call<ShowsList>, id: Int): LiveData<List<ShowPreview>> {
        val movies = MutableLiveData<List<ShowPreview>>()
        call.enqueue(object : Callback<ShowsList> {
            override fun onFailure(call: Call<ShowsList>, t: Throwable) {
//                val moviesResult = getDatabaseSimilar(id, true)
//                moviesResult.observeForever {
//                    movies.postValue(formatToShowPreview(it))
//                }
            }

            override fun onResponse(call: Call<ShowsList>, response: Response<ShowsList>) {
                if (response.isSuccessful) {
//                    handler.post { insertSimilar(id, response.body()!!.results, true) }
                    this@ShowViewModel.similar = response.body()!!.results
                    movies.value = response.body()!!.results
                } else Log.d(
                    "MovieRepository",
                    "APIMoviesSimilar_Error: $id ${fetchErrorMessage(response.errorBody()!!)}"
                )
            }

        })
        return movies
    }

    private fun fetchErrorMessage(error: ResponseBody): String {
        val reader: BufferedReader?
        val sb = StringBuilder()
        try {
            reader =
                BufferedReader(InputStreamReader(error.byteStream()))
            var line: String?
            try {
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return sb.toString()
    }

}
