package com.musicai.data.repository

import com.google.gson.Gson
import com.musicai.data.model.SearchResponse
import com.musicai.data.model.SongEntity
import com.musicai.data.model.SongResponse
import com.musicai.domain.model.Song
import com.musicai.fakes.FakeItunesApiService
import com.musicai.fakes.FakeSongDao
import com.musicai.util.JsonLoader
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class SongRepositoryImplTest {

    private lateinit var fakeApi: FakeItunesApiService
    private lateinit var fakeDao: FakeSongDao
    private lateinit var repository: SongRepositoryImpl

    @Before
    fun setUp() {
        fakeApi = FakeItunesApiService()
        fakeDao = FakeSongDao()
        repository = SongRepositoryImpl(fakeApi, fakeDao)
    }

    // region searchSongs

    @Test
    fun `searchSongs returns domain songs and caches entities on success`() = runTest {
        // Given
        val json = JsonLoader.load("search_result.json")
        fakeApi.searchResponse = Gson().fromJson(json, SearchResponse::class.java)

        // When
        val result = repository.searchSongs(query = "killers", page = 0)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(18, result.getOrNull()?.size)
        assertEquals(18, fakeDao.upsertedSongs.size)
        assertTrue(fakeDao.upsertedSongs.all { it.searchQuery == "killers" })
    }

    @Test
    fun `searchSongs filters out results where kind is not song`() = runTest {
        // Given — response with 1 collection wrapper and 2 songs
        fakeApi.searchResponse = SearchResponse(
            resultCount = 3,
            results = listOf(
                aSongResponse(trackId = 1L, kind = "collection"),
                aSongResponse(trackId = 2L, kind = "song"),
                aSongResponse(trackId = 3L, kind = "song"),
            ),
        )

        // When
        val result = repository.searchSongs(query = "test", page = 0)

        // Then
        assertEquals(2, result.getOrNull()?.size)
        assertEquals(2, fakeDao.upsertedSongs.size)
        assertTrue(fakeDao.upsertedSongs.none { it.trackId == 1L })
    }

    @Test
    fun `searchSongs returns cached songs when network fails`() = runTest {
        // Given
        fakeApi.searchThrows = IOException("no network")
        fakeDao.cachedSongs = listOf(
            aSongEntity(trackId = 10L),
            aSongEntity(trackId = 11L),
            aSongEntity(trackId = 12L),
        )

        // When
        val result = repository.searchSongs(query = "killers", page = 0)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()?.size)
    }

    @Test
    fun `searchSongs propagates exception when network fails and cache is empty`() = runTest {
        // Given
        fakeApi.searchThrows = IOException("no network")
        fakeDao.cachedSongs = emptyList()

        // When
        val result = repository.searchSongs(query = "killers", page = 0)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    // endregion

    // region getAlbumSongs

    @Test
    fun `getAlbumSongs filters collection wrapper and returns only track results`() = runTest {
        // Given — album_result.json has 1 collection + 12 songs = 13 total results
        val json = JsonLoader.load("album_result.json")
        fakeApi.lookupResponse = Gson().fromJson(json, SearchResponse::class.java)

        // When
        val result = repository.getAlbumSongs(collectionId = 1440891236L)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(12, result.getOrNull()?.size)
    }

    @Test
    fun `getAlbumSongs returns cached songs when network fails`() = runTest {
        // Given
        fakeApi.lookupThrows = IOException("no network")
        fakeDao.albumSongs = listOf(
            aSongEntity(trackId = 1L, collectionId = 99L),
            aSongEntity(trackId = 2L, collectionId = 99L),
        )

        // When
        val result = repository.getAlbumSongs(collectionId = 99L)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun `getAlbumSongs propagates exception when network fails and cache is empty`() = runTest {
        // Given
        fakeApi.lookupThrows = IOException("no network")
        fakeDao.albumSongs = emptyList()

        // When
        val result = repository.getAlbumSongs(collectionId = 1L)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    // endregion

    // region markAsPlayed

    @Test
    fun `markAsPlayed delegates to DAO with correct trackId and positive timestamp`() = runTest {
        // Given
        val song = aSong(trackId = 42L)

        // When
        repository.markAsPlayed(song)

        // Then
        assertEquals(1, fakeDao.updatedTimestamps.size)
        val (trackId, timestamp) = fakeDao.updatedTimestamps.first()
        assertEquals(42L, trackId)
        assertTrue("Timestamp must be greater than zero", timestamp > 0L)
    }

    // endregion

    // region helpers

    private fun aSongResponse(
        trackId: Long = 1L,
        kind: String = "song",
        collectionId: Long = 100L,
    ) = SongResponse(
        trackId = trackId,
        trackName = "Track $trackId",
        artistName = "Artist",
        collectionName = "Album",
        collectionId = collectionId,
        artworkUrl100 = "https://example.com/art.jpg",
        previewUrl = "https://example.com/preview.m4a",
        trackTimeMillis = 200000L,
        wrapperType = "track",
        kind = kind,
    )

    private fun aSongEntity(
        trackId: Long = 1L,
        collectionId: Long = 100L,
    ) = SongEntity(
        trackId = trackId,
        trackName = "Track $trackId",
        artistName = "Artist",
        collectionName = "Album",
        collectionId = collectionId,
        artworkUrl = "https://example.com/art.jpg",
        previewUrl = "https://example.com/preview.m4a",
        trackTimeMillis = 200000L,
        searchQuery = "",
        lastPlayedAt = null,
    )

    private fun aSong(trackId: Long = 1L) = Song(
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
