package com.musicai.domain.usecase

import com.musicai.domain.model.Song
import com.musicai.domain.repository.SongRepository
import javax.inject.Inject

interface SaveRecentSongUseCase {
    suspend operator fun invoke(song: Song)
}

class SaveRecentSongUseCaseImpl @Inject constructor(
    private val repository: SongRepository,
) : SaveRecentSongUseCase {
    override suspend fun invoke(song: Song) = repository.markAsPlayed(song)
}
