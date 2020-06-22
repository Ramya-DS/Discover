package com.example.discover.category

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.discover.DiscoverApplication
import com.example.discover.R
import com.example.discover.datamodel.movie.preview.MoviePreview
import com.example.discover.datamodel.multiSearch.MultiSearch
import com.example.discover.datamodel.tvshow.preview.ShowPreview
import com.example.discover.mediaDisplay.MediaListActivity
import com.example.discover.movieScreen.MovieActivity
import com.example.discover.searchScreen.OnNetworkLostListener
import com.example.discover.showsScreen.ShowActivity
import com.example.discover.util.LoadPosterImage
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.lang.ref.WeakReference


class CategoryListAdapter(
    private val isMovie: Boolean?,
    private val activity: WeakReference<Activity>,
    private val withLoading: Boolean,
    private val onNetworkLostListener: OnNetworkLostListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val movieList = mutableListOf<MoviePreview>()
    private val tvList = mutableListOf<ShowPreview>()

    private val mediaList = mutableListOf<MultiSearch>()

    lateinit var category: String

    inner class MediaWithTitleViewHolder(mediaView: View) : RecyclerView.ViewHolder(mediaView),
        View.OnClickListener {
        var mPosition: Int = 0
        var type = 1
        var isMovie = true
        val posterImage: ImageView = mediaView.findViewById(R.id.media_card_grid_poster)
        val title: TextView = mediaView.findViewById(R.id.media_card_grid_title)
        val votingAverage: TextView = mediaView.findViewById(R.id.media_card_grid_voting_average)
        val votingBar: ProgressBar = mediaView.findViewById(R.id.media_card_grid_voting_bar)
        //        var imageTask: LoadPoster? = null
        var imageTask: LoadPosterImage? = null

        init {
            mediaView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            activity.get()?.apply {
                if ((application as DiscoverApplication).checkConnectivity()) {
                    onNetworkLostListener.onNetworkDialogDismiss()
                    val activityClass =
                        if (isMovie) MovieActivity::class.java else ShowActivity::class.java

                    val media = when (type) {
                        1 -> movieList[mPosition]
                        2 -> tvList[mPosition]
                        else -> if (isMovie)
                            formatToMoviesPreview(mediaList[mPosition])
                        else
                            formatToShowsPreview(mediaList[mPosition])
                    }

                    val key = if (isMovie) "movie" else "show"

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            this,
                            posterImage as View,
                            "posterImage"
                        )
                        startActivity(Intent(activity.get()!!, activityClass).apply {
                            putExtra(key, media)
                        }, options.toBundle())
                    } else {
                        startActivity(Intent(activity.get()!!, activityClass).apply {
                            putExtra(key, media)
                        })
                    }
                } else {
                    onNetworkLostListener.onNetworkDialog()
                    onNetworkLostListener.onNetworkLostFragment()
                }
            }
        }
    }

    inner class LoadMoreViewHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        val moreButton: FloatingActionButton = view.findViewById(R.id.more)

        init {
            moreButton.setOnClickListener(this)
            moreButton.hide()
        }

        override fun onClick(v: View?) {
            isMovie?.let {
                val intent = Intent(activity.get()!!, MediaListActivity::class.java)
                if (isMovie) {
                    val arrayList = arrayListOf<MoviePreview>()
                    arrayList.addAll(movieList)
                    intent.putExtra("list", arrayList)
                } else {
                    val arrayList = arrayListOf<ShowPreview>()
                    arrayList.addAll(tvList)
                    intent.putExtra("list", arrayList)
                }
                intent.putExtra("isMovie", isMovie)
                intent.putExtra("section", category)
                activity.get()!!.startActivity(intent)
                activity.get()?.overridePendingTransition(R.anim.right_in, R.anim.left_out)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> MediaWithTitleViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.media_card_grid_with_title,
                    parent,
                    false
                )
            )
            else -> LoadMoreViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.media_grid_load_more,
                    parent,
                    false
                )
            )
        }
    }


    override fun getItemCount() = if (isMovie != null) {
        if (withLoading) {
            if (isMovie) movieList.size + 1 else tvList.size + 1
        } else {
            if (isMovie) movieList.size else tvList.size
        }
    } else mediaList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (isMovie != null) {
            if (getItemViewType(position) == 1) {
                (holder as LoadMoreViewHolder).moreButton.show()

            } else {
                if (getItemViewType(position) == 0) {
                    holder as MediaWithTitleViewHolder
                    holder.mPosition = position

                    if (isMovie)
                        onBindMovie(holder, movieList[position])
                    else
                        onBindShow(holder, tvList[position])
                }

            }
        } else onBindMedia(
            (holder as MediaWithTitleViewHolder).apply {
                mPosition = position
                type = 3
            },
            mediaList[position]
        )
        setFadeAnimation(holder.itemView)
//        animation(holder.itemView, holder.adapterPosition)
    }


    private fun onBindMedia(holder: MediaWithTitleViewHolder, multiSearch: MultiSearch) {
        when (multiSearch.media_type) {
            "movie" -> {
                holder.isMovie = true
                multiSearch.apply {
                    bindDetails(holder, poster_path, title, vote_average)
                }
            }

            "tv" -> {
                holder.isMovie = false
                multiSearch.apply {
                    bindDetails(holder, poster_path, name, vote_average)
                }
            }
        }
    }

    fun setMovieSectionList(newList: List<MoviePreview>) {
        val result = DiffUtil.calculateDiff(
            ResultMoviesListDiffUtilCallback(
                this.movieList,
                newList
            )
        )
        result.dispatchUpdatesTo(this)
        mediaList.clear()
        movieList.addAll(newList)
    }

    fun setTvSectionList(newList: List<ShowPreview>) {
        val result = DiffUtil.calculateDiff(
            ResultShowsListDiffUtilCallback(
                this.tvList,
                newList
            )
        )
        result.dispatchUpdatesTo(this)
        tvList.clear()
        tvList.addAll(newList)
    }

    fun setMediaSectionList(newList: List<MultiSearch>) {
        val result = DiffUtil.calculateDiff(
            ResultMediaListDiffUtilCallback(
                this.mediaList,
                newList
            )
        )
        result.dispatchUpdatesTo(this)
        mediaList.clear()
        mediaList.addAll(newList)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is MediaWithTitleViewHolder)
            holder.imageTask?.interruptThread()
    }

    private fun setPosterImage(holder: MediaWithTitleViewHolder, posterPath: String?) {
        holder.posterImage.scaleType = ImageView.ScaleType.CENTER_INSIDE
        holder.posterImage.setImageDrawable(
            ContextCompat.getDrawable(
                holder.itemView.context,
                R.drawable.ic_media_placeholder
            )
        )
        holder.imageTask?.interruptThread()
        holder.imageTask =
            LoadPosterImage(posterPath, holder.posterImage, activity).apply { loadImage()
            }
//        holder.imageTask!!.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, posterPath)
    }

    private fun onBindMovie(holder: MediaWithTitleViewHolder, movie: MoviePreview) {
        holder.type = 1
        holder.isMovie = true
        bindDetails(holder, movie.poster_path, movie.title, movie.vote_average)
    }

    private fun onBindShow(holder: MediaWithTitleViewHolder, show: ShowPreview) {
        holder.type = 2
        holder.isMovie = false
        bindDetails(holder, show.poster_path, show.name, show.vote_average)
    }

    private fun bindDetails(
        holder: MediaWithTitleViewHolder,
        posterPath: String?,
        title: String,
        rating: Double
    ) {
        setPosterImage(holder, posterPath)
        holder.title.text = title
        val mRating = (rating * 10).toInt()
        if (mRating == 0)
            holder.votingAverage.text = "NR"
        else
            holder.votingAverage.text = "$mRating %"
        holder.votingBar.progress = mRating
    }

    private fun setFadeAnimation(view: View) {
        val anim = AlphaAnimation(0.0f, 1.0f)
        anim.duration = 300
        view.startAnimation(anim)
    }

    override fun getItemViewType(position: Int): Int {
        return if (!withLoading) {
            return 0
        } else {
            if (position < movieList.size || position < tvList.size)
                0
            else
                1
        }
    }

    fun appendMovieSectionList(newList: List<MoviePreview>) {
        val newResultsList = mutableListOf<MoviePreview>()
        newResultsList.addAll(movieList)
        newResultsList.addAll(newList)
        val result = DiffUtil.calculateDiff(
            ResultMoviesListDiffUtilCallback(
                this.movieList,
                newResultsList
            )
        )
        result.dispatchUpdatesTo(this)
        movieList.addAll(newList)
    }

    fun appendTvSectionList(newList: List<ShowPreview>) {
        val newResultsList = mutableListOf<ShowPreview>()
        newResultsList.addAll(tvList)
        newResultsList.addAll(newList)
        val result = DiffUtil.calculateDiff(
            ResultShowsListDiffUtilCallback(
                this.tvList,
                newResultsList
            )
        )
        result.dispatchUpdatesTo(this)
        tvList.addAll(newList)
    }

    fun appendMediaSectionList(newList: List<MultiSearch>) {
        val newResultsList = mutableListOf<MultiSearch>()
        newResultsList.addAll(mediaList)
        newResultsList.addAll(newList)
        val result = DiffUtil.calculateDiff(
            ResultMediaListDiffUtilCallback(
                this.mediaList,
                newResultsList
            )
        )
        result.dispatchUpdatesTo(this)
        mediaList.addAll(newList)
    }

    fun getMovies(): List<MoviePreview> {
        return movieList
    }

    fun getShows(): List<ShowPreview> {
        return tvList
    }

    fun getMedia(): List<MultiSearch> {
        return mediaList
    }

    inner class ResultMoviesListDiffUtilCallback(
        private var oldResult: List<MoviePreview>,
        private var newResult: List<MoviePreview>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldResult.size

        override fun getNewListSize(): Int = newResult.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldResult[oldItemPosition].id == newResult[newItemPosition].id

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldResult[oldItemPosition] == newResult[newItemPosition]

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            val oldMovie = oldResult[oldItemPosition]
            val newMovie = newResult[newItemPosition]

            val diffBundle = Bundle()

            if (oldMovie.id != newMovie.id) {
                diffBundle.putParcelable("movie", newMovie)
            }

            return if (diffBundle.size() == 0) null else diffBundle

        }

    }

    inner class ResultShowsListDiffUtilCallback(
        private var oldResult: List<ShowPreview>,
        private var newResult: List<ShowPreview>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldResult.size

        override fun getNewListSize(): Int = newResult.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldResult[oldItemPosition].id == newResult[newItemPosition].id

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldResult[oldItemPosition] == newResult[newItemPosition]

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            val oldShow = oldResult[oldItemPosition]
            val newShow = newResult[newItemPosition]

            val diffBundle = Bundle()

            if (oldShow.id != newShow.id) {
                diffBundle.putParcelable("show", newShow)
            }

            return if (diffBundle.size() == 0) null else diffBundle

        }

    }

    inner class ResultMediaListDiffUtilCallback(
        private var oldResult: List<MultiSearch>,
        private var newResult: List<MultiSearch>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldResult.size

        override fun getNewListSize(): Int = newResult.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldResult[oldItemPosition].id == newResult[newItemPosition].id

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldResult[oldItemPosition] == newResult[newItemPosition]

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            val oldMedia = oldResult[oldItemPosition]
            val newMedia = newResult[newItemPosition]

            val diffBundle = Bundle()

            if (oldMedia.id != newMedia.id) {
                diffBundle.putParcelable("media", newMedia)
            }

            return if (diffBundle.size() == 0) null else diffBundle

        }

    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            if (holder is MediaWithTitleViewHolder) {
                val bundle = payloads[0] as Bundle
                bundle.getParcelable<MoviePreview>("movie")?.let {
                    onBindMovie(holder, it)
                }

                bundle.getParcelable<ShowPreview>("show")?.let {
                    onBindShow(holder, it)
                }

                bundle.getParcelable<MultiSearch>("media")?.let {
                    onBindMedia(holder, it)
                }
            }
        }
    }

    private fun formatToShowsPreview(multiSearch: MultiSearch): ShowPreview {
        multiSearch.apply {
            return ShowPreview(
                id,
                name,
                0,
                vote_average,
                first_air_date,
                poster_path,
                genre_ids,
                original_language,
                backdrop_path,
                overview,
                emptyList()
            )
        }
    }

    private fun formatToMoviesPreview(multiSearch: MultiSearch): MoviePreview {
        multiSearch.apply {
            return MoviePreview(
                false,
                backdrop_path,
                genre_ids,
                id,
                original_language,
                overview,
                poster_path,
                release_date,
                title,
                vote_average, 0
            )
        }
    }
}