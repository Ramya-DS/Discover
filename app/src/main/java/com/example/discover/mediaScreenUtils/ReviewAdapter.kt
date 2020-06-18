package com.example.discover.mediaScreenUtils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.discover.R
import com.example.discover.datamodel.review.Review

class ReviewAdapter(
    private val reviews: List<Review>,
    private val onReviewClickListener: OnReviewClickListener
) :
    RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(reviewView: View) : RecyclerView.ViewHolder(reviewView),
        View.OnClickListener {
        init {
            reviewView.setOnClickListener(this)
        }

        var review: Review? = null
        val author: TextView = reviewView.findViewById(R.id.review_author)
        val content: TextView = reviewView.findViewById(R.id.review_content)

        override fun onClick(v: View?) {
            review?.let {
                onReviewClickListener.onReviewClicked(it)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ReviewViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.media_review_layout,
            parent,
            false
        )
    )

    override fun getItemCount() = reviews.size

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.review = review
        holder.author.text = review.author
        holder.content.text = review.content
    }
}