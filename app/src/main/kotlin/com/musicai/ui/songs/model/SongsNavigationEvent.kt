package com.musicai.ui.songs.model

sealed class SongsNavigationEvent {
    data class NavigateToPlayer(val trackId: Long) : SongsNavigationEvent()
    data class NavigateToAlbum(val collectionId: Long) : SongsNavigationEvent()
    data object NoConnectionError : SongsNavigationEvent()
    data object GenericError : SongsNavigationEvent()
}