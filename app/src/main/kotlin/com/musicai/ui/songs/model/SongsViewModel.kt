package com.musicai.ui.songs.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicai.domain.Song
import com.musicai.utils.logWip
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

class SongsViewModelImpl(
) : ViewModel(), SongsViewModel {

    private val _state = MutableStateFlow(SongsState())
    override val state = _state.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<SongsNavigationEvent>()
    override val navigationEvents = _navigationEvents.asSharedFlow()

    override fun onQueryChange(query: String) {
        logWip("onQueryChange called with query: $query")
    }

    override fun onToggleSearch() {
        logWip("onToggleSearch called")
        _state.value = _state.value.copy(
            selectedSong = Song(
                trackId = 2L,
                trackName = "Get Lucky",
                artistName = "Daft Punk",
                collectionName = "Get Lucky Ab.",
                collectionId = 2L,
                artworkUrl = "https://cdn11.bigcommerce.com/s-8e25iavqdi/images/stencil/1280x1280/products/27797/27318/get-lucky-album-cover-sticker__90439.1539726074.jpg",
                previewUrl = null,
                trackTimeMillis = 200000L
            )
        )
//        _state.value = _state.value.copy(isSearchActive = !_state.value.isSearchActive)
    }

    override fun onSearch() {
        logWip("onSearch called")
    }

    override fun onLoadMore() {
        logWip("onLoadMore called")
    }

    override fun onSongClick(song: Song) {
        logWip("onSongClick called with song: $song")
    }

    override fun onMoreClick(song: Song) {
        logWip("onMoreClick called with song: $song")
    }

    override fun onDismissSheet() {
        logWip("onDismissSheet called")
    }

    override fun onViewAlbum(song: Song) {
        _state.update { it.copy(selectedSong = null) }
        viewModelScope.launch {
            _navigationEvents.emit(SongsNavigationEvent.NavigateToAlbum(song.collectionId))
        }
    }

    override fun onRefresh() {
        logWip("onRefresh called")
    }
}
