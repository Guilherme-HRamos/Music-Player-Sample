package com.musicai.ui.album.model

sealed class AlbumNavigationEvent {
    data object NoConnectionError : AlbumNavigationEvent()
    data class ShowError(val messageResId: Int) : AlbumNavigationEvent()
}
