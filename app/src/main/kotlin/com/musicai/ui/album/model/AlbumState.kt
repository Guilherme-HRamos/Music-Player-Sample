package com.musicai.ui.album.model

import com.musicai.domain.Album

data class AlbumState(
    val album: Album? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)