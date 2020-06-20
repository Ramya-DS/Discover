package com.example.discover.showsScreen

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.discover.DiscoverApplication
import com.example.discover.R
import com.example.discover.category.CategoryListAdapter
import com.example.discover.datamodel.Language
import com.example.discover.datamodel.credit.Credit
import com.example.discover.datamodel.genre.Genres
import com.example.discover.datamodel.images.ImageDetails
import com.example.discover.datamodel.images.Images
import com.example.discover.datamodel.keyword.Keyword
import com.example.discover.datamodel.review.Review
import com.example.discover.datamodel.tvshow.detail.Season
import com.example.discover.datamodel.tvshow.detail.ShowDetails
import com.example.discover.datamodel.tvshow.preview.ShowPreview
import com.example.discover.firstScreen.GenreAdapter
import com.example.discover.firstScreen.OnGenreSelectedListener
import com.example.discover.genreScreen.GenreMediaActivity
import com.example.discover.mediaScreenUtils.*
import com.example.discover.searchScreen.OnNetworkLostListener
import com.example.discover.util.LoadPoster
import com.example.discover.util.NetworkSnackbar
import com.example.discover.util.NoSwipeBehavior
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.lang.ref.WeakReference
import kotlin.math.abs

class ShowActivity : AppCompatActivity(), OnReviewClickListener, OnUrlSelectedListener,
    OnNetworkLostListener, OnGenreSelectedListener {

    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var tabLayout: TabLayout
    private lateinit var collapsingToolbarLayout: CollapsingToolbarLayout
    private lateinit var toolbar: MaterialToolbar
    private lateinit var backdrop: ViewPager2
    private lateinit var poster: ImageView
    private lateinit var title: TextView
    private lateinit var rating: TextView
    private lateinit var runtime: TextView
    private lateinit var details: TextView
    private lateinit var firstAirDate: TextView
    private lateinit var language: TextView
    private lateinit var overview: TextView

    private lateinit var createdBy: RecyclerView

    private lateinit var allLanguage: List<Language>

    private lateinit var viewModel: ShowViewModel

    private lateinit var currentShow: ShowPreview
    private var snackbar: NetworkSnackbar? = null
    private lateinit var genres: List<Genres>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition()
        }

        fetchIntentData()

        bindActivity(savedInstanceState)

        appBarLayout = findViewById(R.id.show_detail_appbar)
        collapsingToolbarLayout = findViewById<CollapsingToolbarLayout>(R.id.show_detail_collapsing)
        toolbar = findViewById(R.id.show_detail_toolbar)
        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            supportFinishAfterTransition()
        }

        collapsingToolbarChanges()

    }

    private fun fetchIntentData() {
        currentShow = intent?.getParcelableExtra("show")!!
        Log.d("showIntent", "$currentShow")
    }


    private fun collapsingToolbarChanges() {
        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            appBarLayout.post {
                if (abs(verticalOffset) == appBarLayout.totalScrollRange) {
                    toolbar.background = ContextCompat.getDrawable(this, R.drawable.main_background)
                    collapsingToolbarLayout.title = currentShow.name
                } else if (verticalOffset == 0) {
                    collapsingToolbarLayout.title = " "
                } else {
                    collapsingToolbarLayout.title = " "
                    toolbar.background = null
                }
            }
        })
    }

    private fun bindActivity(savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(ShowViewModel::class.java)

        viewModel.onNetworkLostListener = this

        (application as DiscoverApplication).languages.observe(this, Observer {
            allLanguage = it
        })

        coordinatorLayout = findViewById(R.id.show_detail_coordinator)
        tabLayout = findViewById(R.id.show_detail_tab_layout)
        backdrop = findViewById(R.id.show_detail_backdrop)
        poster = findViewById(R.id.show_detail_poster)
        title = findViewById(R.id.show_detail_title)
        rating = findViewById(R.id.show_detail_rating)
        runtime = findViewById(R.id.show_detail_runtime)
        details = findViewById(R.id.show_detail_details)
        firstAirDate = findViewById(R.id.show_details_first_air_date)
        language = findViewById(R.id.show_detail_language)
        overview = findViewById(R.id.show_detail_overview)

        createdBy = findViewById(R.id.show_detail_createdBy)

        (application as DiscoverApplication).showsGenres.observe(this, Observer {
            genres = it
            assignIntentDataToViews()
        })

        if (savedInstanceState == null) {
            viewModel.showDetails(currentShow.id).observe(this, Observer {
                setShowDetails(it)
            })

            viewModel.fetchCredits(currentShow.id).observe(this, Observer {
                setCredits(it)
            })

            viewModel.getKeywords(currentShow.id).observe(this, Observer {
                if (it == null) setKeywords(emptyList()) else setKeywords(it)
            })

            viewModel.fetchRecommendations(currentShow.id).observe(this, Observer {
                setRecommendations(it)
            })

            viewModel.fetchSimilarShows(currentShow.id).observe(this, Observer {
                setSimilarShows(it)
            })

            viewModel.fetchReviews(currentShow.id).observe(this, Observer {
                setReviews(it)
            })

            viewModel.fetchImages(currentShow.id).observe(this, Observer {
                setImages(it)
            })
        }
    }

    private fun assignIntentDataToViews() {
        currentShow.let {
            LoadPoster(
                WeakReference(this@ShowActivity.poster),
                WeakReference(this)
            ).execute(it.poster_path)
            title.text = it.name
            rating.text = "  ${it.vote_average}"
            language.text = "Original Language: ${getLanguageName(it.original_language)} "
            firstAirDate.text = "First Air-date: ${it.first_air_date ?: '-'}"

            if (it.overview != null && it.overview.isNotEmpty())
                overview.text = it.overview
            else {
                overview.visibility = View.GONE
                findViewById<View>(R.id.show_detail_overview_heading).visibility = View.GONE
            }
            setGenres(genresForIds(it.genre_ids))
        }
    }

    private fun setShowDetails(showDetails: ShowDetails) {
        LoadPoster(WeakReference(poster), WeakReference(this)).execute(showDetails.poster_path)
        title.text = showDetails.name
        rating.text = "  ${showDetails.vote_average}"

        if (showDetails.episode_run_time.isNotEmpty()) {
            val time = "Runtime: ${showDetails.episode_run_time[0]} minutes"
            runtime.text = time
        } else runtime.text = "Runtime: -"

        var details = if (showDetails.in_production) "In Production " else "Not in Production "
        details += "|  ${showDetails.type}"
        this.details.text = details

        language.text = "Original Language: ${getLanguageName(showDetails.original_language)} "
        firstAirDate.text = "First Air-date: ${showDetails.first_air_date ?: '-'}"

        if (showDetails.overview != null && showDetails.overview.isEmpty())
            overview.text = showDetails.overview
        else {
            overview.visibility = View.GONE
            findViewById<View>(R.id.show_detail_overview_heading).visibility = View.GONE
        }
//        overview.text = showDetails.overview

        setGenres(showDetails.genres)
        setSeasons(showDetails.seasons)

        val createdHeading: TextView = findViewById(R.id.show_detail_created_by_heading)

        if (showDetails.created_by.isEmpty()) {
            createdHeading.visibility = View.GONE
            createdBy.visibility = View.GONE
            return
        }
        createdBy.layoutManager = GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false)
        createdBy.setHasFixedSize(true)
        createdBy.adapter = CreditAdapter(true, WeakReference(this)).apply {
            crewList = showDetails.created_by
        }
    }

    private fun getLanguageName(id: String): String {
        for (i in allLanguage)
            if (i.isoId == id)
                return i.name

        return id
    }

    private fun setCredits(credit: Credit) {
        val castHeading: TextView = findViewById(R.id.show_detail_cast_heading)
        val crewHeading: TextView = findViewById(R.id.show_detail_crew_heading)

        val cast: RecyclerView = findViewById(R.id.show_detail_cast)
        val crew: RecyclerView = findViewById(R.id.show_detail_crew)

        if (credit.cast.isEmpty()) {
            castHeading.visibility = View.GONE
            cast.visibility = View.GONE
        } else {
            cast.layoutManager = GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false)
            cast.setHasFixedSize(true)
            cast.adapter = CreditAdapter(false, WeakReference(this)).apply {
                castList = credit.cast
            }
            cast.scrollToPosition(viewModel.castPosition)
        }

        if (credit.crew.isEmpty()) {
            crewHeading.visibility = View.GONE
            crew.visibility = View.GONE
        } else {
            crew.layoutManager = GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false)
            crew.setHasFixedSize(true)
            crew.adapter = CreditAdapter(true, WeakReference(this)).apply {
                crewList = credit.crew
            }
            crew.scrollToPosition(viewModel.crewPosition)
        }
    }

    private fun setGenres(genres: List<Genres>) {
        val genreHeading: TextView = findViewById(R.id.show_detail_genres_heading)
        val genresList: RecyclerView = findViewById(R.id.show_detail_genresList)

        if (genres.isEmpty()) {
            genreHeading.visibility = View.GONE
            genresList.visibility = View.GONE
            return
        }
        genresList.layoutManager =
            GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false)
        genresList.setHasFixedSize(true)
        genresList.adapter =
            GenreAdapter(
                default = true,
                isGenre = true,
                isMovie = false,
                onGenreSelectedListener = this
            ).apply {
                setGenresList(genres)
            }
    }

    private fun setKeywords(keywords: List<Keyword>) {
        val keywordHeading: TextView = findViewById(R.id.show_detail_keyword_heading)
        val keywordsList: RecyclerView = findViewById(R.id.show_detail_keywordsList)

        if (keywords.isEmpty()) {
            keywordHeading.visibility = View.GONE
            keywordsList.visibility = View.GONE
            return
        }

        keywordsList.layoutManager =
            GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false)
        keywordsList.setHasFixedSize(true)
        keywordsList.adapter = GenreAdapter(default = true, isGenre = false).apply {
            setKeywordList(keywords)

        }

    }

    override fun onUrlSelected(url: String, type: Int) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }

    override fun onReviewClicked(review: Review) {
        onNetworkDialogDismiss()
        val intent = Intent(this, MediaReviewActivity::class.java)
        intent.putExtra("review", review)
        startActivity(intent)
    }

    private fun setRecommendations(shows: List<ShowPreview>) {
        val recommendationHeading: TextView =
            findViewById(R.id.show_detail_recommendation_heading)
        val recommendations: RecyclerView = findViewById(R.id.show_detail_recommendations)

        if (shows.isEmpty()) {
            recommendationHeading.visibility = View.GONE
            recommendations.visibility = View.GONE
            return
        }

        recommendations.layoutManager =
            GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false)
        recommendations.setHasFixedSize(true)
        recommendations.adapter =
            CategoryListAdapter(false, WeakReference(this), false, this).apply {
                setTvSectionList(shows)
            }
        recommendations.scrollToPosition(viewModel.recommendedPosition)
    }

    private fun setSimilarShows(shows: List<ShowPreview>) {
        val similarHeading: TextView =
            findViewById(R.id.show_detail_similar_heading)
        val similarShows: RecyclerView = findViewById(R.id.show_detail_similarMovies)

        if (shows.isEmpty()) {
            similarHeading.visibility = View.GONE
            similarShows.visibility = View.GONE
            return
        }

        similarShows.layoutManager =
            GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false)
        similarShows.setHasFixedSize(true)
        similarShows.adapter = CategoryListAdapter(false, WeakReference(this), false, this).apply {
            setTvSectionList(shows)
        }
        similarShows.scrollToPosition(viewModel.similarPosition)
    }

    private fun setReviews(reviews: List<Review>) {
        val reviewHeading: TextView = findViewById(R.id.show_detail_review_heading)
        val reviewsList: RecyclerView = findViewById(R.id.show_detail_reviews)

        if (reviews.isEmpty()) {
            reviewHeading.visibility = View.INVISIBLE
            reviewsList.visibility = View.GONE
            return
        }

        reviewsList.layoutManager =
            GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false)
        reviewsList.setHasFixedSize(true)
        reviewsList.adapter = ReviewAdapter(reviews, this)
        reviewsList.scrollToPosition(viewModel.reviewPosition)
    }

    private fun setSeasons(season: List<Season>) {
        val seasonHeading: TextView = findViewById(R.id.show_detail_seasons_heading)
        val seasonsList: RecyclerView = findViewById(R.id.show_detail_season)

        if (season.isEmpty()) {
            seasonHeading.visibility = View.GONE
            seasonsList.visibility = View.GONE
            return
        }
        seasonsList.layoutManager =
            GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false)
        seasonsList.setHasFixedSize(true)
        seasonsList.adapter = SeasonAdapter(WeakReference(this)).apply {
            fetchSeasons(season)
        }
        seasonsList.scrollToPosition(viewModel.seasonPosition)
    }

    fun onSeasonClicked(season: Season) {
        if ((application as DiscoverApplication).checkConnectivity()) {
            onNetworkDialogDismiss()
            val intent = Intent(this, SeasonActivity::class.java).apply {
                putExtra("show id", currentShow.id)
                putExtra("season", season)
                putExtra("show name", currentShow.name)
            }
            startActivity(intent)
        } else
            onNetworkDialog()
    }

    private fun setImages(images: Images) {
        backdrop.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        backdrop.adapter =
            if (images.backdrops != null && images.backdrops.isNotEmpty()) {
                ImageAdapter(true, images.backdrops, WeakReference(this))
            } else if (currentShow.backdrop_path != null) {
                ImageAdapter(
                    true,
                    listOf(ImageDetails(0.0, currentShow.backdrop_path!!)),
                    WeakReference(this)
                )
            } else {
                ImageAdapter(true, null, WeakReference(this))
            }

        TabLayoutMediator(tabLayout, backdrop)
        { tab, _ ->
            backdrop.setCurrentItem(tab.position, true)
        }.attach()
    }

    override fun onNetworkLostFragment() {

    }

    override fun onNetworkDialog() {
        if (snackbar == null)
            snackbar = NetworkSnackbar.make(coordinatorLayout)
                .setBehavior(NoSwipeBehavior())

        snackbar?.show()
    }

    override fun onNetworkDialogDismiss() {
        snackbar?.dismiss()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        viewModel.showDetails?.let {
            setShowDetails(it)
        }
        viewModel.credit?.let {
            setCredits(it)
        }
        viewModel.review?.let {
            setReviews(it)
        }

        viewModel.keywords?.let {
            setKeywords(it)
        }

        viewModel.recommendations?.let {
            setRecommendations(it)
        }

        viewModel.similar?.let {
            setSimilarShows(it)
        }

        viewModel.images?.let {
            setImages(it)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        (findViewById<RecyclerView>(R.id.show_detail_recommendations).layoutManager)?.apply {
            viewModel.recommendedPosition =
                (this as GridLayoutManager).findFirstCompletelyVisibleItemPosition()
        }

        (findViewById<RecyclerView>(R.id.show_detail_similarMovies).layoutManager)?.apply {
            viewModel.similarPosition =
                (this as GridLayoutManager).findFirstCompletelyVisibleItemPosition()
        }

        (findViewById<RecyclerView>(R.id.show_detail_cast).layoutManager)?.apply {
            viewModel.castPosition =
                (this as GridLayoutManager).findFirstCompletelyVisibleItemPosition()
        }

        (findViewById<RecyclerView>(R.id.show_detail_crew).layoutManager)?.apply {
            viewModel.crewPosition =
                (this as GridLayoutManager).findFirstCompletelyVisibleItemPosition()
        }

        (findViewById<RecyclerView>(R.id.show_detail_reviews).layoutManager)?.apply {
            viewModel.reviewPosition =
                (this as GridLayoutManager).findFirstCompletelyVisibleItemPosition()
        }

        (findViewById<RecyclerView>(R.id.show_detail_season).layoutManager)?.apply {
            viewModel.seasonPosition =
                (this as GridLayoutManager).findFirstCompletelyVisibleItemPosition()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        supportFinishAfterTransition()
    }

    override fun onGenreSelected(isMovie: Boolean, genreId: Int) {
        startActivity(Intent(this, GenreMediaActivity::class.java).apply {
            putExtra("isMovie", isMovie)
            putExtra("id", genreId)
        })
        overridePendingTransition(R.anim.right_in, R.anim.left_out)
    }

    private fun genresForIds(list: List<Int>): List<Genres> {
        val genreList = mutableListOf<Genres>()
        for (i in genres)
            if (list.contains(i.id))
                genreList.add(i)
        return genreList
    }
}

