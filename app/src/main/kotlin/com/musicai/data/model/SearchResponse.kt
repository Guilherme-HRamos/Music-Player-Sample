package com.musicai.data.model

import com.google.gson.annotations.SerializedName

data class SearchResponse(
    @SerializedName("resultCount") val resultCount: Int = 0,
    @SerializedName("results") val results: List<SongResponse> = emptyList(),
)
