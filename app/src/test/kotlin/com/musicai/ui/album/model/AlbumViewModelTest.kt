package com.musicai.ui.album.model

import androidx.lifecycle.SavedStateHandle
import com.musicai.ui.utils.MainDispatcherRule
import com.musicai.ui.utils.fakes.FakeConnectivityChecker
import com.musicai.ui.utils.fakes.FakeGetAlbumSongsUseCase
import com.musicai.ui.utils.fakes.MutedLogger
import com.musicai.ui.utils.mocks.getMockAlbum
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
class AlbumViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getAlbumSongs: FakeGetAlbumSongsUseCase
    private lateinit var connectivityChecker: FakeConnectivityChecker
    private lateinit var viewModel: AlbumViewModelImpl
    private val collectionId = 123L

    @Before
    fun setup() {
        getAlbumSongs = FakeGetAlbumSongsUseCase()
        connectivityChecker = FakeConnectivityChecker()
    }

    private fun createViewModel() {
        val savedStateHandle = SavedStateHandle(mapOf("collectionId" to collectionId))
        viewModel = AlbumViewModelImpl(savedStateHandle, getAlbumSongs, connectivityChecker, MutedLogger())
    }

    @Test
    fun `when init then should load album songs for specific collectionId`() = runTest {
        // Given
        val album = getMockAlbum(collectionId, songCount = 10)
        getAlbumSongs.setSuccess(album)

        // When
        createViewModel()
        runCurrent()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(album, state.album)
        assertEquals(1, getAlbumSongs.invokeCalls)
        assertEquals(collectionId, getAlbumSongs.lastCollectionId)
    }

    @Test
    fun `when load fails then should emit error event and allow retry`() = runTest {
        // Given
        getAlbumSongs.setError(RuntimeException("Network Error"))

        // When
        val receivedEvents = mutableListOf<AlbumMessageEvent>()
        val job = launch {
            val savedStateHandle = SavedStateHandle(mapOf("collectionId" to collectionId))
            viewModel = AlbumViewModelImpl(savedStateHandle, getAlbumSongs, connectivityChecker, MutedLogger())
            viewModel.messageEvents.collect { event ->
                receivedEvents.add(event)
            }
        }
        advanceUntilIdle()

        // Then
        assertTrue(receivedEvents.any { it is AlbumMessageEvent.ShowError })

        // And when retry
        getAlbumSongs.setSuccess(getMockAlbum(collectionId))
        viewModel.onRetry()
        advanceUntilIdle()

        // Then
        val successState = viewModel.state.value
        assertFalse(successState.isLoading)
        assertTrue(successState.error == null)
        assertEquals(2, getAlbumSongs.invokeCalls)
        job.cancel()
    }

    @Test
    fun `when no internet connection on load then should emit no connection error`() = runTest {
        // Given
        connectivityChecker.setConnected(false)

        // When
        val receivedEvents = mutableListOf<AlbumMessageEvent>()
        val job = launch {
            val savedStateHandle = SavedStateHandle(mapOf("collectionId" to collectionId))
            viewModel = AlbumViewModelImpl(savedStateHandle, getAlbumSongs, connectivityChecker, MutedLogger())
            viewModel.messageEvents.collect { event ->
                receivedEvents.add(event)
            }
        }
        advanceUntilIdle()

        // Then
        assertTrue(receivedEvents.any { it is AlbumMessageEvent.NoConnectionError })
        job.cancel()
    }

    @Test
    fun `when no internet connection on retry then should emit no connection error`() = runTest {
        // Given
        getAlbumSongs.setSuccess(getMockAlbum(collectionId))
        createViewModel()
        runCurrent()

        connectivityChecker.setConnected(false)

        // When
        val receivedEvents = mutableListOf<AlbumMessageEvent>()
        val job = launch {
            viewModel.messageEvents.collect { event ->
                receivedEvents.add(event)
            }
        }
        runCurrent()
        viewModel.onRetry()
        runCurrent()

        // Then
        assertTrue(receivedEvents.any { it is AlbumMessageEvent.NoConnectionError })
        job.cancel()
    }
}
