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
import com.example.discover.searchScreen.OnNetworkLostListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MovieViewModel(private val mApplication: Application) : AndroidViewModel(mApplication) {


    private val mediaDetailsDao = DiscoverDatabase.getDatabase(mApplication).mediaDetailsDao()
    private val movieDetailApiCall = (mApplication as DiscoverApplication).movieDetailApiCall

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

    var onNetworkLostListener: OnNetworkLostListener? = null

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

    //network movies
    fun movieDetails(id: Int): LiveData<MovieDetails> {
        val call = movieDetailApiCall.movieDetails(id)
        return sendDetailsRequest(call, id)
    }

    private fun sendDetailsRequest(call: Call<MovieDetails>, id: Int): LiveData<MovieDetails> {
        val movieDetails = MutableLiveData<MovieDetails>()
        call.enqueue(object : Callback<MovieDetails> {
            override fun onFailure(call: Call<MovieDetails>, t: Throwable) {
                onNetworkLostListener?.onNetworkDialog()
            }

            override fun onResponse(call: Call<MovieDetails>, response: Response<MovieDetails>) {
                if (response.isSuccessful) {
                    onNetworkLostListener?.onNetworkDialogDismiss()
                    this@MovieViewModel.movieDetails = response.body()
                    writeMoviesDetails(response.body()!!)
                    movieDetails.value = response.body()!!
                } else Log.d(
                    "MovieRepository",
                    "APIMoviesDetails_Error: $id ${(mApplication as DiscoverApplication).fetchErrorMessage(
                        response.errorBody()!!
                    )}"
                )
            }

        })
        return movieDetails
    }

    fun writeMoviesDetails(movieDetails: MovieDetails) {
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

    //network credits
    fun fetchCredits(id: Int): LiveData<Credit> {
        val call = movieDetailApiCall.creditDetails(id)
        return sendCreditsRequest(call, id)
    }

    private fun sendCreditsRequest(call: Call<Credit>, id: Int): LiveData<Credit> {
        val credit = MutableLiveData<Credit>()
        call.enqueue(object : Callback<Credit> {
            override fun onFailure(call: Call<Credit>, t: Throwable) {
                onNetworkLostListener?.onNetworkDialog()
            }

            override fun onResponse(call: Call<Credit>, response: Response<Credit>) {
                if (response.isSuccessful) {
                    onNetworkLostListener?.onNetworkDialogDismiss()
                    this@MovieViewModel.credit = response.body()!!
                    credit.value = response.body()!!
                } else Log.d(
                    "MovieRepository",
                    "APIMoviesCredits_Error: $id ${(mApplication as DiscoverApplication).fetchErrorMessage(
                        response.errorBody()!!
                    )}"
                )
            }
        })

        return credit
    }

    //network reviews
    fun fetchReviews(id: Int): LiveData<List<Review>> {
        val call = movieDetailApiCall.getReviews(id)
        return sendReviewsRequest(call, id)
    }

    private fun sendReviewsRequest(call: Call<ReviewList>, id: Int): LiveData<List<Review>> {
        val reviews = MutableLiveData<List<Review>>()
        call.enqueue(object : Callback<ReviewList> {
            override fun onFailure(call: Call<ReviewList>, t: Throwable) {
                onNetworkLostListener?.onNetworkDialog()
            }

            override fun onResponse(call: Call<ReviewList>, response: Response<ReviewList>) {
                if (response.isSuccessful) {
                    onNetworkLostListener?.onNetworkDialogDismiss()
                    val review = response.body()!!.results
                    this@MovieViewModel.review = review
                    reviews.value = review
                } else Log.d(
                    "MovieRepository",
                    "APIMoviesReviews_Error: $id ${(mApplication as DiscoverApplication).fetchErrorMessage(
                        response.errorBody()!!
                    )}"
                )
            }
        })

        return reviews
    }

    fun fetchExternalIds(id: Int): LiveData<ExternalID> {
        val call = movieDetailApiCall.externalIds(id)
        return sendExternalIdRequest(call, id)
    }

    private fun sendExternalIdRequest(call: Call<ExternalID>, id: Int): LiveData<ExternalID> {
        val externalID = MutableLiveData<ExternalID>()
        call.enqueue(object : Callback<ExternalID> {
            override fun onFailure(call: Call<ExternalID>, t: Throwable) {
                onNetworkLostListener?.onNetworkDialog()
            }

            override fun onResponse(call: Call<ExternalID>, response: Response<ExternalID>) {
                if (response.isSuccessful) {
                    onNetworkLostListener?.onNetworkDialogDismiss()
                    val externalId = response.body()!!
                    this@MovieViewModel.externalId = externalId
                    externalID.value = externalId
                } else Log.d(
                    "MovieRepository",
                    "APIMoviesExternalID_Error: $id ${(mApplication as DiscoverApplication).fetchErrorMessage(
                        response.errorBody()!!
                    )}"
                )
            }
        })

        return externalID
    }

    //network Images
    fun fetchImages(id: Int): LiveData<Images> {
        val call = movieDetailApiCall.getImages(id)
        return sendImagesRequest(call, id)
    }

    private fun sendImagesRequest(call: Call<Images>, id: Int): LiveData<Images> {
        val images = MutableLiveData<Images>()
        call.enqueue(object : Callback<Images> {
            override fun onFailure(call: Call<Images>, t: Throwable) {
                onNetworkLostListener?.onNetworkDialog()
            }

            override fun onResponse(call: Call<Images>, response: Response<Images>) {
                if (response.isSuccessful) {
                    onNetworkLostListener?.onNetworkDialogDismiss()
                    val image = response.body()!!
                    this@MovieViewModel.images = image
                    images.value = image
                } else Log.d(
                    "MovieRepository",
                    "APIMoviesImages_Error: $id ${(mApplication as DiscoverApplication).fetchErrorMessage(
                        response.errorBody()!!
                    )}"
                )
            }
        })

        return images
    }

    //network Keywords
    fun getKeywords(id: Int): LiveData<List<Keyword>> {
        val call = movieDetailApiCall.getKeywords(id)
        return sendKeywordsRequest(call, id)
    }

    private fun sendKeywordsRequest(call: Call<KeywordResult>, id: Int): LiveData<List<Keyword>> {
        val keywords = MutableLiveData<List<Keyword>>()
        call.enqueue(object : Callback<KeywordResult> {
            override fun onFailure(call: Call<KeywordResult>, t: Throwable) {
                onNetworkLostListener?.onNetworkDialog()
            }

            override fun onResponse(call: Call<KeywordResult>, response: Response<KeywordResult>) {
                if (response.isSuccessful) {
                    onNetworkLostListener?.onNetworkDialogDismiss()
                    this@MovieViewModel.keywords = response.body()!!.keywords
                    keywords.value = response.body()!!.keywords
                } else Log.d(
                    "MovieRepository",
                    "APIMoviesKeywords_Error: $id ${(mApplication as DiscoverApplication).fetchErrorMessage(
                        response.errorBody()!!
                    )}"
                )
            }

        })
        return keywords
    }

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
                onNetworkLostListener?.onNetworkDialog()
            }

            override fun onResponse(call: Call<MoviesList>, response: Response<MoviesList>) {
                if (response.isSuccessful) {
                    onNetworkLostListener?.onNetworkDialogDismiss()
                    this@MovieViewModel.recommendations = response.body()!!.results
                    movies.value = response.body()!!.results
                } else Log.d(
                    "MovieRepository",
                    "APIMoviesRecommendations_Error: $id ${(mApplication as DiscoverApplication).fetchErrorMessage(
                        response.errorBody()!!
                    )}"
                )
            }

        })
        return movies
    }

    //network similar
    fun fetchSimilarMovies(id: Int): LiveData<List<MoviePreview>> {
        val call = movieDetailApiCall.getSimilar(id)
        return sendSimilarRequest(call, id)
    }

    private fun sendSimilarRequest(call: Call<MoviesList>, id: Int): LiveData<List<MoviePreview>> {
        val movies = MutableLiveData<List<MoviePreview>>()
        call.enqueue(object : Callback<MoviesList> {
            override fun onFailure(call: Call<MoviesList>, t: Throwable) {
                onNetworkLostListener?.onNetworkDialog()
            }

            override fun onResponse(call: Call<MoviesList>, response: Response<MoviesList>) {
                if (response.isSuccessful) {
                    onNetworkLostListener?.onNetworkDialogDismiss()
                    this@MovieViewModel.similar = response.body()!!.results
                    movies.value = response.body()!!.results
                } else Log.d(
                    "MovieRepository",
                    "APIMoviesSimilar_Error: $id ${(mApplication as DiscoverApplication).fetchErrorMessage(
                        response.errorBody()!!
                    )}"
                )
            }

        })
        return movies
    }
}