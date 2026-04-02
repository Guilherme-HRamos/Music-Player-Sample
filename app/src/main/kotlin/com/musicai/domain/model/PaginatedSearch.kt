package com.musicai.domain.model

data class PaginatedSearch(
    val songs: List<Song>,
    val hasMore: Boolean,
)