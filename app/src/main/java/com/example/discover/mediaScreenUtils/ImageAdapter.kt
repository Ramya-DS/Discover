package com.example.discover.mediaScreenUtils

import android.app.Activity
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.discover.R
import com.example.discover.datamodel.images.ImageDetails
import com.example.discover.util.LoadPosterImage
import java.lang.ref.WeakReference

class ImageAdapter(
    private val isBackdrop: Boolean,
    private val list: List<ImageDetails>?,
    private val activity: WeakReference<Activity>
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(imageView: View) : RecyclerView.ViewHolder(imageView) {
        val image: ImageView = imageView.findViewById(R.id.viewpager_backdropImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ImageViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.media_backdrop_viewpager_layout,
            parent,
            false
        )
    )


    override fun getItemCount() = list?.size ?: 1

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.image.scaleType = ImageView.ScaleType.CENTER_INSIDE
        holder.image.setImageDrawable(
            ContextCompat.getDrawable(
                holder.itemView.context,
                R.drawable.ic_media_placeholder
            )
        )
        if (list != null) {
            if (isBackdrop) {
                getWidth()?.let {
                    LoadPosterImage(list[position].file_path, holder.image, activity).apply {
                        loadImage(it, (it / 1.777777).toInt(), true)
                    }
                }
            } else {
                holder.image.scaleType = ImageView.ScaleType.FIT_XY
                LoadPosterImage(list[position].file_path, holder.image, activity).apply {
                    loadImage()
                }
            }
        }
    }

    private fun getWidth(): Int? {
        val displayMetrics: DisplayMetrics? = activity.get()?.resources?.displayMetrics
        return displayMetrics?.widthPixels
    }
}