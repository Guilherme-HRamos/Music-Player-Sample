package com.musicai.data.repository

import com.musicai.data.api.local.RecentSongDao
import com.musicai.data.api.local.SongDao
import com.musicai.data.api.remote.ItunesApiService
import com.musicai.data.model.RecentSongEntity
import com.musicai.data.model.SearchSession
import com.musicai.domain.model.PaginatedSearch
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

    private var session: SearchSession? = null
    private var isFetching = false

    override suspend fun searchSongs(query: String, page: Int): Result<PaginatedSearch> {
        if (session?.query != query || page == 1) {
            session = SearchSession(query = query)
        }

        val current = session!!

        // Check if page is already in buffer
        val cached = current.pageAt(page)
        if (cached != null) {
            if (isLastPageOfSlot(page) && !current.hasReachedEnd && !isFetching) {
                prefetchNextSlot(query)
            }
            return Result.success(cached)
        }

        // Page not in buffer — fetch from API
        return fetchNextSlot(query)
            .map {
                val updated = session!!
                if (isLastPageOfSlot(page) && !updated.hasReachedEnd && !isFetching) {
                    prefetchNextSlot(query)
                }
                updated.pageAt(page) ?: PaginatedSearch(songs = emptyList(), hasMore = false)
            }
            .recoverCatching { error ->
                val offset = (page - 1) * DISPLAY_PAGE_SIZE
                val cachedSongs = dao.searchCached(query, limit = DISPLAY_PAGE_SIZE, offset = offset)
                    .map { it.toDomain() }

                if (cachedSongs.isNotEmpty()) {
                    PaginatedSearch(songs = cachedSongs, hasMore = false)
                } else {
                    throw error
                }
            }
    }

    private suspend fun fetchNextSlot(query: String): Result<Unit> = runCatching {
        if (isFetching) return@runCatching  // Already fetching, skip duplicate

        isFetching = true
        try {
            val current = session ?: throw IllegalStateException("Session missing")
            val newLimit = current.buffer.size + FETCH_SLOT_SIZE
            val response = api.searchSongs(term = query, limit = newLimit)

            // Only update if session is still the same query
            if (session?.query != query) return@runCatching

            val newEntities = response.results
                .filter { it.kind == "song" }
                .distinctBy { it.trackId }  // Deduplicate within response
                .map { it.toEntity(query = query) }
                .filter { entity -> current.buffer.none { it.trackId == entity.trackId } }  // Deduplicate against buffer

            session = current.copy(
                buffer = current.buffer + newEntities,
                hasReachedEnd = response.resultCount < newLimit,
            )
            dao.upsertAll(newEntities)
        } finally {
            isFetching = false
        }
    }

    private suspend fun prefetchNextSlot(query: String) {
        if (!canPrefetch()) return
        fetchNextSlot(query)
    }

    private fun canPrefetch(): Boolean {
        val current = session ?: return false
        return !isFetching && !current.hasReachedEnd && current.buffer.size < MAX_CACHED_ITEMS
    }

    private fun isLastPageOfSlot(page: Int): Boolean = page % PAGES_PER_SLOT == 0

    private fun SearchSession.pageAt(page: Int): PaginatedSearch? {
        val offset = (page - 1) * DISPLAY_PAGE_SIZE
        if (offset >= buffer.size) return null  // Page doesn't exist yet

        val songs = buffer.drop(offset).take(DISPLAY_PAGE_SIZE).map { it.toDomain() }
        if (songs.isEmpty()) return null

        // hasMore = true if: more items in buffer OR API may have more results
        val hasMore = (offset + DISPLAY_PAGE_SIZE < buffer.size) || !hasReachedEnd
        return PaginatedSearch(songs = songs, hasMore = hasMore)
    }

    override fun getRecentSongs(limit: Int): Flow<List<Song>> =
            recentDao.getRecentlyPlayed(limit).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getAlbumSongs(collectionId: Long): Result<List<Song>> = runCatching {
        val response = api.lookupAlbum(collectionId)
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

    companion object {
        private const val DISPLAY_PAGE_SIZE = 20
        private const val FETCH_SLOT_SIZE = 40
        private const val PAGES_PER_SLOT = 2
        private const val MAX_CACHED_ITEMS = 200
    }
}
