package com.example.discover.datamodel.images

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "ImageData")
data class ImageDetails(
    @SerializedName("aspect_ratio")
    var aspect_ratio: Double,

    @SerializedName("file_path")
    var file_path: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    var media_id: Int = 0
}
