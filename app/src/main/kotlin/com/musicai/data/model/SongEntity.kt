package com.musicai.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.musicai.domain.model.Song

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val trackId: Long,
    val trackName: String,
    val artistName: String,
    val collectionName: String,
    val collectionId: Long,
    val artworkUrl: String,
    val previewUrl: String?,
    val trackTimeMillis: Long?,
    val searchQuery: String,
    val lastPlayedAt: Long?,
) {
    fun toDomain() = Song(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        collectionName = collectionName,
        collectionId = collectionId,
        artworkUrl = artworkUrl,
        previewUrl = previewUrl,
        trackTimeMillis = trackTimeMillis,
    )
}
