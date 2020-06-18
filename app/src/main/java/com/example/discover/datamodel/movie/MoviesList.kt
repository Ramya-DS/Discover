package com.example.discover.datamodel.movie

import com.example.discover.datamodel.movie.preview.MoviePreview
import com.google.gson.annotations.SerializedName

data class MoviesList(
    @SerializedName("page") val page: Int,
    @SerializedName("results") val results: List<MoviePreview>
)