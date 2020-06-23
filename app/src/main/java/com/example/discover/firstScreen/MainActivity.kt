package com.example.discover.firstScreen

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.DisplayMetrics
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.discover.DiscoverApplication
import com.example.discover.R
import com.example.discover.category.MediaCategoryActivity
import com.example.discover.filterScreen.ui.FilterActivity
import com.example.discover.genreScreen.GenreMediaActivity
import com.example.discover.searchScreen.SearchActivity
import com.example.discover.util.LoadPosterImage
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.appbar.MaterialToolbar
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity(), OnGenreSelectedListener {

    private lateinit var collapsingToolbarLayout: CollapsingToolbarLayout
    private lateinit var toolbar: MaterialToolbar
    private lateinit var searchView: SearchView
    private lateinit var filterText: TextView
    private lateinit var movieCard: ImageView
    private lateinit var showCard: ImageView
    private lateinit var movieGenreList: RecyclerView
    private lateinit var showGenreList: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindActivity()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.outlineProvider = null
        }

        searchInitiation()
        discoverFilter()
        mediaCardInitiation()
        movieGenres()
        showGenres()
    }

    private fun bindActivity() {
        collapsingToolbarLayout = findViewById(R.id.main_collapsing_layout)
        toolbar = findViewById(R.id.main_toolbar)
        setSupportActionBar(toolbar)
        searchView = findViewById(R.id.main_searchView)
        searchView.clearFocus()
        filterText = findViewById(R.id.main_discover_filter)
        movieCard = findViewById(R.id.main_movies_image)

        movieCard.setOnClickListener {
            startActivity(Intent(this, MediaCategoryActivity::class.java).apply {
                putExtra("isMovie", true)
            })
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
        showCard = findViewById(R.id.main_shows_image)
        showCard.setOnClickListener {
            startActivity(Intent(this, MediaCategoryActivity::class.java).apply {
                putExtra("isMovie", false)
            })
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
        movieGenreList = findViewById(R.id.main_genres_movies)
        showGenreList = findViewById(R.id.main_genres_shows)
    }

    private fun searchInitiation() {
        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {

                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this@MainActivity,
                    searchView,
                    "searchBox"
                )
                startActivity(
                    Intent(this@MainActivity, SearchActivity::class.java),
                    options.toBundle()
                )
            }
        }
    }

    private fun discoverFilter() {
        highlightText(filterText)
    }

    private fun highlightText(textView: TextView) {
        val text = textView.text
        val spannableString = SpannableString(text)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@MainActivity, FilterActivity::class.java))
                overridePendingTransition(R.anim.right_in, R.anim.left_out)
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

    private fun mediaCardInitiation() {
        LoadPosterImage("orjiB3oUIsyz60hoEqkiGpy5CeO.jpg", movieCard, WeakReference(this)).apply {
            loadImage(getWidth(), false)
        }

        LoadPosterImage("56v2KjBlU4XaOv9rVYEQypROD7P.jpg", showCard, WeakReference(this)).apply {
            loadImage(getWidth(), false)
        }
    }

    private fun getWidth(): Int {
        val displayMetrics: DisplayMetrics? = resources.displayMetrics
        return displayMetrics!!.widthPixels
    }

    private fun movieGenres() {
        movieGenreList.layoutManager =
            GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false)
        movieGenreList.setHasFixedSize(true)
        val adapter = GenreAdapter(
            default = false,
            isGenre = true,
            isMovie = true,
            onGenreSelectedListener = this
        )
        movieGenreList.adapter = adapter

        (application as DiscoverApplication).movieGenres.observe(this, Observer {
            adapter.setGenresList(it)
        })
    }

    private fun showGenres() {
        showGenreList.layoutManager =
            GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false)
        showGenreList.setHasFixedSize(true)
        val adapter = GenreAdapter(
            default = false,
            isGenre = true,
            isMovie = false,
            onGenreSelectedListener = this
        )
        showGenreList.adapter = adapter

        (application as DiscoverApplication).showsGenres.observe(this, Observer {
            adapter.setGenresList(it)
        })
    }

    override fun onGenreSelected(isMovie: Boolean, genreId: Int) {
        startActivity(Intent(this, GenreMediaActivity::class.java).apply {
            putExtra("isMovie", isMovie)
            putExtra("id", genreId)
        })
        overridePendingTransition(R.anim.right_in, R.anim.left_out)
    }

    override fun onResume() {
        super.onResume()
        searchView.clearFocus()
    }


}
