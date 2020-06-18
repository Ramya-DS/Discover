package com.example.discover.movieScreen

import android.app.Application
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.discover.DiscoverApplication
import com.example.discover.datamodel.credit.Credit
import com.example.discover.datamodel.externalId.ExternalID
import com.example.discover.datamodel.genre.Genres
import com.example.discover.datamodel.images.Images
import com.example.discover.datamodel.keyword.Keyword
import com.example.discover.datamodel.keyword.KeywordResult
import com.example.discover.datamodel.media.Media
import com.example.discover.datamodel.movie.MoviesList
import com.example.discover.datamodel.movie.detail.MovieDetails
import com.example.discover.datamodel.movie.preview.MoviePreview
import com.example.discover.datamodel.review.Review
import com.example.discover.datamodel.review.ReviewList
import com.example.discover.roomDatabase.DiscoverDatabase
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class MovieViewModel(mApplication: Application) : AndroidViewModel(mApplication) {


    private val mediaDetailsDao = DiscoverDatabase.getDatabase(mApplication).mediaDetailsDao()
    private val movieDetailApiCall = (mApplication as DiscoverApplication).movieDetailApiCall
    private val languageDao = DiscoverDatabase.getDatabase(mApplication).languageDao()

    var movieDetails: MovieDetails? = null
    var credit: Credit? = null
    var review: List<Review>? = null
    var images: Images? = null
    var externalId: ExternalID? = null
    var recommendations: List<MoviePreview>? = null
    var similar: List<MoviePreview>? = null
    var keywords: List<Keyword>? = null

    var recommendedPosition = 0
    var similarPosition = 0
    var castPosition = 0
    var crewPosition = 0
    var reviewPosition = 0

    private val handlerThread = HandlerThread("insert").apply {
        start()
    }
    val handler = Handler(handlerThread.looper)

    private fun insertMovie(media: Media) {
        mediaDetailsDao.insertMedia(media)
    }

    private fun insertType(movie_id: Int, isMovie: Boolean) {
        mediaDetailsDao.insertType(movie_id, 0, isMovie)
    }

    private fun insertMediaGenres(id: Int, genres: List<Genres>, isMovie: Boolean) {
        mediaDetailsDao.insertMediaGenresObjects(id, genres, isMovie)
    }

//    private fun getDatabaseMovieDetails(id: Int, isMovie: Boolean): LiveData<MediaDatabaseDetails> {
//        val media = MutableLiveData<MediaDatabaseDetails>()
//        viewModelScope.launch(Dispatchers.IO) {
//            media.postValue(mediaDetailsDao.getMediaDetailsUsingApiID(id, isMovie))
//        }
//        return media
//    }

    //network movies
    fun movieDetails(id: Int): LiveData<MovieDetails> {
        val call = movieDetailApiCall.movieDetails(id)
        return sendDetailsRequest(call, id)
    }

    private fun sendDetailsRequest(call: Call<MovieDetails>, id: Int): LiveData<MovieDetails> {
        val movieDetails = MutableLiveData<MovieDetails>()
        call.enqueue(object : Callback<MovieDetails> {
            override fun onFailure(call: Call<MovieDetails>, t: Throwable) {
//                val movie = getDatabaseMovieDetails(id, true)
//                movie.observeForever {
//                    movieDetails.postValue(formatToMovieDetails(it))
//                }

            }

            override fun onResponse(call: Call<MovieDetails>, response: Response<MovieDetails>) {
                if (response.isSuccessful) {
                    this@MovieViewModel.movieDetails = response.body()
                    writeMoviesDetails(response.body()!!)
                    movieDetails.value = response.body()!!
                } else Log.d(
                    "MovieRepository",
                    "APIMoviesDetails_Error: $id ${fetchErrorMessage(response.errorBody()!!)}"
                )
            }

        })
        return movieDetails
    }

//    fun formatToMovieDetails(mediaDatabaseDetails: MediaDatabaseDetails): MovieDetails? {
//        if (mediaDatabaseDetails.media.runtime != 0) {
//            mediaDatabaseDetails.media.run {
//                return MovieDetails(
//                    adult,
//                    backdrop_path,
//                    budget,
//                    mediaDatabaseDetails.genres,
//                    homepage,
//                    id,
//                    original_language,
//                    overview,
//                    poster_path,
//                    release_date,
//                    revenue,
//                    runtime,
//                    status,
//                    tagLine,
//                    title,
//                    vote_average,
//                    vote_count
//                )
//            }
//        }
//        return null
//    }

    fun writeMoviesDetails(movieDetails: MovieDetails) {
        Log.d("MovieRepository", "writeMoviesDetails")
        movieDetails.apply {
            handler.post {
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
                        adult = adult,
                        homepage = homepage,
                        release_date = release_date,
                        revenue = revenue,
                        runtime = runtime,
                        tagLine = tagline,
                        title = title,
                        status = status,
                        budget = budget
                    )
                )
                insertType(movieDetails.id, true)
                insertMediaGenres(id, genres, true)
            }
        }
    }

//    fun formatToMoviePreview(media: MediaRecommendations): List<MoviePreview> {
//        val movies = mutableListOf<MoviePreview>()
//        for (i in media.recommendations) {
//            getGenreIds(i.id).observeForever {
//                i.apply {
//                    movies.add(
//                        MoviePreview(
//                            adult,
//                            backdrop_path,
//                            it,
//                            id,
//                            original_language,
//                            overview,
//                            poster_path,
//                            release_date,
//                            title,
//                            vote_average,
//                            vote_count
//                        )
//                    )
//                }
//            }
//
//        }
//
//        return movies
//    }

//    fun formatToMoviePreview(media: MediaSimilar): List<MoviePreview> {
//        val movies = mutableListOf<MoviePreview>()
//        for (i in media.similars) {
//            getGenreIds(i.id).observeForever {
//                i.apply {
//                    movies.add(
//                        MoviePreview(
//                            adult,
//                            backdrop_path,
//                            it,
//                            id,
//                            original_language,
//                            overview,
//                            poster_path,
//                            release_date,
//                            title,
//                            vote_average,
//                            vote_count
//                        )
//                    )
//                }
//            }
//        }
//
//        return movies
//    }

    //database credits
//    private fun insertCredit(id: Int, credit: Credit, isMovie: Boolean) {
//        mediaDetailsDao.insertCredit(id, credit, isMovie)
//    }
//
//    private fun getDatabaseCredits(id: Int, isMovie: Boolean): LiveData<Credit> {
//        val credit = MutableLiveData<Credit>()
//        viewModelScope.launch(Dispatchers.IO) {
//            credit.postValue(mediaDetailsDao.getCredits(id, isMovie))
//        }
//        return credit
//    }

    //network credits
    fun fetchCredits(id: Int): LiveData<Credit> {
        val call = movieDetailApiCall.creditDetails(id)
        return sendCreditsRequest(call, id)
    }

    private fun sendCreditsRequest(call: Call<Credit>, id: Int): LiveData<Credit> {
        val credit = MutableLiveData<Credit>()
        call.enqueue(object : Callback<Credit> {
            override fun onFailure(call: Call<Credit>, t: Throwable) {
//                val creditData = getDatabaseCredits(id, true)
//                creditData.observeForever {
//                    credit.postValue(it)
//                }

            }

            override fun onResponse(call: Call<Credit>, response: Response<Credit>) {
                if (response.isSuccessful) {
//                    handler.post {
//                        insertCredit(id, response.body()!!, true)
//                    }
                    this@MovieViewModel.credit = response.body()!!
                    credit.value = response.body()!!
                } else Log.d(
                    "MovieRepository",
                    "APIMoviesCredits_Error: $id ${fetchErrorMessage(response.errorBody()!!)}"
                )
            }
        })

        return credit
    }

//    //database reviews
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
        val call = movieDetailApiCall.getReviews(id)
        return sendReviewsRequest(call, id)
    }

    private fun sendReviewsRequest(call: Call<ReviewList>, id: Int): LiveData<List<Review>> {
        val reviews = MutableLiveData<List<Review>>()
        call.enqueue(object : Callback<ReviewList> {
            override fun onFailure(call: Call<ReviewList>, t: Throwable) {
//                val reviewResult = getDatabaseReviews(id, true)
//                reviewResult.observeForever {
//                    reviews.value = it.results
//                }
            }

            override fun onResponse(call: Call<ReviewList>, response: Response<ReviewList>) {
                if (response.isSuccessful) {
                    val review = response.body()!!.results
                    this@MovieViewModel.review = review
//                    handler.post { insertMediaReviews(id, review, true) }
                    reviews.value = review
                } else Log.d(
                    "MovieRepository",
                    "APIMoviesReviews_Error: $id ${fetchErrorMessage(response.errorBody()!!)}"
                )
            }
        })

        return reviews
    }

//    //database external ID
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

    fun fetchExternalIds(id: Int): LiveData<ExternalID> {
        val call = movieDetailApiCall.externalIds(id)
        return sendExternalIdRequest(call, id)
    }

    private fun sendExternalIdRequest(call: Call<ExternalID>, id: Int): LiveData<ExternalID> {
        val externalID = MutableLiveData<ExternalID>()
        call.enqueue(object : Callback<ExternalID> {
            override fun onFailure(call: Call<ExternalID>, t: Throwable) {
//                val externalIDResult = getDatabaseExternalId(id, true)
//                externalIDResult.observeForever {
//                    externalID.value = it
//                }
            }

            override fun onResponse(call: Call<ExternalID>, response: Response<ExternalID>) {
                if (response.isSuccessful) {
                    val externalId = response.body()!!
                    this@MovieViewModel.externalId = externalId
//                    handler.post {
//                        insertExternalIds(externalId, true)
//                    }
                    externalID.value = externalId
                } else Log.d(
                    "MovieRepository",
                    "APIMoviesExternalID_Error: $id ${fetchErrorMessage(response.errorBody()!!)}"
                )
            }
        })

        return externalID
    }

//    private fun getGenreIds(id: Int): LiveData<List<Int>> {
//        val genreIds = MutableLiveData<List<Int>>()
//        viewModelScope.launch(Dispatchers.IO) {
//            genreIds.postValue(mediaDetailsDao.getGenreIds(id))
//        }
//        return genreIds
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
        val call = movieDetailApiCall.getImages(id)
        return sendImagesRequest(call, id)
    }

    private fun sendImagesRequest(call: Call<Images>, id: Int): LiveData<Images> {
        val images = MutableLiveData<Images>()
        call.enqueue(object : Callback<Images> {
            override fun onFailure(call: Call<Images>, t: Throwable) {
//                val imagesResult = getDatabaseImages(id, true)
//                imagesResult.observeForever {
//                    images.value = it
//                }
            }

            override fun onResponse(call: Call<Images>, response: Response<Images>) {
                if (response.isSuccessful) {
                    val image = response.body()!!
                    this@MovieViewModel.images = image
//                    image.backdrops?.let {
//                        handler.post { insertImages(id, it, true) }
//                    }
                    images.value = image
                } else Log.d(
                    "MovieRepository",
                    "APIMoviesImages_Error: $id ${fetchErrorMessage(response.errorBody()!!)}"
                )
            }
        })

        return images
    }

//    //database keywords
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
        val call = movieDetailApiCall.getKeywords(id)
        return sendKeywordsRequest(call, id)
    }

    private fun sendKeywordsRequest(call: Call<KeywordResult>, id: Int): LiveData<List<Keyword>> {
        val keywords = MutableLiveData<List<Keyword>>()
        call.enqueue(object : Callback<KeywordResult> {
            override fun onFailure(call: Call<KeywordResult>, t: Throwable) {
//                val keywordResult = getDatabaseKeywords(id, true)
//                keywordResult.observeForever {
//                    keywords.postValue(it.keywords)
//                }
            }

            override fun onResponse(call: Call<KeywordResult>, response: Response<KeywordResult>) {
                if (response.isSuccessful) {
//                    handler.post { insertMediaKeywords(id, response.body()!!.keywords, true) }
                    this@MovieViewModel.keywords = response.body()!!.keywords
                    keywords.value = response.body()!!.keywords
                } else Log.d(
                    "MovieRepository",
                    "APIMoviesKeywords_Error: $id ${fetchErrorMessage(response.errorBody()!!)}"
                )
            }

        })
        return keywords
    }

//    //database recommended movies
//    private fun insertRecommendations(id: Int, list: List<MoviePreview>, isMovie: Boolean) {
//        mediaDetailsDao.insertMoviesRecommendations(id, list, isMovie)
//    }

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
    fun fetchRecommendations(id: Int): LiveData<List<MoviePreview>> {
        val call = movieDetailApiCall.getRecommendations(id)
        return sendRecommendationsRequest(call, id)
    }

    private fun sendRecommendationsRequest(
        call: Call<MoviesList>,
        id: Int
    ): LiveData<List<MoviePreview>> {
        val movies = MutableLiveData<List<MoviePreview>>()
        call.enqueue(object : Callback<MoviesList> {
            override fun onFailure(call: Call<MoviesList>, t: Throwable) {
//                val moviesResult = getDatabaseRecommendation(id, true)
//                moviesResult.observeForever {
//                    movies.postValue(formatToMoviePreview(it))
//                }
            }

            override fun onResponse(call: Call<MoviesList>, response: Response<MoviesList>) {
                if (response.isSuccessful) {
//                    handler.post { insertRecommendations(id, response.body()!!.results, true) }
                    this@MovieViewModel.recommendations = response.body()!!.results
                    movies.value = response.body()!!.results
                } else Log.d(
                    "MovieRepository",
                    "APIMoviesRecommendations_Error: $id ${fetchErrorMessage(response.errorBody()!!)}"
                )
            }

        })
        return movies
    }

//    //database similar movies
//    private fun insertSimilar(id: Int, list: List<MoviePreview>, isMovie: Boolean) {
//        mediaDetailsDao.insertMoviesSimilar(id, list, isMovie)
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
    fun fetchSimilarMovies(id: Int): LiveData<List<MoviePreview>> {
        val call = movieDetailApiCall.getSimilar(id)
        return sendSimilarRequest(call, id)
    }

    private fun sendSimilarRequest(call: Call<MoviesList>, id: Int): LiveData<List<MoviePreview>> {
        val movies = MutableLiveData<List<MoviePreview>>()
        call.enqueue(object : Callback<MoviesList> {
            override fun onFailure(call: Call<MoviesList>, t: Throwable) {
//                val moviesResult = getDatabaseSimilar(id, true)
//                moviesResult.observeForever {
//                    movies.postValue(formatToMoviePreview(it))
//                }
            }

            override fun onResponse(call: Call<MoviesList>, response: Response<MoviesList>) {
                if (response.isSuccessful) {
//                    handler.post { insertSimilar(id, response.body()!!.results, true) }
                    this@MovieViewModel.similar = response.body()!!.results
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