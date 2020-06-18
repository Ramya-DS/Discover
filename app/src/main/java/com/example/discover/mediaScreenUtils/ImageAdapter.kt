package com.example.discover.mediaScreenUtils

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
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
        if (list != null) {
            if (isBackdrop) {
                getWidth()?.let {
//                    displayBackdropPlaceholder(holder.image, it, (it / 1.77777).toInt())
                    LoadBackdrop(WeakReference(holder.image), activity).executeOnExecutor(
                        AsyncTask.THREAD_POOL_EXECUTOR,
                        list[position].file_path,
                        it.toString(), true.toString()
                    )
                }
            } else {
//                displayPosterPlaceHolder(holder.image)
                holder.image.scaleType = ImageView.ScaleType.FIT_XY
                LoadPoster(
                    WeakReference(holder.image),
                    activity
                ).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, list[position].file_path)
            }
        }
//        else {
//            Log.d("enter null", "")
//            if (isBackdrop)
//                displayBackdropPlaceholder(
//                    holder.image,
//                    getWidth()!!,
//                    (getWidth()!! / 1.77777).toInt()
//                )
//            else
//            {
//                displayPosterPlaceHolder(holder.image)
//            }
//        }
    }

    private fun getWidth(): Int? {
        val displayMetrics: DisplayMetrics? = activity.get()?.resources?.displayMetrics
        return displayMetrics?.widthPixels
    }

//    private fun displayBackdropPlaceholder(imageView: ImageView, width: Int, height: Int) {
//        val drawable =
//            ContextCompat.getDrawable(imageView.context, R.drawable.ic_backdrop_placeholder)
//        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//
//        val canvas = Canvas(bitmap)
//        drawable?.setBounds(0, 0, canvas.width, canvas.height)
//        drawable?.draw(canvas);
//        imageView.setImageBitmap(bitmap)
//    }
//
//    private fun displayPosterPlaceHolder(imageView: ImageView) {
//        val drawable = ContextCompat.getDrawable(imageView.context, R.drawable.ic_media_placeholder)
//        val bitmap = Bitmap.createBitmap(80, 100, Bitmap.Config.ARGB_8888)
//        val canvas = Canvas(bitmap)
//        drawable!!.setBounds(10, 20, canvas.width - 10, canvas.height - 20)
//        drawable.draw(canvas)
//        imageView.setImageBitmap(bitmap)
//    }
}