package com.example.scopedstorage.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.scopedstorage.R

@Composable
fun DeleteDialog(confirmCallback: (Boolean) -> Unit, cancelCallback: () -> Unit) {
    var isPermanently by rememberSaveable { mutableStateOf(false) }
    AlertDialog(
        shape = RoundedCornerShape(8.dp),
        onDismissRequest = { cancelCallback() },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.to_trash_or_delete),
                    style = MaterialTheme.typography.h6
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row {
                    Checkbox(checked = isPermanently, onCheckedChange = { isPermanently = it })
                    Text(text = "Permanently delete", modifier = Modifier.padding(16.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { confirmCallback(isPermanently) },
            ) { Text("Delete") }
        },
        dismissButton = {
            Button(onClick = { cancelCallback() }) { Text(stringResource(R.string.cancel)) }
        }
    )
}