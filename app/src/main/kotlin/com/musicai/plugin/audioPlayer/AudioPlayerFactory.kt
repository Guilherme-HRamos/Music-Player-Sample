package com.musicai.plugin.audioPlayer

/**
 * Functional factory to create instances of [AudioPlayer].
 *
 * Essential for dependency injection and unit testing, as it avoids direct
 * instantiation of [MediaAudioPlayer] which contains Android-specific
 * dependencies like [android.media.MediaPlayer].
 */
fun interface AudioPlayerFactory {
    fun create(): AudioPlayer
}
