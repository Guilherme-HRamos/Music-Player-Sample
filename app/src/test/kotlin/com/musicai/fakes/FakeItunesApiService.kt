package com.musicai.fakes

import com.musicai.data.model.SearchResponse
import com.musicai.data.network.ItunesApiService

class FakeItunesApiService : ItunesApiService {

    var searchResponse: SearchResponse = SearchResponse(0, emptyList())
    var lookupResponse: SearchResponse = SearchResponse(0, emptyList())
    var searchThrows: Exception? = null
    var lookupThrows: Exception? = null

    override suspend fun searchSongs(
        term: String,
        limit: Int,
        offset: Int,
        entity: String,
        media: String,
    ): SearchResponse {
        searchThrows?.let { throw it }
        return searchResponse
    }

    override suspend fun lookupAlbum(
        collectionId: Long,
        entity: String,
    ): SearchResponse {
        lookupThrows?.let { throw it }
        return lookupResponse
    }
}
