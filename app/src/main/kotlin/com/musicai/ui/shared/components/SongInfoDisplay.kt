package com.musicai.ui.shared.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun SongInfoDisplay(
    trackName: String,
    artistName: String,
    modifier: Modifier = Modifier,
    trackStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    artistStyle: TextStyle = MaterialTheme.typography.labelSmall,
    textAlign: TextAlign? = null,
) {
    Column(modifier = modifier) {
        Text(
            text = trackName,
            style = trackStyle,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = textAlign,
        )
        Text(
            text = artistName,
            style = artistStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = textAlign,
        )
    }
}
