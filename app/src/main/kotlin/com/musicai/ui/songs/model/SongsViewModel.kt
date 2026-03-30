package com.musicai.ui.songs.model

import com.musicai.domain.Song
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface SongsViewModel {
    val state: StateFlow<SongsState>
    val navigationEvents: SharedFlow<SongsNavigationEvent>

    fun onQueryChange(query: String)
    fun onToggleSearch()
    fun onSearch()
    fun onLoadMore()
    fun onSongClick(song: Song)
    fun onMoreClick(song: Song)
    fun onDismissSheet()
    fun onViewAlbum(song: Song)
    fun onRefresh()
}
