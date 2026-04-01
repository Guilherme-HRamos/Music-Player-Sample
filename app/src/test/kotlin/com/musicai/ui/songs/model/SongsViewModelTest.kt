package com.musicai.ui.songs.model

import app.cash.turbine.test
import com.musicai.domain.model.Song
import com.musicai.fakes.FakeGetRecentSongsUseCase
import com.musicai.fakes.FakeSearchSongsUseCase
import com.musicai.ui.shared.PlayerController
import com.musicai.util.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SongsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeSearch: FakeSearchSongsUseCase
    private lateinit var fakeRecent: FakeGetRecentSongsUseCase
    private lateinit var playerController: PlayerController
    private lateinit var vm: SongsViewModelImpl

    @Before
    fun setUp() {
        fakeSearch = FakeSearchSongsUseCase()
        fakeRecent = FakeGetRecentSongsUseCase()
        playerController = PlayerController()
        vm = SongsViewModelImpl(fakeSearch, fakeRecent, playerController)
    }

    // region onSearch

    @Test
    fun `onSearch shows loading state before coroutine completes`() = runTest {
        // Given
        fakeSearch.result = Result.success(someSongs(count = 5))
        vm.onQueryChange("killers")

        // When
        vm.onSearch()

        // Then — isLoading must be true before coroutines advance
        assertTrue(vm.state.value.isLoading)

        advanceUntilIdle()
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `onSearch populates songs and clears error on success`() = runTest {
        // Given
        val songs = someSongs(count = 5)
        fakeSearch.result = Result.success(songs)
        vm.onQueryChange("killers")

        // When
        vm.onSearch()
        advanceUntilIdle()

        // Then
        assertEquals(songs, vm.state.value.songs)
        assertNull(vm.state.value.error)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `onSearch sets hasMore to false when result has fewer than 20 songs`() = runTest {
        // Given — 18 songs, below the page-size threshold of 20
        fakeSearch.result = Result.success(someSongs(count = 18))
        vm.onQueryChange("killers")

        // When
        vm.onSearch()
        advanceUntilIdle()

        // Then
        assertFalse(vm.state.value.hasMore)
    }

    @Test
    fun `onSearch sets hasMore to true when result has exactly 20 songs`() = runTest {
        // Given
        fakeSearch.result = Result.success(someSongs(count = 20))
        vm.onQueryChange("killers")

        // When
        vm.onSearch()
        advanceUntilIdle()

        // Then
        assertTrue(vm.state.value.hasMore)
    }

    @Test
    fun `onSearch ignores blank query`() = runTest {
        // Given
        vm.onQueryChange("   ")

        // When
        vm.onSearch()
        advanceUntilIdle()

        // Then
        assertEquals(0, fakeSearch.callCount)
    }

    @Test
    fun `onSearch sets error state on failure`() = runTest {
        // Given
        fakeSearch.result = Result.failure(RuntimeException("Search failed"))
        vm.onQueryChange("killers")

        // When
        vm.onSearch()
        advanceUntilIdle()

        // Then
        assertEquals("Search failed", vm.state.value.error)
        assertFalse(vm.state.value.isLoading)
    }

    // endregion

    // region onLoadMore

    @Test
    fun `onLoadMore appends songs from subsequent pages`() = runTest {
        // Given — page 0 returns 20 songs, page 1 returns 5
        fakeSearch.resultsQueue.addLast(Result.success(someSongs(count = 20, startId = 1)))
        fakeSearch.resultsQueue.addLast(Result.success(someSongs(count = 5, startId = 21)))
        vm.onQueryChange("killers")
        vm.onSearch()
        advanceUntilIdle()

        // When
        vm.onLoadMore()
        advanceUntilIdle()

        // Then
        assertEquals(25, vm.state.value.songs.size)
        assertFalse(vm.state.value.hasMore)
        assertEquals(1, vm.state.value.currentPage)
    }

    @Test
    fun `onLoadMore is ignored when hasMore is false`() = runTest {
        // Given — search result below threshold forces hasMore = false
        fakeSearch.result = Result.success(someSongs(count = 5))
        vm.onQueryChange("killers")
        vm.onSearch()
        advanceUntilIdle()
        val callCountAfterSearch = fakeSearch.callCount

        // When
        vm.onLoadMore()
        advanceUntilIdle()

        // Then — no additional use case calls
        assertEquals(callCountAfterSearch, fakeSearch.callCount)
    }

    @Test
    fun `onLoadMore is ignored when query is blank`() = runTest {
        // Given — default state, no query set

        // When
        vm.onLoadMore()
        advanceUntilIdle()

        // Then
        assertEquals(0, fakeSearch.callCount)
    }

    // endregion

    // region recent songs reactivity

    @Test
    fun `recent songs update state when search is inactive`() = runTest {
        // Given
        val recentSongs = someSongs(count = 3)

        // When
        fakeRecent.songsFlow.emit(recentSongs)
        advanceUntilIdle()

        // Then
        assertEquals(recentSongs, vm.state.value.songs)
    }

    @Test
    fun `recent songs do not replace state when search is active`() = runTest {
        // Given — activate search and load results
        fakeSearch.result = Result.success(someSongs(count = 5))
        vm.onQueryChange("killers")
        vm.onSearch()
        advanceUntilIdle()
        val songsFromSearch = vm.state.value.songs

        // When — recent songs arrive while search is active
        fakeRecent.songsFlow.emit(someSongs(count = 10, startId = 100))
        advanceUntilIdle()

        // Then — search results are preserved
        assertEquals(songsFromSearch, vm.state.value.songs)
    }

    // endregion

    // region navigation events

    @Test
    fun `onSongClick emits NavigateToPlayer with correct trackId`() = runTest {
        // Given
        val song = aSong(trackId = 42L)
        fakeSearch.result = Result.success(listOf(song))
        vm.onQueryChange("killers")
        vm.onSearch()
        advanceUntilIdle()

        // When / Then
        vm.navigationEvents.test {
            vm.onSongClick(song)
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is SongsNavigationEvent.NavigateToPlayer)
            assertEquals(42L, (event as SongsNavigationEvent.NavigateToPlayer).trackId)
        }
    }

    @Test
    fun `onViewAlbum emits NavigateToAlbum and clears selectedSong`() = runTest {
        // Given
        val song = aSong(trackId = 1L, collectionId = 99L)
        vm.onMoreClick(song)
        assertEquals(song, vm.state.value.selectedSong)

        // When / Then
        vm.navigationEvents.test {
            vm.onViewAlbum(song)
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is SongsNavigationEvent.NavigateToAlbum)
            assertEquals(99L, (event as SongsNavigationEvent.NavigateToAlbum).collectionId)
        }
        assertNull(vm.state.value.selectedSong)
    }

    // endregion

    // region bottom sheet

    @Test
    fun `onMoreClick sets selectedSong in state`() {
        // Given
        val song = aSong(42L)

        // When
        vm.onMoreClick(song)

        // Then
        assertEquals(song, vm.state.value.selectedSong)
    }

    @Test
    fun `onDismissSheet clears selectedSong`() {
        // Given
        vm.onMoreClick(aSong(1L))

        // When
        vm.onDismissSheet()

        // Then
        assertNull(vm.state.value.selectedSong)
    }

    // endregion

    // region toggle search / clear search

    @Test
    fun `onToggleSearch activates search mode`() {
        // Given — search is inactive by default

        // When
        vm.onToggleSearch()

        // Then
        assertTrue(vm.state.value.isSearchActive)
    }

    @Test
    fun `onToggleSearch deactivates search and clears songs when already active`() = runTest {
        // Given — activate and load results
        vm.onToggleSearch()
        fakeSearch.result = Result.success(someSongs(count = 5))
        vm.onQueryChange("killers")
        vm.onSearch()
        advanceUntilIdle()

        // When
        vm.onToggleSearch()

        // Then
        assertFalse(vm.state.value.isSearchActive)
        assertEquals("", vm.state.value.query)
        assertTrue(vm.state.value.songs.isEmpty())
    }

    @Test
    fun `onClearSearch clears query when query has text`() {
        // Given
        vm.onQueryChange("killers")

        // When
        vm.onClearSearch()

        // Then
        assertEquals("", vm.state.value.query)
        assertTrue(vm.state.value.isSearchActive)
    }

    @Test
    fun `onClearSearch deactivates search when query is empty`() {
        // Given — search activated with no query
        vm.onToggleSearch()

        // When
        vm.onClearSearch()

        // Then
        assertFalse(vm.state.value.isSearchActive)
    }

    // endregion

    // region helpers

    private fun someSongs(count: Int, startId: Long = 1L): List<Song> =
        (0 until count).map { aSong(trackId = startId + it) }

    private fun aSong(
        trackId: Long = 1L,
        collectionId: Long = 100L,
    ) = Song(
        trackId = trackId,
        trackName = "Track $trackId",
        artistName = "Artist",
        collectionName = "Album",
        collectionId = collectionId,
        artworkUrl = "https://example.com/art.jpg",
        previewUrl = "https://example.com/preview.m4a",
        trackTimeMillis = 200000L,
    )

    // endregion
}
