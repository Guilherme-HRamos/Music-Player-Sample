package com.musicai.plugin.audioPlayer

import android.media.AudioAttributes
import android.media.MediaPlayer

class MediaAudioPlayer : AudioPlayer {

    private val mediaPlayer = MediaPlayer().apply {
        setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build(),
        )
    }

    override val isPlaying: Boolean get() = mediaPlayer.isPlaying
    override val currentPosition: Int get() = mediaPlayer.currentPosition
    override val duration: Int get() = mediaPlayer.duration

    override fun setDataSource(url: String) = mediaPlayer.setDataSource(url)
    override fun prepareAsync() = mediaPlayer.prepareAsync()
    override fun start() = mediaPlayer.start()
    override fun pause() = mediaPlayer.pause()
    override fun seekTo(positionMs: Int) = mediaPlayer.seekTo(positionMs)
    override fun release() = mediaPlayer.release()

    override fun setOnPreparedListener(listener: (AudioPlayer) -> Unit) {
        mediaPlayer.setOnPreparedListener { listener(this) }
    }

    override fun setOnCompletionListener(listener: (AudioPlayer) -> Unit) {
        mediaPlayer.setOnCompletionListener { listener(this) }
    }

    override fun setOnErrorListener(listener: (AudioPlayer, Int, Int) -> Boolean) {
        mediaPlayer.setOnErrorListener { _, what, extra -> listener(this, what, extra) }
    }
}
