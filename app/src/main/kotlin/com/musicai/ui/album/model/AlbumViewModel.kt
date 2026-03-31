package com.musicai.ui.album.model

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicai.domain.usecase.GetAlbumSongsUseCase
import com.musicai.utils.logWip
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

interface AlbumViewModel {
    val state: StateFlow<AlbumState>
    fun onRetry()
}

@HiltViewModel
class AlbumViewModelImpl @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getAlbumSongs: GetAlbumSongsUseCase,
) : ViewModel(), AlbumViewModel {

    private val collectionId: Long = checkNotNull(savedStateHandle["collectionId"])

    private val _state = MutableStateFlow(AlbumState(isLoading = true))
    override val state = _state.asStateFlow()

    init {
        loadAlbum()
    }

    private fun loadAlbum() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            getAlbumSongs(collectionId)
                .onSuccess { album ->
                    _state.update { it.copy(isLoading = false, album = album) }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(isLoading = false, error = e.message ?: "Could not load album")
                    }
                }
        }
    }

    override fun onRetry() = loadAlbum()
}