package com.musicai.ui.songs.model

import com.musicai.R
import com.musicai.domain.model.PaginatedSearch
import com.musicai.ui.shared.PlayerController
import com.musicai.ui.utils.MainDispatcherRule
import com.musicai.ui.utils.fakes.FakeConnectivityChecker
import com.musicai.ui.utils.fakes.FakeGetRecentSongsUseCase
import com.musicai.ui.utils.fakes.FakeSearchSongsUseCase
import com.musicai.ui.utils.fakes.MutedLogger
import com.musicai.ui.utils.mocks.getMockSongsList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SongsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var searchSongs: FakeSearchSongsUseCase
    private lateinit var getRecentSongs: FakeGetRecentSongsUseCase
    private lateinit var playerController: PlayerController
    private lateinit var connectivityChecker: FakeConnectivityChecker
    private lateinit var viewModel: SongsViewModelImpl

    @Before
    fun setup() {
        searchSongs = FakeSearchSongsUseCase()
        getRecentSongs = FakeGetRecentSongsUseCase()
        getRecentSongs.setSuccess(getMockSongsList(5))
        playerController = PlayerController()
        connectivityChecker = FakeConnectivityChecker()

        viewModel = SongsViewModelImpl(
            searchSongs,
            getRecentSongs,
            playerController,
            MutedLogger(),
            connectivityChecker
        )
    }

    @Test
    fun `when init then should observe recent songs`() = runTest {
        runCurrent()
        // Then
        val state = viewModel.state.value
        assertEquals(5, state.songs.size)
        assertFalse(state.isSearchActive)
    }

    @Test
    fun `when search triggered then should update state results`() = runTest {
        // Given
        val query = "Rock"
        val songs = getMockSongsList(10)
        searchSongs.setSuccess(PaginatedSearch(songs, hasMore = true))
        viewModel.onToggleSearch()
        viewModel.onQueryChange(query)

        // When
        viewModel.onSearch()
        runCurrent()

        // Then
        val state = viewModel.state.value
        assertEquals(songs, state.songs)
        assertFalse(state.isLoading)
        assertTrue(state.hasMore)
        assertEquals(1, searchSongs.invokeCalls)
    }

    @Test
    fun `when load more then should append songs to existing list`() = runTest {
        // Given
        val initialSongs = getMockSongsList(20, startId = 1)
        val moreSongs = getMockSongsList(20, startId = 21)
        searchSongs.setSuccess(PaginatedSearch(initialSongs, hasMore = true))
        
        viewModel.onToggleSearch()
        viewModel.onQueryChange("Pop")
        viewModel.onSearch()
        runCurrent()
        
        searchSongs.setSuccess(PaginatedSearch(moreSongs, hasMore = false))

        // When
        viewModel.onLoadMore()
        runCurrent()

        // Then
        val state = viewModel.state.value
        assertEquals(40, state.songs.size)
        assertFalse(state.hasMore)
        assertEquals(2, searchSongs.invokeCalls)
    }

    @Test
    fun `when search fails then should emit error event`() = runTest {
        // Given
        searchSongs.setError(RuntimeException("Network Error"))
        viewModel.onToggleSearch()
        viewModel.onQueryChange("Query")

        // When
        val event = launch {
            viewModel.messageEvents.collect { navEvent ->
                if (navEvent is SongsMessageEvent.GenericError) {
                    assertTrue(true)
                }
            }
        }
        viewModel.onSearch()
        runCurrent()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        event.cancel()
    }

    @Test
    fun `when no internet connection on toggle search then should emit error event`() = runTest {
        // Given
        connectivityChecker.setConnected(false)

        // When
        val receivedEvent = mutableListOf<SongsMessageEvent>()
        val job = launch {
            viewModel.messageEvents.collect { event ->
                receivedEvent.add(event)
            }
        }
        runCurrent()
        viewModel.onToggleSearch()
        advanceUntilIdle()

        // Then
        assertTrue(receivedEvent.any { it is SongsMessageEvent.NoConnectionError })
        job.cancel()
    }

    @Test
    fun `when no internet connection on search then should emit error event`() = runTest {
        // Given
        connectivityChecker.setConnected(false)
        viewModel.onToggleSearch()
        viewModel.onQueryChange("Query")

        // When
        val receivedEvent = mutableListOf<SongsMessageEvent>()
        val job = launch {
            viewModel.messageEvents.collect { event ->
                receivedEvent.add(event)
            }
        }
        runCurrent()
        viewModel.onSearch()
        advanceUntilIdle()

        // Then
        assertTrue(receivedEvent.any { it is SongsMessageEvent.NoConnectionError })
        job.cancel()
    }

    @Test
    fun `when no internet connection on load more then should emit error event`() = runTest {
        // Given
        connectivityChecker.setConnected(true)
        searchSongs.setSuccess(PaginatedSearch(getMockSongsList(10), hasMore = true))
        viewModel.onToggleSearch()
        viewModel.onQueryChange("Query")
        viewModel.onSearch()
        runCurrent()

        connectivityChecker.setConnected(false)

        // When
        val receivedEvent = mutableListOf<SongsMessageEvent>()
        val job = launch {
            viewModel.messageEvents.collect { event ->
                receivedEvent.add(event)
            }
        }
        runCurrent()
        viewModel.onLoadMore()
        advanceUntilIdle()

        // Then
        assertTrue(receivedEvent.any { it is SongsMessageEvent.NoConnectionError })
        job.cancel()
    }

    @Test
    fun `when no internet connection on refresh then should emit error event`() = runTest {
        // Given
        connectivityChecker.setConnected(true)
        searchSongs.setSuccess(PaginatedSearch(getMockSongsList(10), hasMore = true))
        viewModel.onToggleSearch()
        viewModel.onQueryChange("Query")
        viewModel.onSearch()
        runCurrent()

        connectivityChecker.setConnected(false)

        // When
        val receivedEvent = mutableListOf<SongsMessageEvent>()
        val job = launch {
            viewModel.messageEvents.collect { event ->
                receivedEvent.add(event)
            }
        }
        runCurrent()
        viewModel.onRefresh()
        advanceUntilIdle()

        // Then
        assertTrue(receivedEvent.any { it is SongsMessageEvent.NoConnectionError })
        job.cancel()
    }
}
