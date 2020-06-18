package com.example.discover.datamodel.credit.cast

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "Cast")
@Parcelize
data class Cast(

    @SerializedName("character")
    var character: String,

    @PrimaryKey
    @SerializedName("id")
    var id: Int,

    @SerializedName("name")
    var name: String,

    @SerializedName("order")
    var order: Int,

    @SerializedName("profile_path")
    var profile_path: String?

) : Parcelable {
    @IgnoredOnParcel
    var media_id: Int = 0
}