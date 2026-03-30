package com.musicai.ui.theme

import android.widget.Toast
import androidx.compose.material.Snackbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentCompositionLocalContext
import com.musicai.ui.splash.ui.SplashScreen

@Composable
fun MusicAIRoot() {
    MusicAITheme {
        SplashScreen {
            println("---------------------------- OK")
        }
//        AppNavHost()
    }
}
