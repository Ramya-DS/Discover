package com.example.discover.mediaScreenUtils

import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.discover.R

class InfoAdapter(
    private val infoList: List<InfoClass>,
    private val onUrlSelectedListener: OnUrlSelectedListener
) :
    RecyclerView.Adapter<InfoAdapter.InfoViewHolder>() {

    class InfoViewHolder(infoView: View) : RecyclerView.ViewHolder(infoView) {
        val image: ImageView = infoView.findViewById(R.id.media_info_icon)
        val content: TextView = infoView.findViewById(R.id.media_info_content)
        val title: TextView = infoView.findViewById(R.id.media_info_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        InfoViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.media_info_list_layout,
                parent,
                false
            )
        )

    override fun getItemCount() = infoList.size

    override fun onBindViewHolder(holder: InfoViewHolder, position: Int) {
        val info = infoList[position]
        holder.image.setImageDrawable(
            ContextCompat.getDrawable(
                holder.itemView.context,
                info.image
            )
        )
        if (info.content == null || info.content.trim().isEmpty())
            holder.content.text = "-"
        else {
            if (info.title.contains("homepage", true) || info.title.contains("ID")) {
                val type = if (info.title.contains("imdb", true)) 1
                else if (info.title.contains("instagram", true)) 2
                else if (info.title.contains("facebook", true)) 3
                else if (info.title.contains("twitter", true)) 4
                else 0

                holder.content.text = highlightUrl(info.content, type)
                holder.content.movementMethod = LinkMovementMethod.getInstance()
            } else
                holder.content.text = info.content
        }


        holder.title.text = info.title
    }

    private fun highlightUrl(text: String, type: Int): SpannableString {
        val spannableString = SpannableString(text)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                onUrlSelectedListener.onUrlSelected(text, type)
            }
        }
        spannableString.setSpan(
            clickableSpan,
            0,
            text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannableString
    }
}