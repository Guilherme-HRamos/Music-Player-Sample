package com.musicai.data.api.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.musicai.data.model.SongEntity

@Dao
interface SongDao {

    @Upsert
    suspend fun upsertAll(songs: List<SongEntity>)

    @Query("SELECT * FROM songs WHERE searchQuery = :query ORDER BY trackName ASC")
    suspend fun searchCached(query: String): List<SongEntity>

    @Query("SELECT * FROM songs WHERE collectionId = :collectionId ORDER BY trackName ASC")
    suspend fun getAlbumSongs(collectionId: Long): List<SongEntity>
}
