package com.musicai.fakes

import com.musicai.data.api.local.SongDao
import com.musicai.data.model.SongEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSongDao : SongDao {

    val upsertedSongs = mutableListOf<SongEntity>()
    var cachedSongs: List<SongEntity> = emptyList()
    val recentFlow = MutableStateFlow<List<SongEntity>>(emptyList())
    var albumSongs: List<SongEntity> = emptyList()
    val updatedTimestamps = mutableListOf<Pair<Long, Long>>() // (trackId, timestamp)

    override suspend fun upsertAll(songs: List<SongEntity>) {
        upsertedSongs.addAll(songs)
    }

    override suspend fun searchCached(query: String): List<SongEntity> = cachedSongs

    override fun getRecentlyPlayed(limit: Int): Flow<List<SongEntity>> = recentFlow

    override suspend fun updateLastPlayed(trackId: Long, timestamp: Long) {
        updatedTimestamps.add(trackId to timestamp)
    }

    override suspend fun getAlbumSongs(collectionId: Long): List<SongEntity> = albumSongs
}
