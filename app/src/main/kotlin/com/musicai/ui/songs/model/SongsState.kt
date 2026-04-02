package com.musicai.ui.songs.model

import androidx.annotation.StringRes
import com.musicai.R
import com.musicai.domain.model.Song
import com.musicai.ui.shared.ErrorState

data class SongsState(
    val query: String = "",
    val isSearchActive: Boolean = false,
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: ErrorState? = null,
    val hasMore: Boolean = true,
    val selectedSong: Song? = null,
)

sealed class SongsErrorState(@StringRes message: Int): ErrorState(message) {
    data object RefreshFailed : SongsErrorState(R.string.generic_error)
    data object NoConnection : SongsErrorState(R.string.no_internet_connection)
}