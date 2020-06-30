package com.example.discover.showsScreen

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.discover.DiscoverApplication
import com.example.discover.R
import com.example.discover.datamodel.credit.Credit
import com.example.discover.datamodel.credit.cast.Cast
import com.example.discover.datamodel.credit.crew.Crew
import com.example.discover.datamodel.images.ImageDetails
import com.example.discover.datamodel.images.Images
import com.example.discover.datamodel.tvshow.detail.Episode
import com.example.discover.datamodel.tvshow.detail.Season
import com.example.discover.mediaScreenUtils.CreditAdapter
import com.example.discover.mediaScreenUtils.ImageAdapter
import com.example.discover.mediaScreenUtils.InfoDialogFragment
import com.example.discover.searchScreen.OnNetworkLostListener
import com.example.discover.util.ExpandableTextView
import com.example.discover.util.NetworkSnackbar
import com.example.discover.util.NoSwipeBehavior
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.abs

class SeasonActivity : AppCompatActivity(), OnCreditSelectedListener, OnNetworkLostListener {

    private var showId: Int = 0
    private lateinit var showName: String

    private var flag = 1
    private var handler: Handler? = null
    private var timer: TimerTask? = null

    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var poster: ViewPager2
    private lateinit var tabLayout: TabLayout

    private lateinit var overview: ExpandableTextView

    private lateinit var viewModel: SeasonViewModel

    private lateinit var currentSeason: Season

    private var loadingDialog: AlertDialog? = null
    private var refreshDialog: AlertDialog? = null
    private var snackBar: NetworkSnackbar? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_season)
        createDialog(false)
        loadingDialog?.show()

        fetchIntentData()

        val toolbar: MaterialToolbar = findViewById(R.id.season_toolbar)
        setSupportActionBar(toolbar)

        val text = "$showName ${currentSeason.name}"
        supportActionBar?.title = text

        toolbar.setNavigationOnClickListener {
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }


        bindActivity(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.show_detail_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.refresh) {
            if ((application as DiscoverApplication).checkConnectivity()) {
                flag = 1
                timer?.cancel()
                createDialog(true)
                refreshDialog?.show()
                loadDetailsFromNetwork()
            } else onNetworkDialog()
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    private fun bindActivity(savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(SeasonViewModel::class.java)

        viewModel.onNetworkLostListener = this

        coordinatorLayout = findViewById(R.id.season_coordinator)
        poster = findViewById(R.id.season_poster)
        overview = findViewById(R.id.season_overview)

        tabLayout = findViewById(R.id.season_tab_layout)



        if (savedInstanceState == null) {
            loadDetailsFromNetwork()
        }
    }

    private fun loadDetailsFromNetwork() {
        viewModel.seasonDetails(showId, currentSeason.season_number).observe(this, Observer {
            flag = flag shl 1
            setSeasonDetails(it)
        })
        viewModel.fetchCreditDetails(showId, currentSeason.season_number)
            .observe(this, Observer {
                flag = flag shl 1
                setCreditDetails(it)
            })
        viewModel.fetchImages(showId, currentSeason.season_number).observe(this, Observer {
            flag = flag shl 1
            setImages(it)
        })
    }

    private fun fetchIntentData() {
        intent?.apply {
            showId = getIntExtra("show id", 0)
            showName = getStringExtra("show name")!!
            currentSeason = getParcelableExtra("season")!!
        }
    }

    private fun setSeasonDetails(season: Season) {
        setEpisode(season.episodes)

        val overviewHeading: TextView = findViewById(R.id.season_overview_heading)
        if (season.overview != null && season.overview!!.isEmpty()) {
            overview.visibility = View.GONE
            overviewHeading.visibility = View.GONE
        } else overview.text = season.overview
    }

    private fun setCreditDetails(credit: Credit) {
        val castHeading: TextView = findViewById(R.id.season_cast_heading)
        val crewHeading: TextView = findViewById(R.id.season_crew_heading)

        val cast: RecyclerView = findViewById(R.id.season_cast)
        val crew: RecyclerView = findViewById(R.id.season_crew)

        if (credit.crew.isEmpty()) {
            crewHeading.visibility = View.GONE
            crew.visibility = View.GONE
        } else {
            crew.layoutManager = GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false)
            crew.setHasFixedSize(true)
            crew.adapter = CreditAdapter(true, WeakReference(this)).apply {
                crewList = credit.crew
            }
            crew.scrollToPosition(viewModel.crewPosition)
        }

        if (credit.cast.isEmpty()) {
            castHeading.visibility = View.GONE
            cast.visibility = View.GONE
        } else {
            cast.layoutManager = GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false)
            cast.setHasFixedSize(true)
            cast.adapter = CreditAdapter(false, WeakReference(this)).apply {
                castList = credit.cast
            }
            cast.scrollToPosition(viewModel.castPosition)
        }
    }

    private fun setEpisode(episodes: List<Episode>) {
        val episodeHeading: TextView = findViewById(R.id.season_episode_heading)
        val episodeList: RecyclerView = findViewById(R.id.season_episode)

        if (episodes.isEmpty()) {
            episodeHeading.visibility = View.GONE
            episodeList.visibility = View.GONE

        } else {
            episodeList.layoutManager = LinearLayoutManager(this)
            episodeList.setHasFixedSize(false)
            episodeList.adapter = EpisodeAdapter(episodes, WeakReference(this), this)
            episodeList.scrollToPosition(viewModel.episodePosition)
        }
    }

    override fun onCrewSelected(crew: List<Crew>) {
        if (crew.isEmpty()) {
            Snackbar.make(
                findViewById(android.R.id.content),
                "Oops! Crew details are empty",
                Snackbar.LENGTH_LONG
            ).show()
            return
        }
        InfoDialogFragment.newCrewInstance(crew, "Crew").show(supportFragmentManager, "Crew")
    }

    override fun onCastSelected(cast: List<Cast>) {
        if (cast.isEmpty()) {
            Snackbar.make(
                findViewById(android.R.id.content),
                "Oops! Guest Stars details are empty",
                Snackbar.LENGTH_LONG
            ).show()
            return
        }
        InfoDialogFragment.newCastInstance(cast, "Guest Stars").show(supportFragmentManager, "Cast")
    }

    private fun setImages(images: Images) {
        poster.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        poster.setPageTransformer { page, position ->
            when {
                position < -1 ->
                    page.alpha = 0.1f
                position <= 1 -> {
                    page.alpha = 0.2f.coerceAtLeast(1 - abs(position))
                }
                else -> page.alpha = 0.1f
            }
        }

        var total = 0

        if (images.posters != null && images.posters.isNotEmpty()) {
            total = images.posters.size
            poster.adapter = ImageAdapter(false, images.posters, WeakReference(this))
        } else if (currentSeason.poster_path != null) {
            total = 1
            poster.adapter = ImageAdapter(
                false, listOf(ImageDetails(0.0, currentSeason.poster_path!!)),
                WeakReference(this)
            )
        } else
            poster.adapter = ImageAdapter(false, null, WeakReference(this))

        TabLayoutMediator(tabLayout, poster) { tab, _ ->
            poster.setCurrentItem(tab.position, true)
        }.attach()
        automaticPageChange(total)

        refreshDialog?.dismiss()
        loadingDialog?.dismiss()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        viewModel.seasonDetails?.let {
            setSeasonDetails(it)
        }
        viewModel.credit?.let {
            setCreditDetails(it)
        }
        viewModel.images?.let {
            setImages(it)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        (findViewById<RecyclerView>(R.id.season_cast).layoutManager)?.apply {
            viewModel.castPosition =
                (this as GridLayoutManager).findFirstCompletelyVisibleItemPosition()
        }

        (findViewById<RecyclerView>(R.id.season_crew).layoutManager)?.apply {
            viewModel.crewPosition =
                (this as GridLayoutManager).findFirstCompletelyVisibleItemPosition()
        }

        (findViewById<RecyclerView>(R.id.season_episode).layoutManager)?.apply {
            viewModel.episodePosition =
                (this as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun onNetworkLostFragment() {

    }

    override fun onNetworkDialog() {
        snackBar = if ((application as DiscoverApplication).checkConnectivity()) {
            if (flag < 8)
                NetworkSnackbar.make(
                    coordinatorLayout,
                    "Poor Network Detected. Please check connectivity and refresh the screen"
                ).setBehavior(NoSwipeBehavior())
            else null
        } else
            NetworkSnackbar.make(coordinatorLayout).setBehavior(NoSwipeBehavior())

        snackBar?.show()
    }

    override fun onNetworkDialogDismiss() {
        snackBar?.dismiss()
    }

    private fun automaticPageChange(total: Int) {
        var currentPage = 0

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position?.plus(1) != currentPage)
                    tab?.position?.plus(1)?.let {
                        currentPage = it
                    }
            }

        })

        handler = Handler()

        val update = Runnable {
            if (currentPage == total) {
                return@Runnable
            }
            poster.setCurrentItem(currentPage++, true)
        }

        timer = object : TimerTask() {
            override fun run() {
                handler?.post(update)
            }
        }
        Timer().schedule(timer, 500, 4000)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler?.removeCallbacksAndMessages(null)
    }

    private fun createDialog(isRefreshing: Boolean) {
        if (isRefreshing && refreshDialog == null) {
            val view =
                LayoutInflater.from(this).inflate(R.layout.loading_dialog, coordinatorLayout, false)
            view.findViewById<TextView>(R.id.loading_dialog_message).text = "Refreshing..."
            refreshDialog = AlertDialog.Builder(this).setView(view).create()
        } else if (loadingDialog == null)
            loadingDialog = AlertDialog.Builder(this).setView(R.layout.loading_dialog).create()
    }
}
