package com.musicai.ui.utils.mocks

import com.musicai.domain.model.Song

internal fun getMockSong(id: Long = 1L) = Song(
    trackId = id,
    trackName = "Song $id",
    artistName = "Artist $id",
    collectionName = "Album $id",
    collectionId = id,
    artworkUrl = "https://example.com/artwork/$id.jpg",
    previewUrl = "https://example.com/preview/$id.mp3",
    trackTimeMillis = 180000L
)

internal fun getMockSongsList(size: Int = 3, startId: Long = 1L): List<Song> {
    return (0 until size).map { getMockSong(startId + it.toLong()) }
}
