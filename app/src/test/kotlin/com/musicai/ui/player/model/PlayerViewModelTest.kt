package com.musicai.ui.player.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.musicai.domain.model.Song
import com.musicai.fakes.FakeAudioPlayer
import com.musicai.fakes.FakeSaveRecentSongUseCase
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
     * Builds the ViewModel and injects [FakeAudioPlayer] via the internal factory.
     *
     * Strategy: build without a selected song so that [init] does not call [initializePlayer],
     * then inject the factory before any operation that triggers player creation (onNext/onPrevious).
     */
    private fun buildViewModel(): PlayerViewModelImpl =
        PlayerViewModelImpl(playerController, fakeSaveRecent).also {
            it.audioPlayerFactory = { fakePlayer }
        }

    /**
     * Builds with a song that has a null previewUrl — safe because [initializePlayer]
     * returns early before calling the factory.
     */
    private fun buildViewModelWithNullPreviewSong(collectionId: Long = 100L): PlayerViewModelImpl {
        val song = aSong(trackId = 1L, previewUrl = null, collectionId = collectionId)
        playerController.selectSong(listOf(song), song.trackId)
        return PlayerViewModelImpl(playerController, fakeSaveRecent)
    }

    // region init

    @Test
    fun `init sets error state when current song has no previewUrl`() = runTest {
        // Given
        val vm = buildViewModelWithNullPreviewSong()

        // When — init runs during construction

        // Then
        assertEquals("Preview not available for this track", vm.state.value.error)
        assertEquals(0, fakePlayer.prepareAsyncCallCount)
    }

    @Test
    fun `init keeps default state when no song is selected`() = runTest {
        // Given — PlayerController with empty playlist

        // When
        val vm = buildViewModel()

        // Then
        assertNull(vm.state.value.song)
        assertFalse(vm.state.value.isPlaying)
        assertFalse(vm.state.value.isPreparing)
    }

    // endregion

    // region player initialization via onNext

    @Test
    fun `onNext initializes player with the next song previewUrl`() = runTest {
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
    fun `onNext does nothing when there is no next song`() = runTest {
        // Given — single song in playlist, already at last index
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
    fun `simulatePrepared starts playback updates duration and saves song as recent`() = runTest {
        // Given — playlist of 2 songs; factory injected; trigger switch via onNext
        val song1 = aSong(trackId = 1L)
        val song2 = aSong(trackId = 2L, previewUrl = "https://preview.m4a")
        playerController.selectSong(listOf(song1, song2), song1.trackId)
        val vm = buildViewModel()
        vm.onNext()
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
    fun `onPlayPause pauses playback when player is playing`() = runTest {
        // Given — switch to a song and trigger onPrepared to start playback
        val songs = listOf(aSong(trackId = 1L), aSong(trackId = 2L, previewUrl = "https://p.m4a"))
        playerController.selectSong(songs, songs[0].trackId)
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
    fun `onPlayPause resumes playback when player is paused`() = runTest {
        // Given — start and pause
        val songs = listOf(aSong(trackId = 1L), aSong(trackId = 2L, previewUrl = "https://p.m4a"))
        playerController.selectSong(songs, songs[0].trackId)
        val vm = buildViewModel()
        vm.onNext()
        advanceUntilIdle()
        fakePlayer.simulatePrepared()
        advanceUntilIdle()
        vm.onPlayPause()
        assertFalse(vm.state.value.isPlaying)

        // When
        vm.onPlayPause()

        // Then
        assertTrue(vm.state.value.isPlaying)
        assertTrue(fakePlayer.isPlaying)
    }

    // endregion

    // region onSeek

    @Test
    fun `onSeek updates currentPositionMs in state and forwards to player`() = runTest {
        // Given — player initialized
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
    fun `onToggleLoop alternates loopEnabled on each call`() = runTest {
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
    fun `onCompletion without loop advances to next song`() = runTest {
        // Given — 3-song playlist; navigate to song2 so song3 is the next
        val songs = listOf(
            aSong(trackId = 1L),
            aSong(trackId = 2L, previewUrl = "https://p2.m4a"),
            aSong(trackId = 3L, previewUrl = "https://p3.m4a"),
        )
        playerController.selectSong(songs, songs[0].trackId)
        val vm = buildViewModel()
        vm.onNext()
        advanceUntilIdle()
        fakePlayer.simulatePrepared()
        advanceUntilIdle()

        // When
        fakePlayer.simulateCompletion()
        advanceUntilIdle()

        // Then — advanced to song3
        assertEquals(3L, vm.state.value.song?.trackId)
    }

    @Test
    fun `onCompletion with loop enabled seeks to 0 and keeps playing`() = runTest {
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
    fun `simulateError sets error state in player`() = runTest {
        // Given — player initialized
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
    fun `onViewAlbum emits NavigateToAlbum with collectionId of the current song`() = runTest {
        // Given — song with null previewUrl so init does not create a real player
        val vm = buildViewModelWithNullPreviewSong(collectionId = 55L)

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
    fun `onCleared releases the audio player`() = runTest {
        // Given — player initialized via onNext
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
