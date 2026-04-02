package com.musicai.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class MusicSpacing(
    val none: Dp = 0.dp,
    val xSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val intermediate: Dp = 20.dp,
    val large: Dp = 24.dp,
    val xLarge: Dp = 32.dp,
    val xxLarge: Dp = 40.dp,
    val xxxLarge: Dp = 52.dp,
)

@Immutable
data class MusicRadius(
    val none: Dp = 0.dp,
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 12.dp,
    val large: Dp = 20.dp,
    val full: Dp = 100.dp,
)

@Immutable
data class MusicIconSize(
    val small: Dp = 24.dp,
    val medium: Dp = 36.dp,
    val large: Dp = 48.dp,
    val xLarge: Dp = 72.dp,
)

@Immutable
data class MusicComponentTokens(
    val topBarHeight: Dp = 72.dp,
    val albumArtworkSize: Dp = 120.dp,
    val trackThumbnailSize: Dp = 44.dp,
    val listItemArtworkSize: Dp = 52.dp,
    val horizontalDividerThickness: Dp = 0.5.dp,
    val horizontalDividerPadding: Dp = 76.dp,
)

val LocalMusicSpacing = staticCompositionLocalOf { MusicSpacing() }
val LocalMusicRadius = staticCompositionLocalOf { MusicRadius() }
val LocalMusicIconSize = staticCompositionLocalOf { MusicIconSize() }
val LocalMusicComponentTokens = staticCompositionLocalOf { MusicComponentTokens() }

object MusicTheme {
    val spacing: MusicSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalMusicSpacing.current

    val radius: MusicRadius
        @Composable
        @ReadOnlyComposable
        get() = LocalMusicRadius.current

    val icon: MusicIconSize
        @Composable
        @ReadOnlyComposable
        get() = LocalMusicIconSize.current

    val component: MusicComponentTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalMusicComponentTokens.current
}