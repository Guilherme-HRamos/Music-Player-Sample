package com.musicai.domain.usecase

import com.musicai.ui.utils.fakes.FakeSongRepository
import com.musicai.ui.utils.mocks.getMockSong
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SaveRecentSongUseCaseTest {

    @Test
    fun `when invoke called then should call repository markAsPlayed`() = runTest {
        // Given
        val repository = FakeSongRepository.Success()
        val useCase = SaveRecentSongUseCaseImpl(repository)
        val song = getMockSong()

        // When
        useCase(song)

        // Then
        assertEquals(1, repository.markAsPlayedCalls)
        assertEquals(song, repository.lastMarkedSong)
    }
}
