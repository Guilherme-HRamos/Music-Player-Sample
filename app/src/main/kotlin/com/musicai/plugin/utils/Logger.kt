package com.musicai.plugin.utils

import android.util.Log

interface Logger {
    fun debug(message: String)
    fun info(message: String)
    fun warn(message: String)
    fun error(message: String, throwable: Throwable? = null)
}

internal class LogCatLogger : Logger {
    override fun debug(message: String) {
        Log.d("MusicAI", message)
    }

    override fun info(message: String) {
        Log.i("MusicAI", message)
    }

    override fun warn(message: String) {
        Log.w("MusicAI", message)
    }

    override fun error(message: String, throwable: Throwable?) {
        Log.e("MusicAI", message, throwable)
    }
}