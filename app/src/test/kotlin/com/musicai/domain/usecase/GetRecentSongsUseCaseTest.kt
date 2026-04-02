package com.musicai.domain.usecase

import com.musicai.ui.utils.fakes.FakeSongRepository
import com.musicai.ui.utils.mocks.getMockSongsList
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetRecentSongsUseCaseTest {

    @Test
    fun `when repository emits songs then invoke should emit same songs`() = runTest {
        // Given
        val songs = getMockSongsList(5)
        val repository = FakeSongRepository.Success(recentSongs = songs)
        val useCase = GetRecentSongsUseCaseImpl(repository)
        val limit = 3

        // When
        val result = useCase(limit).first()

        // Then
        assertEquals(limit, result.size)
        assertEquals(songs.take(limit), result)
        assertEquals(1, repository.getRecentSongsCalls)
        assertEquals(limit, repository.lastRecentSongsLimit)
    }

    @Test
    fun `when repository emits empty list then invoke should emit empty list`() = runTest {
        // Given
        val repository = FakeSongRepository.Empty()
        val useCase = GetRecentSongsUseCaseImpl(repository)

        // When
        val result = useCase().first()

        // Then
        assertTrue(result.isEmpty())
        assertEquals(1, repository.getRecentSongsCalls)
        assertEquals(20, repository.lastRecentSongsLimit) // Default limit
    }
}
