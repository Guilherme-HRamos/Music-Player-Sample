package com.musicai.ui.shared.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.musicai.ui.album.AlbumScreen
import com.musicai.ui.album.model.AlbumViewModel
import com.musicai.ui.album.model.AlbumViewModelImpl
import com.musicai.ui.player.PlayerScreen
import com.musicai.ui.player.model.PlayerViewModel
import com.musicai.ui.player.model.PlayerViewModelImpl
import com.musicai.ui.songs.SongsScreen
import com.musicai.ui.songs.model.SongsViewModel
import com.musicai.ui.songs.model.SongsViewModelImpl
import com.musicai.ui.splash.ui.SplashScreen
import dagger.hilt.android.lifecycle.HiltViewModel

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateToSongs = {
                    navController.navigate(Routes.SONGS) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.SONGS) {
            val viewModel: SongsViewModelImpl = hiltViewModel()
            SongsScreen(
                viewModel = viewModel,
                onNavigateToPlayer = { trackId ->
                    navController.navigate(Routes.player(trackId))
                },
                onNavigateToAlbum = { collectionId ->
                    navController.navigate(Routes.album(collectionId))
                },
            )
        }

        composable(
            route = Routes.PLAYER,
            arguments = listOf(
                navArgument("trackId") { type = NavType.LongType },
            ),
        ) {
            val viewModel: PlayerViewModelImpl = hiltViewModel()
            PlayerScreen(
                viewModel = viewModel,
                onNavigateToAlbum = { collectionId ->
                    navController.navigate(Routes.album(collectionId))
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.ALBUM,
            arguments = listOf(
                navArgument("collectionId") { type = NavType.LongType },
            ),
        ) {
            val viewModel: AlbumViewModelImpl = hiltViewModel()
            AlbumScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
