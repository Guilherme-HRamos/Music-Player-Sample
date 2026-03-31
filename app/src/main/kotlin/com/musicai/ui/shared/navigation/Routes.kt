package com.musicai.ui.shared.navigation

object Routes {
    const val SONGS = "songs"
    const val PLAYER = "player/{trackId}"
    const val ALBUM = "album/{collectionId}"

    fun player(trackId: Long) = "player/$trackId"
    fun album(collectionId: Long) = "album/$collectionId"
}
