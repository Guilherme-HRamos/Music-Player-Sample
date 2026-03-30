package com.musicai.ui.album.model

import kotlinx.coroutines.flow.StateFlow

interface AlbumViewModel {
    val state: StateFlow<AlbumState>
    fun onRetry()
}
