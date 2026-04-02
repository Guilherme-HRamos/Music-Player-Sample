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
    }
}
