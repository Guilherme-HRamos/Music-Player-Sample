package com.musicai.data.model

import com.google.gson.annotations.SerializedName
import com.musicai.domain.model.Song

data class SongResponse(
    @SerializedName("trackId") val trackId: Long = 0L,
    @SerializedName("trackName") val trackName: String = "",
    @SerializedName("artistName") val artistName: String = "",
    @SerializedName("collectionName") val collectionName: String = "",
    @SerializedName("collectionId") val collectionId: Long = 0L,
    @SerializedName("artworkUrl100") val artworkUrl100: String = "",
    @SerializedName("previewUrl") val previewUrl: String? = null,
    @SerializedName("trackTimeMillis") val trackTimeMillis: Long? = null,
    @SerializedName("wrapperType") val wrapperType: String = "",
    @SerializedName("kind") val kind: String = "",
) {
    fun toDomain() = Song(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        collectionName = collectionName,
        collectionId = collectionId,
        artworkUrl = artworkUrl100,
        previewUrl = previewUrl,
        trackTimeMillis = trackTimeMillis,
    )

    fun toEntity(query: String = "", lastPlayedAt: Long? = null) = SongEntity(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        collectionName = collectionName,
        collectionId = collectionId,
        artworkUrl = artworkUrl100,
        previewUrl = previewUrl,
        trackTimeMillis = trackTimeMillis,
        searchQuery = query,
        lastPlayedAt = lastPlayedAt,
    )
}
