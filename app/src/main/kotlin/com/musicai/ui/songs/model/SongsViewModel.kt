package com.musicai.ui.songs.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicai.domain.model.Song
import com.musicai.domain.usecase.GetRecentSongsUseCase
import com.musicai.domain.usecase.SearchSongsUseCase
import com.musicai.plugin.utils.ConnectivityChecker
import com.musicai.plugin.utils.Logger
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
    val messageEvents: SharedFlow<SongsMessageEvent>

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
    private val logger: Logger,
    private val connectivityChecker: ConnectivityChecker,
) : ViewModel(), SongsViewModel {

    private val _state = MutableStateFlow(SongsState())
    override val state = _state.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<SongsNavigationEvent>()
    override val navigationEvents = _navigationEvents.asSharedFlow()

    private val _messageEvents = MutableSharedFlow<SongsMessageEvent>(replay = 1)
    override val messageEvents = _messageEvents.asSharedFlow()

    private var songsSourceJob: Job? = null
    private var currentPage: Int = 0

    init {
        observeRecentSongs()
    }

    private fun observeRecentSongs() {
        logger.debug("Observing recent songs")
        songsSourceJob?.cancel()
        songsSourceJob = getRecentSongs()
            .onEach { recent ->
                if (!_state.value.isSearchActive) {
                    _state.update { it.copy(songs = recent.distinctBy { song -> song.trackId }) }
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
    }

    override fun onToggleSearch() {
        if (_state.value.isSearchActive) {
            logger.debug("Closing search mode")
            songsSourceJob?.cancel()
            _state.update { it.copy(isSearchActive = false, query = "", songs = emptyList()) }
            observeRecentSongs()
        } else {
            if (!connectivityChecker.isInternetAvailable()) {
                logger.error("No internet connection while opening search mode")
                viewModelScope.launch {
                    _messageEvents.emit(SongsMessageEvent.NoConnectionError)
                }
                return
            }
            logger.debug("Opening search mode")
            songsSourceJob?.cancel()
            _state.update { it.copy(isSearchActive = true, songs = emptyList()) }
        }
    }

    override fun onSearch() {
        val query = _state.value.query.trim()
        if (query.isBlank()) {
            logger.debug("Search query is blank, skipping search")
            return
        }

        if (!connectivityChecker.isInternetAvailable()) {
            logger.error("No internet connection while performing search for query: '$query'")
            viewModelScope.launch {
                _messageEvents.emit(SongsMessageEvent.NoConnectionError)
            }
            return
        }

        logger.info("Starting search with query: '$query'")
        songsSourceJob?.cancel()
        currentPage = 0
        _state.update { it.copy(isLoading = true, songs = emptyList(), hasMore = true, error = null) }
        songsSourceJob = viewModelScope.launch {
            searchSongs(query, page = 1)
                .onSuccess { result ->
                    logger.debug("Search success -> Page=1 | Songs=${result.songs.size} | HasMore=${result.hasMore}")
                    currentPage = 1
                    _state.update {
                        it.copy(
                            isLoading = false,
                            songs = result.songs.distinctBy { song -> song.trackId },
                            hasMore = result.hasMore,
                        )
                    }
                }
                .onFailure { e ->
                    logger.error("Search failed for query '$query': ${e.message}", e)
                    _messageEvents.emit(SongsMessageEvent.GenericError)
                    _state.update {
                        it.copy(isLoading = false)
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
        if (current.isLoadingMore || !current.hasMore || current.query.isBlank()) {
            logger.debug("Skipping load more - isLoadingMore: ${current.isLoadingMore}, hasMore: ${current.hasMore}, query: '${current.query}'")
            return
        }

        if (!connectivityChecker.isInternetAvailable()) {
            logger.error("No internet connection while loading more results for query: '${current.query}'")
            viewModelScope.launch {
                _messageEvents.emit(SongsMessageEvent.NoConnectionError)
            }
            return
        }

        val nextPage = currentPage + 1
        logger.info("Loading more results for query: '${current.query}' - Page: $nextPage")
        _state.update { it.copy(isLoadingMore = true) }

        viewModelScope.launch {
            searchSongs(current.query, page = nextPage)
                .onSuccess { result ->
                    logger.debug("Load more success -> Page=$nextPage | Songs=${result.songs.size} | HasMore=${result.hasMore}")
                    currentPage = nextPage
                    _state.update { state ->
                        val newSongs = (state.songs + result.songs).distinctBy { it.trackId }
                        state.copy(
                            isLoadingMore = false,
                            songs = newSongs,
                            hasMore = result.hasMore,
                        )
                    }
                }
                .onFailure { e ->
                    logger.error("Load more failed for query '${current.query}' page $nextPage: ${e.message}", e)
                    _messageEvents.emit(SongsMessageEvent.GenericError)
                    _state.update {
                        it.copy(isRefreshing = false)
                    }
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
            logger.info("Query is blank, refreshing recent songs")
            _state.update { it.copy(isRefreshing = true) }
            songsSourceJob = viewModelScope.launch {
                delay(500)
                _state.update { it.copy(isRefreshing = false) }
                observeRecentSongs()
            }
            return
        }

        if (!connectivityChecker.isInternetAvailable()) {
            logger.error("No internet connection while refreshing search results for query: '$query'")
            viewModelScope.launch {
                _messageEvents.emit(SongsMessageEvent.NoConnectionError)
            }
            _state.update {
                it.copy(isRefreshing = false, error = SongsErrorState.NoConnection)
            }
            return
        }

        logger.info("Refreshing search results for query: '$query'")
        _state.update { it.copy(isRefreshing = true, error = null) }
        currentPage = 0
        songsSourceJob = viewModelScope.launch {
            searchSongs.refresh(query)
                .onSuccess { result ->
                    logger.debug("Refresh success -> Songs=${result.songs.size} | HasMore=${result.hasMore}")
                    currentPage = 1
                    _state.update {
                        it.copy(
                            isRefreshing = false,
                            songs = result.songs.distinctBy { song -> song.trackId },
                            hasMore = result.hasMore,
                        )
                    }
                }
                .onFailure { e ->
                    logger.error("Refresh failed for query '$query': ${e.message}", e)
                    _messageEvents.emit(SongsMessageEvent.GenericError)
                    _state.update {
                        it.copy(isRefreshing = false, error = SongsErrorState.RefreshFailed)
                    }
                }
        }
    }
}
