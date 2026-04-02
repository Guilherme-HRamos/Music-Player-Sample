package com.musicai.ui.album.model

sealed class AlbumMessageEvent {
    data object NoConnectionError : AlbumMessageEvent()
    data class ShowError(val messageResId: Int) : AlbumMessageEvent()
}
