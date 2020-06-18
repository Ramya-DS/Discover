package com.example.discover.category

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.discover.R
import com.example.discover.datamodel.movie.preview.MoviePreview
import com.example.discover.datamodel.tvshow.preview.ShowPreview
import com.example.discover.searchScreen.OnNetworkLostListener
import java.lang.ref.WeakReference


class CategoryListResultFragment : Fragment() {

    companion object {
        fun newInstance(isMovie: Boolean, category: String): CategoryListResultFragment {
            return CategoryListResultFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("isMovie", isMovie)
                    putString("category", category)
                }
            }
        }
    }

    private var isMovie = true
    private lateinit var category: String
    private var adapter: CategoryListAdapter? = null
    private var onCategoryAdapterCreatedListener: OnCategoryAdapterCreatedListener? = null
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isMovie = arguments?.getBoolean("isMovie")!!
        category = arguments?.getString("category")!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView = inflater.inflate(R.layout.fragment_search_result, container, false)

        recyclerView = rootView.findViewById(R.id.searchResult)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager =
            GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
        adapter = CategoryListAdapter(
            isMovie,
            WeakReference(activity!!),
            true,
            activity as OnNetworkLostListener
        ).apply {
            this.category = this@CategoryListResultFragment.category
        }

        recyclerView.adapter = adapter
        onCategoryAdapterCreatedListener?.onCategoryAdapterCreated(category, this)
        return rootView
    }

    fun setMovies(results: List<MoviePreview>, position: Int = -1) {
        adapter?.setMovieSectionList(results)
        if (position != -1)
            restoreElementPosition(position)
    }

    fun setShows(results: List<ShowPreview>, position: Int = -1) {
        adapter?.setTvSectionList(results)
        if (position != -1)
            restoreElementPosition(position)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity is MediaCategoryActivity)
            onCategoryAdapterCreatedListener = context as OnCategoryAdapterCreatedListener
    }

    fun lastElementPosition(): Int {
        return (recyclerView.layoutManager as GridLayoutManager).findFirstVisibleItemPosition()
    }

    private fun restoreElementPosition(position: Int) {
        recyclerView.scrollToPosition(position)
    }
}
