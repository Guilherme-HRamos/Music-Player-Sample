package com.musicai.ui.shared

import com.musicai.domain.model.Song
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds the currently playing song and the surrounding playlist so the
 * PlayerViewModel can implement previous/next without depending on a
 * repository during navigation.
 */
@Singleton
class PlayerController @Inject constructor() {

    var playlist: List<Song> = emptyList()
        private set
    var currentIndex: Int = 0
        private set

    val currentSong: Song?
        get() = playlist.getOrNull(currentIndex)

    fun selectSong(songs: List<Song>, trackId: Long) {
        playlist = songs
        currentIndex = songs.indexOfFirst { it.trackId == trackId }.coerceAtLeast(0)
    }

    fun next(): Song? {
        if (currentIndex < playlist.lastIndex) {
            currentIndex++
            return currentSong
        }
        return null
    }

    fun previous(): Song? {
        if (currentIndex > 0) {
            currentIndex--
            return currentSong
        }
        return null
    }

    val hasNext: Boolean get() = currentIndex < playlist.lastIndex
    val hasPrevious: Boolean get() = currentIndex > 0
}
