package com.example.discover.datamodel.genre

import com.google.gson.annotations.SerializedName

data class GenreResult (
    @SerializedName("genres") val genres: List<Genres>
)