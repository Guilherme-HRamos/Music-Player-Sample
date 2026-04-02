package com.musicai.ui.utils.fakes

import com.musicai.domain.model.Song
import com.musicai.domain.usecase.SaveRecentSongUseCase

internal sealed class FakeSaveRecentSongUseCase : SaveRecentSongUseCase {
    data object Success : FakeSaveRecentSongUseCase() {
        override suspend fun invoke(song: Song) {
            // No-op
        }
    }

    data class Error(val throwable: Throwable = IllegalArgumentException()) : FakeSaveRecentSongUseCase() {
        override suspend fun invoke(song: Song) {
            throw throwable
        }
    }
}
