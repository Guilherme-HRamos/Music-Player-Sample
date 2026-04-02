package com.musicai.ui.player.model

sealed class PlayerNavigationEvent {
    data class NavigateToAlbum(val collectionId: Long) : PlayerNavigationEvent()
}

sealed class PlayerMessagesEvent {
    data object NoConnectionError : PlayerMessagesEvent()
    data class ShowError(val messageResId: Int) : PlayerMessagesEvent()
}