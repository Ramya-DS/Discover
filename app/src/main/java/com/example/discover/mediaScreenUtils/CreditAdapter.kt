package com.example.discover.mediaScreenUtils

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.discover.R
import com.example.discover.datamodel.credit.cast.Cast
import com.example.discover.datamodel.credit.crew.Crew
import com.example.discover.util.LoadPosterImage
import java.lang.ref.WeakReference

class CreditAdapter(
    private val isCrew: Boolean,
    private val activity: WeakReference<Activity>
) : RecyclerView.Adapter<CreditAdapter.CreditViewHolder>() {

    var crewList = emptyList<Crew>()
    var castList = emptyList<Cast>()

    inner class CreditViewHolder(creditView: View) : RecyclerView.ViewHolder(creditView) {
        var imageTask: LoadPosterImage? = null
        val image: ImageView = creditView.findViewById(R.id.credit_image)
        val name: TextView = creditView.findViewById(R.id.credit_name)
        val job: TextView = creditView.findViewById(R.id.credit_job)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = CreditViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.media_credit_layout,
            parent,
            false
        )
    )

    override fun getItemCount() = if (isCrew) crewList.size else castList.size

    override fun onBindViewHolder(holder: CreditViewHolder, position: Int) {
        if (isCrew) {
            val crew = crewList[position]
            setPlaceHolder(holder.image)
            if (crew.profile_path != null) {
                holder.imageTask?.interruptThread()
                holder.imageTask =
                    LoadPosterImage(crew.profile_path, holder.image, activity).apply {
                        loadImage(100, 100, false)
                    }
            }
            holder.name.text = crew.name
            holder.job.text = crew.job
        } else {
            val cast = castList[position]

            setPlaceHolder(holder.image)
            if (cast.profile_path != null) {
                holder.imageTask?.interruptThread()
                holder.imageTask =
                    LoadPosterImage(cast.profile_path, holder.image, activity).apply {
                        loadImage(100, 100, false)
                    }
            }
            holder.name.text = cast.name
            holder.job.text = cast.character
        }

    }

    private fun setPlaceHolder(imageView: ImageView) {
        val drawable =
            ContextCompat.getDrawable(imageView.context, R.drawable.ic_credit_placeholder)
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)
        drawable?.setBounds(0, 0, canvas.width, canvas.height)
        drawable?.draw(canvas);
        imageView.setImageBitmap(bitmap)

    }

    override fun onViewRecycled(holder: CreditViewHolder) {
        super.onViewRecycled(holder)
        holder.imageTask?.interruptThread()
    }
}