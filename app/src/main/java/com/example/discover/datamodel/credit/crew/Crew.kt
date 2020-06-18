package com.example.discover.datamodel.credit.crew

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "Crew")
@Parcelize
data class Crew(

    @SerializedName("department")
    var department: String,

    @PrimaryKey
    @SerializedName("id")
    var id: Int,

    @SerializedName("job")
    var job: String,

    @SerializedName("name")
    var name: String,

    @SerializedName("profile_path")
    var profile_path: String?

) : Parcelable {
    @IgnoredOnParcel
    var media_id: Int = 0

    constructor(id: Int, name: String, profile_path: String?) : this(
        " ",
        id,
        " ",
        name,
        profile_path
    )
}