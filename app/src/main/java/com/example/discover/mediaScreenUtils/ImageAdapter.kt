package com.example.discover.mediaScreenUtils

import android.app.Activity
import android.os.AsyncTask
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.discover.R
import com.example.discover.datamodel.images.ImageDetails
import com.example.discover.util.LoadBackdrop
import com.example.discover.util.LoadPoster
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
                    LoadBackdrop(WeakReference(holder.image), activity).executeOnExecutor(
                        AsyncTask.THREAD_POOL_EXECUTOR,
                        list[position].file_path,
                        it.toString(), true.toString()
                    )
                }
            } else {
                holder.image.scaleType = ImageView.ScaleType.FIT_XY
                LoadPoster(
                    WeakReference(holder.image),
                    activity
                ).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, list[position].file_path)
            }
        }
    }

    private fun getWidth(): Int? {
        val displayMetrics: DisplayMetrics? = activity.get()?.resources?.displayMetrics
        return displayMetrics?.widthPixels
    }
}