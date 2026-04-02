package com.musicai.data.model

import com.musicai.ui.utils.mocks.getMockRecentSongEntity
import com.musicai.ui.utils.mocks.getMockSongEntity
import com.musicai.ui.utils.mocks.getMockSongResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class DataMapperTest {

    @Test
    fun `when SongResponse toDomain then should map correctly`() {
        // Given
        val response = getMockSongResponse(1L)

        // When
        val domain = response.toDomain()

        // Then
        assertEquals(response.trackId, domain.trackId)
        assertEquals(response.trackName, domain.trackName)
        assertEquals(response.artistName, domain.artistName)
        assertEquals(response.collectionName, domain.collectionName)
        assertEquals(response.collectionId, domain.collectionId)
        assertEquals(response.artworkUrl100, domain.artworkUrl)
        assertEquals(response.previewUrl, domain.previewUrl)
        assertEquals(response.trackTimeMillis, domain.trackTimeMillis)
    }

    @Test
    fun `when SongResponse toEntity then should map correctly`() {
        // Given
        val response = getMockSongResponse(1L)
        val query = "search query"

        // When
        val entity = response.toEntity(query)

        // Then
        assertEquals(response.trackId, entity.trackId)
        assertEquals(response.trackName, entity.trackName)
        assertEquals(response.artistName, entity.artistName)
        assertEquals(response.collectionName, entity.collectionName)
        assertEquals(response.collectionId, entity.collectionId)
        assertEquals(response.artworkUrl100, entity.artworkUrl)
        assertEquals(response.previewUrl, entity.previewUrl)
        assertEquals(response.trackTimeMillis, entity.trackTimeMillis)
        assertEquals(query, entity.searchQuery)
    }

    @Test
    fun `when SongEntity toDomain then should map correctly`() {
        // Given
        val entity = getMockSongEntity(1L, "query")

        // When
        val domain = entity.toDomain()

        // Then
        assertEquals(entity.trackId, domain.trackId)
        assertEquals(entity.trackName, domain.trackName)
        assertEquals(entity.artistName, domain.artistName)
        assertEquals(entity.collectionName, domain.collectionName)
        assertEquals(entity.collectionId, domain.collectionId)
        assertEquals(entity.artworkUrl, domain.artworkUrl)
        assertEquals(entity.previewUrl, domain.previewUrl)
        assertEquals(entity.trackTimeMillis, domain.trackTimeMillis)
    }

    @Test
    fun `when RecentSongEntity toDomain then should map correctly`() {
        // Given
        val entity = getMockRecentSongEntity(1L)

        // When
        val domain = entity.toDomain()

        // Then
        assertEquals(entity.trackId, domain.trackId)
        assertEquals(entity.trackName, domain.trackName)
        assertEquals(entity.artistName, domain.artistName)
        assertEquals(entity.collectionName, domain.collectionName)
        assertEquals(entity.collectionId, domain.collectionId)
        assertEquals(entity.artworkUrl, domain.artworkUrl)
        assertEquals(entity.previewUrl, domain.previewUrl)
        assertEquals(entity.trackTimeMillis, domain.trackTimeMillis)
    }
}
