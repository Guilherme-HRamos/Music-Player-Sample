package com.musicai.ui.player

import android.widget.Toast
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.musicai.R
import com.musicai.domain.model.Song
import com.musicai.plugin.utils.DurationUtils
import com.musicai.ui.player.model.PlayerNavigationEvent
import com.musicai.ui.player.model.PlayerState
import com.musicai.ui.player.model.PlayerViewModel
import com.musicai.ui.shared.components.RoundedArtwork
import com.musicai.ui.shared.components.SongInfoDisplay
import com.musicai.ui.songs.MoreOptionsSheet
import com.musicai.ui.theme.ColorBackground
import com.musicai.ui.theme.ColorSheetBackground
import com.musicai.ui.theme.MusicAITheme
import com.musicai.ui.theme.MusicTheme
import com.musicai.ui.theme.displayNormal
import com.musicai.ui.theme.labelMediumNormal

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onNavigateToAlbum: (Long) -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                is PlayerNavigationEvent.NavigateToAlbum -> onNavigateToAlbum(event.collectionId)
                is PlayerNavigationEvent.NoConnectionError -> {
                    Toast.makeText(context, R.string.no_internet_connection, Toast.LENGTH_SHORT).show()
                }
                is PlayerNavigationEvent.GenericError -> {
                    Toast.makeText(context, R.string.generic_error, Toast.LENGTH_SHORT).show()
                }
            }
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
        onModalDismissed = viewModel::onModalDismissed,
        onMoreClick = viewModel::onMoreClick,
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
    onModalDismissed: () -> Unit,
    onMoreClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = MusicTheme.spacing.small),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {

            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = MusicTheme.spacing.xSmall),
                text = stringResource(R.string.now_playing),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Start,
            )


            IconButton(
                modifier = Modifier
                    .padding(start = MusicTheme.spacing.small),
                onClick = onMoreClick
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.cd_more_options),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = MusicTheme.spacing.large),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            // Album artwork
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = ColorBackground)
                    .padding(horizontal = MusicTheme.spacing.xxxLarge)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center,
            ) {

                RoundedArtwork(
                    model = state.song?.artworkUrl?.replace("100x100bb", "600x600bb"),
                    contentDescription = state.song?.collectionName,
                    radius = MusicTheme.radius.large,
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(1f),
                )
                if (state.isPreparing) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            // Song info
            Column() {
                SongInfoDisplay(
                    trackName = state.song?.trackName ?: "",
                    artistName = state.song?.artistName ?: "",
                    trackStyle = MaterialTheme.typography.displayNormal,
                    artistStyle = MaterialTheme.typography.titleSmall,
                )

                Spacer(modifier = Modifier.height(MusicTheme.spacing.large))

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

                Spacer(modifier = Modifier.height(MusicTheme.spacing.medium))

                // Playback controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {

                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier
                            .size(MusicTheme.icon.xLarge)
                            .background(color = ColorSheetBackground, shape = RoundedCornerShape(MusicTheme.radius.full)),
                    ) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (state.isPlaying) stringResource(R.string.cd_pause) else stringResource(R.string.cd_play),
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(MusicTheme.icon.medium),
                        )
                    }

                    IconButton(
                        modifier = Modifier.padding(start = MusicTheme.spacing.large),
                        onClick = onPrevious,
                        enabled = state.hasPrevious,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_backward),
                            contentDescription = stringResource(R.string.cd_previous),
                            tint = if (state.hasPrevious) {
                                MaterialTheme.colorScheme.onBackground
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(MusicTheme.icon.medium),
                        )
                    }

                    IconButton(
                        modifier = Modifier.padding(start = MusicTheme.spacing.large),
                        onClick = onNext,
                        enabled = state.hasNext,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_forward),
                            contentDescription = stringResource(R.string.cd_next),
                            tint = if (state.hasNext) {
                                MaterialTheme.colorScheme.onBackground
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(MusicTheme.icon.medium),
                        )
                    }

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        IconButton(onClick = onToggleLoop) {
                            Icon(
                                painter = painterResource(R.drawable.ic_repeat),
                                contentDescription = stringResource(R.string.cd_repeat),
                                tint = if (state.loopEnabled) {
                                    MaterialTheme.colorScheme.onBackground
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(MusicTheme.icon.small),
                            )
                        }
                    }
                }
            }


            // Error state
            state.error?.let { error ->
                Spacer(modifier = Modifier.height(MusicTheme.spacing.medium))
                Text(
                    text = error,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            if (state.shouldDisplayModal && state.song != null) {
                MoreOptionsSheet(
                    song = state.song,
                    onDismiss = onModalDismissed,
                    onViewAlbum = onViewAlbum,
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
                onModalDismissed = {},
                onMoreClick = {}
            )
        }
    }
}
