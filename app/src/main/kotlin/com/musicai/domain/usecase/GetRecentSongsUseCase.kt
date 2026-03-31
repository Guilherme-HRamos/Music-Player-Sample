package com.musicai.domain.usecase

import com.musicai.domain.model.Song
import com.musicai.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface GetRecentSongsUseCase {
    operator fun invoke(limit: Int = 20): Flow<List<Song>>
}

class GetRecentSongsUseCaseImpl @Inject constructor(
    private val repository: SongRepository,
) : GetRecentSongsUseCase {
    override fun invoke(limit: Int): Flow<List<Song>> =
            repository.getRecentSongs(limit)
}
