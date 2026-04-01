package com.musicai.data.model

import com.google.gson.Gson
import com.musicai.util.JsonLoader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SongResponseMapperTest {

    // region toDomain

    @Test
    fun `toDomain maps artworkUrl100 to artworkUrl in domain model`() {
        // Given
        val response = aSongResponse(artworkUrl100 = "https://example.com/100x100.jpg")

        // When
        val song = response.toDomain()

        // Then
        assertEquals("https://example.com/100x100.jpg", song.artworkUrl)
    }

    @Test
    fun `toDomain maps all identifier fields correctly`() {
        // Given
        val response = aSongResponse(
            trackId = 42L,
            trackName = "When You Were Young",
            artistName = "The Killers",
            collectionName = "Sam's Town",
            collectionId = 99L,
        )

        // When
        val song = response.toDomain()

        // Then
        assertEquals(42L, song.trackId)
        assertEquals("When You Were Young", song.trackName)
        assertEquals("The Killers", song.artistName)
        assertEquals("Sam's Town", song.collectionName)
        assertEquals(99L, song.collectionId)
    }

    @Test
    fun `toDomain preserves null previewUrl`() {
        // Given
        val response = aSongResponse(previewUrl = null)

        // When
        val song = response.toDomain()

        // Then
        assertNull(song.previewUrl)
    }

    @Test
    fun `toDomain preserves null trackTimeMillis`() {
        // Given
        val response = aSongResponse(trackTimeMillis = null)

        // When
        val song = response.toDomain()

        // Then
        assertNull(song.trackTimeMillis)
    }

    // endregion

    // region toEntity

    @Test
    fun `toEntity stores the search query`() {
        // Given
        val response = aSongResponse()

        // When
        val entity = response.toEntity(query = "killers")

        // Then
        assertEquals("killers", entity.searchQuery)
    }

    @Test
    fun `toEntity defaults to empty string when query is not provided`() {
        // Given
        val response = aSongResponse()

        // When
        val entity = response.toEntity()

        // Then
        assertEquals("", entity.searchQuery)
    }

    @Test
    fun `toEntity defaults lastPlayedAt to null`() {
        // Given
        val response = aSongResponse()

        // When
        val entity = response.toEntity()

        // Then
        assertNull(entity.lastPlayedAt)
    }

    @Test
    fun `toEntity maps artworkUrl100 to artworkUrl`() {
        // Given
        val response = aSongResponse(artworkUrl100 = "https://example.com/art.jpg")

        // When
        val entity = response.toEntity()

        // Then
        assertEquals("https://example.com/art.jpg", entity.artworkUrl)
    }

    // endregion

    // region JSON fixtures

    @Test
    fun `toDomain with search_result fixture returns correct data for first result`() {
        // Given
        val json = JsonLoader.load("search_result.json")
        val response = Gson().fromJson(json, SearchResponse::class.java)

        // When
        val firstSong = response.results.first().toDomain()

        // Then
        assertEquals(1440891180L, firstSong.trackId)
        assertEquals("When You Were Young", firstSong.trackName)
        assertEquals("The Killers", firstSong.artistName)
    }

    @Test
    fun `toEntity with search_result fixture preserves searchQuery`() {
        // Given
        val json = JsonLoader.load("search_result.json")
        val response = Gson().fromJson(json, SearchResponse::class.java)

        // When
        val entity = response.results.first().toEntity(query = "killers")

        // Then
        assertEquals(1440891180L, entity.trackId)
        assertEquals("killers", entity.searchQuery)
        assertEquals(
            "https://is1-ssl.mzstatic.com/image/thumb/Music126/v4/11/64/9c/11649c80-2066-dba8-77a9-df7eecae26c1/17UM1IM06937.rgb.jpg/100x100bb.jpg",
            entity.artworkUrl,
        )
    }

    // endregion

    // region helpers

    private fun aSongResponse(
        trackId: Long = 1L,
        trackName: String = "Track Name",
        artistName: String = "Artist",
        collectionName: String = "Collection",
        collectionId: Long = 10L,
        artworkUrl100: String = "https://example.com/art.jpg",
        previewUrl: String? = "https://example.com/preview.m4a",
        trackTimeMillis: Long? = 210000L,
        wrapperType: String = "track",
        kind: String = "song",
    ) = SongResponse(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        collectionName = collectionName,
        collectionId = collectionId,
        artworkUrl100 = artworkUrl100,
        previewUrl = previewUrl,
        trackTimeMillis = trackTimeMillis,
        wrapperType = wrapperType,
        kind = kind,
    )

    // endregion
}
