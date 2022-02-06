package com.example.scopedstorage.compose

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scopedstorage.R.string
import com.example.scopedstorage.data.movie_list.Movie
import com.example.scopedstorage.ui.theme.ScopedStorageTheme
import com.example.scopedstorage.utils.haveQ
import kotlinx.coroutines.launch


@Composable
fun MovieListScreen(
    hasPermission: Boolean,
    movieList: List<Movie>,
    onDownloadButtonCLick: (String, String, Uri?) -> Unit,
    downloadProgress: Boolean,
    errorWithMimeType: Boolean,
    onDeleteClick: (Long) -> Unit,
    permissionsRequest: () -> Unit,
    onAddFavorite: (Uri, Boolean) -> Unit,
    onToTrash: (Uri, Boolean) -> Unit
) {
    var openDialog by rememberSaveable { mutableStateOf(false) }
    var openDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var movieToDelete by rememberSaveable { mutableStateOf<List<Movie>>(emptyList()) }
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    if (errorWithMimeType) {
        LaunchedEffect(true) {
            scope.launch {
                scaffoldState.snackbarHostState.showSnackbar(
                    message = "Wrong video file type!",
                    actionLabel = "Hide",
                )
            }
        }
    }
    Scaffold(
        floatingActionButton = { if (hasPermission) FABButton { openDialog = true } },
        topBar = {
            MyTopAppBar(title = "Video list", icon = {}, onIconClick = {})
        },
        scaffoldState = scaffoldState,
        snackbarHost = { scaffoldState.snackbarHostState }
    ) {
        Box(Modifier.fillMaxSize()) {
            LazyColumn(Modifier.padding(top = 8.dp)) {
                items(items = movieList) {
                    MovieItem(
                        movie = it,
                        onDeleteClick = {
                            movieToDelete = listOf(it)
                            if (haveQ()) {
                                openDeleteDialog = true
                            } else {
                                onDeleteClick(movieToDelete[0].id)
                            }
                        },
                        onAddToFavorite = onAddFavorite,
                        onNonAndroidQStarClick = {
                            scope.launch {
                                scaffoldState.snackbarHostState.showSnackbar(
                                    message = "This feature is only available on Android 11 and above!",
                                    actionLabel = "Hide",
                                )
                            }
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    ProgressIndicator(visibility = downloadProgress)
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (movieList.isEmpty()) CenterScreenMessage(message = stringResource(string.video_not_found))
            }
            GetPermissionBlock(visible = !hasPermission) {
                permissionsRequest()
            }
            if (openDialog) {
                DownloadDialog(
                    confirmCallback = { name, url, uri ->
                        onDownloadButtonCLick(name, url, uri)
                        openDialog = false
                    },
                    cancelCallback = {
                        openDialog = false
                    }
                )
            }
            if (openDeleteDialog) {
                DeleteDialog(
                    confirmCallback = { isPermanently ->
                        if (isPermanently) {
                            onDeleteClick(movieToDelete[0].id)
                        } else {
                            onToTrash(movieToDelete[0].uri, true)
                        }
                        openDeleteDialog = false
                    },
                    cancelCallback = { openDeleteDialog = false }
                )
            }
            SnackbarHost(
                hostState = scaffoldState.snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(vertical = 59.dp)
            )
        }
    }
//        DefaultSnackbar(
//            snackbarHostState = scaffoldState.snackbarHostState,
//            onDismiss = {
//                scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
//            },
//            modifier = Modifier
//        )

}

@Composable
fun ColumnScope.CenterScreenMessage(message: String) {
    Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            fontWeight = FontWeight.Light,
            fontSize = 18.sp,
            color = MaterialTheme.colors.onPrimary
        )
    }
}

@Composable
fun GetPermissionBlock(visible: Boolean, onClick: () -> Unit) {
    if (visible) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(string.need_allow_message),
                style = MaterialTheme.typography.body1,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = { onClick() },
                elevation = ButtonDefaults.elevatedButtonElevation(6.dp),
                modifier = Modifier.padding(8.dp),
                colors = ButtonDefaults.buttonColors(MaterialTheme.colors.primary)
            ) {
                Text(
                    text = stringResource(string.allow_access),
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
fun FABButton(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = { onClick() },
        backgroundColor = MaterialTheme.colors.primary
    ) {
        Icon(
            imageVector = Icons.Filled.Download,
            contentDescription = "Download new video"
        )
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
fun DefaultPreview() {
    ScopedStorageTheme {
        MovieListScreen(
            hasPermission = true,
            movieList = Movie.exampleList,
            onDownloadButtonCLick = { _, _, _ -> },
            downloadProgress = false,
            errorWithMimeType = false,
            onDeleteClick = {},
            permissionsRequest = {},
            onAddFavorite = { _, _ -> }
        ) { _, _ -> }
    }
}