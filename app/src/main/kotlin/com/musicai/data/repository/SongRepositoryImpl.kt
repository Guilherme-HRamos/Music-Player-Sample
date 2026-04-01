package com.musicai.data.repository

import com.musicai.data.api.local.RecentSongDao
import com.musicai.data.api.local.SongDao
import com.musicai.data.model.RecentSongEntity
import com.musicai.data.api.remote.ItunesApiService
import com.musicai.domain.model.Song
import com.musicai.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SongRepositoryImpl @Inject constructor(
    private val api: ItunesApiService,
    private val dao: SongDao,
    private val recentDao: RecentSongDao,
) : SongRepository {

    override suspend fun searchSongs(query: String, page: Int): Result<List<Song>> = runCatching {
        val offset = page * ItunesApiService.PAGE_SIZE
        val response = api.searchSongs(term = query, offset = offset)
        val songs = response.results.filter { it.kind == "song" }
        dao.upsertAll(songs.map { it.toEntity(query = query) })
        songs.map { it.toDomain() }
    }.recoverCatching {
        // Offline fallback: return cached results for this query (respecting pagination)
        val offset = page * ItunesApiService.PAGE_SIZE
        dao.searchCached(query, limit = ItunesApiService.PAGE_SIZE, offset = offset)
            .map { entity -> entity.toDomain() }
            .also { cached -> if (cached.isEmpty()) throw it }
    }

    override fun getRecentSongs(limit: Int): Flow<List<Song>> =
            recentDao.getRecentlyPlayed(limit).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getAlbumSongs(collectionId: Long): Result<List<Song>> = runCatching {
        val response = api.lookupAlbum(collectionId)
        // The first result is the collection itself; the rest are tracks
        val songs = response.results.filter { it.kind == "song" }
        dao.upsertAll(songs.map { it.toEntity() })
        songs.map { it.toDomain() }
    }.recoverCatching {
        dao.getAlbumSongs(collectionId).map { entity -> entity.toDomain() }.also { cached ->
            if (cached.isEmpty()) throw it
        }
    }

    override suspend fun markAsPlayed(song: Song) {
        recentDao.upsert(
            RecentSongEntity(
                trackId = song.trackId,
                trackName = song.trackName,
                artistName = song.artistName,
                collectionName = song.collectionName,
                collectionId = song.collectionId,
                artworkUrl = song.artworkUrl,
                previewUrl = song.previewUrl,
                trackTimeMillis = song.trackTimeMillis,
                playedAt = System.currentTimeMillis(),
            )
        )
    }
}
