package com.musicai.plugin.utils

object DurationUtils {

    fun formatDuration(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }

    fun formatRemaining(currentMs: Long, totalMs: Long): String {
        val remaining = (totalMs - currentMs).coerceAtLeast(0L)
        return "-${formatDuration(remaining)}"
    }
}
