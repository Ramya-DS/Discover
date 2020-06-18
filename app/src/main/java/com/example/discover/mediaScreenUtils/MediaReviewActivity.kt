package com.example.discover.mediaScreenUtils

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.discover.R
import com.example.discover.datamodel.review.Review
import com.google.android.material.appbar.MaterialToolbar

class MediaReviewActivity : AppCompatActivity() {

    lateinit var review: Review

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_review)
        receiveIntent()

        bindToolbar()

        bindReviewDetails()

    }

    private fun receiveIntent() {
        review = intent.getParcelableExtra("review")!!
    }

    private fun bindToolbar() {
        val toolbar: MaterialToolbar = findViewById(R.id.review_toolbar)

        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun bindReviewDetails() {
        val author: TextView = findViewById(R.id.review_author)
        author.text = review.author

        val link: TextView = findViewById(R.id.review_link)
        link.text = highlightUrl()
        link.movementMethod = LinkMovementMethod.getInstance()

        val content: TextView = findViewById(R.id.review_content)
        content.text = review.content
    }

    private fun highlightUrl(): SpannableString {
        val spannableString = SpannableString(review.url)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(review.url))
                startActivity(browserIntent)
            }
        }
        spannableString.setSpan(
            clickableSpan,
            0,
            review.url.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannableString
    }
}
