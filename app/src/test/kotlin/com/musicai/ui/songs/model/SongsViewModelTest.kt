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
    fun `onSearch - estado de loading antes de completar`() = runTest {
        // Given
        fakeSearch.result = Result.success(someSongs(count = 5))
        vm.onQueryChange("killers")

        // When
        vm.onSearch()

        // Then — antes de avançar as coroutines, isLoading deve ser true
        assertTrue(vm.state.value.isLoading)

        advanceUntilIdle()
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `onSearch - sucesso popula songs e limpa erro`() = runTest {
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
    fun `onSearch - hasMore é false quando resultado tem menos de 20 songs`() = runTest {
        // Given — 18 songs (abaixo do threshold de 20)
        fakeSearch.result = Result.success(someSongs(count = 18))
        vm.onQueryChange("killers")

        // When
        vm.onSearch()
        advanceUntilIdle()

        // Then
        assertFalse(vm.state.value.hasMore)
    }

    @Test
    fun `onSearch - hasMore é true quando resultado tem exatamente 20 songs`() = runTest {
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
    fun `onSearch - query em branco é ignorada`() = runTest {
        // Given
        vm.onQueryChange("   ")

        // When
        vm.onSearch()
        advanceUntilIdle()

        // Then
        assertEquals(0, fakeSearch.callCount)
    }

    @Test
    fun `onSearch - falha atualiza estado de erro`() = runTest {
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
    fun `onLoadMore - acumula songs de páginas subsequentes`() = runTest {
        // Given — página 0 retorna 20 songs, página 1 retorna 5
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
    fun `onLoadMore - ignorado quando hasMore é false`() = runTest {
        // Given — forçamos hasMore = false via busca com < 20 resultados
        fakeSearch.result = Result.success(someSongs(count = 5))
        vm.onQueryChange("killers")
        vm.onSearch()
        advanceUntilIdle()
        assertEquals(1, fakeSearch.callCount)

        // When
        vm.onLoadMore()
        advanceUntilIdle()

        // Then
        assertEquals(1, fakeSearch.callCount) // nenhuma chamada adicional
    }

    @Test
    fun `onLoadMore - ignorado quando query está em branco`() = runTest {
        // Given — state padrão sem query
        // When
        vm.onLoadMore()
        advanceUntilIdle()

        // Then
        assertEquals(0, fakeSearch.callCount)
    }

    // endregion

    // region recent songs

    @Test
    fun `recent songs atualizam state quando search está inativo`() = runTest {
        // Given
        val recentSongs = someSongs(count = 3)

        // When
        fakeRecent.songsFlow.emit(recentSongs)
        advanceUntilIdle()

        // Then
        assertEquals(recentSongs, vm.state.value.songs)
    }

    @Test
    fun `recent songs não substituem state quando search está ativo`() = runTest {
        // Given — ativar busca e popular com resultado
        fakeSearch.result = Result.success(someSongs(count = 5))
        vm.onQueryChange("killers")
        vm.onSearch()
        advanceUntilIdle()
        val songsFromSearch = vm.state.value.songs

        // When — recentes chegam enquanto search está ativo
        fakeRecent.songsFlow.emit(someSongs(count = 10, startId = 100))
        advanceUntilIdle()

        // Then — songs da busca são mantidas
        assertEquals(songsFromSearch, vm.state.value.songs)
    }

    // endregion

    // region onSongClick / navigation

    @Test
    fun `onSongClick - emite NavigateToPlayer com trackId correto`() = runTest {
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
    fun `onViewAlbum - emite NavigateToAlbum e limpa selectedSong`() = runTest {
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

    // region onMoreClick / onDismissSheet

    @Test
    fun `onMoreClick - define selectedSong no state`() = runTest {
        // Given
        val song = aSong(42L)

        // When
        vm.onMoreClick(song)

        // Then
        assertEquals(song, vm.state.value.selectedSong)
    }

    @Test
    fun `onDismissSheet - limpa selectedSong`() = runTest {
        // Given
        vm.onMoreClick(aSong(1L))

        // When
        vm.onDismissSheet()

        // Then
        assertNull(vm.state.value.selectedSong)
    }

    // endregion

    // region onToggleSearch / onClearSearch

    @Test
    fun `onToggleSearch - ativa modo de busca`() = runTest {
        // When
        vm.onToggleSearch()

        // Then
        assertTrue(vm.state.value.isSearchActive)
    }

    @Test
    fun `onToggleSearch - desativa busca e limpa songs quando já estava ativo`() = runTest {
        // Given
        vm.onToggleSearch() // ativa
        fakeSearch.result = Result.success(someSongs(count = 5))
        vm.onQueryChange("killers")
        vm.onSearch()
        advanceUntilIdle()

        // When
        vm.onToggleSearch() // desativa

        // Then
        assertFalse(vm.state.value.isSearchActive)
        assertEquals("", vm.state.value.query)
        assertTrue(vm.state.value.songs.isEmpty())
    }

    @Test
    fun `onClearSearch - limpa query quando há texto`() = runTest {
        // Given
        vm.onQueryChange("killers")

        // When
        vm.onClearSearch()

        // Then
        assertEquals("", vm.state.value.query)
        assertTrue(vm.state.value.isSearchActive) // search continua ativo
    }

    @Test
    fun `onClearSearch - desativa search quando query está vazia`() = runTest {
        // Given
        vm.onToggleSearch() // ativa search sem query

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
