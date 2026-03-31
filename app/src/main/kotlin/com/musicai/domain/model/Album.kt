package com.musicai.domain.model

data class Album(
    val collectionId: Long,
    val collectionName: String,
    val artistName: String,
    val artworkUrl: String,
    val songs: List<Song>,
)
