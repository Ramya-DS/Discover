package com.example.discover.datamodel.review

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Entity(
    tableName = "Review"
)
@Parcelize
data class Review(
    @PrimaryKey
    @SerializedName("id")
    var id: String = " ",

    @SerializedName("author") var author: String,

    @SerializedName("content") var content: String,

    @SerializedName("url") var url: String

) : Parcelable {
    var media_id: Int = 0
}
