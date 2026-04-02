package com.musicai.ui.utils.fakes

import com.musicai.plugin.utils.ConnectivityChecker

class FakeConnectivityChecker : ConnectivityChecker {
    private var isConnected = true

    fun setConnected(connected: Boolean) {
        isConnected = connected
    }

    override fun isInternetAvailable(): Boolean = isConnected
}
