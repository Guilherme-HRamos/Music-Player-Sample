package com.musicai.data.utils.fakes

import com.musicai.data.api.local.SongDao
import com.musicai.data.model.SongEntity

internal class FakeSongDao : SongDao {
    var upsertAllCalls = 0
        private set
    var lastUpsertedSongs: List<SongEntity>? = null
        private set

    val buffer = mutableListOf<SongEntity>()

    override suspend fun upsertAll(songs: List<SongEntity>) {
        upsertAllCalls++
        lastUpsertedSongs = songs
        // Simple logic to replace or add
        songs.forEach { song ->
            buffer.removeIf { it.trackId == song.trackId }
            buffer.add(song)
        }
    }

    var searchCachedCalls = 0
        private set

    override suspend fun searchCached(query: String, limit: Int, offset: Int): List<SongEntity> {
        searchCachedCalls++
        return buffer.filter { it.searchQuery == query }
            .sortedBy { it.trackName }
            .drop(offset)
            .take(limit)
    }

    var getAlbumSongsCalls = 0
        private set

    override suspend fun getAlbumSongs(collectionId: Long): List<SongEntity> {
        getAlbumSongsCalls++
        return buffer.filter { it.collectionId == collectionId }
            .sortedBy { it.trackName }
    }
}
