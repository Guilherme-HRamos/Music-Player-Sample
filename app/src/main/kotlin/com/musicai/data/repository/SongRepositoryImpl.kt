package com.musicai.data.repository

import com.musicai.data.api.local.SongDao
import com.musicai.data.network.ItunesApiService
import com.musicai.domain.model.Song
import com.musicai.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SongRepositoryImpl @Inject constructor(
    private val api: ItunesApiService,
    private val dao: SongDao,
) : SongRepository {

    override suspend fun searchSongs(query: String, page: Int): Result<List<Song>> = runCatching {
        val offset = page * ItunesApiService.PAGE_SIZE
        val response = api.searchSongs(term = query, offset = offset)
        val songs = response.results.filter { it.kind == "song" }
        dao.upsertAll(songs.map { it.toEntity(query = query) })
        songs.map { it.toDomain() }
    }.recoverCatching {
        // Offline fallback: return cached results for this query
        dao.searchCached(query).map { entity -> entity.toDomain() }.also { cached ->
            if (cached.isEmpty()) throw it
        }
    }

    override fun getRecentSongs(limit: Int): Flow<List<Song>> =
            dao.getRecentlyPlayed(limit).map { entities -> entities.map { it.toDomain() } }

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
        dao.updateLastPlayed(
            trackId = song.trackId,
            timestamp = System.currentTimeMillis(),
        )
    }
}
