package com.musicai.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.musicai.ui.shared.navigation.AppNavHost

@Composable
fun MusicAIRoot() {
    MusicAITheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AppNavHost()
        }
    }
}
