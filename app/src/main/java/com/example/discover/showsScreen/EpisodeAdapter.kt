package com.example.discover.showsScreen

import android.app.Activity
import android.graphics.Color
import android.os.AsyncTask
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.discover.R
import com.example.discover.datamodel.tvshow.detail.Episode
import com.example.discover.util.LoadBackdrop
import java.lang.ref.WeakReference


class EpisodeAdapter(
    private val episodes: List<Episode>,
    private val activity: WeakReference<Activity>,
    private val onCreditSelectedListener: OnCreditSelectedListener
) :
    RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder>() {

    inner class EpisodeViewHolder(episodeView: View) : RecyclerView.ViewHolder(episodeView) {
        var mPosition = 0
        val stillPath: ImageView = episodeView.findViewById(R.id.episode_still_path)
        val rating: TextView = episodeView.findViewById(R.id.episode_ratings)
        val airDate: TextView = episodeView.findViewById(R.id.episode_air_date)
        val name: TextView = episodeView.findViewById(R.id.episode_name)
        val overview: TextView = episodeView.findViewById(R.id.episode_overview)

        init {
            highlightText(episodeView.findViewById(R.id.episode_crewList), mPosition)
            highlightText(episodeView.findViewById(R.id.episode_guestStarsList), mPosition)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        EpisodeViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.show_episode_layout, parent, false
            )
        )

    override fun getItemCount() = episodes.size
    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        val episode = episodes[position]

        holder.mPosition = position

        if (episode.still_path != null) {
            LoadBackdrop(
                WeakReference(holder.stillPath),
                activity
            ).executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR,
                episode.still_path,
                getWidth().toString(), true.toString()
            )
        }

        holder.name.text = episode.name
        holder.overview.text = episode.overview
        holder.rating.text = " ${episode.vote_average}  "
        holder.airDate.text = "  ${episode.air_date?:'-'}"
    }

    private fun getWidth(): Int? {
        val displayMetrics: DisplayMetrics? = activity.get()?.resources?.displayMetrics
        return displayMetrics?.widthPixels
    }

    private fun highlightText(textView: TextView, position: Int) {
        val text = textView.text
        val spannableString = SpannableString(text)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                if (text.contains("crew", true)) {
                    onCreditSelectedListener.onCrewSelected(episodes[position].crew)
                } else {
                    onCreditSelectedListener.onCastSelected(episodes[position].guest_stars)
                }
            }
        }

        spannableString.setSpan(
            clickableSpan,
            0,
            text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            ForegroundColorSpan(Color.WHITE),
            0,
            text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()
    }
}