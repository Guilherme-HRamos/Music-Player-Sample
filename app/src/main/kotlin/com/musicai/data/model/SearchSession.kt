package com.musicai.data.model

data class SearchSession(
    val query: String,
    val buffer: List<SongEntity> = emptyList(),
    val hasReachedEnd: Boolean = false,
)