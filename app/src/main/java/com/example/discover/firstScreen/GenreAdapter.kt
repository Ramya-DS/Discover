package com.example.discover.firstScreen

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.discover.R
import com.example.discover.datamodel.genre.Genres
import com.example.discover.datamodel.keyword.Keyword
import com.google.android.material.chip.Chip

class GenreAdapter(
    private val default: Boolean,
    private val isGenre: Boolean,
    private val isMovie: Boolean? = null,
    private val onGenreSelectedListener: OnGenreSelectedListener? = null
) :
    RecyclerView.Adapter<GenreAdapter.GenreViewHolder>() {

    private var genres = emptyList<Genres>()
    var keywords = emptyList<Keyword>()

    inner class GenreViewHolder(genreView: View) : RecyclerView.ViewHolder(genreView),
        View.OnClickListener {

        init {
            genreView.setOnClickListener(this)
        }

        var id = 0
        val text: Chip = genreView.findViewById(R.id.genre)

        override fun onClick(v: View?) {
            isMovie?.let {
                onGenreSelectedListener?.onGenreSelected(isMovie, id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        if (!default) GenreViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.chip_layout,
                parent,
                false
            )
        ) else GenreViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.chip_layout_small,
                parent,
                false
            )
        )

    override fun getItemCount() = if (isGenre) genres.size else keywords.size

    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        if (isGenre) {
            holder.id = genres[position].id
            holder.text.text = genres[position].name
        } else
            holder.text.text = keywords[position].name
    }

    fun setGenresList(list: List<Genres>) {
        Log.d("GenresAdapter: Genre", list.toString())
        genres = list
        notifyDataSetChanged()
    }

    fun setKeywordList(list: List<Keyword>) {
        Log.d("GenresAdapter: keyword", list.toString())
        keywords = list
        notifyDataSetChanged()
    }
}