package com.musicai.ui.songs.model

sealed class SongsNavigationEvent {
    data class NavigateToPlayer(val trackId: Long) : SongsNavigationEvent()
    data class NavigateToAlbum(val collectionId: Long) : SongsNavigationEvent()
}

sealed class SongsMessageEvent {
    data object NoConnectionError : SongsMessageEvent()
    data object GenericError : SongsMessageEvent()
}