package com.musicai.domain.usecase

import com.musicai.domain.model.PaginatedSearch
import com.musicai.domain.model.Song
import com.musicai.domain.repository.SongRepository
import javax.inject.Inject

interface SearchSongsUseCase {
    suspend operator fun invoke(
        query: String,
        page: Int,
        previousResults: List<Song> = emptyList(),
    ): Result<PaginatedSearch>
}

class SearchSongsUseCaseImpl @Inject constructor(
    private val repository: SongRepository,
) : SearchSongsUseCase {
    override suspend fun invoke(
        query: String,
        page: Int,
        previousResults: List<Song>,
    ): Result<PaginatedSearch> =
        repository.searchSongs(query, page).map { newSongs ->
            // Deduplicate against previousResults
            val dedupedSongs = newSongs.filter { newSong ->
                previousResults.none { it.trackId == newSong.trackId }
            }

            // If API returned items but none are new, or returned empty list, no more pages
            val hasMoreData = newSongs.isNotEmpty() && dedupedSongs.isNotEmpty()
            val hasMore = if (hasMoreData) newSongs.size >= 20 else false

            PaginatedSearch(
                songs = dedupedSongs,
                currentPage = page,
                hasMore = hasMore,
            )
        }
}
