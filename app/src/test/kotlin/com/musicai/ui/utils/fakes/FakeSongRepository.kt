package com.musicai.ui.utils.fakes

import com.musicai.domain.model.PaginatedSearch
import com.musicai.domain.model.Song
import com.musicai.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal sealed class FakeSongRepository : SongRepository {
    var searchSongsCalls = 0
        private set
    var lastSearchQuery: String? = null
        private set
    var lastSearchPage: Int? = null
        private set

    protected fun trackSearch(query: String, page: Int) {
        searchSongsCalls++
        lastSearchQuery = query
        lastSearchPage = page
    }

    var getRecentSongsCalls = 0
        private set
    var lastRecentSongsLimit: Int? = null
        private set

    protected fun trackRecent(limit: Int) {
        getRecentSongsCalls++
        lastRecentSongsLimit = limit
    }

    var getAlbumSongsCalls = 0
        private set
    var lastAlbumCollectionId: Long? = null
        private set

    protected fun trackAlbum(collectionId: Long) {
        getAlbumSongsCalls++
        lastAlbumCollectionId = collectionId
    }

    var markAsPlayedCalls = 0
        private set
    var lastMarkedSong: Song? = null
        private set

    protected fun trackMarkAsPlayed(song: Song) {
        markAsPlayedCalls++
        lastMarkedSong = song
    }

    data class Success(
        var songs: List<Song> = emptyList(),
        var paginatedSearch: PaginatedSearch = PaginatedSearch(emptyList(), false),
        var recentSongs: List<Song> = emptyList()
    ) : FakeSongRepository() {
        override suspend fun searchSongs(query: String, page: Int): Result<PaginatedSearch> {
            trackSearch(query, page)
            return Result.success(paginatedSearch)
        }

        override fun getRecentSongs(limit: Int): Flow<List<Song>> {
            trackRecent(limit)
            return flowOf(recentSongs.take(limit))
        }

        override suspend fun getAlbumSongs(collectionId: Long): Result<List<Song>> {
            trackAlbum(collectionId)
            return Result.success(songs)
        }

        override suspend fun markAsPlayed(song: Song) {
            trackMarkAsPlayed(song)
        }
    }

    data class Empty(
        var paginatedSearch: PaginatedSearch = PaginatedSearch(emptyList(), false)
    ) : FakeSongRepository() {
        override suspend fun searchSongs(query: String, page: Int): Result<PaginatedSearch> {
            trackSearch(query, page)
            return Result.success(paginatedSearch)
        }

        override fun getRecentSongs(limit: Int): Flow<List<Song>> {
            trackRecent(limit)
            return flowOf(emptyList())
        }

        override suspend fun getAlbumSongs(collectionId: Long): Result<List<Song>> {
            trackAlbum(collectionId)
            return Result.success(emptyList())
        }

        override suspend fun markAsPlayed(song: Song) {
            trackMarkAsPlayed(song)
        }
    }

    data class Error(val throwable: Throwable = IllegalArgumentException()) : FakeSongRepository() {
        override suspend fun searchSongs(query: String, page: Int): Result<PaginatedSearch> {
            trackSearch(query, page)
            return Result.failure(throwable)
        }

        override fun getRecentSongs(limit: Int): Flow<List<Song>> {
            trackRecent(limit)
            return flowOf(emptyList())
        }

        override suspend fun getAlbumSongs(collectionId: Long): Result<List<Song>> {
            trackAlbum(collectionId)
            return Result.failure(throwable)
        }

        override suspend fun markAsPlayed(song: Song) {
            trackMarkAsPlayed(song)
        }
    }
}
