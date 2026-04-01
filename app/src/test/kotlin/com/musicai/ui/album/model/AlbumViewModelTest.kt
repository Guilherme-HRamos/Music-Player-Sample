package com.musicai.ui.album.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.musicai.domain.model.Album
import com.musicai.domain.model.Song
import com.musicai.fakes.FakeGetAlbumSongsUseCase
import com.musicai.util.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AlbumViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var fakeGetAlbum: FakeGetAlbumSongsUseCase

    @Before
    fun setUp() {
        fakeGetAlbum = FakeGetAlbumSongsUseCase()
    }

    private fun buildViewModel(collectionId: Long = DEFAULT_COLLECTION_ID) =
        AlbumViewModelImpl(
            savedStateHandle = SavedStateHandle(mapOf("collectionId" to collectionId)),
            getAlbumSongs = fakeGetAlbum,
        )

    // region init

    @Test
    fun `init loads album successfully and updates state`() = runTest {
        // Given
        val album = anAlbum(collectionId = DEFAULT_COLLECTION_ID)
        fakeGetAlbum.result = Result.success(album)

        // When
        val vm = buildViewModel()
        advanceUntilIdle()

        // Then
        assertFalse(vm.state.value.isLoading)
        assertEquals(album, vm.state.value.album)
        assertNull(vm.state.value.error)
    }

    @Test
    fun `init sets isLoading to true while fetch is in progress`() = runTest {
        // Given
        fakeGetAlbum.result = Result.success(anAlbum())

        // When — build without advancing coroutines
        val vm = buildViewModel()

        // Then — isLoading is set before the coroutine completes
        assertTrue(vm.state.value.isLoading)
    }

    @Test
    fun `init sets error state and clears album on failure`() = runTest {
        // Given
        fakeGetAlbum.result = Result.failure(RuntimeException("Server error"))

        // When
        val vm = buildViewModel()
        advanceUntilIdle()

        // Then
        assertFalse(vm.state.value.isLoading)
        assertNull(vm.state.value.album)
        assertEquals("Server error", vm.state.value.error)
    }

    @Test
    fun `init uses generic fallback message when exception has no message`() = runTest {
        // Given
        fakeGetAlbum.result = Result.failure(RuntimeException())

        // When
        val vm = buildViewModel()
        advanceUntilIdle()

        // Then
        assertEquals("Could not load album", vm.state.value.error)
    }

    @Test
    fun `init reads collectionId from SavedStateHandle and passes it to use case`() = runTest {
        // Given
        fakeGetAlbum.result = Result.success(anAlbum(collectionId = 9999L))

        // When
        buildViewModel(collectionId = 9999L)
        advanceUntilIdle()

        // Then
        assertEquals(9999L, fakeGetAlbum.capturedCollectionId)
    }

    // endregion

    // region onRetry

    @Test
    fun `onRetry reloads album successfully after an error`() = runTest {
        // Given — first load fails
        fakeGetAlbum.result = Result.failure(RuntimeException("Network error"))
        val vm = buildViewModel()
        advanceUntilIdle()
        assertNotNull(vm.state.value.error)

        // When — fix the result and trigger retry
        val album = anAlbum()
        fakeGetAlbum.result = Result.success(album)
        vm.onRetry()
        advanceUntilIdle()

        // Then
        assertNull(vm.state.value.error)
        assertEquals(album, vm.state.value.album)
    }

    @Test
    fun `onRetry clears error and shows loading state before completing`() = runTest {
        // Given — first load fails
        fakeGetAlbum.result = Result.failure(RuntimeException("fail"))
        val vm = buildViewModel()
        advanceUntilIdle()

        // When — swap result and retry without advancing
        fakeGetAlbum.result = Result.success(anAlbum())
        vm.onRetry()

        // Then — error is cleared and loading starts
        assertNull(vm.state.value.error)
        assertTrue(vm.state.value.isLoading)
    }

    // endregion

    // region helpers

    private fun anAlbum(
        collectionId: Long = DEFAULT_COLLECTION_ID,
        collectionName: String = "Sam's Town",
        artistName: String = "The Killers",
    ) = Album(
        collectionId = collectionId,
        collectionName = collectionName,
        artistName = artistName,
        artworkUrl = "https://example.com/art.jpg",
        songs = listOf(aSong()),
    )

    private fun aSong() = Song(
        trackId = 1L,
        trackName = "Mr. Brightside",
        artistName = "The Killers",
        collectionName = "Hot Fuss",
        collectionId = DEFAULT_COLLECTION_ID,
        artworkUrl = "https://example.com/art.jpg",
        previewUrl = "https://example.com/preview.m4a",
        trackTimeMillis = 222000L,
    )

    companion object {
        private const val DEFAULT_COLLECTION_ID = 1440891236L
    }

    // endregion
}
