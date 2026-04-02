package com.musicai.ui.album.model

import androidx.annotation.StringRes
import com.musicai.R
import com.musicai.domain.model.Album
import com.musicai.ui.shared.ErrorState

data class AlbumState(
    val album: Album? = null,
    val isLoading: Boolean = false,
    val error: ErrorState? = null,
)

sealed class AlbumErrorState(@StringRes message: Int): ErrorState(message) {
    data object NoConnection : AlbumErrorState(R.string.no_internet_connection)
}