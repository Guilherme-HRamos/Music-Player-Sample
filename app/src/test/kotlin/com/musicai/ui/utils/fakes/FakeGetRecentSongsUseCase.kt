package com.musicai.ui.utils.fakes

import com.musicai.domain.model.Song
import com.musicai.domain.usecase.GetRecentSongsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeGetRecentSongsUseCase : GetRecentSongsUseCase {
    var invokeCalls = 0
    var lastLimit: Int? = null

    private val _songsFlow = MutableStateFlow<List<Song>>(emptyList())

    fun setSuccess(songs: List<Song>) {
        _songsFlow.value = songs
    }

    override fun invoke(limit: Int): Flow<List<Song>> {
        invokeCalls++
        lastLimit = limit
        return _songsFlow.asStateFlow().map { it.take(limit) }
    }
}
