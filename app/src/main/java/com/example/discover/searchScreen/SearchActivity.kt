package com.example.discover.searchScreen

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.discover.DiscoverApplication
import com.example.discover.R
import com.example.discover.datamodel.movie.preview.MoviePreview
import com.example.discover.datamodel.multiSearch.MultiSearch
import com.example.discover.datamodel.tvshow.preview.ShowPreview
import com.example.discover.genreScreen.GenreMediaResultFragment
import com.example.discover.mediaDisplay.OnAdapterCreatedListener
import com.example.discover.util.LoadingFragment
import com.example.discover.util.NoInternetFragment
import com.example.discover.util.NoMatchFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SearchActivity : AppCompatActivity(), DrawerLayout.DrawerListener, OnAdapterCreatedListener,
    SwipeRefreshLayout.OnRefreshListener, OnNetworkLostListener {

    private lateinit var searchView: SearchView
    private lateinit var viewModel: SearchViewModel
    private lateinit var resultStatus: TextView
    private var isMovie: Boolean? = null
    private var isLinear = true
    private var displayFragment: GenreMediaResultFragment? = null
    private lateinit var textView: TextView
    private lateinit var filterIcon: ImageView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var drawerLayout: DrawerLayout
    private var mQuery = ""

    //    private var matrixCursor: MatrixCursor? = null
    private lateinit var resultView: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        viewModelInitialisation()

        drawerLayout = findViewById(R.id.search_drawerLayout)

        swipeRefreshLayout = findViewById(R.id.search_swipe_refresh)
        swipeRefreshLayout.setOnRefreshListener(this)
        swipeRefreshLayout.setColorScheme(R.color.colorPrimary, R.color.colorAccent)

        filterIcon = findViewById(R.id.search_filter)
        resultStatus = findViewById(R.id.search_resultText)

        resultView = findViewById(R.id.search_view_change)

        resultView.setOnClickListener {
            viewDrawable(isLinear)
            displayFragment?.changeView(isMovie, isLinear)
            isLinear = !isLinear
        }

        textView = findViewById(R.id.search_resultText)
        val navigationIcon: ImageButton = findViewById(R.id.search_back)

        navigationIcon.setOnClickListener {
            searchView.clearFocus()
            supportFinishAfterTransition()
        }
        initializeSearchView()
    }

    private fun viewModelInitialisation() {
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(SearchViewModel::class.java)
        viewModel.onNetworkLostListener = this
    }

    private fun initializeSearchView() {
        searchView = findViewById(R.id.search_searchView)

//        val adapter = SimpleCursorAdapter(
//            this,
//            android.R.layout.simple_list_item_1,
//            matrixCursor,
//            arrayOf("Query"),
//            IntArray(1) { android.R.id.text1 },
//            FLAG_REGISTER_CONTENT_OBSERVER
//        )

//        searchView.suggestionsAdapter = adapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    if (it.isNotBlank()) {
                        viewModel.insertQuery(it)
                        resultStatus.text = null
                        clearViewModelLists()
                        resultStatus.requestFocus()
                        viewModel.focus = false
                        initiateSearch(it.trim())
                    }
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    mQuery = it
                }
//                viewModel.getSuggestions(mQuery)
//                    .observe(this@SearchActivity, Observer { suggestions ->
//                        populateAdapter(suggestions, adapter, searchView)
//                    })
                return false
            }
        })

        sideSheetInitialisation(searchView)

    }

//    private fun populateAdapter(
//        suggestions: List<String>,
//        adapter: SimpleCursorAdapter,
//        searchView: SearchView
//    ) {
//        matrixCursor?.close()
//        matrixCursor = MatrixCursor(arrayOf(BaseColumns._ID, "query"))
//        startManagingCursor(matrixCursor)
//        for (i in suggestions.indices) {
//            matrixCursor?.addRow(arrayOf(i, suggestions[i]))
//        }
//        adapter.changeCursor(matrixCursor)
//
//        searchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
//            override fun onSuggestionSelect(position: Int): Boolean {
//                return false
//            }
//
//            override fun onSuggestionClick(position: Int): Boolean {
//                searchView.clearFocus()
//                initiateSearch(suggestions[position])
//                searchView.setQuery(suggestions[position], false)
//                return true
//            }
//
//        })
//    }

    fun onFilterClick(view: View) {
        if (view is RadioButton) {
            val checked = view.isChecked
            if (checked) {
                when (view.id) {
                    R.id.search_movieFilter -> {
                        isMovie = true
                    }
                    R.id.search_showsFilter -> {
                        isMovie = false
                    }
                    R.id.search_multiFilter -> {
                        isMovie = null
                    }
                }
            }
        }
    }

    private fun sideSheetInitialisation(searchView: SearchView) {
        searchView.clearFocus()
        val filterButton: MaterialButton = findViewById(R.id.search_filterButton)

        filterButton.setOnClickListener {
            viewModel.isMovie = isMovie
            if (mQuery != "") {
                initiateSearch(mQuery)
            }
            drawerLayout.closeDrawer(GravityCompat.END)
            resultStatus.text = null
            clearViewModelLists()
        }

        filterIcon.setOnClickListener {
            searchView.clearFocus()
            drawerLayout.openDrawer(GravityCompat.END)
        }

        drawerLayout.setDrawerListener(this)
    }

    private fun displayResultFragment() {
        displayFragment = GenreMediaResultFragment.newInstance(isLinear, typeConvert())
        supportFragmentManager.beginTransaction()
            .replace(R.id.search_container, displayFragment!!, "search_result").commit()

        supportFragmentManager.executePendingTransactions()
    }

    private fun displayLoadingFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.search_container, LoadingFragment(), "search_loading").commit()
    }

    private fun removeLoadingFragment() {
        supportFragmentManager.findFragmentByTag("search_loading")?.let {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    private fun displayNoResultFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.search_container, NoMatchFragment(), "search_no_match")
            .commit()
    }

    private fun removeNoResultFragment() {
        supportFragmentManager.findFragmentByTag("search_no_match")?.let {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    private fun removeResultFragment() {
        supportFragmentManager.findFragmentByTag("search_result")?.let {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }
        displayFragment = null
    }

    private fun firstPageOfMovies(results: List<MoviePreview>) {
        viewModel.moviesList = results
        removeNoConnectionFragment()
        removeNoResultFragment()
        removeLoadingFragment()
        removeResultFragment()
        displayResultFragment()
    }

    private fun firstPageOfShows(results: List<ShowPreview>) {
        viewModel.showsList = results
        removeNoConnectionFragment()
        removeNoResultFragment()
        removeLoadingFragment()
        removeResultFragment()
        displayResultFragment()
    }

    private fun firstPageOfMultiSearch(results: List<MultiSearch>) {
        viewModel.multiList = results
        removeNoConnectionFragment()
        removeNoResultFragment()
        removeLoadingFragment()
        removeResultFragment()
        displayResultFragment()
    }

    private fun displayRestMovies(results: List<MoviePreview>) {
        viewModel.moviesList = listOf(viewModel.moviesList, results).flatten()
        displayFragment?.appendMoviesResult(isLinear, results)
    }

    private fun displayRestShows(results: List<ShowPreview>) {
        viewModel.showsList = listOf(viewModel.showsList, results).flatten()
        displayFragment?.appendShowsResult(isLinear, results)
    }

    private fun displayRestMultiSearch(results: List<MultiSearch>) {
        viewModel.multiList = listOf(viewModel.multiList, results).flatten()
        displayFragment?.appendMediaResult(isLinear, results)
    }

    private fun initiateSearch(query: String) {
        displayLoadingFragment()
        viewModel.queryMap["query"] = query
        viewModel.queryMap["page"] = "1"
        typeCall()
    }

    override fun onDrawerStateChanged(newState: Int) {
    }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
    }

    override fun onDrawerClosed(drawerView: View) {
    }

    override fun onDrawerOpened(drawerView: View) {
        when (viewModel.isMovie) {
            null -> drawerView.findViewById<RadioButton>(R.id.search_multiFilter)
                .isChecked =
                true
            true -> drawerView.findViewById<RadioButton>(R.id.search_movieFilter).isChecked =
                true
            false -> drawerView.findViewById<RadioButton>(R.id.search_showsFilter).isChecked =
                true
        }
    }

    fun fetchMore() {
        viewModel.queryMap["page"] = (viewModel.queryMap["page"]!!.toInt() + 1).toString()
        typeCall()
        displayFragment?.loading = true
    }

    private fun typeCall() {
        when (viewModel.isMovie) {
            null -> viewModel.getMultiSearchResult().observe(this, Observer {
                Log.d("result", "$it")
                if (filterMedia(it).isEmpty()) {
                    if (viewModel.queryMap["page"] == "1")
                        displayNoResultFragment()
                } else {
                    if (viewModel.queryMap["page"] == "1") {
                        resultStatus.text =
                            "Movies and Shows for \"${viewModel.queryMap["query"]}\""
                        firstPageOfMultiSearch(filterMedia(it))
                    } else
                        displayRestMultiSearch(filterMedia(it))
                }
            })
            true -> viewModel.getSearchedMovies().observe(this, Observer {
                if (it.isEmpty()) {
                    if (viewModel.queryMap["page"] == "1")
                        displayNoResultFragment()
                } else {
                    if (viewModel.queryMap["page"] == "1") {
                        resultStatus.text = "Movies for \"${viewModel.queryMap["query"]}\""
                        firstPageOfMovies(it)
                    } else
                        displayRestMovies(it)
                }
            })
            false -> viewModel.getSearchedShows().observe(this, Observer {
                if (it.isEmpty()) {
                    if (viewModel.queryMap["page"] == "1")
                        displayNoResultFragment()
                } else {
                    if (viewModel.queryMap["page"] == "1") {
                        resultStatus.text = "Shows for \"${viewModel.queryMap["query"]}\""
                        firstPageOfShows(it)
                    } else
                        displayRestShows(it)
                }
            })
        }
    }

    private fun displayNoInternetConnectionFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.search_network_container, NoInternetFragment(), "search_no_connection")
            .commit()
    }

    private fun removeNoConnectionFragment() {
        supportFragmentManager.findFragmentByTag("search_no_connection")?.let {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    private fun getKeyboard() {
        searchView.requestFocus()
        searchView.requestFocusFromTouch()
    }

    private fun viewDrawable(isLinear: Boolean) {
        if (!isLinear) resultView.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_grid
            )
        ) else {
            resultView.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_linear_list
                )
            )
        }
    }

    override fun onAdapterCreated() {
        if (searchView.query.isEmpty())
            searchView.setQuery(viewModel.queryMap["query"]!!, false)

        searchView.clearFocus()

        if (isMovie != null) {
            if (isMovie!!)
                displayFragment?.setMovieResults(isLinear, viewModel.moviesList)
            else
                displayFragment?.setShowsResults(isLinear, viewModel.showsList)
        } else
            displayFragment?.setMultiResults(isLinear, viewModel.multiList)

        displayFragment?.restorePosition(viewModel.position)
    }

    private fun typeConvert(): String {
        return when (isMovie) {
            true -> "movie"
            false -> "show"
            else -> "media"
        }
    }

    override fun onNetworkLostFragment() {
        displayNoInternetConnectionFragment()
        removeLoadingFragment()
    }

    override fun onNetworkDialog() {
        displayNoInternetConnectionFragment()
    }

    override fun onNetworkDialogDismiss() {
        removeNoConnectionFragment()
    }

    private fun clearViewModelLists() {
        viewModel.moviesList = listOf()
        viewModel.showsList = listOf()
        viewModel.multiList = listOf()
        viewModel.position = 0
    }

    fun restorePosition(position: Int) {
        viewModel.position = position
    }

    private fun filterMedia(list: List<MultiSearch>): List<MultiSearch> {
        val mediaList = mutableListOf<MultiSearch>()
        for (i in list)
            if (i.media_type == "movie" || i.media_type == "tv")
                mediaList.add(i)
        return mediaList
    }

    override fun onRefresh() {
        Handler().postDelayed({
            if ((application as DiscoverApplication).checkConnectivity()) {
                viewModel.queryMap["page"] = "1"
                displayLoadingFragment()
                typeCall()

            } else
                displayNoInternetConnectionFragment()

            swipeRefreshLayout.isRefreshing = false
        }, 1000)
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.focus && !drawerLayout.isDrawerOpen(GravityCompat.END))
            getKeyboard()
        else
            resultStatus.requestFocus()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.isLinear = isLinear
    }

    override fun onPause() {
        super.onPause()
        viewModel.fragmentExists =
            supportFragmentManager.findFragmentByTag("search_no_connection") != null
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        isMovie = viewModel.isMovie
        isLinear = viewModel.isLinear
        viewDrawable(!isLinear)
        viewModel.queryMap["query"]?.let {
            mQuery = it
            searchView.setQuery(it, true)
            var isResultEmpty = false
            when (viewModel.isMovie) {
                null -> {
                    firstPageOfMultiSearch(viewModel.multiList)
                    if (viewModel.multiList.isEmpty())
                        isResultEmpty = true
                }
                true -> {
                    firstPageOfMovies(viewModel.moviesList)
                    if (viewModel.moviesList.isEmpty())
                        isResultEmpty = true
                }
                false -> {
                    firstPageOfShows(viewModel.showsList)
                    if (viewModel.showsList.isEmpty())
                        isResultEmpty = true
                }
            }

//            if (!(application as DiscoverApplication).checkConnectivity() && isResultEmpty) {
//                displayNoInternetConnectionFragment()
//                displayLoadingFragment()
//            }

            if (viewModel.fragmentExists) {
                displayNoInternetConnectionFragment()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        searchView.clearFocus()
    }

    override fun onBackPressed() {
        searchView.clearFocus()
        if (drawerLayout.isDrawerOpen(GravityCompat.END))
            drawerLayout.closeDrawer(GravityCompat.END)
        else
            supportFinishAfterTransition()
    }
}
