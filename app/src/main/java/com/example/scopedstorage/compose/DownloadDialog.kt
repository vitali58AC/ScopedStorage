package com.example.scopedstorage.compose

import android.net.Uri
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.scopedstorage.R
import com.example.scopedstorage.utils.haveQ

@Composable
fun DownloadDialog(confirmCallback: (String, String, Uri?) -> Unit, cancelCallback: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var url by rememberSaveable { mutableStateOf("") }
    var uri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val selectDirectoryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(),
        onResult = { resultUri ->
            if (resultUri == null) {
                Log.e("download_dialog", "Select uri in launcher is null ${uri?.path}")
            } else {
                uri = resultUri
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