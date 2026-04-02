package com.musicai.data.repository

import com.musicai.data.api.local.RecentSongDao
import com.musicai.data.api.local.SongDao
import com.musicai.data.api.remote.ItunesApiService
import com.musicai.data.model.SearchSession
import com.musicai.data.model.toRecentEntity
import com.musicai.domain.model.PaginatedSearch
import com.musicai.domain.model.Song
import com.musicai.domain.repository.SongRepository
import com.musicai.plugin.utils.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SongRepositoryImpl @Inject constructor(
    private val api: ItunesApiService,
    private val dao: SongDao,
    private val recentDao: RecentSongDao,
    private val logger: Logger,
) : SongRepository {

    private var session: SearchSession? = null
    private var isFetching = false

    // --- Search ---

    override suspend fun searchSongs(query: String, page: Int): Result<PaginatedSearch> {
        logger.info("Searching songs with query: '$query' - Page: $page")

        if (session?.query != query || page == 1) {
            logger.debug("Creating new search session for query: '$query'")
            session = SearchSession(query = query)
        }

        // Serve from buffer if available; otherwise, fetch from API first
        val buffered = session!!.pageAt(page)
        if (buffered == null) {
            logger.debug("Page $page not in buffer, fetching from API")
            fetchNextSlot(query).onFailure { error ->
                logger.warn("Failed to fetch from API, attempting to recover from cache for page $page")
                return recoverFromCache(query, page, error)
            }
        } else {
            logger.debug("Page $page found in buffer - Songs: ${buffered.songs.size}")
        }

        prefetchIfNeeded(query, page)
        val result = session!!.pageAt(page) ?: PaginatedSearch(emptyList(), false)
        logger.debug("Search result for '$query' page $page - Songs: ${result.songs.size}, HasMore: ${result.hasMore}")
        return Result.success(result)
    }

    override suspend fun refreshSearch(query: String): Result<PaginatedSearch> {
        logger.info("Refreshing search for query: '$query'")
        session = null
        dao.clearSearchCache(query)
        logger.debug("Cache cleared for query: '$query'")
        return searchSongs(query, page = 1)
    }

    private suspend fun fetchNextSlot(query: String): Result<Unit> = runCatching {
        if (isFetching) {
            logger.debug("Already fetching, skipping fetch for query: '$query'")
            return@runCatching
        }

        isFetching = true
        try {
            val current = session ?: throw IllegalStateException("Session missing")
            val limit = current.buffer.size + FETCH_SLOT_SIZE
            logger.debug("Fetching next slot for query: '$query' - Limit: $limit")

            val response = api.searchSongs(term = query, limit = limit)
            logger.debug("API response received - Total results: ${response.resultCount}")

            if (session?.query != query) {
                logger.debug("Query changed mid-flight, skipping update")
                return@runCatching // Query changed mid-flight
            }

            val newEntities = response.results
                .filter { it.kind == "song" }
                .distinctBy { it.trackId } // Deduplicate within response
                .map { it.toEntity(query = query) }
                .filter { new -> current.buffer.none { it.trackId == new.trackId } } // Deduplicate against buffer

            logger.debug("New entities to cache: ${newEntities.size} - HasReachedEnd: ${response.resultCount < limit}")
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
        logger.warn("Recovering from cache for query: '$query' page $page - Original error: ${cause.message}")
        val offset = (page - 1) * DISPLAY_PAGE_SIZE
        val cached = dao.searchCached(query, limit = DISPLAY_PAGE_SIZE, offset = offset)
            .map { it.toDomain() }
        return if (cached.isNotEmpty()) {
            logger.info("Cache recovery successful for '$query' page $page - Found ${cached.size} songs")
            Result.success(PaginatedSearch(songs = cached, hasMore = false))
        } else {
            logger.error("Cache recovery failed for '$query' page $page - No cached data available", cause)
            Result.failure(cause)
        }
    }

    // --- Album ---

    override suspend fun getAlbumSongs(collectionId: Long): Result<List<Song>> = runCatching {
        logger.info("Fetching album songs for collectionId: $collectionId")
        val response = api.lookupAlbum(collectionId)
        val songs = response.results.filter { it.kind == "song" }
        logger.debug("Album API response received - Total songs: ${songs.size}")
        dao.upsertAll(songs.map { it.toEntity() })
        logger.info("Album songs cached successfully - Count: ${songs.size}")
        songs.map { it.toDomain() }
    }.recoverCatching { error ->
        logger.warn("Failed to fetch album from API, attempting to recover from cache for collectionId: $collectionId - Error: ${error.message}")
        val cached = dao.getAlbumSongs(collectionId).map { it.toDomain() }
        if (cached.isNotEmpty()) {
            logger.info("Cache recovery successful for album $collectionId - Found ${cached.size} songs")
        } else {
            logger.error("Cache recovery failed for album $collectionId - No cached data available", error)
        }
        cached.ifEmpty { throw error }
    }

    // --- Recent ---

    override fun getRecentSongs(limit: Int): Flow<List<Song>> =
        recentDao.getRecentlyPlayed(limit).map { entities -> entities.map { it.toDomain() } }

    override suspend fun markAsPlayed(song: Song) {
        logger.debug("Marking song as played: '${song.trackName}' (ID: ${song.trackId})")
        recentDao.upsert(song.toRecentEntity())
    }

    companion object {
        private const val DISPLAY_PAGE_SIZE = 20
        private const val FETCH_SLOT_SIZE = 40
        private const val PAGES_PER_SLOT = 2
        private const val MAX_CACHED_ITEMS = 200
    }
}
