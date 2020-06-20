package com.example.discover.showsScreen

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.discover.DiscoverApplication
import com.example.discover.datamodel.credit.Credit
import com.example.discover.datamodel.images.Images
import com.example.discover.datamodel.tvshow.detail.Season
import com.example.discover.searchScreen.OnNetworkLostListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SeasonViewModel(private val mApplication: Application) : AndroidViewModel(mApplication) {

    var credit: Credit? = null
    var seasonDetails: Season? = null
    var images: Images? = null

    var castPosition = 0
    var crewPosition = 0
    var episodePosition = 0

    var onNetworkLostListener: OnNetworkLostListener? = null

    fun fetchCreditDetails(showId: Int, seasonNumber: Int): LiveData<Credit> {
        val call = getSeasonApiCall().credits(showId, seasonNumber)
        return sendCreditsRequest(call)
    }

    private fun sendCreditsRequest(call: Call<Credit>): LiveData<Credit> {
        val credit = MutableLiveData<Credit>()
        call.enqueue(object : Callback<Credit> {
            override fun onFailure(call: Call<Credit>, t: Throwable) {
                onNetworkLostListener?.onNetworkDialog()
            }

            override fun onResponse(call: Call<Credit>, response: Response<Credit>) {
                if (response.isSuccessful) {
                    onNetworkLostListener?.onNetworkDialogDismiss()
                    this@SeasonViewModel.credit = response.body()!!
                    credit.postValue(response.body()!!)
                } else Log.d(
                    "ShowDetail",
                    "Error ${(mApplication as DiscoverApplication).fetchErrorMessage(response.errorBody()!!)}"
                )
            }

        })
        return credit
    }

    fun seasonDetails(showId: Int, seasonNumber: Int): LiveData<Season> {
        val call = getSeasonApiCall().seasonDetails(showId, seasonNumber)
        return sendDetailsRequest(call)
    }

    private fun sendDetailsRequest(call: Call<Season>): LiveData<Season> {
        val season = MutableLiveData<Season>()

        call.enqueue(object : Callback<Season> {
            override fun onFailure(call: Call<Season>, t: Throwable) {
                onNetworkLostListener?.onNetworkDialog()
            }

            override fun onResponse(call: Call<Season>, response: Response<Season>) {
                if (response.isSuccessful) {
                    onNetworkLostListener?.onNetworkDialogDismiss()
                    this@SeasonViewModel.seasonDetails = response.body()!!
                    season.postValue(response.body()!!)
                } else Log.d(
                    "ShowDetail",
                    "Error ${(mApplication as DiscoverApplication).fetchErrorMessage(response.errorBody()!!)}"
                )
            }

        })
        return season
    }

    fun fetchImages(showId: Int, seasonNumber: Int): LiveData<Images> {
        val call = getSeasonApiCall().getImages(showId, seasonNumber)
        return sendImagesRequest(call)
    }

    private fun sendImagesRequest(call: Call<Images>): LiveData<Images> {
        val images = MutableLiveData<Images>()
        call.enqueue(object : Callback<Images> {
            override fun onFailure(call: Call<Images>, t: Throwable) {
                onNetworkLostListener?.onNetworkDialog()
            }

            override fun onResponse(call: Call<Images>, response: Response<Images>) {
                if (response.isSuccessful) {
                    onNetworkLostListener?.onNetworkDialogDismiss()
                    this@SeasonViewModel.images = response.body()!!
                    images.postValue(response.body()!!)
                } else Log.d(
                    "SeasonDetail",
                    "Error ${(mApplication as DiscoverApplication).fetchErrorMessage(response.errorBody()!!)}"
                )
            }
        })
        return images
    }

    private fun getSeasonApiCall() = (mApplication as DiscoverApplication).seasonApiCall
}
