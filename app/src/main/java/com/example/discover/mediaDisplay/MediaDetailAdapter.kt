package com.example.discover.mediaDisplay

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.discover.DiscoverApplication
import com.example.discover.R
import com.example.discover.datamodel.Language
import com.example.discover.datamodel.genre.Genres
import com.example.discover.datamodel.movie.preview.MoviePreview
import com.example.discover.datamodel.multiSearch.MultiSearch
import com.example.discover.datamodel.tvshow.preview.ShowPreview
import com.example.discover.firstScreen.GenreAdapter
import com.example.discover.movieScreen.MovieActivity
import com.example.discover.searchScreen.OnNetworkLostListener
import com.example.discover.showsScreen.ShowActivity
import com.example.discover.util.LoadPoster
import java.lang.ref.WeakReference


class MediaDetailAdapter(
    private val activity: WeakReference<Activity>,
    private val isMovie: Boolean?,
    private val languages: List<Language>,
    private val genres: List<Genres>,
    private val onNetworkLostListener: OnNetworkLostListener
) :
    RecyclerView.Adapter<MediaDetailAdapter.MediaDetailViewHolder>() {

    private var movies = mutableListOf<MoviePreview>()
    private var shows = mutableListOf<ShowPreview>()

    private var media = mutableListOf<MultiSearch>()

    inner class MediaDetailViewHolder(medialDetailView: View) :
        RecyclerView.ViewHolder(medialDetailView), View.OnClickListener {

        init {
            medialDetailView.setOnClickListener(this)
        }

        var id = 0
        var isMovie: Boolean = true
        var posterTask: LoadPoster? = null
        val poster: ImageView = medialDetailView.findViewById(R.id.media_layout_poster)
        val title: TextView = medialDetailView.findViewById(R.id.media_layout_title)
        val releaseDate: TextView = medialDetailView.findViewById(R.id.media_layout_release_date)
        val language: TextView = medialDetailView.findViewById(R.id.media_layout_language)
        val genreList: RecyclerView = medialDetailView.findViewById(R.id.media_layout_genre_list)
        val ratingBar: ProgressBar = medialDetailView.findViewById(R.id.media_layout_voting_bar)
        val rating: TextView = medialDetailView.findViewById(R.id.media_layout_voting_average)

        override fun onClick(v: View?) {
            activity.get()?.apply {
                if ((application as DiscoverApplication).checkConnectivity()) {
                    onNetworkLostListener.onNetworkDialogDismiss()
                    val mClass =
                        if (isMovie) MovieActivity::class.java else ShowActivity::class.java

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            this,
                            poster as View,
                            "posterImage"
                        )
                        startActivity(Intent(this, mClass).apply {
                            putExtra("id", id)
                            putExtra("name", this@MediaDetailViewHolder.title.text.split(": ")[1])
                        }, options.toBundle())
                    } else {
                        startActivity(Intent(this, mClass).apply {
                            putExtra("id", id)
                            putExtra("name", this@MediaDetailViewHolder.title.text.split(": ")[1])
                        })
                    }
                } else {
                    onNetworkLostListener.onNetworkDialog()
                    onNetworkLostListener.onNetworkLostFragment()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MediaDetailViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.media_layout,
            parent,
            false
        )
    )

    override fun getItemCount(): Int {
        return if (isMovie != null) {
            if (isMovie) movies.size else shows.size
        } else
            media.size
    }

    override fun onBindViewHolder(holder: MediaDetailViewHolder, position: Int) {
        holder.poster.setImageDrawable(
            ContextCompat.getDrawable(
                holder.itemView.context,
                R.drawable.ic_media_placeholder
            )
        )

        if (isMovie != null)
            if (isMovie)
                onBindMovie(holder, movies[position])
            else
                onBindShow(holder, shows[position])
        else
            onBindMedia(holder, media[position])


        setAnimation(holder.itemView)
    }

    private fun onBindMovie(holder: MediaDetailViewHolder, moviePreview: MoviePreview) {
        holder.id = moviePreview.id
        holder.isMovie = true
        moviePreview.apply {
            bindDetails(
                holder,
                poster_path,
                title,
                release_date,
                original_language,
                genre_ids,
                vote_average
            )
        }
    }

    private fun onBindShow(holder: MediaDetailViewHolder, showPreview: ShowPreview) {
        holder.id = showPreview.id
        holder.isMovie = false
        showPreview.apply {
            bindDetails(
                holder,
                poster_path,
                name,
                first_air_date,
                original_language,
                genre_ids,
                vote_average
            )
        }
    }

    private fun onBindMedia(holder: MediaDetailViewHolder, multiSearch: MultiSearch) {
        holder.id = multiSearch.id
        when (multiSearch.media_type) {
            "movie" -> {
                holder.isMovie = true
                multiSearch.apply {
                    bindDetails(
                        holder,
                        poster_path,
                        title,
                        release_date,
                        original_language,
                        genre_ids, vote_average
                    )
                }
            }

            "tv" -> {
                holder.isMovie = false
                multiSearch.apply {
                    bindDetails(
                        holder,
                        poster_path,
                        name,
                        first_air_date,
                        original_language,
                        genre_ids, vote_average
                    )
                }
            }
        }
    }

    private fun bindDetails(
        holder: MediaDetailViewHolder,
        posterPath: String?,
        title: String,
        date: String?,
        language: String,
        genreIds: List<Int>, rating: Double
    ) {
        holder.posterTask?.cancel(true)
        holder.posterTask = LoadPoster(
            WeakReference(
                holder.poster
            ), activity
        ).apply {
            executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, posterPath)
        }

        holder.ratingBar.progress = (rating * 10).toInt()
        holder.rating.text = "${(rating * 10).toInt()} %"
        val mTitle = "Title: $title"
        holder.title.text = mTitle

        val releaseDate =
            if (date != null && date.trim().isEmpty()) "Release Date: -" else "Release Date: $date"
        holder.releaseDate.text = releaseDate

        val mLanguage = "Language: ${getLanguageName(language)}"
        holder.language.text = mLanguage

        holder.genreList.apply {
            layoutManager =
                GridLayoutManager(
                    holder.itemView.context,
                    1,
                    GridLayoutManager.HORIZONTAL,
                    false
                )
            adapter = GenreAdapter(default = false, isGenre = true).apply {
                setGenresList(getGenres(genreIds))
            }
        }
    }

    private fun getLanguageName(id: String): String {
        for (i in languages)
            if (i.isoId == id)
                return i.name
        return "-"
    }

    private fun getGenres(id: List<Int>): List<Genres> {
        val list = mutableListOf<Genres>()
        for (i in id)
            for (g in genres)
                if (g.id == i) {
                    list.add(g)
                    break
                }
        return list
    }

    fun setMovieList(list: List<MoviePreview>) {
        Log.d("Recycler view", list.size.toString())
        val result = DiffUtil.calculateDiff(
            ResultMoviesListDiffUtilCallback(
                movies,
                list
            )
        )
        result.dispatchUpdatesTo(this)
        movies.clear()
        movies.addAll(list)
    }

    fun setTvShowList(list: List<ShowPreview>) {
        Log.d("setTvShowList", "${list.size} ${shows.size}")
        val result = DiffUtil.calculateDiff(
            ResultShowsListDiffUtilCallback(
                this.shows,
                list
            )
        )
        result.dispatchUpdatesTo(this)
        shows.clear()
        shows.addAll(list)
    }

    fun setMediaList(list: List<MultiSearch>) {
        val result = DiffUtil.calculateDiff(
            ResultMediaListDiffUtilCallback(
                this.media,
                list
            )
        )
        result.dispatchUpdatesTo(this)
        media.clear()
        media.addAll(list)
    }

    fun appendMovieList(list: List<MoviePreview>) {
        val newResultsList = mutableListOf<MoviePreview>()
        newResultsList.addAll(movies)
        newResultsList.addAll(list)
        val result = DiffUtil.calculateDiff(
            ResultMoviesListDiffUtilCallback(
                this.movies,
                newResultsList
            )
        )
        result.dispatchUpdatesTo(this)
        movies.addAll(list)
    }

    fun appendTvShowList(list: List<ShowPreview>) {
        val newResultsList = mutableListOf<ShowPreview>()
        newResultsList.addAll(shows)
        newResultsList.addAll(list)
        val result = DiffUtil.calculateDiff(
            ResultShowsListDiffUtilCallback(
                this.shows,
                newResultsList
            )
        )
        result.dispatchUpdatesTo(this)
        shows.addAll(list)
    }

    fun appendMediaList(list: List<MultiSearch>) {
        val newResultsList = mutableListOf<MultiSearch>()
        newResultsList.addAll(media)
        newResultsList.addAll(list)
        val result = DiffUtil.calculateDiff(
            ResultMediaListDiffUtilCallback(
                this.media,
                newResultsList
            )
        )
        result.dispatchUpdatesTo(this)
        media.addAll(list)
    }

    fun getMovies(): List<MoviePreview> {
        return movies
    }

    fun getShows(): List<ShowPreview> {
        return shows
    }

    fun getMedia(): List<MultiSearch> {
        return media
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
        holder: MediaDetailViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {

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

    private fun setAnimation(view: View) {
        val anim = AlphaAnimation(0.0f, 1.0f)
        anim.duration = 300
        view.startAnimation(anim)
    }
}