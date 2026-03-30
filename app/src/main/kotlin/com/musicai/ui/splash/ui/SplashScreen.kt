package com.musicai.ui.splash.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.musicai.R
import kotlinx.coroutines.delay

private val SplashBackground = Color(0xFF0A1520)

@Composable
fun SplashScreen(onNavigateToSongs: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1_500)
        onNavigateToSongs()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF000000),
                        Color(0xFF000000)
                    )
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painter = painterResource(R.drawable.img_musical_note),
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = Color.White)
    }
}
