package com.musicai.ui.songs.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicai.domain.model.Song
import com.musicai.domain.usecase.GetRecentSongsUseCase
import com.musicai.domain.usecase.SearchSongsUseCase
import com.musicai.plugin.utils.logWip
import com.musicai.ui.shared.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    fun onClearSearch()
}

@HiltViewModel
class SongsViewModelImpl @Inject constructor(
    private val searchSongs: SearchSongsUseCase,
    private val getRecentSongs: GetRecentSongsUseCase,
    private val playerController: PlayerController,
) : ViewModel(), SongsViewModel {

    private val _state = MutableStateFlow(SongsState())
    override val state = _state.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<SongsNavigationEvent>()
    override val navigationEvents = _navigationEvents.asSharedFlow()

    private var songsSourceJob: Job? = null

    init {
        observeRecentSongs()
    }
    private fun observeRecentSongs() {
        songsSourceJob?.cancel()
        songsSourceJob = getRecentSongs()
            .onEach { recent ->
                if (!_state.value.isSearchActive) {
                    logWip("SongsViewModel -> recent: $recent")
                    _state.update { it.copy(songs = recent) }
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
    }

    override fun onToggleSearch() {
        songsSourceJob?.cancel()
        _state.update { current ->
            if (current.isSearchActive) {
                current.copy(isSearchActive = false, query = "", songs = emptyList())
            } else {
                current.copy(isSearchActive = true, songs = emptyList())
            }
        }
        if (!_state.value.isSearchActive) {
            observeRecentSongs()
        }
    }

    override fun onSearch() {
        val query = _state.value.query.trim()
        if (query.isBlank()) return
        songsSourceJob?.cancel()
        _state.update { it.copy(isLoading = true, songs = emptyList(), currentPage = 0, hasMore = true, error = null) }
        songsSourceJob = viewModelScope.launch {
            searchSongs(query, page = 0)
                .onSuccess { result ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            songs = result.songs,
                            currentPage = result.currentPage,
                            hasMore = result.hasMore,
                        )
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(isLoading = false, error = e.message ?: "Search failed")
                    }
                }
        }
    }

    override fun onClearSearch() {
        val query = _state.value.query.trim()
        if (query.isBlank()) onToggleSearch() else onQueryChange(query = "")
    }

    override fun onLoadMore() {
        val current = _state.value
        if (current.isLoadingMore || !current.hasMore || current.query.isBlank()) return

        val nextPage = current.currentPage + 1
        _state.update { it.copy(isLoadingMore = true) }

        viewModelScope.launch {
            searchSongs(current.query, page = nextPage, previousResults = current.songs)
                .onSuccess { result ->
                    _state.update { state ->
                        state.copy(
                            isLoadingMore = false,
                            songs = state.songs + result.songs,
                            currentPage = result.currentPage,
                            hasMore = result.hasMore,
                        )
                    }
                }
                .onFailure {
                    _state.update { it.copy(isLoadingMore = false) }
                }
        }
    }

    override fun onSongClick(song: Song) {
        playerController.selectSong(_state.value.songs, song.trackId)
        viewModelScope.launch {
            _navigationEvents.emit(SongsNavigationEvent.NavigateToPlayer(song.trackId))
        }
    }

    override fun onMoreClick(song: Song) {
        _state.update { it.copy(selectedSong = song) }
    }

    override fun onDismissSheet() {
        _state.update { it.copy(selectedSong = null) }
    }

    override fun onViewAlbum(song: Song) {
        _state.update { it.copy(selectedSong = null) }
        viewModelScope.launch {
            _navigationEvents.emit(SongsNavigationEvent.NavigateToAlbum(song.collectionId))
        }
    }

    override fun onRefresh() {
        val query = _state.value.query.trim()
        songsSourceJob?.cancel()

        if (query.isBlank()) {
            _state.update { it.copy(isRefreshing = true) }
            songsSourceJob = viewModelScope.launch {
                delay(500)
                _state.update { it.copy(isRefreshing = false) }
                observeRecentSongs()
            }
            return
        }

        _state.update { it.copy(isRefreshing = true, error = null) }
        songsSourceJob = viewModelScope.launch {
            searchSongs(query, page = 0)
                .onSuccess { result ->
                    _state.update {
                        it.copy(
                            isRefreshing = false,
                            songs = result.songs,
                            currentPage = result.currentPage,
                            hasMore = result.hasMore,
                        )
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(isRefreshing = false, error = e.message)
                    }
                }
        }
    }
}
