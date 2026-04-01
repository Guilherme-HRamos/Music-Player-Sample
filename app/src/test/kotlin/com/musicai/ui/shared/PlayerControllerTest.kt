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
    fun `selectSong finds song by trackId and sets the correct index`() {
        // Given
        val songs = listOf(aSong(10L), aSong(20L), aSong(30L))

        // When
        controller.selectSong(songs, trackId = 20L)

        // Then
        assertEquals(1, controller.currentIndex)
        assertEquals(20L, controller.currentSong?.trackId)
    }

    @Test
    fun `selectSong defaults to index 0 when trackId is not found`() {
        // Given
        val songs = listOf(aSong(10L), aSong(20L), aSong(30L))

        // When
        controller.selectSong(songs, trackId = 999L)

        // Then
        assertEquals(0, controller.currentIndex)
        assertEquals(10L, controller.currentSong?.trackId)
    }

    @Test
    fun `selectSong replaces the previous playlist`() {
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
    fun `next advances index and returns the next song`() {
        // Given
        val songs = listOf(aSong(1L), aSong(2L), aSong(3L))
        controller.selectSong(songs, trackId = 1L)

        // When
        val next = controller.next()

        // Then
        assertEquals(2L, next?.trackId)
        assertEquals(1, controller.currentIndex)
    }

    @Test
    fun `next returns null and does not advance when on the last song`() {
        // Given
        val songs = listOf(aSong(1L), aSong(2L), aSong(3L))
        controller.selectSong(songs, trackId = 3L)

        // When
        val next = controller.next()

        // Then
        assertNull(next)
        assertEquals(2, controller.currentIndex)
    }

    @Test
    fun `next reflects correct hasNext and hasPrevious after advancing`() {
        // Given
        val songs = listOf(aSong(1L), aSong(2L), aSong(3L))
        controller.selectSong(songs, trackId = 1L)

        // When
        controller.next()

        // Then — now at index 1
        assertTrue(controller.hasNext)
        assertTrue(controller.hasPrevious)
    }

    // endregion

    // region previous

    @Test
    fun `previous decrements index and returns the previous song`() {
        // Given
        val songs = listOf(aSong(1L), aSong(2L), aSong(3L))
        controller.selectSong(songs, trackId = 3L)

        // When
        val prev = controller.previous()

        // Then
        assertEquals(2L, prev?.trackId)
        assertEquals(1, controller.currentIndex)
    }

    @Test
    fun `previous returns null and does not go back when on the first song`() {
        // Given
        val songs = listOf(aSong(1L), aSong(2L), aSong(3L))
        controller.selectSong(songs, trackId = 1L)

        // When
        val prev = controller.previous()

        // Then
        assertNull(prev)
        assertEquals(0, controller.currentIndex)
    }

    // endregion

    // region hasNext / hasPrevious

    @Test
    fun `hasNext is false when on the last song`() {
        // Given
        val songs = listOf(aSong(1L), aSong(2L))
        controller.selectSong(songs, trackId = 2L)

        // When / Then
        assertFalse(controller.hasNext)
    }

    @Test
    fun `hasPrevious is false when on the first song`() {
        // Given
        val songs = listOf(aSong(1L), aSong(2L))
        controller.selectSong(songs, trackId = 1L)

        // When / Then
        assertFalse(controller.hasPrevious)
    }

    @Test
    fun `hasNext and hasPrevious are both true when in the middle of the playlist`() {
        // Given
        val songs = listOf(aSong(1L), aSong(2L), aSong(3L))

        // When
        controller.selectSong(songs, trackId = 2L)

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
