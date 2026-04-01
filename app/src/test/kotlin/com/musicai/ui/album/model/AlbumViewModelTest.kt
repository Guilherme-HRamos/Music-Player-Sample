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

    private fun buildViewModel(
        collectionId: Long = DEFAULT_COLLECTION_ID,
    ) = AlbumViewModelImpl(
        savedStateHandle = SavedStateHandle(mapOf("collectionId" to collectionId)),
        getAlbumSongs = fakeGetAlbum,
    )

    // region init / loadAlbum

    @Test
    fun `init - carrega album com sucesso e atualiza state`() = runTest {
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
    fun `init - isLoading é true enquanto a carga está em andamento`() = runTest {
        // Given — fakeGetAlbum nunca retorna (resultado nunca é consumido sem advanceUntilIdle)
        fakeGetAlbum.result = Result.success(anAlbum())

        // When — construir sem avançar as coroutines
        val vm = buildViewModel()

        // Then — isLoading configurado como true no construtor antes do launch completar
        assertTrue(vm.state.value.isLoading)
    }

    @Test
    fun `init - erro popula state com mensagem e sem album`() = runTest {
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
    fun `init - erro sem mensagem usa fallback genérico`() = runTest {
        // Given
        fakeGetAlbum.result = Result.failure(RuntimeException())

        // When
        val vm = buildViewModel()
        advanceUntilIdle()

        // Then
        assertEquals("Could not load album", vm.state.value.error)
    }

    @Test
    fun `collectionId é lido do SavedStateHandle e passado ao use case`() = runTest {
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
    fun `onRetry - re-carrega album após erro`() = runTest {
        // Given — primeira carga falha
        fakeGetAlbum.result = Result.failure(RuntimeException("Network error"))
        val vm = buildViewModel()
        advanceUntilIdle()
        assertNotNull(vm.state.value.error)

        // When — resultado corrigido e retry acionado
        val album = anAlbum()
        fakeGetAlbum.result = Result.success(album)
        vm.onRetry()
        advanceUntilIdle()

        // Then
        assertNull(vm.state.value.error)
        assertEquals(album, vm.state.value.album)
    }

    @Test
    fun `onRetry - limpa erro e mostra loading antes de completar`() = runTest {
        // Given
        fakeGetAlbum.result = Result.failure(RuntimeException("fail"))
        val vm = buildViewModel()
        advanceUntilIdle()

        // When
        fakeGetAlbum.result = Result.success(anAlbum())
        vm.onRetry()

        // Then — enquanto está carregando, error é null e isLoading é true
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
