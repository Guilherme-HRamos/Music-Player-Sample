package com.musicai.ui.album.model

import androidx.lifecycle.ViewModel
import com.musicai.utils.logWip
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface AlbumViewModel {
    val state: StateFlow<AlbumState>
    fun onRetry()
}

class AlbumViewModelImpl : ViewModel(), AlbumViewModel {
    private val _state = MutableStateFlow(AlbumState(isLoading = true))
    override val state = _state.asStateFlow()

    override fun onRetry() {
        logWip("onRetry called")
    }
}