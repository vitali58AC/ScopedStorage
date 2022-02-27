package com.example.scopedstorage.compose

import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.scopedstorage.R
import com.example.scopedstorage.utils.getMimeType
import com.example.scopedstorage.utils.haveQ

@Composable
fun DownloadDialog(confirmCallback: (String, String, Uri?) -> Unit, cancelCallback: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var url by rememberSaveable { mutableStateOf("") }
    var uri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val selectDirectoryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(),
        onResult = { resultUri ->
            if (resultUri == null) {
                Log.e("download_dialog", "Select uri in launcher is null ${uri?.path}")
            } else {
                val videoCollectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    TODO("VERSION.SDK_INT < Q")
                }
                //Log.e("download_dialog", "Uri is ${resultUri.path}")
                val mimeType = getMimeType(url)
                val movieDetails = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, name)
                    put(MediaStore.Video.Media.MIME_TYPE, mimeType)
                    put(MediaStore.Video.Media.RELATIVE_PATH, "DCIM/")
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }
                val resultMediaUri = context.contentResolver.insert(videoCollectionUri, movieDetails)
                uri = resultMediaUri
            }
        }
    )
    AlertDialog(
        shape = RoundedCornerShape(8.dp),
        onDismissRequest = { cancelCallback() },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.enter_name_url_to),
                    style = MaterialTheme.typography.h6
                )
                Spacer(modifier = Modifier.height(24.dp))
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(text = "File name") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (haveQ()) {
                    IconButton(onClick = { selectDirectoryLauncher.launch(name) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DriveFileMoveIcon()
                            Text(text = "Choose directory", Modifier.padding(8.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(text = "Url") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { confirmCallback(name, url, uri) },
                enabled = name.length >= 3 && Patterns.WEB_URL.matcher(url).matches()
            ) { Text("Download") }
        },
        dismissButton = {
            Button(onClick = { cancelCallback() }) { Text(stringResource(R.string.cancel)) }
        }
    )
}