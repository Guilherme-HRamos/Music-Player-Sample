package com.musicai.ui.songs.model

import com.musicai.domain.model.Song

data class SongsState(
    val query: String = "",
    val isSearchActive: Boolean = false,
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = true,
    val currentPage: Int = 0,
    val selectedSong: Song? = null,
)