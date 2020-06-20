package com.example.discover.filterScreen.ui


import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.discover.DiscoverApplication
import com.example.discover.R
import com.example.discover.datamodel.movie.preview.MoviePreview
import com.example.discover.datamodel.tvshow.preview.ShowPreview
import com.example.discover.filterScreen.FilterViewModel
import com.example.discover.filterScreen.OnSortOptionSelectedListener
import com.example.discover.genreScreen.GenreMediaResultFragment
import com.example.discover.mediaDisplay.OnAdapterCreatedListener
import com.example.discover.searchScreen.OnNetworkLostListener
import com.example.discover.util.LoadingFragment
import com.example.discover.util.NoInternetFragment
import com.example.discover.util.NoMatchFragment
import com.google.android.material.button.MaterialButton


/**
 * A simple [Fragment] subclass.
 */
class FilterFragment : Fragment(), OnSortOptionSelectedListener,
    SwipeRefreshLayout.OnRefreshListener, OnAdapterCreatedListener,
    OnNetworkLostListener {

    private lateinit var queryMap: HashMap<String, String>
    private lateinit var viewModel: FilterViewModel
    private var isMovie = true
    private var isLinear = true

    private var resultFragment: GenreMediaResultFragment? = null

    private var movieButton: RadioButton? = null
    private lateinit var voteAverageText: EditText
    private lateinit var runtimeText: EditText
    private lateinit var yearText: EditText
    private lateinit var grid: ImageView

//    private var discover = false

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private var size = MutableLiveData<Int>().apply { postValue(0) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_filter, container, false)

        showFilterFragment()

        swipeRefreshLayout = rootView.findViewById(R.id.filter_swipeRefresh)

        swipeRefreshLayout.setOnRefreshListener(this)

        swipeRefreshLayout.setColorScheme(R.color.colorPrimary, R.color.colorAccent)

        viewModelInitialisation()

        //media selection
        mediaInitialisation(rootView)

        //info button
        infoInitialisation(rootView)

        sortAction(rootView)
        listViewChange(rootView)

        //edit text
        voteAverageText = rootView.findViewById(R.id.filter_voteAverage)
        runtimeText = rootView.findViewById(R.id.filter_runtime)
        yearText = rootView.findViewById(R.id.filter_year)

        //filter button
        val filter: MaterialButton = rootView.findViewById(R.id.filter_button)
        filter.setOnClickListener {
            viewModel.discover = true
            size.postValue(0)
            if (yearText.text.toString().trim().isNotEmpty())
                queryMap["primary_release_year"] = yearText.text.toString().trim()
            if (voteAverageText.text.trim().isNotEmpty())
                queryMap["vote_average.gte"] = voteAverageText.text.toString()
            if (runtimeText.text.trim().isNotEmpty())
                queryMap["with_runtime.lte"] = runtimeText.text.toString()
            Log.d("values", queryMap.toString())
            isMovie = queryMap["media"] == "movie"
            queryMap["page"] = "1"
            displayLoadingFragment()
            fillMedia(isMovie)
            (activity!! as FilterActivity).toggleFilters()
        }

        val sizeText: TextView = rootView.findViewById(R.id.filter_size)

        size.observe(viewLifecycleOwner, Observer {
            val text = "$it item(s)"
            sizeText.text = text
        })

        return rootView
    }

    private fun viewModelInitialisation() {
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(activity!!.application)
        ).get(FilterViewModel::class.java)
        viewModel.onNetworkLostListener = this
    }

    private fun mediaInitialisation(rootView: View) {
        movieButton = rootView.findViewById(R.id.filter_movies)
        val tvButton: RadioButton = rootView.findViewById(R.id.filter_shows)

        movieButton?.setOnClickListener {
            isMovie = true
            queryMap["media"] = "movie"
        }

        tvButton.setOnClickListener {
            isMovie = false
            queryMap["media"] = "tv"
        }
    }

    private fun infoInitialisation(rootView: View) {
        val voteAverageInfo: ImageView =
            rootView.findViewById(R.id.filter_voteAverageInfo)
        val runtimeInfo: ImageView = rootView.findViewById(R.id.filter_runtimeInfo)
        val yearInfo: ImageView = rootView.findViewById(R.id.filter_yearInfo)

        voteAverageInfo.setOnClickListener {
            showInfo("Filter and only include movies that have a vote count that is greater or equal to the entered value. Value must range from 0 to 10.0")
        }

        runtimeInfo.setOnClickListener {
            showInfo("Filter and only include movies that have a runtime that is less than or equal to a value. Value in terms of minutes")
        }

        yearInfo.setOnClickListener {
            showInfo("A filter to limit the results to a specific primary release year.")
        }
    }

    private fun showInfo(text: String) {
        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.info_dialog)

        val textBox = dialog.findViewById(R.id.info_content) as TextView
        textBox.text = text

        val dialogButton = dialog.findViewById(R.id.info_button) as Button
        dialogButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun fillMedia(isMovie: Boolean) {
        if (isMovie)
            viewModel.discoverMovies(queryMap).observe(viewLifecycleOwner, Observer {
                if (it.isNotEmpty()) {
                    if (queryMap["page"] == "1") {
                        size.postValue(it.size)
                        firstPageOfMovies(it)
                    } else {
                        size.postValue(size.value?.plus(it.size))
                        displayRestMovies(it)
                    }
                } else {
                    displayNoResultFragment()
                    size.postValue(0)
                }
            })
        else
            viewModel.discoverTv(queryMap).observe(viewLifecycleOwner, Observer {
                if (it.isNotEmpty()) {
                    if (queryMap["page"] == "1") {
                        size.postValue(it.size)
                        firstPageOfShows(it)
                    } else {
                        size.postValue(size.value?.plus(it.size))
                        displayRestShows(it)
                    }
                } else {
                    displayNoResultFragment()
                    size.postValue(0)
                }
            })
    }


    private fun sortAction(rootView: View) {
        val sortIcon: ImageView = rootView.findViewById(R.id.filter_sortIcon)

        sortIcon.setOnClickListener {
            resultFragment?.let {
                SortFragment.newInstance(
                    isMovie,
                    queryMap["sort_by"]
                ).show(childFragmentManager, "SORT")
            }
        }
    }

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        if (childFragment is SortFragment)
            childFragment.onSortOptionSelectedListener = this
    }

    override fun onSortSelected(option: String) {
        queryMap["sort_by"] = option
        queryMap["page"] = "1"
        size.postValue(0)
        displayLoadingFragment()
        fillMedia(isMovie)
        viewModel.position = 0

    }

    fun resetAction() {
        clearViewModelData()
        queryMap.clear()
        queryMap["media"] = "movie"
        movieButton?.isChecked = true
        voteAverageText.text.clear()
        runtimeText.text.clear()
        yearText.text.clear()
        removeResultFragment()
        removeNoResultFragment()
        showFilterFragment()
        size.postValue(0)
        viewModel.discover = false
    }

    private fun listViewChange(rootView: View) {
        grid = rootView.findViewById(R.id.filter_gridView)
        grid.setOnClickListener {
            resultFragment?.let {
                viewDrawable(isLinear)
                resultFragment?.changeView(isMovie, isLinear)
                isLinear = !isLinear
            }
        }

    }

    private fun viewDrawable(isLinear: Boolean) {
        if (!isLinear) grid.setImageDrawable(
            ContextCompat.getDrawable(
                context!!,
                R.drawable.ic_grid
            )
        ) else {
            grid.setImageDrawable(
                ContextCompat.getDrawable(
                    context!!,
                    R.drawable.ic_linear_list
                )
            )
        }
    }

    override fun onRefresh() {
        Handler().postDelayed({
            viewModel.position = 0
            if ((activity?.application as DiscoverApplication).checkConnectivity()) {
                removeNoConnectionFragment()
                queryMap["page"] = "1"
                if (viewModel.discover) {
                    displayLoadingFragment()
                    fillMedia(isMovie)
                }
            } else {
                if (!viewModel.discover)
                    size.postValue(0)
                displayNoInternetConnectionFragment()
            }
            swipeRefreshLayout.isRefreshing = false
        }, 1000)
    }

    private fun showFilterFragment() {
        childFragmentManager.beginTransaction()
            .replace(R.id.filter_container, StartFilterFragment(), "filter_start")
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit()
    }

    private fun removeFilterFragment() {
        childFragmentManager.findFragmentByTag("filter_start")?.let {
            childFragmentManager.beginTransaction().remove(it)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit()
        }
    }

    private fun displayResultFragment() {
        resultFragment = GenreMediaResultFragment.newInstance(isLinear, typeConvert())
        childFragmentManager.beginTransaction()
            .replace(R.id.filter_container, resultFragment!!, "filter_result")
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commitNow()

        childFragmentManager.executePendingTransactions()
    }

    private fun displayLoadingFragment() {
        childFragmentManager.beginTransaction()
            .replace(R.id.filter_container, LoadingFragment(), "filter_loading")
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit()
    }

    private fun removeLoadingFragment() {
        childFragmentManager.findFragmentByTag("filter_loading")?.let {
            childFragmentManager.beginTransaction().remove(it)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit()
        }
    }

    private fun displayNoResultFragment() {
        childFragmentManager.beginTransaction()
            .replace(R.id.filter_container, NoMatchFragment(), "filter_no_match")
            .commit()
    }

    private fun removeNoResultFragment() {
        childFragmentManager.findFragmentByTag("filter_no_match")?.let {
            childFragmentManager.beginTransaction().remove(it)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit()
        }
    }

    private fun removeResultFragment() {
        resultFragment?.let {
            childFragmentManager.beginTransaction().remove(it)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit()
            childFragmentManager.executePendingTransactions()
        }
        resultFragment = null
    }

    private fun firstPageOfMovies(results: List<MoviePreview>) {
        viewModel.movies = results
        removeNoConnectionFragment()
        removeNoResultFragment()
        removeFilterFragment()
        removeLoadingFragment()
        removeResultFragment()
        displayResultFragment()
    }

    private fun firstPageOfShows(results: List<ShowPreview>) {
        viewModel.shows = results
        removeNoConnectionFragment()
        removeNoResultFragment()
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
        queryMap["page"] = ((queryMap["page"] ?: error("1")).toInt() + 1).toString()
        fillMedia(isMovie)
        resultFragment?.loading = true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.isMovie = isMovie
        viewModel.queryMap = queryMap
        resultFragment?.let {
            if (isMovie)
                viewModel.movies = it.getMovies(isLinear)
            else
                viewModel.shows = it.getShows(isLinear)
        }

        viewModel.isLinear = isLinear
        size.value?.let {
            viewModel.size = it
        }

        viewModel.network = childFragmentManager.findFragmentByTag("search_no_connection") != null
        Log.d("network", viewModel.network.toString())
    }

    private fun typeConvert(): String {
        return when (isMovie) {
            true -> "movie"
            false -> "show"
        }
    }

    override fun onAdapterCreated() {
        removeNoConnectionFragment()
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

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        isMovie = viewModel.isMovie
        isLinear = viewModel.isLinear

        if (viewModel.network)
            displayNoInternetConnectionFragment()

        if (isMovie)
            viewModel.movies?.let {
                firstPageOfMovies(it)
            }
        else
            viewModel.shows?.let {
                firstPageOfShows(it)
            }

        queryMap = viewModel.queryMap
        size.postValue(viewModel.size)
        Log.d("networkRestore", viewModel.network.toString())

        if (viewModel.size == 0 && viewModel.discover)
            displayLoadingFragment()
    }

    private fun clearViewModelData() {
        viewModel.queryMap.clear()
        viewModel.movies = null
        viewModel.shows = null
        viewModel.isMovie = true
        viewModel.position = 0
    }

    private fun displayNoInternetConnectionFragment() {
        childFragmentManager.beginTransaction()
            .replace(R.id.filter_network_container, NoInternetFragment(), "search_no_connection")
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .commitNow()

        childFragmentManager.executePendingTransactions()
    }

    private fun removeNoConnectionFragment() {
        childFragmentManager.findFragmentByTag("search_no_connection")?.let {
            childFragmentManager.beginTransaction()
                .remove(it)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit()
        }
    }

    override fun onNetworkLostFragment() {
        displayNoInternetConnectionFragment()
        removeResultFragment()
        displayLoadingFragment()
        viewModel.discover = true
        size.postValue(0)
    }

    override fun onNetworkDialog() {
        displayNoInternetConnectionFragment()
    }

    override fun onNetworkDialogDismiss() {
        removeNoConnectionFragment()
    }

    fun restorePosition(position: Int) {
        viewModel.position = position
    }
}

