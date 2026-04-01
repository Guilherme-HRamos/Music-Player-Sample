package com.musicai.fakes

import com.musicai.domain.model.Song
import com.musicai.domain.usecase.SaveRecentSongUseCase

class FakeSaveRecentSongUseCase : SaveRecentSongUseCase {

    val savedSongs = mutableListOf<Song>()

    override suspend fun invoke(song: Song) {
        savedSongs.add(song)
    }
}
