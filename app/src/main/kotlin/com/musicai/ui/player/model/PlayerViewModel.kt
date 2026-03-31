package com.musicai.ui.player.model

import androidx.lifecycle.ViewModel
import com.musicai.ui.songs.model.SongsViewModel
import com.musicai.utils.logWip
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface PlayerViewModel {
    val state: StateFlow<PlayerState>
    fun onPlayPause()
    fun onNext()
    fun onPrevious()
    fun onSeek(positionMs: Long)
}

class PlayerViewModelImpl : ViewModel(), PlayerViewModel {

    private val _state = MutableStateFlow(PlayerState())
    override val state = _state.asStateFlow()

    override fun onPlayPause() {
        logWip("onPlayPause called")
    }

    override fun onNext() {
        logWip("onNext called")
    }

    override fun onPrevious() {
        logWip("onPrevious called")
    }

    override fun onSeek(positionMs: Long) {
        logWip("onSeek called with positionMs: $positionMs")
    }
}