package com.example.discover.genreScreen

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.discover.DiscoverApplication
import com.example.discover.R
import com.example.discover.datamodel.genre.Genres
import com.example.discover.datamodel.movie.preview.MoviePreview
import com.example.discover.datamodel.tvshow.preview.ShowPreview
import com.example.discover.mediaDisplay.OnAdapterCreatedListener
import com.example.discover.searchScreen.OnNetworkLostListener
import com.example.discover.util.LoadingFragment
import com.google.android.material.appbar.MaterialToolbar

class GenreMediaActivity : AppCompatActivity(), OnAdapterCreatedListener,
    SwipeRefreshLayout.OnRefreshListener, OnNetworkLostListener {

    var isMovie: Boolean = true
    private var genreId: Int = 0
    var page = 1

    var isLinear = true
    lateinit var viewModel: GenreMediaViewModel
    var resultFragment: GenreMediaResultFragment? = null

    lateinit var movieGenre: List<Genres>
    lateinit var showGenre: List<Genres>

    private lateinit var resultText: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_genre_media)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition()
        }

        displayLoadingFragment()

        fetchIntent()

        val toolbar: MaterialToolbar = findViewById(R.id.genre_media_toolbar)
        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            finish()
            overridePendingTransition(R.anim.left_in, R.anim.right_out)
        }

        swipeRefreshLayout = findViewById(R.id.genre_media_swipe_refresh)
        swipeRefreshLayout.setOnRefreshListener(this)
        swipeRefreshLayout.setColorScheme(R.color.colorPrimary, R.color.colorAccent)

        resultText = findViewById(R.id.genre_media_status)

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(GenreMediaViewModel::class.java)

        (application as DiscoverApplication).apply {
            movieGenres.observe(this@GenreMediaActivity, Observer {
                movieGenre = it
                showsGenres.observe(this@GenreMediaActivity, Observer {
                    showGenre = it
                    resultRequest(1)
                })
            })
        }


    }

    private fun resultRequest(page: Int) {
        if (isMovie) {
            viewModel.genreRelatedMovies(genreId, page).observe(this, Observer {
                if (page == 1) {
                    val text = "Results for \"${getGenreName()}\" movies"
                    resultText.text = text
                    firstPageOfMovies(it)
                } else {
                    displayRestMovies(it)
                }
            })
        } else {
            viewModel.genreRelatedShows(genreId, page).observe(this, Observer {
                if (page == 1) {
                    val text = "Results for \"${getGenreName()}\"shows"
                    resultText.text = text
                    firstPageOfShows(it)
                } else {
                    displayRestShows(it)
                }
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.genre_media_menu, menu)
        return true
    }

    private fun fetchIntent() {
        intent?.apply {
            isMovie = getBooleanExtra("isMovie", true)
            genreId = getIntExtra("id", 0)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.genre_menu_view) {
            item.icon = if (isLinear) ContextCompat.getDrawable(
                this,
                R.drawable.ic_linear_list
            ) else ContextCompat.getDrawable(this, R.drawable.ic_grid)
            resultFragment?.changeView(isMovie, isLinear)
            isLinear = !isLinear
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun displayLoadingFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.genre_media_container, LoadingFragment(), "LOADING").commit()
    }

    private fun removeLoadingFragment() {
        supportFragmentManager.findFragmentByTag("LOADING")?.let {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    private fun removeResultFragment() {
        supportFragmentManager.findFragmentByTag("RESULT")?.let {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }
        resultFragment = null
    }

    private fun displayResultFragment() {
        resultFragment = GenreMediaResultFragment.newInstance(isLinear, typeConvert())
        supportFragmentManager.beginTransaction()
            .replace(R.id.genre_media_container, resultFragment!!, "RESULT").commit()

        supportFragmentManager.executePendingTransactions()
    }

    private fun firstPageOfMovies(results: List<MoviePreview>) {
        viewModel.movies = results
        removeLoadingFragment()
        removeResultFragment()
        displayResultFragment()
    }

    private fun firstPageOfShows(results: List<ShowPreview>) {
        viewModel.shows = results
        removeLoadingFragment()
        removeResultFragment()
        displayResultFragment()
    }

    private fun displayRestMovies(results: List<MoviePreview>) {
        resultFragment?.appendMoviesResult(isLinear, results)
    }

    private fun displayRestShows(results: List<ShowPreview>) {
        resultFragment?.appendShowsResult(isLinear, results)
    }

    fun fetchMore() {
        page++
        resultRequest(page)
        resultFragment?.loading = true
    }

    private fun getGenreName(): String {
        if (isMovie) {
            for (i in movieGenre)
                if (i.id == genreId)
                    return i.name
        }
        for (i in showGenre)
            if (genreId == i.id)
                return i.name

        return ""
    }

    private fun typeConvert(): String {
        return when (isMovie) {
            true -> "movie"
            false -> "show"
        }
    }

    override fun onAdapterCreated() {
        if (isMovie)
            viewModel.movies?.let {
                resultFragment?.setMovieResults(isLinear, it)
            }
        else
            viewModel.shows?.let {
                resultFragment?.setShowsResults(isLinear, it)
            }
        resultFragment?.restorePosition(viewModel.position)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.isLinear = isLinear
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        isLinear = viewModel.isLinear
    }

    override fun onRefresh() {
        Handler().postDelayed({
            displayLoadingFragment()
            resultRequest(1)
            swipeRefreshLayout.isRefreshing = false
        }, 1000)
    }

    override fun onNetworkLostFragment() {

    }

    override fun onNetworkDialog() {
    }

    override fun onNetworkDialogDismiss() {
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.left_in, R.anim.right_out)
    }

    fun restorePosition(position: Int) {
        viewModel.position = position
    }
}
