package com.musicai.ui.theme

import androidx.compose.ui.unit.dp

sealed interface MusicTokens {

    data object Default: MusicTokens {
        val topBarHeight = 72.dp
        val horizontalScreenPadding = 24.dp
        val iconButtonSize = 48.dp
        val defaultRoundedCornerSize = 12.dp

    }

    data object SongList: MusicTokens {

        val horizontalDividerPadding = 76.dp
        val horizontalDividerThickness = 0.5.dp

        val textFieldHorizontalPadding = 16.dp
        val textFieldVerticalPadding = 8.dp

    }

}