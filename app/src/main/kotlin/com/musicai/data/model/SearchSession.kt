package com.musicai.data.model

import com.musicai.domain.model.PaginatedSearch

private const val DISPLAY_PAGE_SIZE = 20

data class SearchSession(
    val query: String,
    val buffer: List<SongEntity> = emptyList(),
    val hasReachedEnd: Boolean = false,
) {
    fun pageAt(page: Int): PaginatedSearch? {
        val offset = (page - 1) * DISPLAY_PAGE_SIZE
        if (offset >= buffer.size) return null

        val songs = buffer.drop(offset).take(DISPLAY_PAGE_SIZE).map { it.toDomain() }
        if (songs.isEmpty()) return null

        // hasMore = true if there are more items in buffer OR API may have more
        val hasMore = (offset + DISPLAY_PAGE_SIZE < buffer.size) || !hasReachedEnd
        return PaginatedSearch(songs = songs, hasMore = hasMore)
    }
}