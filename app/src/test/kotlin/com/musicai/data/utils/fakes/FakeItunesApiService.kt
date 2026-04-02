package com.musicai.data.utils.fakes

import com.musicai.data.api.remote.ItunesApiService
import com.musicai.data.model.SearchResponse
import com.musicai.ui.utils.mocks.getMockSearchResponse

internal class FakeItunesApiService : ItunesApiService {
    var searchSongsCalls = 0
        private set
    var lastSearchTerm: String? = null
        private set
    var lastSearchLimit: Int? = null
        private set

    // Allows setting a list of responses to be returned in order
    private val searchResponses = mutableListOf<SearchResponse>()
    var defaultSearchResponse: SearchResponse = SearchResponse(0, emptyList())
    var error: Throwable? = null

    fun addSearchResponse(response: SearchResponse) {
        searchResponses.add(response)
    }

    override suspend fun searchSongs(
        term: String,
        limit: Int,
        entity: String,
        media: String,
        country: String
    ): SearchResponse {
        searchSongsCalls++
        lastSearchTerm = term
        lastSearchLimit = limit
        error?.let { throw it }
        
        return if (searchResponses.isNotEmpty()) {
            searchResponses.removeAt(0)
        } else {
            defaultSearchResponse
        }
    }

    var lookupAlbumCalls = 0
        private set
    var lastLookupId: Long? = null
        private set

    override suspend fun lookupAlbum(collectionId: Long, entity: String): SearchResponse {
        lookupAlbumCalls++
        lastLookupId = collectionId
        error?.let { throw it }
        return defaultSearchResponse
    }
}
