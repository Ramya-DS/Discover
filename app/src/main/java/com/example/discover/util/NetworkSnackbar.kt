package com.example.discover.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.example.discover.R
import com.google.android.material.snackbar.BaseTransientBottomBar

class NetworkSnackbar(
    parent: ViewGroup,
    content: NetworkSnackBarView
) : BaseTransientBottomBar<NetworkSnackbar>(parent, content, content) {

    init {
        getView().setBackgroundColor(
            ContextCompat.getColor(
                view.context,
                android.R.color.background_dark
            )
        )
        getView().setPadding(0, 0, 0, 0)
    }

    companion object {

        fun make(view: View): NetworkSnackbar {
            val parent = view.findSuitableParent() ?: throw IllegalArgumentException(
                "No suitable parent found from the given view. Please provide a valid view."
            )
            val customView = LayoutInflater.from(view.context).inflate(
                R.layout.layout_network_snackbar,
                parent,
                false
            ) as NetworkSnackBarView

            return NetworkSnackbar(
                parent,
                customView
            )
        }

    }

}