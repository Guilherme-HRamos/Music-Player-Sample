package com.musicai.fakes

import com.musicai.domain.model.Song
import com.musicai.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSongRepository : SongRepository {

    var searchSongsResult: Result<List<Song>> = Result.success(emptyList())
    var albumSongsResult: Result<List<Song>> = Result.success(emptyList())
    val recentSongsFlow = MutableStateFlow<List<Song>>(emptyList())

    val searchCallArgs = mutableListOf<Pair<String, Int>>() // (query, page)
    val markedAsPlayed = mutableListOf<Song>()
    var albumSongsCallId: Long? = null

    override suspend fun searchSongs(query: String, page: Int): Result<List<Song>> {
        searchCallArgs.add(query to page)
        return searchSongsResult
    }

    override fun getRecentSongs(limit: Int): Flow<List<Song>> = recentSongsFlow

    override suspend fun getAlbumSongs(collectionId: Long): Result<List<Song>> {
        albumSongsCallId = collectionId
        return albumSongsResult
    }

    override suspend fun markAsPlayed(song: Song) {
        markedAsPlayed.add(song)
    }
}
