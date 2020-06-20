package com.example.discover.showsScreen

import android.app.Activity
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.discover.R
import com.example.discover.datamodel.tvshow.detail.Season
import com.example.discover.util.LoadPoster
import java.lang.ref.WeakReference

class SeasonAdapter(private val activity: WeakReference<Activity>) :
    RecyclerView.Adapter<SeasonAdapter.SeasonViewHolder>() {

    private var seasons = emptyList<Season>()

    inner class SeasonViewHolder(seasonView: View) : RecyclerView.ViewHolder(seasonView),
        View.OnClickListener {
        init {
            seasonView.setOnClickListener(this)
        }

        var mPosition = 0
        val poster: ImageView = seasonView.findViewById(R.id.show_season_poster)
        val season: TextView = seasonView.findViewById(R.id.show_season)
        val episode: TextView = seasonView.findViewById(R.id.show_season_episode_count)
        val airDate: TextView = seasonView.findViewById(R.id.show_season_air_date)
        override fun onClick(v: View?) {
            (activity.get() as ShowActivity).onSeasonClicked(seasons[mPosition])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SeasonViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.show_season_layout,
            parent,
            false
        )
    )

    override fun getItemCount() = seasons.size

    override fun onBindViewHolder(holder: SeasonViewHolder, position: Int) {
        val season = seasons[position]
        holder.mPosition = position
        holder.season.text = season.name

        val episodeText = "Episodes: ${season.episode_count}"
        holder.episode.text = episodeText

        val airDateText = "AirDate: ${season.air_date ?: '-'}"
        holder.airDate.text = airDateText

        holder.poster.scaleType = ImageView.ScaleType.CENTER_INSIDE
        holder.poster.setImageDrawable(
            ContextCompat.getDrawable(
                holder.poster.context,
                R.drawable.ic_media_placeholder
            )
        )
        if (season.poster_path != null)
            LoadPoster(
                WeakReference(holder.poster),
                activity
            ).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, season.poster_path)
    }

//    private fun setPlaceHolder(imageView: ImageView) {
//        val drawable = ContextCompat.getDrawable(activity.get()!!, R.drawable.ic_media_placeholder)
//        val bitmap = Bitmap.createBitmap(80, 100, Bitmap.Config.ARGB_8888)
//        val canvas = Canvas(bitmap)
//        drawable!!.setBounds(10, 20, canvas.width - 10, canvas.height - 20)
//        drawable.draw(canvas)
//        imageView.setImageBitmap(bitmap)
//    }

    fun fetchSeasons(season: List<Season>) {
        seasons = season
        notifyDataSetChanged()
    }
}