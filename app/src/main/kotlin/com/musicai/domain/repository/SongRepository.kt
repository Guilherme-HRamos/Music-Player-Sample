package com.musicai.domain.repository

import com.musicai.domain.model.PaginatedSearch
import com.musicai.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    suspend fun searchSongs(query: String, page: Int): Result<PaginatedSearch>
    suspend fun refreshSearch(query: String): Result<PaginatedSearch>
    fun getRecentSongs(limit: Int = 20): Flow<List<Song>>
    suspend fun getAlbumSongs(collectionId: Long): Result<List<Song>>
    suspend fun markAsPlayed(song: Song)
}
