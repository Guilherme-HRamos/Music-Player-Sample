package com.musicai.ui.songs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.musicai.R
import com.musicai.domain.model.Song
import com.musicai.ui.theme.ColorSheetBackground
import com.musicai.ui.theme.MusicAITheme
import com.musicai.ui.theme.MusicTheme
import com.musicai.ui.theme.labelMediumNormal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreOptionsSheet(
    song: Song,
    onDismiss: () -> Unit,
    onViewAlbum: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ColorSheetBackground,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MusicTheme.spacing.large)
                .padding(bottom = MusicTheme.spacing.large),
        ) {
            Text(
                text = song.trackName,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                text = song.artistName,
                style = MaterialTheme.typography.labelMediumNormal,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = MusicTheme.spacing.xSmall),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(MusicTheme.spacing.medium))
            TextButton(
                onClick = onViewAlbum,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(MusicTheme.spacing.none)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_setlist),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(MusicTheme.icon.small),
                    )
                    Spacer(modifier = Modifier.width(MusicTheme.spacing.medium))
                    Text(
                        text = stringResource(R.string.view_album),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MoreOptionsSheetPreview() {
    MusicAITheme {
        MoreOptionsSheet(
            song = Song(
                trackId = 1L,
                trackName = "Song name",
                artistName = "Artist name",
                collectionName = "Sample Album",
                collectionId = 1L,
                artworkUrl = "",
                previewUrl = null,
                trackTimeMillis = 180000L
            ),
            onDismiss = {},
            onViewAlbum = {}
        )
    }
}
