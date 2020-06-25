package com.example.discover.loginScreen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.discover.R
import com.example.discover.mediaScreenUtils.InfoClass

class LoginDetailsAdapter(private val detailsList: List<InfoClass>) :
    RecyclerView.Adapter<LoginDetailsAdapter.LoginDetailsViewHolder>() {

    class LoginDetailsViewHolder(loginDetailsView: View) :
        RecyclerView.ViewHolder(loginDetailsView) {

        val imageView: ImageView = loginDetailsView.findViewById(R.id.login_viewpager_image)
        val text: TextView = loginDetailsView.findViewById(R.id.login_viewpager_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = LoginDetailsViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.login_viewpager_layout,
            parent,
            false
        )
    )

    override fun getItemCount() = detailsList.size

    override fun onBindViewHolder(holder: LoginDetailsViewHolder, position: Int) {
        val detail = detailsList[position]
        holder.imageView.setImageDrawable(
            ContextCompat.getDrawable(
                holder.itemView.context,
                detail.image
            )
        )
        holder.text.text = detail.title
    }
}