package com.musicai.ui.shared.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.musicai.ui.theme.MusicAITheme
import com.musicai.ui.theme.MusicTheme

@Composable
fun SongLoadingItem() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = MusicTheme.spacing.medium),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Preview
@Composable
private fun SongsSearchPreview() {
    MusicAITheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            SongLoadingItem()
        }
    }
}