package com.musicai.domain.usecase

import com.musicai.ui.utils.fakes.FakeSongRepository
import com.musicai.ui.utils.mocks.getMockSongsList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetAlbumSongsUseCaseTest {

    @Test
    fun `when repository returns success then invoke should return mapped album`() = runTest {
        // Given
        val songs = getMockSongsList(3)
        val repository = FakeSongRepository.Success(songs = songs)
        val useCase = GetAlbumSongsUseCaseImpl(repository)
        val collectionId = 1L

        // When
        val result = useCase(collectionId)

        // Then
        assertTrue(result.isSuccess)
        val album = result.getOrNull()
        assertEquals(collectionId, album?.collectionId)
        assertEquals(3, album?.songs?.size)
        assertEquals(songs[0].collectionName, album?.collectionName)
        assertEquals(1, repository.getAlbumSongsCalls)
        assertEquals(collectionId, repository.lastAlbumCollectionId)
    }

    @Test
    fun `when repository returns empty list then invoke should return album with empty strings`() = runTest {
        // Given
        val repository = FakeSongRepository.Success(songs = emptyList())
        val useCase = GetAlbumSongsUseCaseImpl(repository)
        val collectionId = 1L

        // When
        val result = useCase(collectionId)

        // Then
        assertTrue(result.isSuccess)
        val album = result.getOrNull()
        assertEquals(collectionId, album?.collectionId)
        assertTrue(album?.songs?.isEmpty() == true)
        assertEquals("", album?.collectionName)
        assertEquals("", album?.artistName)
        assertEquals(1, repository.getAlbumSongsCalls)
        assertEquals(collectionId, repository.lastAlbumCollectionId)
    }

    @Test
    fun `when repository returns error then invoke should return failure`() = runTest {
        // Given
        val repository = FakeSongRepository.Error()
        val useCase = GetAlbumSongsUseCaseImpl(repository)

        // When
        val result = useCase(1L)

        // Then
        assertTrue(result.isFailure)
        assertEquals(1, repository.getAlbumSongsCalls)
        assertEquals(1L, repository.lastAlbumCollectionId)
    }
}
