package com.example.discover.datamodel

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "Language")
data class Language(
    @PrimaryKey
    @SerializedName("iso_639_1")
    val isoId: String,

    @SerializedName("english_name")
    val name: String
) : Parcelable