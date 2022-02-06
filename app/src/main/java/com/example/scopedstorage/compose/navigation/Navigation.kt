package com.example.scopedstorage.compose.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.scopedstorage.compose.MovieListScreen
import com.example.scopedstorage.data.Screens
import com.example.scopedstorage.view_models.MovieListViewModel

@Composable
fun Navigation(
    navController: NavHostController,
    movieListViewModel: MovieListViewModel,
    permissionsRequest: () -> Unit
) {
    NavHost(navController = navController, startDestination = Screens.MovieList.name) {
        composable(Screens.MovieList.name) {
            MovieListScreen(
                hasPermission = movieListViewModel.permissionGrantedState.value,
                movieList = movieListViewModel.movieListState,
                onDownloadButtonCLick = { name, url, uri ->
                    movieListViewModel.downloadVideo(name, url, uri)
                },
                downloadProgress = movieListViewModel.downloadProgress,
                errorWithMimeType = movieListViewModel.incorrectMimeTypeState,
                onDeleteClick = { videoId ->
                    movieListViewModel.deleteMovie(videoId)
                },
                permissionsRequest = permissionsRequest,
                onAddFavorite = { uri, isFavorite ->
                movieListViewModel.addToFavorite(uri, isFavorite)
                }
            ) { uri, toTrash ->
                movieListViewModel.addToTrash(uri, toTrash)
            }
        }
    }
}