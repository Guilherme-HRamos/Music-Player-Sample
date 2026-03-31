package com.musicai.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.musicai.R
import com.musicai.domain.model.Song
import com.musicai.ui.player.model.PlayerNavigationEvent
import com.musicai.ui.player.model.PlayerState
import com.musicai.ui.player.model.PlayerViewModel
import com.musicai.ui.songs.MoreOptionsSheet
import com.musicai.ui.songs.model.SongsNavigationEvent
import com.musicai.ui.theme.ColorBackground
import com.musicai.ui.theme.ColorSheetBackground
import com.musicai.ui.theme.MusicAITheme
import com.musicai.ui.theme.displayNormal
import com.musicai.ui.theme.labelMediumNormal
import com.musicai.utils.DurationUtils

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onNavigateToAlbum: (Long) -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { event ->
            if (event is PlayerNavigationEvent.NavigateToAlbum)
                onNavigateToAlbum(event.collectionId)
        }
    }

    PlayerContent(
        state = state,
        onBack = onBack,
        onSeek = viewModel::onSeek,
        onPrevious = viewModel::onPrevious,
        onPlayPause = viewModel::onPlayPause,
        onNext = viewModel::onNext,
        onToggleLoop = viewModel::onToggleLoop,
        onViewAlbum = viewModel::onViewAlbum,
    )
}

@Composable
private fun PlayerContent(
    state: PlayerState,
    onBack: () -> Unit,
    onSeek: (Long) -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onToggleLoop: () -> Unit,
    onViewAlbum: () -> Unit,
) {

    var shouldDisplayModal by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {

            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
                text = "Now playing",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Start,
            )


            IconButton(
                modifier = Modifier
                    .padding(start = 8.dp),
                onClick = {
                    shouldDisplayModal = true
                }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            // Album artwork
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = ColorBackground)
                    .padding(horizontal = 52.dp)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center,
            )
            {

                AsyncImage(
                    model = state.song?.artworkUrl?.replace("100x100bb", "600x600bb"),
                    contentDescription = state.song?.collectionName,
                    placeholder = painterResource(R.drawable.cover_sample),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                        .aspectRatio(1f),
                )
                if (state.isPreparing) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            // Song info
            Column() {
                Text(
                    text = state.song?.trackName ?: "",
                    style = MaterialTheme.typography.displayNormal,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = state.song?.artistName ?: "",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Progress slider
                val progress = if (state.durationMs > 0) {
                    state.currentPositionMs.toFloat() / state.durationMs.toFloat()
                } else 0f

                Slider(
                    value = progress,
                    onValueChange = { fraction ->
                        val seekMs = (fraction * state.durationMs).toLong()
                        onSeek(seekMs)
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.onBackground,
                        activeTrackColor = MaterialTheme.colorScheme.onBackground,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                // Time labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = DurationUtils.formatDuration(state.currentPositionMs),
                        style = MaterialTheme.typography.labelMediumNormal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = DurationUtils.formatRemaining(state.currentPositionMs, state.durationMs),
                        style = MaterialTheme.typography.labelMediumNormal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Playback controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {

                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier
                            .size(72.dp)
                            .background(color = ColorSheetBackground, shape = RoundedCornerShape(CornerSize(100))),
                    ) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (state.isPlaying) "Pause" else "Play",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(36.dp),
                        )
                    }

                    IconButton(
                        modifier = Modifier.padding(start = 24.dp),
                        onClick = onPrevious,
                        enabled = state.hasPrevious,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_backward),
                            contentDescription = "Previous",
                            tint = if (state.hasPrevious) {
                                MaterialTheme.colorScheme.onBackground
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(36.dp),
                        )
                    }

                    IconButton(
                        modifier = Modifier.padding(start = 24.dp),
                        onClick = onNext,
                        enabled = state.hasNext,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_forward),
                            contentDescription = "Next",
                            tint = if (state.hasNext) {
                                MaterialTheme.colorScheme.onBackground
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(36.dp),
                        )
                    }

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        IconButton(onClick = onToggleLoop) {
                            Icon(
                                painter = painterResource(R.drawable.ic_repeat),
                                contentDescription = "Repeat",
                                tint = if (state.loopEnabled) {
                                    MaterialTheme.colorScheme.onBackground
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }
            }


            // Error state
            state.error?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            if (shouldDisplayModal && state.song != null) {
                MoreOptionsSheet(
                    song = state.song,
                    onDismiss = { shouldDisplayModal = false },
                    onViewAlbum = { onViewAlbum() },
                )
            }
        }
    }
}

@Preview
@Composable
fun PlayerScreenPreview() {
    val sampleSong = Song(
        trackId = 1L,
        trackName = "Sample Song",
        artistName = "Sample Artist",
        collectionName = "Sample Album",
        collectionId = 1L,
        artworkUrl = "https://cdn11.bigcommerce.com/s-8e25iavqdi/images/stencil/1280x1280/products/27797/27318/get-lucky-album-cover-sticker__90439.1539726074.jpg",
        previewUrl = null,
        trackTimeMillis = 180000L
    )
    val sampleState = PlayerState(
        song = sampleSong,
        isPlaying = true,
        currentPositionMs = 45000L,
        durationMs = 180000L,
        hasNext = true,
        hasPrevious = true
    )

    MusicAITheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PlayerContent(
                state = sampleState,
                onBack = {},
                onSeek = {},
                onPrevious = {},
                onPlayPause = {},
                onNext = {},
                onToggleLoop = {},
                onViewAlbum = {},
            )
        }
    }
}
