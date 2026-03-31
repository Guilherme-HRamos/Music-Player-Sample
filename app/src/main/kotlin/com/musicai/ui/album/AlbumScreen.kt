package com.musicai.ui.album

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.musicai.R
import com.musicai.domain.model.Album
import com.musicai.domain.model.Song
import com.musicai.ui.album.model.AlbumState
import com.musicai.ui.album.model.AlbumViewModel
import com.musicai.ui.theme.ColorDarkText
import com.musicai.ui.theme.MusicAITheme
import com.musicai.ui.theme.components.AppErrorState
import com.musicai.ui.theme.components.AppLoadingIndicator

@Composable
fun AlbumScreen(
    viewModel: AlbumViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
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
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            Text(
                text = state.album?.collectionName ?: "Album",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        when {
            state.isLoading -> AppLoadingIndicator()
            state.error != null -> AppErrorState(
                message = state.error,
                onRetry = onRetry,
            )

            state.album != null -> {
                val album = state.album
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // Album header
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            AsyncImage(
                                model = album.artworkUrl,
                                contentDescription = album.collectionName,
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(R.drawable.cover_sample),
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(20.dp)),
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = album.collectionName,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = album.artistName,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }

                    // Track list
                    itemsIndexed(album.songs) { index, song ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AsyncImage(
                                model = album.artworkUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(
                                modifier = Modifier.height(44.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    modifier = Modifier.padding(top = 4.dp),
                                    text = song.trackName,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    modifier = Modifier.padding(bottom = 4.dp),
                                    text = song.artistName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ColorDarkText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
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
