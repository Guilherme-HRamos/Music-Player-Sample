package com.musicai.data.utils.fakes

import com.musicai.data.api.local.RecentSongDao
import com.musicai.data.model.RecentSongEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeRecentSongDao : RecentSongDao {
    var upsertCalls = 0
        private set
    var lastUpsertedSong: RecentSongEntity? = null
        private set

    private val _recentSongs = MutableStateFlow<List<RecentSongEntity>>(emptyList())

    override suspend fun upsert(song: RecentSongEntity) {
        upsertCalls++
        lastUpsertedSong = song
        val current = _recentSongs.value.toMutableList()
        current.removeIf { it.trackId == song.trackId }
        current.add(0, song) // Add to top (recent)
        _recentSongs.value = current
    }

    override fun getRecentlyPlayed(limit: Int): Flow<List<RecentSongEntity>> {
        return _recentSongs.map { it.take(limit) }
    }
}
