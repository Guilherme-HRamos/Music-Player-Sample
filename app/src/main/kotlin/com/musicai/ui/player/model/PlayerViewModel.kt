package com.musicai.ui.player.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicai.domain.model.Song
import com.musicai.domain.usecase.SaveRecentSongUseCase
import com.musicai.plugin.audioPlayer.AudioPlayer
import com.musicai.plugin.audioPlayer.AudioPlayerFactory
import com.musicai.plugin.utils.ConnectivityChecker
import com.musicai.ui.shared.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

interface PlayerViewModel {
    val state: StateFlow<PlayerState>
    val navigationEvents: SharedFlow<PlayerNavigationEvent>

    fun onPlayPause()
    fun onNext()
    fun onPrevious()
    fun onSeek(positionMs: Long)
    fun onToggleLoop()
    fun onViewAlbum()
    fun onModalDismissed()
    fun onMoreClick()
}

@HiltViewModel
class PlayerViewModelImpl @Inject constructor(
    private val playerController: PlayerController,
    private val saveRecentSong: SaveRecentSongUseCase,
    private val audioPlayerFactory: AudioPlayerFactory,
    private val connectivityChecker: ConnectivityChecker,
) : ViewModel(), PlayerViewModel {

    private val _state = MutableStateFlow(PlayerState())
    override val state = _state.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<PlayerNavigationEvent>()
    override val navigationEvents = _navigationEvents.asSharedFlow()

    private var audioPlayer: AudioPlayer? = null
    private var progressJob: Job? = null

    init {
        val song = playerController.currentSong
        if (song != null) {
            _state.update {
                it.copy(
                    song = song,
                    hasNext = playerController.hasNext,
                    hasPrevious = playerController.hasPrevious,
                    shouldDisplayModal = false
                )
            }
            initializePlayer(song)
        }
    }

    private fun initializePlayer(song: Song) {
        val url = song.previewUrl
        if (url.isNullOrBlank()) {
            _state.update { it.copy(error = "Preview not available for this track") }
            return
        }

        if (!connectivityChecker.isInternetAvailable()) {
            viewModelScope.launch {
                _navigationEvents.emit(PlayerNavigationEvent.NoConnectionError)
            }
            _state.update { it.copy(error = "No internet connection") }
            return
        }

        _state.update { it.copy(isPreparing = true, error = null) }

        audioPlayer?.release()
        audioPlayer = audioPlayerFactory.create().also { player ->
            player.setDataSource(url)
            player.setOnPreparedListener { mp ->
                _state.update {
                    it.copy(durationMs = mp.duration.toLong(), isPreparing = false)
                }
                mp.start()
                _state.update { it.copy(isPlaying = true) }
                startProgressTracking()
                viewModelScope.launch { saveRecentSong(song) }
            }
            player.setOnCompletionListener {
                if (_state.value.loopEnabled) {
                    it.seekTo(0)
                    it.start()
                    _state.update { s -> s.copy(currentPositionMs = 0L) }
                } else {
                    stopProgressTracking()
                    _state.update { s -> s.copy(isPlaying = false, currentPositionMs = 0L) }
                    onNext()
                }
            }
            player.setOnErrorListener { _, _, _ ->
                viewModelScope.launch {
                    _navigationEvents.emit(PlayerNavigationEvent.GenericError)
                }
                _state.update {
                    it.copy(isPreparing = false, error = "Could not play this track")
                }
                true
            }
            player.prepareAsync()
        }
    }

    override fun onPlayPause() {
        val mp = audioPlayer ?: return
        if (mp.isPlaying) {
            mp.pause()
            stopProgressTracking()
            _state.update { it.copy(isPlaying = false) }
        } else {
            mp.start()
            _state.update { it.copy(isPlaying = true) }
            startProgressTracking()
        }
    }

    override fun onSeek(positionMs: Long) {
        audioPlayer?.seekTo(positionMs.toInt())
        _state.update { it.copy(currentPositionMs = positionMs) }
    }

    override fun onNext() {
        val nextSong = playerController.next() ?: return
        switchSong(nextSong)
    }

    override fun onPrevious() {
        val prevSong = playerController.previous() ?: return
        switchSong(prevSong)
    }

    private fun switchSong(song: Song) {
        stopProgressTracking()
        audioPlayer?.release()
        audioPlayer = null
        _state.update {
            it.copy(
                song = song,
                isPlaying = false,
                currentPositionMs = 0L,
                durationMs = 0L,
                hasNext = playerController.hasNext,
                hasPrevious = playerController.hasPrevious,
            )
        }
        initializePlayer(song)
    }

    private fun startProgressTracking() {
        stopProgressTracking()
        progressJob = viewModelScope.launch {
            while (true) {
                val mp = audioPlayer ?: break

                if (mp.isPlaying) {
                    _state.update { it.copy(currentPositionMs = mp.currentPosition.toLong()) }
                }

                delay(500)
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }

    override fun onToggleLoop() {
        _state.update { it.copy(loopEnabled = !it.loopEnabled) }
    }

    override fun onViewAlbum() {
        onModalDismissed()
        viewModelScope.launch {
            _state.value.song?.run {
                _navigationEvents.emit(PlayerNavigationEvent.NavigateToAlbum(collectionId))
            }
        }
    }

    override fun onModalDismissed() {
        _state.update { it.copy(shouldDisplayModal = false) }
    }

    override fun onMoreClick() {
        _state.update { it.copy(shouldDisplayModal = true) }
    }

    override fun onCleared() {
        super.onCleared()
        stopProgressTracking()
        audioPlayer?.release()
        audioPlayer = null
    }
}
