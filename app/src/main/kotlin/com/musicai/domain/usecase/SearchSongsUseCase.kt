package com.musicai.domain.usecase

import com.musicai.domain.model.Song
import com.musicai.domain.repository.SongRepository
import javax.inject.Inject

interface SearchSongsUseCase {
    suspend operator fun invoke(query: String, page: Int): Result<List<Song>>
}

class SearchSongsUseCaseImpl @Inject constructor(
    private val repository: SongRepository,
) : SearchSongsUseCase {
    override suspend fun invoke(query: String, page: Int): Result<List<Song>> =
            repository.searchSongs(query, page)
}
