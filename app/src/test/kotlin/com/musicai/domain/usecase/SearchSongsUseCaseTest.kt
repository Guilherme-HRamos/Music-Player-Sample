package com.musicai.domain.usecase

import com.musicai.domain.model.PaginatedSearch
import com.musicai.ui.utils.fakes.FakeSongRepository
import com.musicai.ui.utils.mocks.getMockSongsList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchSongsUseCaseTest {

    @Test
    fun `when repository returns success then invoke should return search result`() = runTest {
        // Given
        val songs = getMockSongsList(3)
        val paginated = PaginatedSearch(songs = songs, hasMore = true)
        val repository = FakeSongRepository.Success(paginatedSearch = paginated)
        val useCase = SearchSongsUseCaseImpl(repository)

        // When
        val result = useCase("query", 1)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(paginated, result.getOrNull())
        assertEquals(1, repository.searchSongsCalls)
        assertEquals("query", repository.lastSearchQuery)
        assertEquals(1, repository.lastSearchPage)
    }

    @Test
    fun `when repository returns error then invoke should return failure`() = runTest {
        // Given
        val repository = FakeSongRepository.Error()
        val useCase = SearchSongsUseCaseImpl(repository)

        // When
        val result = useCase("query", 1)

        // Then
        assertTrue(result.isFailure)
        assertEquals(1, repository.searchSongsCalls)
        assertEquals("query", repository.lastSearchQuery)
        assertEquals(1, repository.lastSearchPage)
    }
}
