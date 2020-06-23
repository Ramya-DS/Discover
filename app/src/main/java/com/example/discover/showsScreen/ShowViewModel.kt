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
import com.example.discover.datamodel.review.Review
import com.example.discover.datamodel.review.ReviewList
import com.example.discover.datamodel.tvshow.detail.Season
import com.example.discover.datamodel.tvshow.detail.ShowDetails
import com.example.discover.datamodel.tvshow.preview.ShowPreview
import com.example.discover.datamodel.tvshow.preview.ShowsList
import com.example.discover.mediaScreenUtils.dao.MediaDetailsDao
import com.example.discover.roomDatabase.DiscoverDatabase
import com.example.discover.searchScreen.OnNetworkLostListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShowViewModel(private val mApplication: Application) : AndroidViewModel(mApplication) {

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

    //network shows
    fun showDetails(id: Int): LiveData<ShowDetails> {
        val call = showDetailApiCall.showDetails(id)
        return sendDetailsRequest(call, id)
    }

    private fun sendDetailsRequest(call: Call<ShowDetails>, id: Int): LiveData<ShowDetails> {
        val showDetails = MutableLiveData<ShowDetails>()
        call.enqueue(object : Callback<ShowDetails> {
            override fun onFailure(call: Call<ShowDetails>, t: Throwable) {
                onNetworkLostListener?.onNetworkDialog()
            }

            override fun onResponse(call: Call<ShowDetails>, response: Response<ShowDetails>) {
                if (response.isSuccessful) {
                    onNetworkLostListener?.onNetworkDialogDismiss()
                    writeShowDetails(response.body()!!)
                    this@ShowViewModel.showDetails = response.body()!!
                    showDetails.value = response.body()!!
                } else Log.d(
                    "MovieRepository",
                    "APIMoviesDetails_Error: $id ${(mApplication as DiscoverApplication).fetchErrorMessage(
                        response.errorBody()!!
                    )}"
                )
            }
        })
        return showDetails
    }

    fun writeShowDetails(showDetails: ShowDetails) {
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

    //network credits
    fun fetchCredits(id: Int): LiveData<Credit> {
        val call = showDetailApiCall.getCredits(id)
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
                    this@ShowViewModel.credit = response.body()!!
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
        val call = showDetailApiCall.getReviews(id)
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
                    this@ShowViewModel.review = review
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

    //network Images
    fun fetchImages(id: Int): LiveData<Images> {
        val call = showDetailApiCall.getImages(id)
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
                    this@ShowViewModel.images = image
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
        val call = showDetailApiCall.getKeywords(id)
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
                    this@ShowViewModel.keywords = response.body()!!.keywords
                    keywords.postValue(response.body()!!.keywords)
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
                onNetworkLostListener?.onNetworkDialog()
            }

            override fun onResponse(call: Call<ShowsList>, response: Response<ShowsList>) {
                if (response.isSuccessful) {
                    onNetworkLostListener?.onNetworkDialogDismiss()
                    this@ShowViewModel.recommendations = response.body()!!.results
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
    fun fetchSimilarShows(id: Int): LiveData<List<ShowPreview>> {
        val call = showDetailApiCall.getSimilarShows(id)
        return sendSimilarRequest(call, id)
    }

    private fun sendSimilarRequest(call: Call<ShowsList>, id: Int): LiveData<List<ShowPreview>> {
        val movies = MutableLiveData<List<ShowPreview>>()
        call.enqueue(object : Callback<ShowsList> {
            override fun onFailure(call: Call<ShowsList>, t: Throwable) {
                onNetworkLostListener?.onNetworkDialog()
            }

            override fun onResponse(call: Call<ShowsList>, response: Response<ShowsList>) {
                if (response.isSuccessful) {
                    onNetworkLostListener?.onNetworkDialogDismiss()
                    this@ShowViewModel.similar = response.body()!!.results
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
