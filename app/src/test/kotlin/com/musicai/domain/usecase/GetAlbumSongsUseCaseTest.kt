package com.musicai.domain.usecase

import com.musicai.domain.model.Song
import com.musicai.fakes.FakeSongRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetAlbumSongsUseCaseTest {

    private lateinit var fakeRepository: FakeSongRepository
    private lateinit var useCase: GetAlbumSongsUseCaseImpl

    @Before
    fun setUp() {
        fakeRepository = FakeSongRepository()
        useCase = GetAlbumSongsUseCaseImpl(fakeRepository)
    }

    @Test
    fun `invoke builds Album with metadata from the first song`() = runTest {
        // Given
        val songs = listOf(
            aSong(trackId = 1L, collectionName = "Sam's Town", artistName = "The Killers", artworkUrl = "https://art.jpg"),
            aSong(trackId = 2L, collectionName = "Sam's Town", artistName = "The Killers", artworkUrl = "https://art.jpg"),
        )
        fakeRepository.albumSongsResult = Result.success(songs)

        // When
        val result = useCase(collectionId = 1440891236L)

        // Then
        assertTrue(result.isSuccess)
        val album = result.getOrNull()!!
        assertEquals("Sam's Town", album.collectionName)
        assertEquals("The Killers", album.artistName)
        assertEquals("https://art.jpg", album.artworkUrl)
        assertEquals(songs, album.songs)
    }

    @Test
    fun `invoke returns Album with empty strings when songs list is empty`() = runTest {
        // Given
        fakeRepository.albumSongsResult = Result.success(emptyList())

        // When
        val result = useCase(collectionId = 99L)

        // Then
        assertTrue(result.isSuccess)
        val album = result.getOrNull()!!
        assertEquals("", album.collectionName)
        assertEquals("", album.artistName)
        assertEquals("", album.artworkUrl)
        assertTrue(album.songs.isEmpty())
    }

    @Test
    fun `invoke uses collectionId from parameter in Album and passes it to repository`() = runTest {
        // Given
        fakeRepository.albumSongsResult = Result.success(listOf(aSong()))

        // When
        val result = useCase(collectionId = 777L)

        // Then
        assertEquals(777L, fakeRepository.albumSongsCallId)
        assertEquals(777L, result.getOrNull()?.collectionId)
    }

    @Test
    fun `invoke propagates repository failure as Result Failure`() = runTest {
        // Given
        fakeRepository.albumSongsResult = Result.failure(RuntimeException("not found"))

        // When
        val result = useCase(collectionId = 1L)

        // Then
        assertTrue(result.isFailure)
        assertEquals("not found", result.exceptionOrNull()?.message)
    }

    // region helpers

    private fun aSong(
        trackId: Long = 1L,
        collectionName: String = "Collection",
        artistName: String = "Artist",
        artworkUrl: String = "https://example.com/art.jpg",
        collectionId: Long = 100L,
    ) = Song(
        trackId = trackId,
        trackName = "Track $trackId",
        artistName = artistName,
        collectionName = collectionName,
        collectionId = collectionId,
        artworkUrl = artworkUrl,
        previewUrl = "https://example.com/preview.m4a",
        trackTimeMillis = 200000L,
    )

    // endregion
}
