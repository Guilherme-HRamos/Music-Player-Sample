package com.musicai.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SongEntityMapperTest {

    @Test
    fun `toDomain - todos os campos são mapeados corretamente`() {
        // Given
        val entity = aSongEntity(
            trackId = 7L,
            trackName = "Smile Like You Mean It",
            artistName = "The Killers",
            collectionName = "Hot Fuss",
            collectionId = 55L,
            artworkUrl = "https://example.com/art.jpg",
            previewUrl = "https://example.com/preview.m4a",
            trackTimeMillis = 195000L,
        )

        // When
        val song = entity.toDomain()

        // Then
        assertEquals(7L, song.trackId)
        assertEquals("Smile Like You Mean It", song.trackName)
        assertEquals("The Killers", song.artistName)
        assertEquals("Hot Fuss", song.collectionName)
        assertEquals(55L, song.collectionId)
        assertEquals("https://example.com/art.jpg", song.artworkUrl)
        assertEquals("https://example.com/preview.m4a", song.previewUrl)
        assertEquals(195000L, song.trackTimeMillis)
    }

    @Test
    fun `toDomain - previewUrl null é propagado para o domínio`() {
        // Given
        val entity = aSongEntity(previewUrl = null)

        // When
        val song = entity.toDomain()

        // Then
        assertNull(song.previewUrl)
    }

    @Test
    fun `toDomain - trackTimeMillis null é propagado para o domínio`() {
        // Given
        val entity = aSongEntity(trackTimeMillis = null)

        // When
        val song = entity.toDomain()

        // Then
        assertNull(song.trackTimeMillis)
    }

    @Test
    fun `toDomain - searchQuery e lastPlayedAt não existem no modelo de domínio`() {
        // Given — entity com dados de cache que não devem vazar para o domínio
        val entity = aSongEntity(searchQuery = "killers", lastPlayedAt = 1234567890L)

        // When
        val song = entity.toDomain()

        // Then — Song não expõe essas propriedades (verificação em compile-time pelo tipo)
        // O teste documenta que esses campos são intencionalmente excluídos do domínio
        assertEquals(entity.trackId, song.trackId)
        assertEquals(entity.trackName, song.trackName)
    }

    // region helpers

    private fun aSongEntity(
        trackId: Long = 1L,
        trackName: String = "Track",
        artistName: String = "Artist",
        collectionName: String = "Collection",
        collectionId: Long = 10L,
        artworkUrl: String = "https://example.com/art.jpg",
        previewUrl: String? = "https://example.com/preview.m4a",
        trackTimeMillis: Long? = 200000L,
        searchQuery: String = "",
        lastPlayedAt: Long? = null,
    ) = SongEntity(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        collectionName = collectionName,
        collectionId = collectionId,
        artworkUrl = artworkUrl,
        previewUrl = previewUrl,
        trackTimeMillis = trackTimeMillis,
        searchQuery = searchQuery,
        lastPlayedAt = lastPlayedAt,
    )

    // endregion
}
