package com.musicai.ui.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.musicai.R
import com.musicai.domain.model.Song
import com.musicai.ui.theme.ColorDarkText
import com.musicai.ui.theme.MusicAITheme
import com.musicai.ui.theme.MusicTheme

@Composable
fun SongListItem(
    song: Song,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = MusicTheme.spacing.medium, vertical = MusicTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = song.artworkUrl,
            contentDescription = song.collectionName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(start = MusicTheme.spacing.small)
                .size(MusicTheme.component.listItemArtworkSize)
                .clip(RoundedCornerShape(MusicTheme.radius.small)),
        )
        Spacer(modifier = Modifier.width(MusicTheme.spacing.medium))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.trackName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                modifier = Modifier.padding(top = MusicTheme.spacing.xSmall),
                text = song.artistName,
                style = MaterialTheme.typography.labelSmall,
                color = ColorDarkText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(onClick = onMoreClick) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.cd_more_options),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview
@Composable
fun SongListItemPreview() {
    MusicAITheme {
        Surface {
            SongListItem(
                song = Song(
                    trackId = 1L,
                    trackName = "Song Name",
                    artistName = "Artist Name",
                    collectionName = "Album Name",
                    collectionId = 1L,
                    artworkUrl = "android.resource://com.musicai/${R.drawable.cover_sample}",
                    previewUrl = null,
                    trackTimeMillis = 180000L
                ),
                onClick = {},
                onMoreClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SongListItemListPreview() {
    val songs = listOf(
        Song(1, "Song 1", "Artist 1", "Album 1", 1, "android.resource://com.musicai/${R.drawable.cover_sample}", null, 180000L),
        Song(2, "Song 2", "Artist 2", "Album 2", 2, "android.resource://com.musicai/${R.drawable.cover_sample}", null, 200000L),
        Song(3, "Song 3", "Artist 3", "Album 3", 3, "android.resource://com.musicai/${R.drawable.cover_sample}", null, 220000L),
        Song(4, "Song 4", "Artist 4", "Album 4", 4, "android.resource://com.musicai/${R.drawable.cover_sample}", null, 240000L),
        Song(5, "Song 5", "Artist 5", "Album 5", 5, "android.resource://com.musicai/${R.drawable.cover_sample}", null, 260000L),
    )
    MusicAITheme {
        Surface {
            LazyColumn {
                items(songs) { song ->
                    SongListItem(
                        song = song,
                        onClick = {},
                        onMoreClick = {}
                    )
                }
            }
        }
    }
}
