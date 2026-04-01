package com.musicai.plugin.audioPlayer

interface AudioPlayer {
    val isPlaying: Boolean
    val currentPosition: Int
    val duration: Int

    fun setDataSource(url: String)
    fun prepareAsync()
    fun start()
    fun pause()
    fun seekTo(positionMs: Int)
    fun release()

    fun setOnPreparedListener(listener: (AudioPlayer) -> Unit)
    fun setOnCompletionListener(listener: (AudioPlayer) -> Unit)
    fun setOnErrorListener(listener: (AudioPlayer, Int, Int) -> Boolean)
}
