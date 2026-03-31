package com.musicai.ui.player.model

import com.musicai.domain.model.Song

data class PlayerState(
    val song: Song? = null,
    val isPlaying: Boolean = false,
    val isPreparing: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false,
    val error: String? = null,
)