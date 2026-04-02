package com.musicai.domain.usecase

import com.musicai.domain.model.PaginatedSearch
import com.musicai.domain.repository.SongRepository
import javax.inject.Inject

interface SearchSongsUseCase {
    suspend operator fun invoke(query: String, page: Int): Result<PaginatedSearch>
}

class SearchSongsUseCaseImpl @Inject constructor(
    private val repository: SongRepository,
) : SearchSongsUseCase {
    override suspend fun invoke(query: String, page: Int): Result<PaginatedSearch> =
        repository.searchSongs(query, page)
}
