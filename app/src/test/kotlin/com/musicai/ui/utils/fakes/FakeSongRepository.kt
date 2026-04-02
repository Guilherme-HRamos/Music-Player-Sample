package com.musicai.ui.utils.fakes

import com.musicai.domain.model.PaginatedSearch
import com.musicai.domain.model.Song
import com.musicai.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal sealed class FakeSongRepository : SongRepository {
    data class Success(
        var songs: List<Song> = emptyList(),
        var paginatedSearch: PaginatedSearch = PaginatedSearch(emptyList(), false),
        var recentSongs: List<Song> = emptyList()
    ) : FakeSongRepository() {
        override suspend fun searchSongs(query: String, page: Int): Result<PaginatedSearch> =
            Result.success(paginatedSearch)

        override fun getRecentSongs(limit: Int): Flow<List<Song>> =
            flowOf(recentSongs.take(limit))

        override suspend fun getAlbumSongs(collectionId: Long): Result<List<Song>> =
            Result.success(songs)

        override suspend fun markAsPlayed(song: Song) {
            // No-op or update recentSongs if needed for specific tests
        }
    }

    data object Empty : FakeSongRepository() {
        override suspend fun searchSongs(query: String, page: Int): Result<PaginatedSearch> =
            Result.success(PaginatedSearch(emptyList(), false))

        override fun getRecentSongs(limit: Int): Flow<List<Song>> =
            flowOf(emptyList())

        override suspend fun getAlbumSongs(collectionId: Long): Result<List<Song>> =
            Result.success(emptyList())

        override suspend fun markAsPlayed(song: Song) {}
    }

    data class Error(val throwable: Throwable = IllegalArgumentException()) : FakeSongRepository() {
        override suspend fun searchSongs(query: String, page: Int): Result<PaginatedSearch> =
            Result.failure(throwable)

        override fun getRecentSongs(limit: Int): Flow<List<Song>> =
            flowOf(emptyList())

        override suspend fun getAlbumSongs(collectionId: Long): Result<List<Song>> =
            Result.failure(throwable)

        override suspend fun markAsPlayed(song: Song) {}
    }
}
