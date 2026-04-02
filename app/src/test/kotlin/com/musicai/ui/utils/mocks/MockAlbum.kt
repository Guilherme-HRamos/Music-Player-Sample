package com.musicai.ui.utils.mocks

import com.musicai.domain.model.Album
import com.musicai.domain.model.Song

internal fun getAlbumWithEmptySongs(collectionId: Long = 0) =
    Album(
        collectionId,
        collectionName = "Album Name",
        artistName = "Artist Name",
        artworkUrl = "Artwork URL",
        songs = emptyList()
    )

internal fun getMockAlbum(id: Long = 1L, songCount: Int = 5): Album {
    val songs = (1..songCount).map { 
        getMockSong(it.toLong()).copy(collectionId = id, collectionName = "Album $id") 
    }
    return Album(
        collectionId = id,
        collectionName = "Album $id",
        artistName = "Artist $id",
        artworkUrl = "https://example.com/artwork/$id.jpg",
        songs = songs
    )
}