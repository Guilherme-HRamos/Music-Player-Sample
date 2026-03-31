package com.musicai.ui.player.model

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicai.domain.model.Song
import com.musicai.domain.usecase.SaveRecentSongUseCase
import com.musicai.ui.shared.PlayerController
import com.musicai.ui.songs.model.SongsNavigationEvent
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
    fun onViewAlbum()
}

@HiltViewModel
class PlayerViewModelImpl @Inject constructor(
    private val playerController: PlayerController,
    private val saveRecentSong: SaveRecentSongUseCase,
) : ViewModel(), PlayerViewModel {

    private val _state = MutableStateFlow(PlayerState())
    override val state = _state.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<PlayerNavigationEvent>()
    override val navigationEvents = _navigationEvents.asSharedFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null

    init {
        val song = playerController.currentSong
        if (song != null) {
            _state.update {
                it.copy(
                    song = song,
                    hasNext = playerController.hasNext,
                    hasPrevious = playerController.hasPrevious,
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

        _state.update { it.copy(isPreparing = true, error = null) }

        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build(),
            )
            setDataSource(url)
            prepareAsync()
            setOnPreparedListener { mp ->
                _state.update {
                    it.copy(durationMs = mp.duration.toLong(), isPreparing = false)
                }
                mp.start()
                _state.update { it.copy(isPlaying = true) }
                startProgressTracking()
                viewModelScope.launch { saveRecentSong(song) }
            }
            setOnCompletionListener {
                stopProgressTracking()
                _state.update { it.copy(isPlaying = false, currentPositionMs = 0L) }
            }
            setOnErrorListener { _, _, _ ->
                _state.update {
                    it.copy(isPreparing = false, error = "Could not play this track")
                }
                true
            }
        }
    }

    override fun onPlayPause() {
        val mp = mediaPlayer ?: return
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
        mediaPlayer?.seekTo(positionMs.toInt())
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
        mediaPlayer?.release()
        mediaPlayer = null
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
                delay(500)
                val mp = mediaPlayer ?: break
                if (mp.isPlaying) {
                    _state.update { it.copy(currentPositionMs = mp.currentPosition.toLong()) }
                }
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }

    override fun onViewAlbum() {
        viewModelScope.launch {
            _state.value.song?.run {
                _navigationEvents.emit(PlayerNavigationEvent.NavigateToAlbum(collectionId))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopProgressTracking()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}