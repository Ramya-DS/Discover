package com.example.discover.movieScreen

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
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
import com.example.discover.datamodel.externalId.ExternalID
import com.example.discover.datamodel.genre.Genres
import com.example.discover.datamodel.images.ImageDetails
import com.example.discover.datamodel.images.Images
import com.example.discover.datamodel.keyword.Keyword
import com.example.discover.datamodel.movie.detail.MovieDetails
import com.example.discover.datamodel.movie.preview.MoviePreview
import com.example.discover.datamodel.review.Review
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

class MovieActivity : AppCompatActivity(),
    OnReviewClickListener,
    OnUrlSelectedListener, OnNetworkLostListener, OnGenreSelectedListener {

    private var id = 0

    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var tabLayout: TabLayout
    private lateinit var collapsingToolbarLayout: CollapsingToolbarLayout
    private lateinit var toolbar: MaterialToolbar
    private lateinit var movieName: String
    private lateinit var backdropImage: ViewPager2
    private lateinit var posterImage: ImageView
    private lateinit var rating: TextView
    private lateinit var title: TextView
    private lateinit var tagLine: TextView
    private lateinit var runtime: TextView
    private lateinit var overview: TextView
    private lateinit var status: TextView
    private lateinit var infoButton: ImageButton


    private lateinit var cast: RecyclerView
    private lateinit var crew: RecyclerView


    private var infoDialog: InfoDialogFragment? = null
    private var linkDialog: InfoDialogFragment? = null
    lateinit var viewModel: MovieViewModel

    private lateinit var languages: List<Language>

    private var menu: Menu? = null

    var currentMovie: MovieDetails? = null

    private var snackbar: NetworkSnackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_detail)

        fetchIntentData()
        bindActivity(savedInstanceState)

        appBarLayout = findViewById(R.id.movie_detail_app_bar)
        collapsingToolbarLayout = findViewById(R.id.movie_detail_collapsing)
        toolbar = findViewById(R.id.movie_detail_toolbar)
        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            supportFinishAfterTransition()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.movie_detail_menu, menu)
        collapsingToolbarChanges()
        return true
    }

    private fun collapsingToolbarChanges() {
        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val infoItem = menu?.findItem(R.id.info_menu)
            appBarLayout.post {
                if (abs(verticalOffset) == appBarLayout.totalScrollRange) {
                    toolbar.background = ContextCompat.getDrawable(this, R.drawable.main_background)
                    collapsingToolbarLayout.title = movieName
                    infoItem?.isVisible = true
                } else if (verticalOffset == 0) {
                    collapsingToolbarLayout.title = " "
                    infoItem?.isVisible = false
                } else {
                    collapsingToolbarLayout.title = " "
                    toolbar.background = null
                }
            }
        })
    }

    private fun fetchIntentData() {
        id = intent.getIntExtra("id", 0)
        movieName = intent.getStringExtra("name")!!
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.info_menu) {
            displayInfoDialog(infoDialog!!)
            return true
        } else if (item.itemId == R.id.externalLink) {
            displayInfoDialog(linkDialog!!)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun bindActivity(savedInstanceState: Bundle?) {
        coordinatorLayout = findViewById(R.id.movie_detail_coordinator)
        backdropImage = findViewById(R.id.movie_detail_backdrop)
        tabLayout = findViewById(R.id.movie_detail_tab_layout)
        posterImage = findViewById(R.id.movie_detail_poster)
        rating = findViewById(R.id.movie_detail_rating)
        title = findViewById(R.id.movie_detail_title)
        tagLine = findViewById(R.id.movie_detail_tagline)
        runtime = findViewById(R.id.movie_detail_runtime)
        overview = findViewById(R.id.movie_detail_overview)
        cast = findViewById(R.id.movie_detail_cast)
        crew = findViewById(R.id.movie_detail_crew)
        status = findViewById(R.id.movie_detail_status)
        infoButton = findViewById(R.id.movie_detail_more_info)

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(MovieViewModel::class.java)

        (application as DiscoverApplication).languages.observe(this, Observer {
            languages = it
        })

        if (savedInstanceState == null) {
            viewModel.movieDetails(id).observe(this, Observer {
                setMovieDetails(it)
            })

            viewModel.fetchCredits(id).observe(this, Observer {
                setCredits(it)
            })

            viewModel.getKeywords(id).observe(this, Observer {
                setKeywords(it)
            })

            viewModel.fetchRecommendations(id).observe(this, Observer {
                setRecommendations(it)
            })

            viewModel.fetchSimilarMovies(id).observe(this, Observer {
                setSimilarMovies(it)
            })

            viewModel.fetchReviews(id).observe(this, Observer {
                setReviews(it)
            })

            viewModel.fetchExternalIds(id).observe(this, Observer {
                setExternalIds(it)
            })

            viewModel.fetchImages(id).observe(this, Observer {
                setImages(it)
            })
        }
    }


    private fun setMovieDetails(movieDetails: MovieDetails) {
        currentMovie = movieDetails
        infoDialog =
            InfoDialogFragment.newInfoInstance(createInfo(movieDetails), "More Information")

        LoadPoster(
            WeakReference(posterImage),
            WeakReference(this)
        ).execute(movieDetails.poster_path)

        title.text = movieDetails.title
        rating.text = "${movieDetails.vote_average} "
        val tag =
            if (movieDetails.tagline != null && movieDetails.tagline.isNotEmpty()) "Tagline: ${movieDetails.tagline}" else "Tagline: -"
        tagLine.text = tag
        val text = "Runtime: ${movieDetails.runtime} minutes"
        runtime.text = text
        overview.text = movieDetails.overview
        status.text = movieDetails.status

        setGenres(movieDetails.genres)
        infoButton.setOnClickListener {
            displayInfoDialog(infoDialog!!)
        }
    }

    private fun setCredits(credit: Credit) {
        val crewHeading = findViewById<TextView>(R.id.movie_detail_crew_heading)
        val castHeading = findViewById<TextView>(R.id.movie_detail_cast_heading)

        if (credit.cast.isEmpty()) {
            castHeading.visibility = View.GONE
            cast.visibility = View.GONE
        } else {
            cast.layoutManager = GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false)
            cast.setHasFixedSize(true)
            cast.adapter = CreditAdapter(
                false,
                WeakReference(this)
            ).apply {
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
            crew.adapter = CreditAdapter(
                true,
                WeakReference(this)
            ).apply {
                crewList = credit.crew
            }
            crew.scrollToPosition(viewModel.crewPosition)
        }
    }

    private fun setGenres(genres: List<Genres>) {
        val genreHeading: TextView = findViewById(R.id.movie_detail_genre_heading)
        val genresList: RecyclerView = findViewById(R.id.movie_detail_genresList)

        if (genres.isEmpty()) {
            genreHeading.visibility = View.GONE
            genresList.visibility = View.GONE
            return
        }

        genresList.layoutManager = GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false)
        genresList.setHasFixedSize(true)
        genresList.adapter =
            GenreAdapter(
                default = true,
                isGenre = true,
                isMovie = true,
                onGenreSelectedListener = this
            ).apply {
                setGenresList(genres)
            }

    }

    private fun setKeywords(keywords: List<Keyword>) {
        val keywordHeading: TextView = findViewById(R.id.movie_detail_keywords_heading)
        val keywordList: RecyclerView = findViewById(R.id.movie_detail_keywordsList)

        if (keywords.isEmpty()) {
            keywordHeading.visibility = View.GONE
            keywordList.visibility = View.GONE
            return
        }

        keywordList.layoutManager = GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false)
        keywordList.setHasFixedSize(true)
        keywordList.adapter = GenreAdapter(default = true, isGenre = false).apply {
            setKeywordList(keywords)
        }
    }

    private fun setRecommendations(movies: List<MoviePreview>) {
        val recommendationHeading: TextView =
            findViewById(R.id.movie_detail_recommendations_heading)
        val recommendations: RecyclerView = findViewById(R.id.movie_detail_recommendations)

        if (movies.isEmpty()) {
            recommendationHeading.visibility = View.GONE
            recommendations.visibility = View.GONE
            return
        }
        recommendations.layoutManager =
            GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false)
        recommendations.setHasFixedSize(true)
        recommendations.adapter =
            CategoryListAdapter(true, WeakReference(this), false, this).apply {
                setMovieSectionList(movies)
            }
        recommendations.scrollToPosition(viewModel.recommendedPosition)
    }

    private fun setSimilarMovies(movies: List<MoviePreview>) {
        val similarHeading: TextView =
            findViewById(R.id.movie_detail_similar_heading)
        val similarMoviesList: RecyclerView = findViewById(R.id.movie_detail_similarMovies)

        if (movies.isEmpty()) {
            similarHeading.visibility = View.GONE
            similarMoviesList.visibility = View.GONE
            return
        }

        similarMoviesList.layoutManager =
            GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false)
        similarMoviesList.setHasFixedSize(true)
        similarMoviesList.adapter =
            CategoryListAdapter(true, WeakReference(this), false, this).apply {
                setMovieSectionList(movies)
            }
        similarMoviesList.scrollToPosition(viewModel.similarPosition)
    }

    private fun setReviews(reviews: List<Review>) {
        val reviewHeading: TextView = findViewById(R.id.movie_detail_review_heading)
        val reviewsList: RecyclerView = findViewById(R.id.movie_detail_reviews)

        if (reviews.isEmpty()) {
            reviewHeading.visibility = View.INVISIBLE
            reviewsList.visibility = View.GONE
            return
        }
        reviewsList.layoutManager = GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false)
        reviewsList.setHasFixedSize(true)
        reviewsList.adapter = ReviewAdapter(reviews, this)
    }

    private fun createInfo(movieDetails: MovieDetails): List<InfoClass> {
        val list = mutableListOf<InfoClass>()
        if (movieDetails.revenue != 0L)
            list.add(
                InfoClass(
                    R.drawable.ic_revenue,
                    movieDetails.revenue.toString(),
                    "Revenue"
                )
            )
        if (movieDetails.homepage != null && movieDetails.homepage.trim().isNotEmpty())
            list.add(
                InfoClass(
                    R.drawable.ic_homepage,
                    movieDetails.homepage,
                    "Homepage"
                )
            )
        list.add(
            InfoClass(
                R.drawable.ic_language,
                getLanguageName(movieDetails.original_language),
                "Original Language"
            )
        )
        if (movieDetails.release_date != null && movieDetails.release_date.trim().isNotEmpty())
            list.add(
                InfoClass(
                    R.drawable.ic_release_date,
                    movieDetails.release_date,
                    "Release Date"
                )
            )
        if (movieDetails.budget != 0)
            list.add(
                InfoClass(
                    R.drawable.ic_budget,
                    movieDetails.budget.toString(),
                    "Budget"
                )
            )

        return list
    }

    private fun getLanguageName(id: String): String {
        for (i in languages) {
            if (i.isoId == id)
                return i.name
        }
        return id
    }

    private fun createLinkInfo(externalID: ExternalID): List<InfoClass> {
        val list = mutableListOf<InfoClass>()

        externalID.apply {
            list.add(InfoClass(R.drawable.ic_imdb, imdb_id, "IMDB ID"))

            list.add(InfoClass(R.drawable.ic_instagram, instagram_id, "Instagram ID"))

            list.add(InfoClass(R.drawable.ic_facebook, facebook_id, "Facebook ID"))

            list.add(InfoClass(R.drawable.ic_twitter, twitter_id, "Twitter ID"))
        }

        return list
    }

    private fun displayInfoDialog(fragment: InfoDialogFragment) {
        fragment.show(supportFragmentManager, "INFO")
    }

    private fun setExternalIds(externalID: ExternalID) {
        linkDialog =
            InfoDialogFragment.newInfoInstance(createLinkInfo(externalID), "External Links")
    }

    override fun onReviewClicked(review: Review) {
        val intent = Intent(this, MediaReviewActivity::class.java)
        intent.putExtra("review", review)
        startActivity(intent)
    }

    override fun onUrlSelected(url: String, type: Int) {
        val newUrl = when (type) {
            1 -> "https://www.imdb.com/title/$url"
            2 -> "https://www.instagram.com/$url"
            3 -> "https://www.facebook.com/$url"
            4 -> "https://twitter.com/$url"
            else -> url
        }
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(newUrl))
        startActivity(browserIntent)
    }

    private fun setImages(images: Images?) {
        backdropImage.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        if (images?.backdrops != null && images.backdrops.isNotEmpty()) {
            backdropImage.adapter =
                ImageAdapter(
                    true,
                    images.backdrops,
                    WeakReference(this)
                )
        } else if (currentMovie != null && currentMovie!!.backdrop_path != null)
            backdropImage.adapter =
                ImageAdapter(
                    true,
                    listOf(ImageDetails(0.0, currentMovie!!.backdrop_path!!)),
                    WeakReference(this)
                )
        else backdropImage.adapter = ImageAdapter(true, null, WeakReference(this))
        TabLayoutMediator(tabLayout, backdropImage) { tab, _ ->
            backdropImage.setCurrentItem(tab.position, true)
        }.attach()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.handler.removeCallbacksAndMessages(null)
        viewModel.handler.looper.quit()
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

        viewModel.movieDetails?.let {
            setMovieDetails(it)
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

        viewModel.externalId?.let {
            setExternalIds(it)
        }

        viewModel.recommendations?.let {
            setRecommendations(it)
        }

        viewModel.similar?.let {
            setSimilarMovies(it)
        }

        viewModel.images?.let {
            setImages(it)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        (findViewById<RecyclerView>(R.id.movie_detail_recommendations).layoutManager)?.apply {
            viewModel.recommendedPosition =
                (this as GridLayoutManager).findFirstCompletelyVisibleItemPosition()
        }

        (findViewById<RecyclerView>(R.id.movie_detail_similarMovies).layoutManager)?.apply {
            viewModel.similarPosition =
                (this as GridLayoutManager).findFirstCompletelyVisibleItemPosition()
        }

        (findViewById<RecyclerView>(R.id.movie_detail_cast).layoutManager)?.apply {
            viewModel.castPosition =
                (this as GridLayoutManager).findFirstCompletelyVisibleItemPosition()
        }

        (findViewById<RecyclerView>(R.id.movie_detail_crew).layoutManager)?.apply {
            viewModel.crewPosition =
                (this as GridLayoutManager).findFirstCompletelyVisibleItemPosition()
        }

        (findViewById<RecyclerView>(R.id.movie_detail_reviews).layoutManager)?.apply {
            viewModel.reviewPosition =
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
}
