package com.musicai.ui.utils.fakes

import com.musicai.domain.model.Song
import com.musicai.domain.usecase.SaveRecentSongUseCase

class FakeSaveRecentSongUseCase : SaveRecentSongUseCase {
    var invokeCalls = 0
    var lastSong: Song? = null
    
    private var error: Throwable? = null

    fun setError(e: Throwable) {
        error = e
    }

    override suspend fun invoke(song: Song) {
        invokeCalls++
        lastSong = song
        error?.let { throw it }
    }
}
