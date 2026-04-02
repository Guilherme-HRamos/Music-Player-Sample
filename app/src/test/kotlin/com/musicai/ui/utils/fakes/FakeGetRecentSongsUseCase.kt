package com.musicai.ui.utils.fakes

import com.musicai.domain.model.Song
import com.musicai.domain.usecase.GetRecentSongsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal sealed class FakeGetRecentSongsUseCase : GetRecentSongsUseCase {
    data class Success(var songs: List<Song> = emptyList()) : FakeGetRecentSongsUseCase() {
        override fun invoke(limit: Int): Flow<List<Song>> = flowOf(songs.take(limit))
    }

    data object Empty : FakeGetRecentSongsUseCase() {
        override fun invoke(limit: Int): Flow<List<Song>> = flowOf(emptyList())
    }
}
