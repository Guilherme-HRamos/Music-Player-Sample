package com.musicai.ui.album.model

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicai.R
import com.musicai.domain.usecase.GetAlbumSongsUseCase
import com.musicai.plugin.utils.ConnectivityChecker
import com.musicai.plugin.utils.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

interface AlbumViewModel {
    val state: StateFlow<AlbumState>
    val navigationEvents: SharedFlow<AlbumNavigationEvent>

    fun onRetry()
}

@HiltViewModel
class AlbumViewModelImpl @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getAlbumSongs: GetAlbumSongsUseCase,
    private val connectivityChecker: ConnectivityChecker,
    private val logger: Logger,
) : ViewModel(), AlbumViewModel {

    private val collectionId: Long = checkNotNull(savedStateHandle["collectionId"])

    private val _state = MutableStateFlow(AlbumState(isLoading = true))
    override val state = _state.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<AlbumNavigationEvent>(replay = 1)
    override val navigationEvents = _navigationEvents.asSharedFlow()

    init {
        loadAlbum()
    }

    private fun loadAlbum() {
        if (!connectivityChecker.isInternetAvailable()) {
            logger.error("No internet connection while loading album (collectionId: $collectionId)")
            viewModelScope.launch {
                _navigationEvents.emit(AlbumNavigationEvent.NoConnectionError)
                _state.update { it.copy(isLoading = false, error = AlbumErrorState.NoConnection) }
            }
            return
        }

        logger.info("Loading album with collectionId: $collectionId")
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            getAlbumSongs(collectionId)
                .onSuccess { album ->
                    logger.debug("Album loaded successfully. Songs count: ${album.songs.size}")
                    _state.update { it.copy(isLoading = false, album = album) }
                }
                .onFailure { e ->
                    logger.error("Failed to load album: ${e.message}", e)
                    _navigationEvents.emit(AlbumNavigationEvent.ShowError(R.string.album_load_error))
                    _state.update { it.copy(isLoading = false) }
                }
        }
    }

    override fun onRetry() = loadAlbum()
}