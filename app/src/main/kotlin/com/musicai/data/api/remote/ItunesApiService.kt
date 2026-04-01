package com.musicai.data.api.remote

import com.musicai.data.model.SearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesApiService {

    @GET("search")
    suspend fun searchSongs(
        @Query("term") term: String,
        @Query("limit") limit: Int = PAGE_SIZE,
        @Query("offset") offset: Int = 0,
        @Query("entity") entity: String = "song",
        @Query("media") media: String = "music",
        @Query("country") country: String = "US",
    ): SearchResponse

    @GET("lookup")
    suspend fun lookupAlbum(
        @Query("id") collectionId: Long,
        @Query("entity") entity: String = "song",
    ): SearchResponse

    companion object {
        const val BASE_URL = "https://itunes.apple.com/"
        const val PAGE_SIZE = 20
    }
}
