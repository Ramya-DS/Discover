package com.example.discover.datamodel.multiSearch

import com.google.gson.annotations.SerializedName

data class MultiSearchResult(
    @SerializedName("page") val page: Int,
    @SerializedName("results") val results: List<MultiSearch>,
    @SerializedName("total_pages") val total_pages: Int,
    @SerializedName("total_results") val total_results: Int
)