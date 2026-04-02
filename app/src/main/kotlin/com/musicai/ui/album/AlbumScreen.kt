package com.musicai.ui.album

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.musicai.R
import com.musicai.domain.model.Album
import com.musicai.domain.model.Song
import com.musicai.ui.album.model.AlbumMessageEvent
import com.musicai.ui.album.model.AlbumState
import com.musicai.ui.album.model.AlbumViewModel
import com.musicai.ui.shared.components.ContentStateWrapper
import com.musicai.ui.shared.components.RoundedArtwork
import com.musicai.ui.shared.components.ScreenTopBar
import com.musicai.ui.shared.components.SongInfoDisplay
import com.musicai.ui.theme.MusicAITheme
import com.musicai.ui.theme.MusicTheme

@Composable
fun AlbumScreen(
    viewModel: AlbumViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.messageEvents.collect { event ->
            when (event) {
                is AlbumMessageEvent.NoConnectionError -> {
                    Toast.makeText(context, R.string.no_internet_connection, Toast.LENGTH_SHORT).show()
                }
                is AlbumMessageEvent.ShowError -> {
                    Toast.makeText(context, event.messageResId, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    AlbumScreenContent(
        state = state,
        onBack = onBack,
        onRetry = viewModel::onRetry,
    )
}

@Composable
fun AlbumScreenContent(
    state: AlbumState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        ScreenTopBar(
            title = state.album?.collectionName ?: stringResource(R.string.album_fallback),
            onBack = onBack,
        )

        ContentStateWrapper(
            isLoading = state.isLoading,
            error = state.error,
            onRetry = onRetry,
        ) {
            if (state.album != null) {
                val album = state.album
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // Album header
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = MusicTheme.spacing.medium)
                                .padding(horizontal = MusicTheme.spacing.medium),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            RoundedArtwork(
                                model = album.artworkUrl,
                                contentDescription = album.collectionName,
                                radius = MusicTheme.radius.large,
                                modifier = Modifier.size(MusicTheme.component.albumArtworkSize),
                            )
                            Spacer(modifier = Modifier.height(MusicTheme.spacing.medium))
                            Text(
                                text = album.collectionName,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Spacer(modifier = Modifier.height(MusicTheme.spacing.small))
                            Text(
                                text = album.artistName,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Spacer(modifier = Modifier.height(MusicTheme.spacing.xxLarge))
                        }
                    }

                    // Track list
                    itemsIndexed(album.songs) { index, song ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = MusicTheme.spacing.large, vertical = MusicTheme.spacing.small),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RoundedArtwork(
                                model = album.artworkUrl,
                                contentDescription = null,
                                modifier = Modifier.size(MusicTheme.component.trackThumbnailSize),
                                radius = MusicTheme.radius.small,
                            )
                            Spacer(modifier = Modifier.width(MusicTheme.spacing.medium))
                            SongInfoDisplay(
                                trackName = song.trackName,
                                artistName = song.artistName,
                                modifier = Modifier.height(MusicTheme.component.trackThumbnailSize),
                                trackStyle = MaterialTheme.typography.titleSmall,
                                artistStyle = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AlbumScreenPreview() {
    val sampleSongs = listOf(
        Song(
            trackId = 1,
            trackName = "Song 1",
            artistName = "Artist Name",
            collectionName = "Album Name",
            collectionId = 1,
            artworkUrl = "https://cdn-images.dzcdn.net/images/cover/39e2281a0e9f564e73b4f49dfa06f4ab/0x1900-000000-80-0-0.jpg",
            previewUrl = null,
            trackTimeMillis = 210000
        ),
        Song(
            trackId = 2,
            trackName = "Song 2",
            artistName = "Artist Name",
            collectionName = "Album Name",
            collectionId = 1,
            artworkUrl = "https://cdn-images.dzcdn.net/images/cover/39e2281a0e9f564e73b4f49dfa06f4ab/0x1900-000000-80-0-0.jpg",
            previewUrl = null,
            trackTimeMillis = 185000
        ),
        Song(
            trackId = 3,
            trackName = "Song 3",
            artistName = "Artist Name",
            collectionName = "Album Name",
            collectionId = 1,
            artworkUrl = "https://cdn-images.dzcdn.net/images/cover/39e2281a0e9f564e73b4f49dfa06f4ab/0x1900-000000-80-0-0.jpg",
            previewUrl = null,
            trackTimeMillis = 210000
        ),
        Song(
            trackId = 4,
            trackName = "Song 4",
            artistName = "Artist Name",
            collectionName = "Album Name",
            collectionId = 1,
            artworkUrl = "https://cdn-images.dzcdn.net/images/cover/39e2281a0e9f564e73b4f49dfa06f4ab/0x1900-000000-80-0-0.jpg",
            previewUrl = null,
            trackTimeMillis = 185000
        ),
        Song(
            trackId = 5,
            trackName = "Song 5",
            artistName = "Artist Name",
            collectionName = "Album Name",
            collectionId = 1,
            artworkUrl = "https://cdn-images.dzcdn.net/images/cover/39e2281a0e9f564e73b4f49dfa06f4ab/0x1900-000000-80-0-0.jpg",
            previewUrl = null,
            trackTimeMillis = 210000
        ),
        Song(
            trackId = 6,
            trackName = "Song 6",
            artistName = "Artist Name",
            collectionName = "Album Name",
            collectionId = 1,
            artworkUrl = "https://cdn-images.dzcdn.net/images/cover/39e2281a0e9f564e73b4f49dfa06f4ab/0x1900-000000-80-0-0.jpg",
            previewUrl = null,
            trackTimeMillis = 185000
        )

    )

    val sampleAlbum = Album(
        collectionId = 1,
        collectionName = "Album Title",
        artistName = "Daft Punk",
        artworkUrl = "https://cdn11.bigcommerce.com/s-8e25iavqdi/images/stencil/1280x1280/products/27797/27318/get-lucky-album-cover-sticker__90439.1539726074.jpg",
        songs = sampleSongs
    )

    val state = AlbumState(
        album = sampleAlbum,
        isLoading = false,
        error = null
    )

    MusicAITheme {
        AlbumScreenContent(
            state = state,
            onBack = {},
            onRetry = {}
        )
    }
}
