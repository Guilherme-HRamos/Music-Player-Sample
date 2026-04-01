package com.musicai.ui.shared

import com.musicai.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PlayerControllerTest {

    private lateinit var controller: PlayerController

    @Before
    fun setUp() {
        controller = PlayerController()
    }

    // region selectSong

    @Test
    fun `selectSong - encontra song pelo trackId e define índice correto`() {
        // Given
        val songs = listOf(aSong(10L), aSong(20L), aSong(30L))

        // When
        controller.selectSong(songs, trackId = 20L)

        // Then
        assertEquals(1, controller.currentIndex)
        assertEquals(20L, controller.currentSong?.trackId)
    }

    @Test
    fun `selectSong - trackId desconhecido usa índice 0 por padrão`() {
        // Given
        val songs = listOf(aSong(10L), aSong(20L), aSong(30L))

        // When
        controller.selectSong(songs, trackId = 999L)

        // Then
        assertEquals(0, controller.currentIndex)
        assertEquals(10L, controller.currentSong?.trackId)
    }

    @Test
    fun `selectSong - substitui playlist anterior`() {
        // Given
        val firstPlaylist = listOf(aSong(1L), aSong(2L))
        val secondPlaylist = listOf(aSong(10L), aSong(20L), aSong(30L))
        controller.selectSong(firstPlaylist, trackId = 1L)

        // When
        controller.selectSong(secondPlaylist, trackId = 20L)

        // Then
        assertEquals(secondPlaylist, controller.playlist)
        assertEquals(20L, controller.currentSong?.trackId)
    }

    // endregion

    // region next

    @Test
    fun `next - avança o índice e retorna a próxima song`() {
        // Given
        val songs = listOf(aSong(1L), aSong(2L), aSong(3L))
        controller.selectSong(songs, trackId = 1L) // índice 0

        // When
        val next = controller.next()

        // Then
        assertEquals(2L, next?.trackId)
        assertEquals(1, controller.currentIndex)
    }

    @Test
    fun `next - retorna null e não avança quando está na última song`() {
        // Given
        val songs = listOf(aSong(1L), aSong(2L), aSong(3L))
        controller.selectSong(songs, trackId = 3L) // índice 2 (lastIndex)

        // When
        val next = controller.next()

        // Then
        assertNull(next)
        assertEquals(2, controller.currentIndex)
    }

    @Test
    fun `next - após avançar hasNext e hasPrevious refletem posição correta`() {
        // Given
        val songs = listOf(aSong(1L), aSong(2L), aSong(3L))
        controller.selectSong(songs, trackId = 1L)

        // When
        controller.next() // agora no índice 1

        // Then
        assertTrue(controller.hasNext)
        assertTrue(controller.hasPrevious)
    }

    // endregion

    // region previous

    @Test
    fun `previous - volta o índice e retorna a song anterior`() {
        // Given
        val songs = listOf(aSong(1L), aSong(2L), aSong(3L))
        controller.selectSong(songs, trackId = 3L) // índice 2

        // When
        val prev = controller.previous()

        // Then
        assertEquals(2L, prev?.trackId)
        assertEquals(1, controller.currentIndex)
    }

    @Test
    fun `previous - retorna null e não volta quando está na primeira song`() {
        // Given
        val songs = listOf(aSong(1L), aSong(2L), aSong(3L))
        controller.selectSong(songs, trackId = 1L) // índice 0

        // When
        val prev = controller.previous()

        // Then
        assertNull(prev)
        assertEquals(0, controller.currentIndex)
    }

    // endregion

    // region hasNext / hasPrevious

    @Test
    fun `hasNext é false quando está na última song`() {
        // Given
        val songs = listOf(aSong(1L), aSong(2L))
        controller.selectSong(songs, trackId = 2L) // lastIndex

        // Then
        assertFalse(controller.hasNext)
    }

    @Test
    fun `hasPrevious é false quando está na primeira song`() {
        // Given
        val songs = listOf(aSong(1L), aSong(2L))
        controller.selectSong(songs, trackId = 1L) // índice 0

        // Then
        assertFalse(controller.hasPrevious)
    }

    @Test
    fun `hasNext e hasPrevious são true quando no meio da playlist`() {
        // Given
        val songs = listOf(aSong(1L), aSong(2L), aSong(3L))
        controller.selectSong(songs, trackId = 2L) // índice 1

        // Then
        assertTrue(controller.hasNext)
        assertTrue(controller.hasPrevious)
    }

    // endregion

    // region helpers

    private fun aSong(trackId: Long) = Song(
        trackId = trackId,
        trackName = "Track $trackId",
        artistName = "Artist",
        collectionName = "Album",
        collectionId = 100L,
        artworkUrl = "https://example.com/art.jpg",
        previewUrl = "https://example.com/preview.m4a",
        trackTimeMillis = 200000L,
    )

    // endregion
}
