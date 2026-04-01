package com.musicai.data.api.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.musicai.data.model.RecentSongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentSongDao {

    @Upsert
    suspend fun upsert(song: RecentSongEntity)

    @Query("SELECT * FROM recent_songs ORDER BY playedAt DESC LIMIT :limit")
    fun getRecentlyPlayed(limit: Int): Flow<List<RecentSongEntity>>
}
