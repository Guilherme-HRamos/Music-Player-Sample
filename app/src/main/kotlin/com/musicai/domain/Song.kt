package com.musicai.domain

data class Song(
    val trackId: Long,
    val trackName: String,
    val artistName: String,
    val collectionName: String,
    val collectionId: Long,
    val artworkUrl: String,
    val previewUrl: String?,
    val trackTimeMillis: Long?,
)