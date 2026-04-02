package com.musicai.ui.utils.fakes

import com.musicai.plugin.utils.Logger

class MutedLogger: Logger {
    override fun debug(message: String) {}

    override fun info(message: String) {}

    override fun warn(message: String) {}

    override fun error(message: String, throwable: Throwable?) {}
}