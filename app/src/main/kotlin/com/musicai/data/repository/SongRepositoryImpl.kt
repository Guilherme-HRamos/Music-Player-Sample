package com.musicai.data.repository

import com.musicai.data.api.local.RecentSongDao
import com.musicai.data.api.local.SongDao
import com.musicai.data.api.remote.ItunesApiService
import com.musicai.data.model.SearchSession
import com.musicai.data.model.toRecentEntity
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

    // --- Search ---

    override suspend fun searchSongs(query: String, page: Int): Result<PaginatedSearch> {
        if (session?.query != query || page == 1) {
            session = SearchSession(query = query)
        }

        // Serve from buffer if available; otherwise, fetch from API first
        val buffered = session!!.pageAt(page)
        if (buffered == null) {
            fetchNextSlot(query).onFailure { error ->
                return recoverFromCache(query, page, error)
            }
        }

        prefetchIfNeeded(query, page)
        return Result.success(session!!.pageAt(page) ?: PaginatedSearch(emptyList(), false))
    }

    override suspend fun refreshSearch(query: String): Result<PaginatedSearch> {
        session = null
        dao.clearSearchCache(query)
        return searchSongs(query, page = 1)
    }

    private suspend fun fetchNextSlot(query: String): Result<Unit> = runCatching {
        if (isFetching) return@runCatching

        isFetching = true
        try {
            val current = session ?: throw IllegalStateException("Session missing")
            val limit = current.buffer.size + FETCH_SLOT_SIZE
            val response = api.searchSongs(term = query, limit = limit)

            if (session?.query != query) return@runCatching // Query changed mid-flight

            val newEntities = response.results
                .filter { it.kind == "song" }
                .distinctBy { it.trackId }                                          // Deduplicate within response
                .map { it.toEntity(query = query) }
                .filter { new -> current.buffer.none { it.trackId == new.trackId } } // Deduplicate against buffer

            session = current.copy(
                buffer = current.buffer + newEntities,
                hasReachedEnd = response.resultCount < limit,
            )
            dao.upsertAll(newEntities)
        } finally {
            isFetching = false
        }
    }

    private suspend fun prefetchIfNeeded(query: String, page: Int) {
        val current = session ?: return
        val shouldPrefetch = !isFetching
            && !current.hasReachedEnd
            && current.buffer.size < MAX_CACHED_ITEMS
            && page % PAGES_PER_SLOT == 0

        if (shouldPrefetch) fetchNextSlot(query)
    }

    private suspend fun recoverFromCache(query: String, page: Int, cause: Throwable): Result<PaginatedSearch> {
        val offset = (page - 1) * DISPLAY_PAGE_SIZE
        val cached = dao.searchCached(query, limit = DISPLAY_PAGE_SIZE, offset = offset)
            .map { it.toDomain() }
        return if (cached.isNotEmpty()) {
            Result.success(PaginatedSearch(songs = cached, hasMore = false))
        } else {
            Result.failure(cause)
        }
    }

    // --- Album ---

    override suspend fun getAlbumSongs(collectionId: Long): Result<List<Song>> = runCatching {
        val response = api.lookupAlbum(collectionId)
        val songs = response.results.filter { it.kind == "song" }
        dao.upsertAll(songs.map { it.toEntity() })
        songs.map { it.toDomain() }
    }.recoverCatching { error ->
        val cached = dao.getAlbumSongs(collectionId).map { it.toDomain() }
        cached.ifEmpty { throw error }
    }

    // --- Recent ---

    override fun getRecentSongs(limit: Int): Flow<List<Song>> =
        recentDao.getRecentlyPlayed(limit).map { entities -> entities.map { it.toDomain() } }

    override suspend fun markAsPlayed(song: Song) {
        recentDao.upsert(song.toRecentEntity())
    }

    companion object {
        private const val DISPLAY_PAGE_SIZE = 20
        private const val FETCH_SLOT_SIZE = 40
        private const val PAGES_PER_SLOT = 2
        private const val MAX_CACHED_ITEMS = 200
    }
}
