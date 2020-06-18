package com.example.discover.genreScreen

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.discover.DiscoverApplication
import com.example.discover.R
import com.example.discover.category.CategoryListAdapter
import com.example.discover.datamodel.Language
import com.example.discover.datamodel.genre.Genres
import com.example.discover.datamodel.movie.preview.MoviePreview
import com.example.discover.datamodel.multiSearch.MultiSearch
import com.example.discover.datamodel.tvshow.preview.ShowPreview
import com.example.discover.filterScreen.ui.FilterFragment
import com.example.discover.mediaDisplay.MediaDetailAdapter
import com.example.discover.mediaDisplay.MediaListActivity
import com.example.discover.mediaDisplay.OnAdapterCreatedListener
import com.example.discover.searchScreen.OnNetworkLostListener
import com.example.discover.searchScreen.SearchActivity
import com.example.discover.util.NoInternetFragment
import java.lang.ref.WeakReference
import kotlin.math.roundToInt


class GenreMediaResultFragment : Fragment(), OnNetworkLostListener {

    companion object {
        fun newInstance(isLinear: Boolean, type: String): GenreMediaResultFragment {
            return GenreMediaResultFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("isLinear", isLinear)
                    putString("type", type)
                }
            }
        }
    }

    var position = 0

    private var onAdapterCreatedListener: OnAdapterCreatedListener? = null
    private var linearAdapter: MediaDetailAdapter? = null
    private var gridAdapter: CategoryListAdapter? = null

    var isMovie: Boolean? = null
    var isLinear = true
    lateinit var movieGenres: List<Genres>
    lateinit var showsGenres: List<Genres>
    lateinit var languages: List<Language>
    var loading = true

    private lateinit var networkContainer: FrameLayout

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isLinear = arguments!!.getBoolean("isLinear")
            val type = arguments!!.getString("type")
            isMovie = when (type) {
                "movie" -> true
                "show" -> false
                else -> null
            }
            Log.d("isLinear", "onCreate $isLinear $isMovie")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_search_result, container, false)

        networkContainer = rootView.findViewById(R.id.result_network_container)

        (activity?.application as DiscoverApplication).let {
            it.languages.observe(viewLifecycleOwner, Observer { lang ->
                languages = lang
                it.movieGenres.observe(viewLifecycleOwner, Observer { movies ->
                    movieGenres = movies
                    it.showsGenres.observe(viewLifecycleOwner, Observer { shows ->
                        showsGenres = shows
                        recyclerView = rootView.findViewById(R.id.searchResult)
                        if (isLinear) {
                            recyclerView.layoutManager = LinearLayoutManager(context!!)
                            linearAdapter = MediaDetailAdapter(
                                WeakReference(activity!!),
                                isMovie,
                                languages,
                                if (isMovie == null)
                                    listOf(movieGenres, showsGenres).flatten()
                                else {
                                    if (isMovie!!) movieGenres else showsGenres
                                },
                                this
                            )
                            recyclerView.layoutAnimation = AnimationUtils.loadLayoutAnimation(
                                context?.applicationContext,
                                R.anim.recycler_dropdown
                            )
                            recyclerView.adapter = linearAdapter
                        } else {
                            val configuration = activity!!.resources.configuration
                            val screenWidthDp: Int = configuration.screenWidthDp
                            val spanCount = (screenWidthDp / 130.0).roundToInt()
                            recyclerView.layoutManager = GridLayoutManager(context, spanCount)
                            gridAdapter =
                                CategoryListAdapter(isMovie, WeakReference(activity!!), false, this)
                            recyclerView.adapter = gridAdapter
                            recyclerView.layoutAnimation = AnimationUtils.loadLayoutAnimation(
                                context?.applicationContext,
                                R.anim.recycler_dropdown
                            )
                        }
                        onAdapterCreatedListener?.onAdapterCreated()
                        infiniteRecyclerView()
//
                    })
                })
            })

        }

        return rootView
    }

    fun setMovieResults(isLinear: Boolean, list: List<MoviePreview>) {
        if (isLinear) {
            linearAdapter?.setMovieList(list)
        } else
            gridAdapter?.setMovieSectionList(list)

        recyclerView.scrollToPosition(position)
        recyclerView.scheduleLayoutAnimation()
    }

    fun setShowsResults(isLinear: Boolean, list: List<ShowPreview>) {
        if (isLinear) {
            linearAdapter?.setTvShowList(list)
        } else
            gridAdapter?.setTvSectionList(list)
        recyclerView.scrollToPosition(position)
        recyclerView.scheduleLayoutAnimation()
    }

    fun setMultiResults(isLinear: Boolean, list: List<MultiSearch>) {
        if (isLinear)
            linearAdapter?.setMediaList(list)
        else
            gridAdapter?.setMediaSectionList(list)
        recyclerView.scrollToPosition(position)
        recyclerView.scheduleLayoutAnimation()
    }

    fun appendShowsResult(isLinear: Boolean, list: List<ShowPreview>) {
        if (isLinear)
            linearAdapter?.appendTvShowList(list)
        else
            gridAdapter?.appendTvSectionList(list)
    }

    fun appendMoviesResult(isLinear: Boolean, list: List<MoviePreview>) {
        if (isLinear)
            linearAdapter?.appendMovieList(list)
        else
            gridAdapter?.appendMovieSectionList(list)
    }

    fun appendMediaResult(isLinear: Boolean, list: List<MultiSearch>) {
        if (isLinear)
            linearAdapter?.appendMediaList(list)
        else
            gridAdapter?.appendMediaSectionList(list)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is GenreMediaActivity) {
            onAdapterCreatedListener = context as GenreMediaActivity
        }

        if (activity is MediaListActivity) {
            Log.d("onAttach", "attached")
            onAdapterCreatedListener = context as OnAdapterCreatedListener
        }

        if (activity is SearchActivity)
            onAdapterCreatedListener = context as SearchActivity

        if (parentFragment is FilterFragment)
            onAdapterCreatedListener = parentFragment as FilterFragment
    }

    private fun infiniteRecyclerView() {
        var pastVisibleItems: Int
        var visibleItemCount: Int
        var totalItemCount: Int
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) { //check for scroll down
                    val mLayoutManager = recyclerView.layoutManager as LinearLayoutManager
                    visibleItemCount = mLayoutManager.childCount
                    totalItemCount = mLayoutManager.itemCount
                    pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition()
                    if (loading) {
                        if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                            loading = false
                            if (activity is GenreMediaActivity) (activity as GenreMediaActivity).fetchMore()
                            if (activity is SearchActivity) (activity as SearchActivity).fetchMore()
                            if (parentFragment is FilterFragment) (parentFragment as FilterFragment).fetchMore()
                            if (activity is MediaListActivity) (activity as MediaListActivity).fetchMore()
                        }
                    }
                }
            }
        })
    }

    fun changeView(isMovie: Boolean?, isLinear: Boolean) {
        if (isLinear) {
            val configuration = activity!!.resources.configuration
            val screenWidthDp: Int = configuration.screenWidthDp
            val spanCount = (screenWidthDp / 130.0).roundToInt()
            position =
                (recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
            recyclerView.layoutManager = GridLayoutManager(context, spanCount)
            gridAdapter =
                CategoryListAdapter(isMovie, WeakReference(activity!!), false, this).apply {
                    if (isMovie == null) setMediaSectionList(linearAdapter!!.getMedia()) else {
                        if (isMovie) setMovieSectionList(linearAdapter!!.getMovies())
                        else setTvSectionList(linearAdapter!!.getShows())
                    }
                }
            recyclerView.layoutAnimation = AnimationUtils.loadLayoutAnimation(
                context?.applicationContext,
                R.anim.recycler_dropdown
            )
            recyclerView.adapter = gridAdapter
            recyclerView.scrollToPosition(position)

        } else {
            position =
                (recyclerView.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
            recyclerView.layoutManager = LinearLayoutManager(context)
            linearAdapter = MediaDetailAdapter(
                WeakReference(activity as Activity),
                isMovie,
                languages,
                if (isMovie == null)
                    listOf(movieGenres, showsGenres).flatten()
                else {
                    if (isMovie) movieGenres else showsGenres
                }, this
            ).apply {
                if (isMovie == null)
                    setMediaList(gridAdapter!!.getMedia())
                else {
                    if (isMovie) setMovieList(gridAdapter!!.getMovies())
                    else setTvShowList(gridAdapter!!.getShows())
                }
            }
            recyclerView.layoutAnimation = AnimationUtils.loadLayoutAnimation(
                context?.applicationContext,
                R.anim.recycler_dropdown
            )
            recyclerView.adapter = linearAdapter
            recyclerView.scrollToPosition(position)
        }
    }

    fun getMovies(isLinear: Boolean): List<MoviePreview> {
        return if (isLinear)
            linearAdapter!!.getMovies()
        else
            gridAdapter!!.getMovies()
    }

    fun getShows(isLinear: Boolean): List<ShowPreview> {
        return if (isLinear)
            linearAdapter!!.getShows()
        else
            gridAdapter!!.getShows()
    }

    fun getMedia(isLinear: Boolean): List<MultiSearch> {
        return if (isLinear)
            linearAdapter!!.getMedia()
        else
            gridAdapter!!.getMedia()
    }

    override fun onNetworkLostFragment() {

    }

    override fun onNetworkDialog() {
        if (parentFragment is FilterFragment) {
            (parentFragment as FilterFragment).onNetworkDialog()
            return
        }

        if (activity is SearchActivity) {
            (activity as SearchActivity).onNetworkDialog()
            return
        }
        childFragmentManager.beginTransaction()
            .replace(R.id.result_network_container, NoInternetFragment(), "no_connection").commit()
    }

    override fun onNetworkDialogDismiss() {
        if (parentFragment is FilterFragment) {
            (parentFragment as FilterFragment).onNetworkDialogDismiss()
            return
        }

        if (activity is SearchActivity) {
            (activity as SearchActivity).onNetworkDialogDismiss()
            return
        }
        childFragmentManager.findFragmentByTag("no_connection")?.let {
            childFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (activity is MediaListActivity) {
            (activity as MediaListActivity).restorePosition(obtainPosition())
        }
        if (parentFragment is FilterFragment)
            (parentFragment as FilterFragment).restorePosition(obtainPosition())

        if (activity is SearchActivity)
            (activity as SearchActivity).restorePosition(obtainPosition())

        if (activity is GenreMediaActivity)
            (activity as GenreMediaActivity).restorePosition(obtainPosition())
    }

    fun restorePosition(position: Int) {
        recyclerView.scrollToPosition(position)
    }

    private fun obtainPosition(): Int {
        if (!isLinear)
            (recyclerView.layoutManager as GridLayoutManager).apply {
                return findFirstCompletelyVisibleItemPosition()
            }
        else
            (recyclerView.layoutManager as LinearLayoutManager).apply {
                return findFirstCompletelyVisibleItemPosition()
            }
    }
}