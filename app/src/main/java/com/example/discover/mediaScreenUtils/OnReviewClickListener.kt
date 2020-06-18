package com.example.discover.mediaScreenUtils

import com.example.discover.datamodel.review.Review

interface OnReviewClickListener {
    fun onReviewClicked(review: Review)
}