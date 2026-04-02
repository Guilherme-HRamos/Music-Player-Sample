package com.musicai.ui.shared.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import com.musicai.R
import com.musicai.ui.theme.MusicTheme

@Composable
fun RoundedArtwork(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    radius: Dp = MusicTheme.radius.small,
) {
    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        placeholder = painterResource(R.drawable.cover_sample),
        modifier = modifier.clip(RoundedCornerShape(radius)),
    )
}
