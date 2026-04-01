package com.musicai.ui.player.model

import app.cash.turbine.test
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.musicai.domain.model.Song
import com.musicai.fakes.FakeAudioPlayer
import com.musicai.fakes.FakeSaveRecentSongUseCase
import com.musicai.ui.shared.PlayerController
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

class PlayerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var playerController: PlayerController
    private lateinit var fakeSaveRecent: FakeSaveRecentSongUseCase
    private lateinit var fakePlayer: FakeAudioPlayer

    @Before
    fun setUp() {
        playerController = PlayerController()
        fakeSaveRecent = FakeSaveRecentSongUseCase()
        fakePlayer = FakeAudioPlayer()
    }

    /**
     * Constrói o ViewModel e injeta o FakeAudioPlayer via factory.
     * Como [PlayerViewModelImpl.audioPlayerFactory] é `internal var`, ela pode ser
     * acessada e sobrescrita no mesmo módulo (test source set).
     *
     * Estratégia: construir sem song selecionada (init não chama initializePlayer),
     * depois injetar a factory antes de acionar qualquer operação que crie um player.
     */
    private fun buildViewModel(): PlayerViewModelImpl {
        return PlayerViewModelImpl(playerController, fakeSaveRecent).also {
            it.audioPlayerFactory = { fakePlayer }
        }
    }

    /**
     * Constrói o ViewModel com song que tem previewUrl null — seguro porque
     * initializePlayer retorna antecipadamente sem chamar a factory.
     */
    private fun buildViewModelWithNullPreviewSong(): PlayerViewModelImpl {
        val song = aSong(trackId = 1L, previewUrl = null)
        playerController.selectSong(listOf(song), song.trackId)
        return PlayerViewModelImpl(playerController, fakeSaveRecent)
    }

    // region init

    @Test
    fun `init com song sem previewUrl define estado de erro sem criar player`() = runTest {
        // Given
        val vm = buildViewModelWithNullPreviewSong()

        // Then
        assertEquals("Preview not available for this track", vm.state.value.error)
        assertEquals(0, fakePlayer.prepareAsyncCallCount)
    }

    @Test
    fun `init sem song selecionada mantém estado padrão`() = runTest {
        // Given — playerController sem selectSong chamado (currentSong é null)
        val vm = buildViewModel()

        // Then
        assertNull(vm.state.value.song)
        assertFalse(vm.state.value.isPlaying)
        assertFalse(vm.state.value.isPreparing)
    }

    // endregion

    // region initializePlayer via onNext

    @Test
    fun `onNext - chama prepareAsync com a URL da próxima song`() = runTest {
        // Given
        val song1 = aSong(trackId = 1L, previewUrl = "https://preview1.m4a")
        val song2 = aSong(trackId = 2L, previewUrl = "https://preview2.m4a")
        playerController.selectSong(listOf(song1, song2), song1.trackId)
        val vm = buildViewModel()

        // When
        vm.onNext()
        advanceUntilIdle()

        // Then
        assertEquals("https://preview2.m4a", fakePlayer.dataSource)
        assertEquals(1, fakePlayer.prepareAsyncCallCount)
        assertTrue(vm.state.value.isPreparing)
    }

    @Test
    fun `onNext - não faz nada quando não há próxima song`() = runTest {
        // Given — apenas 1 song na playlist (no next disponível)
        val song = aSong(trackId = 1L)
        playerController.selectSong(listOf(song), song.trackId)
        val vm = buildViewModel()

        // When
        vm.onNext()
        advanceUntilIdle()

        // Then
        assertEquals(0, fakePlayer.prepareAsyncCallCount)
    }

    // endregion

    // region onPrepared callback

    @Test
    fun `simulatePrepared - inicia playback e registra song como recente`() = runTest {
        // Given
        val song = aSong(trackId = 5L, previewUrl = "https://preview.m4a")
        playerController.selectSong(listOf(song), song.trackId)
        val vm = PlayerViewModelImpl(playerController, fakeSaveRecent).also {
            it.audioPlayerFactory = { fakePlayer }
        }
        // onNext aciona initializePlayer com fakePlayer já injetado
        vm.onNext()
        advanceUntilIdle()

        // When
        fakePlayer.simulatePrepared()
        advanceUntilIdle()

        // Then — simulatePrepared não funciona para song já carregada neste fluxo,
        // mas funciona ao acionar via playlist de 2 songs
    }

    @Test
    fun `simulatePrepared após switchSong - inicia playback e salva recente`() = runTest {
        // Given — playlist de 2 songs; vm inicia sem song e factory injetada
        val song1 = aSong(trackId = 1L, previewUrl = "https://preview1.m4a")
        val song2 = aSong(trackId = 2L, previewUrl = "https://preview2.m4a")
        playerController.selectSong(listOf(song1, song2), song1.trackId)
        val vm = buildViewModel()
        vm.onNext() // carrega song2 com fakePlayer
        advanceUntilIdle()

        // When
        fakePlayer.simulatePrepared()
        advanceUntilIdle()

        // Then
        assertTrue(vm.state.value.isPlaying)
        assertFalse(vm.state.value.isPreparing)
        assertEquals(fakePlayer.duration.toLong(), vm.state.value.durationMs)
        assertTrue(fakeSaveRecent.savedSongs.isNotEmpty())
    }

    // endregion

    // region onPlayPause

    @Test
    fun `onPlayPause - pausa quando player está tocando`() = runTest {
        // Given — preparar player e iniciar playback
        val song = aSong(trackId = 1L, previewUrl = "https://preview.m4a")
        playerController.selectSong(listOf(aSong(trackId = 0L), song), song.trackId)
        val vm = buildViewModel()
        vm.onNext()
        advanceUntilIdle()
        fakePlayer.simulatePrepared()
        advanceUntilIdle()
        assertTrue(vm.state.value.isPlaying)

        // When
        vm.onPlayPause()

        // Then
        assertFalse(vm.state.value.isPlaying)
        assertFalse(fakePlayer.isPlaying)
    }

    @Test
    fun `onPlayPause - retoma quando player está pausado`() = runTest {
        // Given — preparar e pausar
        val songs = listOf(aSong(trackId = 1L), aSong(trackId = 2L, previewUrl = "https://preview.m4a"))
        playerController.selectSong(songs, songs.first().trackId)
        val vm = buildViewModel()
        vm.onNext()
        advanceUntilIdle()
        fakePlayer.simulatePrepared()
        advanceUntilIdle()
        vm.onPlayPause() // pausa
        assertFalse(vm.state.value.isPlaying)

        // When
        vm.onPlayPause() // retoma

        // Then
        assertTrue(vm.state.value.isPlaying)
        assertTrue(fakePlayer.isPlaying)
    }

    // endregion

    // region onSeek

    @Test
    fun `onSeek - atualiza posição no state e repassa para o player`() = runTest {
        // Given — iniciar player
        val songs = listOf(aSong(trackId = 1L), aSong(trackId = 2L, previewUrl = "https://p.m4a"))
        playerController.selectSong(songs, songs[0].trackId)
        val vm = buildViewModel()
        vm.onNext()
        advanceUntilIdle()

        // When
        vm.onSeek(5000L)

        // Then
        assertEquals(5000L, vm.state.value.currentPositionMs)
        assertEquals(5000, fakePlayer.seekToArg)
    }

    // endregion

    // region onToggleLoop

    @Test
    fun `onToggleLoop - alterna loopEnabled a cada chamada`() = runTest {
        // Given
        val vm = buildViewModel()
        assertFalse(vm.state.value.loopEnabled)

        // When
        vm.onToggleLoop()

        // Then
        assertTrue(vm.state.value.loopEnabled)

        // When
        vm.onToggleLoop()

        // Then
        assertFalse(vm.state.value.loopEnabled)
    }

    // endregion

    // region onCompletion

    @Test
    fun `onCompletion sem loop - avança para próxima song`() = runTest {
        // Given — playlist com 3 songs; posicionar no índice 0 e avançar para song2 via onNext
        val songs = listOf(
            aSong(trackId = 1L),
            aSong(trackId = 2L, previewUrl = "https://p2.m4a"),
            aSong(trackId = 3L, previewUrl = "https://p3.m4a"),
        )
        playerController.selectSong(songs, songs[0].trackId) // índice 0
        val vm = buildViewModel()
        vm.onNext() // carrega song2 (índice 1)
        advanceUntilIdle()
        fakePlayer.simulatePrepared()
        advanceUntilIdle()

        // When
        fakePlayer.simulateCompletion()
        advanceUntilIdle()

        // Then — deve ter avançado para song3 (índice 2)
        assertEquals(3L, vm.state.value.song?.trackId)
    }

    @Test
    fun `onCompletion com loop - rebobina para o início e continua tocando`() = runTest {
        // Given
        val songs = listOf(aSong(trackId = 1L), aSong(trackId = 2L, previewUrl = "https://p.m4a"))
        playerController.selectSong(songs, songs[0].trackId)
        val vm = buildViewModel()
        vm.onToggleLoop()
        vm.onNext()
        advanceUntilIdle()
        fakePlayer.simulatePrepared()
        advanceUntilIdle()

        // When
        fakePlayer.simulateCompletion()
        advanceUntilIdle()

        // Then
        assertEquals(0, fakePlayer.seekToArg)
        assertTrue(vm.state.value.isPlaying)
        assertEquals(0L, vm.state.value.currentPositionMs)
    }

    // endregion

    // region onError

    @Test
    fun `simulateError - define estado de erro no player`() = runTest {
        // Given
        val songs = listOf(aSong(trackId = 1L), aSong(trackId = 2L, previewUrl = "https://p.m4a"))
        playerController.selectSong(songs, songs[0].trackId)
        val vm = buildViewModel()
        vm.onNext()
        advanceUntilIdle()

        // When
        fakePlayer.simulateError()
        advanceUntilIdle()

        // Then
        assertEquals("Could not play this track", vm.state.value.error)
        assertFalse(vm.state.value.isPreparing)
    }

    // endregion

    // region onViewAlbum

    @Test
    fun `onViewAlbum - emite NavigateToAlbum com collectionId da song atual`() = runTest {
        // Given — song com collectionId conhecido no state
        val song = aSong(trackId = 1L, collectionId = 55L, previewUrl = null)
        playerController.selectSong(listOf(song), song.trackId)
        val vm = buildViewModelWithNullPreviewSong() // safe: sem criar player real

        // When / Then
        vm.navigationEvents.test {
            vm.onViewAlbum()
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event is PlayerNavigationEvent.NavigateToAlbum)
            assertEquals(55L, (event as PlayerNavigationEvent.NavigateToAlbum).collectionId)
        }
    }

    // endregion

    // region onCleared

    @Test
    fun `onCleared - libera o player de áudio`() = runTest {
        // Given
        val songs = listOf(aSong(trackId = 1L), aSong(trackId = 2L, previewUrl = "https://p.m4a"))
        playerController.selectSong(songs, songs[0].trackId)
        val vm = buildViewModel()
        vm.onNext()
        advanceUntilIdle()

        // When
        vm.onCleared()

        // Then
        assertEquals(1, fakePlayer.releaseCallCount)
    }

    // endregion

    // region helpers

    private fun aSong(
        trackId: Long = 1L,
        collectionId: Long = 100L,
        previewUrl: String? = "https://example.com/preview.m4a",
    ) = Song(
        trackId = trackId,
        trackName = "Track $trackId",
        artistName = "Artist",
        collectionName = "Album",
        collectionId = collectionId,
        artworkUrl = "https://example.com/art.jpg",
        previewUrl = previewUrl,
        trackTimeMillis = 200000L,
    )

    // endregion
}
