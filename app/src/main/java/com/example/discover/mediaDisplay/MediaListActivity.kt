package com.example.discover.mediaDisplay

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.discover.R
import com.example.discover.category.CategoryViewModel
import com.example.discover.category.MediaCategoryActivity
import com.example.discover.datamodel.movie.preview.MoviePreview
import com.example.discover.datamodel.tvshow.preview.ShowPreview
import com.example.discover.genreScreen.GenreMediaResultFragment
import com.example.discover.searchScreen.OnNetworkLostListener
import com.example.discover.util.NoInternetFragment
import com.google.android.material.appbar.MaterialToolbar

class MediaListActivity : AppCompatActivity(), OnAdapterCreatedListener, OnNetworkLostListener {

    var isMovie = true
    lateinit var section: String

    private var page = 1

    private var resultFragment: GenreMediaResultFragment? = null
    private lateinit var viewModel: CategoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_list)

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(CategoryViewModel::class.java)

        intentData(savedInstanceState)

        val toolbar = findViewById<MaterialToolbar>(R.id.media_list_toolbar)
        if (isMovie)
            toolbar.title = "$section Movies"
        else
            toolbar.title = "$section Shows"

        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            finish()
            overridePendingTransition(R.anim.left_in,R.anim.right_out)
        }
    }

    private fun intentData(savedInstanceState: Bundle?) {
        isMovie = intent.getBooleanExtra("isMovie", true)
        section = intent.getStringExtra("section")!!

        if (savedInstanceState == null) {
            viewModel.moviesList.addAll(intent.getSerializableExtra("list") as List<MoviePreview>)
            viewModel.showsList.addAll(intent.getSerializableExtra("list") as List<ShowPreview>)
        }

        resultFragment = GenreMediaResultFragment.newInstance(viewModel.isLinear, typeConvert())
        supportFragmentManager.beginTransaction()
            .replace(R.id.media_list_result, resultFragment!!, "result_media").commitNow()

        supportFragmentManager.executePendingTransactions()
    }

    private fun resultRequest(page: Int) {
        if (isMovie) {
            when (section) {
                MediaCategoryActivity.MOVIE_CATEGORY[0] -> viewModel.trendingMoviesData(page).observe(
                    this@MediaListActivity,
                    Observer {
                        viewModel.moviesList.addAll(it)
                        resultFragment?.appendMoviesResult(viewModel.isLinear, it)
                    })
                MediaCategoryActivity.MOVIE_CATEGORY[1] -> viewModel.nowPlayingMoviesData(
                    page
                ).observe(this@MediaListActivity, Observer {
                    viewModel.moviesList.addAll(it)
                    resultFragment?.appendMoviesResult(viewModel.isLinear, it)
                })
                MediaCategoryActivity.MOVIE_CATEGORY[2] -> viewModel.popularMoviesData(
                    page
                ).observe(this@MediaListActivity, Observer {
                    viewModel.moviesList.addAll(it)
                    resultFragment?.appendMoviesResult(viewModel.isLinear, it)
                })
                MediaCategoryActivity.MOVIE_CATEGORY[3] -> viewModel.upcomingMoviesData(
                    page
                ).observe(this@MediaListActivity, Observer {
                    viewModel.moviesList.addAll(it)
                    resultFragment?.appendMoviesResult(viewModel.isLinear, it)
                })
                MediaCategoryActivity.MOVIE_CATEGORY[4] -> viewModel.topRatedMoviesData(
                    page
                ).observe(this@MediaListActivity, Observer {
                    viewModel.moviesList.addAll(it)
                    resultFragment?.appendMoviesResult(viewModel.isLinear, it)
                })
            }
        } else {
            when (section) {
                MediaCategoryActivity.TV_CATEGORY[0] -> viewModel.setTrendingShowsData(
                    page
                ).observe(this@MediaListActivity, Observer {
                    viewModel.showsList.addAll(it)
                    resultFragment?.appendShowsResult(viewModel.isLinear, it)
                })
                MediaCategoryActivity.TV_CATEGORY[1] -> viewModel.setOnAirShowsData(
                    page
                ).observe(this@MediaListActivity, Observer {
                    viewModel.showsList.addAll(it)
                    resultFragment?.appendShowsResult(viewModel.isLinear, it)
                })
                MediaCategoryActivity.TV_CATEGORY[2] -> viewModel.setPopularShowsData(
                    page
                ).observe(this@MediaListActivity, Observer {
                    viewModel.showsList.addAll(it)
                    resultFragment?.appendShowsResult(viewModel.isLinear, it)
                })
                MediaCategoryActivity.TV_CATEGORY[3] -> viewModel.setAiringTodayShowsData(
                    page
                ).observe(this@MediaListActivity, Observer {
                    viewModel.showsList.addAll(it)
                    resultFragment?.appendShowsResult(viewModel.isLinear, it)
                })
                MediaCategoryActivity.TV_CATEGORY[4] -> viewModel.setTopRatedShowsData(
                    page
                ).observe(this@MediaListActivity, Observer {
                    viewModel.showsList.addAll(it)
                    resultFragment?.appendShowsResult(viewModel.isLinear, it)
                })
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.genre_media_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.genre_menu_view) {
            item.icon = if (viewModel.isLinear) ContextCompat.getDrawable(
                this,
                R.drawable.ic_linear_list
            ) else ContextCompat.getDrawable(this, R.drawable.ic_grid)
            resultFragment?.changeView(isMovie, viewModel.isLinear)
            viewModel.isLinear = !viewModel.isLinear
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun fetchMore() {
        page++
        resultRequest(page)
        resultFragment?.loading = true
    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)
        if (fragment is GenreMediaResultFragment) {
            resultFragment = fragment
        }
    }

    override fun onAdapterCreated() {
        if (isMovie) {
            resultFragment?.setMovieResults(
                viewModel.isLinear, viewModel.moviesList
            )
        } else {
            resultFragment?.setShowsResults(
                viewModel.isLinear, viewModel.showsList
            )
        }
        resultFragment?.restorePosition(viewModel.position)
    }

    private fun typeConvert(): String {
        return when (isMovie) {
            true -> "movie"
            false -> "show"
        }
    }

    private fun displayNoConnectionFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.media_list_network_container, NoInternetFragment(), "media_list_network")
            .commit()
    }

    override fun onNetworkLostFragment() {
        displayNoConnectionFragment()
    }

    override fun onNetworkDialog() {
        displayNoConnectionFragment()
    }

    override fun onNetworkDialogDismiss() {
        supportFragmentManager.findFragmentByTag("media_list_network")?.let {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    fun restorePosition(position: Int) {
        viewModel.position = position
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.left_in,R.anim.right_out)
    }

}