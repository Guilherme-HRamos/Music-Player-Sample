package com.musicai.fakes

import com.musicai.domain.model.Song
import com.musicai.domain.usecase.GetRecentSongsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeGetRecentSongsUseCase : GetRecentSongsUseCase {

    val songsFlow = MutableStateFlow<List<Song>>(emptyList())

    override fun invoke(limit: Int): Flow<List<Song>> = songsFlow
}
