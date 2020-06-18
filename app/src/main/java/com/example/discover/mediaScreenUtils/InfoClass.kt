package com.example.discover.mediaScreenUtils

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class InfoClass(
    val image: Int,
    val content: String?,
    val title: String
) : Parcelable