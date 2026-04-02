package com.musicai.ui.player.model

sealed class PlayerNavigationEvent {
    data class NavigateToAlbum(val collectionId: Long) : PlayerNavigationEvent()
    data object NoConnectionError : PlayerNavigationEvent()
    data object GenericError : PlayerNavigationEvent()
}