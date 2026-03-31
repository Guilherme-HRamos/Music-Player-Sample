package com.musicai.ui.player.model

import androidx.lifecycle.ViewModel
import com.musicai.ui.songs.model.SongsViewModel
import com.musicai.utils.logWip
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

interface PlayerViewModel {
    val state: StateFlow<PlayerState>
    fun onPlayPause()
    fun onNext()
    fun onPrevious()
    fun onSeek(positionMs: Long)
}

@HiltViewModel
class PlayerViewModelImpl @Inject constructor(): ViewModel(), PlayerViewModel {

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