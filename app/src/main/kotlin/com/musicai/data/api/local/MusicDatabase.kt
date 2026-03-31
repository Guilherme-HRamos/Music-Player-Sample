package com.musicai.data.api.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.musicai.data.model.SongEntity

@Database(
    entities = [SongEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
}
