package com.example.scopedstorage.compose.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DefaultSnackbar(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier,
    onDismiss: () -> Unit
) {
    SnackbarHost(
        hostState = snackbarHostState,
        snackbar = { data ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                content = {
                    Text(
                        text = data.message,
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.background
                    )
                },
                action = {
                    data.actionLabel?.let { label ->
                        TextButton(onClick = { onDismiss() }) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.background
                            )
                        }
                    }
                }
            )
        },
        modifier = modifier,
    )
}