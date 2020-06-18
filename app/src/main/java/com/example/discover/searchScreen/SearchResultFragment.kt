package com.example.discover.searchScreen


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.discover.R
import com.example.discover.category.CategoryListAdapter
import com.example.discover.datamodel.movie.preview.MoviePreview
import com.example.discover.datamodel.tvshow.preview.ShowPreview
import com.example.discover.genreScreen.GenreMediaActivity
import com.example.discover.mediaDisplay.MediaDetailAdapter
import java.lang.ref.WeakReference

/**
 * A simple [Fragment] subclass.
 */
//class SearchResultFragment : Fragment() {
//
//    companion object {
//
//        const val TAG = "SearchResultFragment"
//
//        fun newInstance(type: String): SearchResultFragment {
//            val fragment = SearchResultFragment()
//            fragment.arguments = Bundle().apply {
//                putString("type", type)
//            }
//
//            return fragment
//        }
//    }
//
//    private var linearAdapter: MediaDetailAdapter? = null
//    private var gridAdapter: CategoryListAdapter? = null
//    lateinit var recyclerView: RecyclerView
//
//    var isMovie: Boolean? = true
//
//    var loading = true
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//            it.getString("type")
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val rootView = inflater.inflate(R.layout.fragment_search_result, container, false)
//        recyclerView = rootView.findViewById(R.id.searchResult)
//        recyclerView.layoutManager = LinearLayoutManager(context)
//        recyclerView.setHasFixedSize(true)
//        linearAdapter = MediaDetailAdapter(
//            WeakReference(activity!!),
//            isMovie,
//            languages,
//            if (isMovie) movieGenres else showsGenres
//        )
//        recyclerView.adapter = linearAdapter
//        infiniteRecyclerView()
//
//        return rootView
//    }
//
//    fun setMovieResults(isLinear: Boolean, list: List<MoviePreview>) {
//        if (isLinear)
//            linearAdapter?.setMovieList(list)
//        else
//            gridAdapter?.setMovieSectionList(list)
//    }
//
//    fun setShowsResults(isLinear: Boolean, list: List<ShowPreview>) {
//        if (isLinear)
//            linearAdapter?.setTvShowList(list)
//        else
//            gridAdapter?.setTvSectionList(list)
//    }
//
//    fun appendShowsResult(isLinear: Boolean, list: List<ShowPreview>) {
//        if (isLinear)
//            linearAdapter?.appendTvShowList(list)
//        else
//            gridAdapter?.appendTvSectionList(list)
//    }
//
//    fun appendMoviesResult(isLinear: Boolean, list: List<MoviePreview>) {
//        if (isLinear)
//            linearAdapter?.appendMovieList(list)
//        else
//            gridAdapter?.appendMovieSectionList(list)
//    }
//
//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        (activity as GenreMediaActivity).apply {
//            this@GenreMediaResultFragment.isMovie = isMovie
//            this@GenreMediaResultFragment.movieGenres = movieGenre
//            this@GenreMediaResultFragment.showsGenres = showGenre
//            this@GenreMediaResultFragment.languages = this.languages
//        }
//    }
//
//    private fun infiniteRecyclerView() {
//        var pastVisibleItems: Int
//        var visibleItemCount: Int
//        var totalItemCount: Int
//        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                if (dy > 0) { //check for scroll down
//                    val mLayoutManager = recyclerView.layoutManager as LinearLayoutManager
//                    visibleItemCount = mLayoutManager.childCount
//                    totalItemCount = mLayoutManager.itemCount
//                    pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition()
//                    if (loading) {
//                        if (visibleItemCount + pastVisibleItems >= totalItemCount) {
//                            loading = false
//                            (activity as GenreMediaActivity).fetchMore()
//                        }
//                    }
//                }
//            }
//        })
//    }
//
//    fun changeView(isMovie: Boolean, isLinear: Boolean) {
//        if (isLinear) {
//            recyclerView.layoutManager = GridLayoutManager(context, 3)
//            gridAdapter = CategoryListAdapter(isMovie, WeakReference(activity!!), false).apply {
//                if (isMovie) setMovieSectionList(linearAdapter!!.getMovies())
//                else setTvSectionList(linearAdapter!!.getShows())
//            }
//            recyclerView.adapter = gridAdapter
//
//        } else {
//            recyclerView.layoutManager = LinearLayoutManager(context)
//            linearAdapter = MediaDetailAdapter(
//                WeakReference(activity!!),
//                isMovie,
//                languages,
//                if (isMovie) movieGenres else showsGenres
//            ).apply {
//                if (isMovie) setMovieList(gridAdapter!!.getMovies())
//                else setTvShowList(linearAdapter!!.getShows())
//            }
//            recyclerView.adapter = linearAdapter
//        }
//    }
//}
//
//}
