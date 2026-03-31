package com.musicai.ui.player.model

import kotlinx.coroutines.flow.StateFlow

interface PlayerViewModel {
    val state: StateFlow<PlayerState>
    fun onPlayPause()
    fun onNext()
    fun onPrevious()
    fun onSeek(positionMs: Long)
}
