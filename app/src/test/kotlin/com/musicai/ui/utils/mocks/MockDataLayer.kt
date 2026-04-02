package com.musicai.ui.utils.mocks

import com.musicai.data.model.RecentSongEntity
import com.musicai.data.model.SearchResponse
import com.musicai.data.model.SongEntity
import com.musicai.data.model.SongResponse

internal fun getMockSongResponse(id: Long = 1L) = SongResponse(
    trackId = id,
    trackName = "Song $id",
    artistName = "Artist $id",
    collectionName = "Album $id",
    collectionId = id,
    artworkUrl100 = "https://example.com/artwork/$id.jpg",
    previewUrl = "https://example.com/preview/$id.mp3",
    trackTimeMillis = 180000L,
    kind = "song"
)

internal fun getMockSearchResponse(count: Int = 5, startId: Long = 1L) = SearchResponse(
    resultCount = count,
    results = (0 until count).map { getMockSongResponse(startId + it.toLong()) }
)

internal fun getMockSongEntity(id: Long = 1L, query: String = "") = SongEntity(
    trackId = id,
    trackName = "Song $id",
    artistName = "Artist $id",
    collectionName = "Album $id",
    collectionId = id,
    artworkUrl = "https://example.com/artwork/$id.jpg",
    previewUrl = "https://example.com/preview/$id.mp3",
    trackTimeMillis = 180000L,
    searchQuery = query
)

internal fun getMockRecentSongEntity(id: Long = 1L) = RecentSongEntity(
    trackId = id,
    trackName = "Song $id",
    artistName = "Artist $id",
    collectionName = "Album $id",
    collectionId = id,
    artworkUrl = "https://example.com/artwork/$id.jpg",
    previewUrl = "https://example.com/preview/$id.mp3",
    trackTimeMillis = 180000L,
    playedAt = System.currentTimeMillis()
)
