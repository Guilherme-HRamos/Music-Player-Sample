package com.musicai.ui.songs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.musicai.R
import com.musicai.domain.model.Song
import com.musicai.ui.shared.SongListItem
import com.musicai.ui.songs.model.SongsNavigationEvent
import com.musicai.ui.songs.model.SongsState
import com.musicai.ui.songs.model.SongsViewModel
import com.musicai.ui.theme.MusicAITheme
import com.musicai.ui.theme.components.AppErrorState
import com.musicai.ui.theme.components.AppLoadingIndicator
import com.musicai.ui.theme.screenTitle

@Composable
fun SongsScreen(
    viewModel: SongsViewModel,
    onNavigateToPlayer: (Long) -> Unit,
    onNavigateToAlbum: (Long) -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                is SongsNavigationEvent.NavigateToPlayer -> onNavigateToPlayer(event.trackId)
                is SongsNavigationEvent.NavigateToAlbum -> onNavigateToAlbum(event.collectionId)
            }
        }
    }

    SongsContent(
        state = state,
        onRefresh = viewModel::onRefresh,
        onLoadMore = viewModel::onLoadMore,
        onQueryChange = viewModel::onQueryChange,
        onToggleSearch = viewModel::onToggleSearch,
        onSearch = viewModel::onSearch,
        onSongClick = viewModel::onSongClick,
        onMoreClick = viewModel::onMoreClick,
        onDismissSheet = viewModel::onDismissSheet,
        onViewAlbum = viewModel::onViewAlbum,
        onClearSearch = viewModel::onClearSearch
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SongsContent(
    state: SongsState,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onSearch: () -> Unit,
    onSongClick: (Song) -> Unit,
    onMoreClick: (Song) -> Unit,
    onDismissSheet: () -> Unit,
    onViewAlbum: (Song) -> Unit,
    onClearSearch: () -> Unit,
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.isRefreshing,
        onRefresh = onRefresh,
    )

    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - 3
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        SongsTopBar(
            isSearchActive = state.isSearchActive,
            query = state.query,
            onQueryChange = onQueryChange,
            onToggleSearch = onToggleSearch,
            onSearch = onSearch,
            onClear = onClearSearch
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState),
        ) {
            when {
                state.isLoading -> AppLoadingIndicator()
                state.error != null -> AppErrorState(
                    message = state.error,
                    onRetry = onRefresh,
                )

                state.songs.isEmpty() -> AppErrorState(
                    message = if (state.isSearchActive) "Nenhum resultado encontrado" else "Suas músicas recentes aparecerão aqui",
                )

                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(items = state.songs, key = { it.trackId }) { song ->
                        SongListItem(
                            song = song,
                            onClick = { onSongClick(song) },
                            onMoreClick = { onMoreClick(song) },
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(start = 76.dp),
                        )
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = state.isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface,
            )
        }
    }

    state.selectedSong?.let { song ->
        MoreOptionsSheet(
            song = song,
            onDismiss = onDismissSheet,
            onViewAlbum = { onViewAlbum(song) },
        )
    }
}

@Composable
private fun SongsTopBar(
    isSearchActive: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Songs",
            style = MaterialTheme.typography.screenTitle,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        if (!isSearchActive)
            IconButton(
                modifier = Modifier.size(48.dp),
                onClick = onToggleSearch,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
    }

    if (isSearchActive) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            trailingIcon = {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            placeholder = { Text("Search") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                onSearch()
                keyboardController?.hide()
            }),
            colors = TextFieldDefaults.colors(
                // Hide the indicator line to keep it clean
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                // Custom container colors
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                cursorColor = MaterialTheme.colorScheme.onBackground,
            ),
            shape = RoundedCornerShape(12.dp), // Add rounding for a modern look
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Preview
@Composable
private fun SongsContentPreview() {
    val sampleSongs = listOf(
        Song(
            trackId = 1L,
            trackName = "Song 1",
            artistName = "Artist 1",
            collectionName = "Album 1",
            collectionId = 1L,
            artworkUrl = "",
            previewUrl = null,
            trackTimeMillis = 180000L
        ),
        Song(
            trackId = 2L,
            trackName = "Song 2",
            artistName = "Artist 2",
            collectionName = "Album 2",
            collectionId = 2L,
            artworkUrl = "",
            previewUrl = null,
            trackTimeMillis = 200000L
        ),
        Song(
            trackId = 3L,
            trackName = "Song 3",
            artistName = "Artist 3",
            collectionName = "Album 3",
            collectionId = 3L,
            artworkUrl = "",
            previewUrl = null,
            trackTimeMillis = 220000L
        )
    )
    MusicAITheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            SongsContent(
                state = SongsState(
                    songs = sampleSongs,
                    isLoading = false,
                    isRefreshing = false
                ),
                onRefresh = {},
                onLoadMore = {},
                onQueryChange = {},
                onToggleSearch = {},
                onSearch = {},
                onSongClick = {},
                onMoreClick = {},
                onDismissSheet = {},
                onViewAlbum = {},
                onClearSearch = {}
            )
        }
    }
}

@Preview
@Composable
private fun SongsSearchPreview() {
    val sampleSongs = listOf(
        Song(
            trackId = 1L,
            trackName = "Song 1",
            artistName = "Artist 1",
            collectionName = "Album 1",
            collectionId = 1L,
            artworkUrl = "",
            previewUrl = null,
            trackTimeMillis = 180000L
        ),
        Song(
            trackId = 2L,
            trackName = "Song 2",
            artistName = "Artist 2",
            collectionName = "Album 2",
            collectionId = 2L,
            artworkUrl = "",
            previewUrl = null,
            trackTimeMillis = 200000L
        ),
        Song(
            trackId = 3L,
            trackName = "Song 3",
            artistName = "Artist 3",
            collectionName = "Album 3",
            collectionId = 3L,
            artworkUrl = "",
            previewUrl = null,
            trackTimeMillis = 220000L
        )
    )
    MusicAITheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            SongsContent(
                state = SongsState(
                    songs = sampleSongs,
                    isLoading = false,
                    isRefreshing = false,
                    isSearchActive = true
                ),
                onRefresh = {},
                onLoadMore = {},
                onQueryChange = {},
                onToggleSearch = {},
                onSearch = {},
                onSongClick = {},
                onMoreClick = {},
                onDismissSheet = {},
                onViewAlbum = {},
                onClearSearch = {}
            )
        }
    }
}
