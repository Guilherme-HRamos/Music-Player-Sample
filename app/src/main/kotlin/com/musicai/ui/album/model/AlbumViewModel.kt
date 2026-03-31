package com.musicai.ui.album.model

import androidx.lifecycle.ViewModel
import com.musicai.utils.logWip
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

interface AlbumViewModel {
    val state: StateFlow<AlbumState>
    fun onRetry()
}

@HiltViewModel
class AlbumViewModelImpl @Inject constructor(): ViewModel(), AlbumViewModel {
    private val _state = MutableStateFlow(AlbumState(isLoading = true))
    override val state = _state.asStateFlow()

    override fun onRetry() {
        logWip("onRetry called")
    }
}