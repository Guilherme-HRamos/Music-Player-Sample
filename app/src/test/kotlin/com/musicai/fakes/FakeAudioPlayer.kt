package com.musicai.fakes

import com.musicai.plugin.audioPlayer.AudioPlayer

class FakeAudioPlayer : AudioPlayer {

    override var isPlaying: Boolean = false
    override var currentPosition: Int = 0
    override var duration: Int = 30_000

    var dataSource: String? = null
    var prepareAsyncCallCount = 0
    var releaseCallCount = 0
    var seekToArg: Int? = null

    private var onPrepared: ((AudioPlayer) -> Unit)? = null
    private var onCompletion: ((AudioPlayer) -> Unit)? = null
    private var onError: ((AudioPlayer, Int, Int) -> Boolean)? = null

    override fun setDataSource(url: String) {
        dataSource = url
    }

    override fun prepareAsync() {
        prepareAsyncCallCount++
    }

    override fun start() {
        isPlaying = true
    }

    override fun pause() {
        isPlaying = false
    }

    override fun seekTo(positionMs: Int) {
        seekToArg = positionMs
        currentPosition = positionMs
    }

    override fun release() {
        releaseCallCount++
        isPlaying = false
    }

    override fun setOnPreparedListener(listener: (AudioPlayer) -> Unit) {
        onPrepared = listener
    }

    override fun setOnCompletionListener(listener: (AudioPlayer) -> Unit) {
        onCompletion = listener
    }

    override fun setOnErrorListener(listener: (AudioPlayer, Int, Int) -> Boolean) {
        onError = listener
    }

    // Helpers para simular eventos do MediaPlayer nos testes
    fun simulatePrepared() {
        onPrepared?.invoke(this)
    }

    fun simulateCompletion() {
        onCompletion?.invoke(this)
    }

    fun simulateError(what: Int = 1, extra: Int = 0) {
        onError?.invoke(this, what, extra)
    }
}
