package com.example.discover.category

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.discover.R
import com.example.discover.searchScreen.OnNetworkLostListener
import com.example.discover.util.LoadingFragment
import com.example.discover.util.NoInternetFragment
import com.google.android.material.appbar.MaterialToolbar

class MediaCategoryActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener,
    OnNetworkLostListener, OnCategoryAdapterCreatedListener {

    private var isMovie: Boolean = true

    companion object {
        val MOVIE_CATEGORY = listOf(
            "Trending Today",
            "Now Playing in Theaters",
            "Popular",
            "Coming Soon",
            "Top-rated"
        )
        val TV_CATEGORY = listOf(
            "Trending Today",
            "On Air",
            "Popular",
            "Airing Today",
            "Top-rated"
        )
    }

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var viewModel: CategoryViewModel
    private lateinit var title: List<TextView>
    private lateinit var containers: List<FrameLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_category)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition()
        }
        getIntentData()

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(CategoryViewModel::class.java)

        val toolbar: MaterialToolbar = findViewById(R.id.category_toolbar)
        toolbar.title = if (isMovie) "Discover Movies" else "Discover TV Shows"

        toolbar.setNavigationOnClickListener {
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        swipeRefreshLayout = findViewById(R.id.media_category_swipe_refresh)
        swipeRefreshLayout.setOnRefreshListener(this)
        swipeRefreshLayout.setColorScheme(R.color.colorPrimary, R.color.colorAccent)


        title = listOf(
            findViewById(R.id.category_title_1),
            findViewById(R.id.category_title_2),
            findViewById(R.id.category_title_3),
            findViewById(R.id.category_title_4),
            findViewById(R.id.category_title_5)
        )

        containers = listOf(
            findViewById(R.id.category_container_1),
            findViewById(R.id.category_container_2),
            findViewById(R.id.category_container_3),
            findViewById(R.id.category_container_4),
            findViewById(R.id.category_container_5)
        )

        if (savedInstanceState == null)
            loadData()
        else {
            loadViewModelData()
        }

    }

    private fun getIntentData() {
        isMovie = intent?.getBooleanExtra("isMovie", true)!!
    }

    private fun displayLoadingFragment(containerId: Int) {
        supportFragmentManager.beginTransaction()
            .replace(containerId, LoadingFragment(), "$containerId LOAD").commit()
    }

    private fun removeLoadingFragment(containerId: Int) {
        supportFragmentManager.findFragmentByTag("$containerId LOAD")?.let {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    private fun displayResultFragment(
        containerId: Int,
        category: String
    ) {
        val displayFragment = CategoryListResultFragment.newInstance(isMovie, category)

        supportFragmentManager.beginTransaction()
            .replace(containerId, displayFragment, "$containerId RESULT")
            .commit()

        supportFragmentManager.executePendingTransactions()
    }

    private fun setMovieResults(containerId: Int, category: String) {
        removeLoadingFragment(containerId)
        displayResultFragment(containerId, category)
    }

    private fun setShowsResults(containerId: Int, category: String) {
        removeLoadingFragment(containerId)
        displayResultFragment(containerId, category)
    }

    override fun onRefresh() {
        Handler().postDelayed({
            loadData()
            swipeRefreshLayout.isRefreshing = false
        }, 1000)
    }

    private fun loadData() {
        for (i in 0..4) {
            displayLoadingFragment(containers[i].id)
            title[i].text = if (isMovie) MOVIE_CATEGORY[i] else TV_CATEGORY[i]
            if (isMovie) {
                viewModel.apply {
                    when (i) {
                        0 -> trendingMoviesData().observe(this@MediaCategoryActivity, Observer {
                            if (it.isNotEmpty()) {
                                viewModel.movies[0] = it
                                setMovieResults(containers[i].id, MOVIE_CATEGORY[0])
                            }
                        })
                        1 -> nowPlayingMoviesData().observe(this@MediaCategoryActivity, Observer {
                            if (it.isNotEmpty()) {
                                viewModel.movies[1] = it
                                setMovieResults(containers[i].id, MOVIE_CATEGORY[1])
                            }
                        })
                        2 -> popularMoviesData().observe(this@MediaCategoryActivity, Observer {
                            if (it.isNotEmpty()) {
                                viewModel.movies[2] = it
                                setMovieResults(containers[i].id, MOVIE_CATEGORY[2])
                            }
                        })
                        3 -> upcomingMoviesData().observe(this@MediaCategoryActivity, Observer {
                            if (it.isNotEmpty()) {
                                viewModel.movies[3] = it
                                setMovieResults(containers[i].id, MOVIE_CATEGORY[3])
                            }
                        })
                        4 -> topRatedMoviesData().observe(this@MediaCategoryActivity, Observer {
                            if (it.isNotEmpty()) {
                                viewModel.movies[4] = it
                                setMovieResults(containers[i].id, MOVIE_CATEGORY[4])
                            }
                        })
                    }
                }
            } else {
                viewModel.apply {
                    when (i) {
                        0 -> setTrendingShowsData().observe(this@MediaCategoryActivity, Observer {
                            if (it.isNotEmpty()) {
                                viewModel.shows[0] = it
                                setShowsResults(containers[i].id, TV_CATEGORY[0])
                            }
                        })
                        1 -> setOnAirShowsData().observe(this@MediaCategoryActivity, Observer {
                            if (it.isNotEmpty()) {
                                viewModel.shows[1] = it
                                setShowsResults(containers[i].id, TV_CATEGORY[1])
                            }
                        })
                        2 -> setPopularShowsData().observe(this@MediaCategoryActivity, Observer {
                            if (it.isNotEmpty()) {
                                viewModel.shows[2] = it
                                setShowsResults(containers[i].id, TV_CATEGORY[2])
                            }
                        })
                        3 -> setAiringTodayShowsData().observe(
                            this@MediaCategoryActivity,
                            Observer {
                                if (it.isNotEmpty()) {
                                    viewModel.shows[3] = it
                                    setShowsResults(containers[i].id, TV_CATEGORY[3])
                                }
                            })
                        4 -> setTopRatedShowsData().observe(this@MediaCategoryActivity, Observer {
                            if (it.isNotEmpty()) {
                                viewModel.shows[4] = it
                                setShowsResults(containers[i].id, TV_CATEGORY[4])
                            }
                        })
                    }
                }
            }
        }
    }

    private fun loadViewModelData() {
        for (i in 0..4) {
            displayLoadingFragment(containers[i].id)
            title[i].text = if (isMovie) MOVIE_CATEGORY[i] else TV_CATEGORY[i]
            if (isMovie)
                setMovieResults(containers[i].id, MOVIE_CATEGORY[i])
            else
                setShowsResults(containers[i].id, TV_CATEGORY[i])
        }
    }

    override fun onNetworkLostFragment() {

    }

    override fun onNetworkDialog() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.media_category_network_container, NoInternetFragment(), "media_network")
            .commit()
    }

    override fun onNetworkDialogDismiss() {
        supportFragmentManager.findFragmentByTag("media_network")?.let {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    override fun onCategoryAdapterCreated(category: String, fragment: CategoryListResultFragment) {
        if (isMovie)
            when (category) {
                MOVIE_CATEGORY[0] -> viewModel.movies[0]?.let {
                    fragment.setMovies(it, viewModel.positions.get(containers[0].id, -1))
                }
                MOVIE_CATEGORY[1] -> viewModel.movies[1]?.let {
                    fragment.setMovies(
                        it,
                        viewModel.positions.get(containers[1].id, -1)
                    )
                }
                MOVIE_CATEGORY[2] -> viewModel.movies[2]?.let {
                    fragment.setMovies(
                        it,
                        viewModel.positions.get(containers[2].id, -1)
                    )
                }
                MOVIE_CATEGORY[3] -> viewModel.movies[3]?.let {
                    fragment.setMovies(
                        it,
                        viewModel.positions.get(containers[3].id, -1)
                    )
                }
                MOVIE_CATEGORY[4] -> viewModel.movies[4]?.let {
                    fragment.setMovies(
                        it,
                        viewModel.positions.get(containers[4].id, -1)
                    )
                }

            }
        else
            when (category) {
                TV_CATEGORY[0] -> viewModel.shows[0]?.let {
                    fragment.setShows(it, viewModel.positions.get(containers[0].id, -1))
                }
                TV_CATEGORY[1] -> viewModel.shows[1]?.let {
                    fragment.setShows(
                        it, viewModel.positions.get(containers[1].id, -1)
                    )
                }
                TV_CATEGORY[2] -> viewModel.shows[2]?.let {
                    fragment.setShows(
                        it,
                        viewModel.positions.get(containers[2].id, -1)
                    )
                }
                TV_CATEGORY[3] -> viewModel.shows[3]?.let {
                    fragment.setShows(
                        it,
                        viewModel.positions.get(containers[3].id, -1)
                    )
                }
                TV_CATEGORY[4] -> viewModel.shows[4]?.let {
                    fragment.setShows(
                        it,
                        viewModel.positions.get(containers[4].id, -1)
                    )
                }
            }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        for (i in containers) {
            supportFragmentManager.findFragmentByTag("${i.id} RESULT")?.let {
                if (it is CategoryListResultFragment)
                    viewModel.positions.put(
                        i.id,
                        it.lastElementPosition()
                    )
            }
//            supportFragmentManager.findFragmentById(i.id)?.let {
//                if (it is CategoryListResultFragment)
//                    viewModel.positions.put(
//                        i.id,
//                        it.lastElementPosition()
//                    )
//            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}
