package com.musicai.data.repository

import com.musicai.data.utils.fakes.FakeItunesApiService
import com.musicai.data.utils.fakes.FakeRecentSongDao
import com.musicai.data.utils.fakes.FakeSongDao
import com.musicai.ui.utils.fakes.MutedLogger
import com.musicai.ui.utils.mocks.getMockSearchResponse
import com.musicai.ui.utils.mocks.getMockSong
import com.musicai.ui.utils.mocks.getMockSongEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SongRepositoryImplTest {

    private lateinit var api: FakeItunesApiService
    private lateinit var dao: FakeSongDao
    private lateinit var recentDao: FakeRecentSongDao
    private lateinit var repository: SongRepositoryImpl

    @Before
    fun setup() {
        api = FakeItunesApiService()
        dao = FakeSongDao()
        recentDao = FakeRecentSongDao()
        repository = SongRepositoryImpl(api, dao, recentDao, MutedLogger())
    }

    @Test
    fun `when search new query then should fetch from API and save to DAO`() = runTest {
        // Given
        val query = "query"
        api.addSearchResponse(getMockSearchResponse(count = 40))

        // When
        val result = repository.searchSongs(query, 1)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(20, result.getOrNull()?.songs?.size)
        assertEquals(1, api.searchSongsCalls)
        assertEquals(1, dao.upsertAllCalls)
        assertEquals(40, dao.buffer.size)
    }

    @Test
    fun `when search page 2 then should get from buffer and prefetch next slot`() = runTest {
        // Given
        val query = "query"
        // Slot 1: items 1-40
        api.addSearchResponse(getMockSearchResponse(count = 40, startId = 1))
        // Slot 2: items 1-80 (returning items 1-80 as simulated search results)
        api.addSearchResponse(getMockSearchResponse(count = 80, startId = 1))
        
        repository.searchSongs(query, 1) // First call: fetches items 1-40

        // When
        val page2Result = repository.searchSongs(query, 2)
        val page3Result = repository.searchSongs(query, 3)

        // Then
        // Page 2: returning items 21-40 from buffer, but also prefetching items 41-80
        assertTrue(page2Result.isSuccess)
        val songsPage2 = page2Result.getOrNull()?.songs ?: emptyList()
        assertEquals(20, songsPage2.size)
        assertEquals(21L, songsPage2.first().trackId)

        // Verify prefetch happened
        assertEquals(2, api.searchSongsCalls)
        assertEquals(80, api.lastSearchLimit)
        assertEquals(80, dao.buffer.size)

        // Page 3: returning items 41-60 from the prefetched buffer
        assertTrue(page3Result.isSuccess)
        val songsPage3 = page3Result.getOrNull()?.songs ?: emptyList()
        assertEquals(20, songsPage3.size)
        assertEquals(41L, songsPage3.first().trackId)
    }

    @Test
    fun `when API returns fewer results than limit then should mark as end reached`() = runTest {
        // Given
        val query = "query"
        // Slot 1: API returns only 30 items even though slot is 40
        api.addSearchResponse(getMockSearchResponse(count = 30))

        // When
        val result = repository.searchSongs(query, 1) // Page 1: 1-20
        val page2 = repository.searchSongs(query, 2) // Page 2: 21-30

        // Then
        assertTrue(result.getOrNull()?.hasMore == true) // More in buffer (30 > 20)
        
        assertTrue(page2.isSuccess)
        val page2Data = page2.getOrNull()
        assertEquals(10, page2Data?.songs?.size)
        assertFalse(page2Data?.hasMore ?: true) // Reached end at 30
    }

    @Test
    fun `when API failure then should recover from DAO searchCached`() = runTest {
        // Given
        val query = "query"
        dao.upsertAll(listOf(getMockSongEntity(1L, query), getMockSongEntity(2L, query)))
        api.error = RuntimeException("API Error")

        // When
        val result = repository.searchSongs(query, 1)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.songs?.size)
        assertEquals(1, dao.searchCachedCalls)
    }

    @Test
    fun `when getAlbumSongs success then should fetch from API and save to DAO`() = runTest {
        // Given
        val id = 1L
        api.defaultSearchResponse = getMockSearchResponse(count = 5)

        // When
        val result = repository.getAlbumSongs(id)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(5, result.getOrNull()?.size)
        assertEquals(1, dao.upsertAllCalls)
    }

    @Test
    fun `when markAsPlayed then should call recentDao upsert`() = runTest {
        // Given
        val song = getMockSong()

        // When
        repository.markAsPlayed(song)

        // Then
        assertEquals(1, recentDao.upsertCalls)
        assertEquals(song.trackId, recentDao.lastUpsertedSong?.trackId)
    }
}
