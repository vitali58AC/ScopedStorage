package com.example.scopedstorage.compose

import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

@Composable
fun MyTopAppBar(title: String, icon: @Composable () -> Unit, onIconClick: () -> Unit = {}) {
    TopAppBar(
        title = {
            Text(
                text = title,
                color = MaterialTheme.colors.onPrimary,
                fontWeight = FontWeight.Light
            )
        },
        backgroundColor = MaterialTheme.colors.primary,
        actions = {
            IconButton(onClick = { onIconClick() }) {
                icon()
            }
        }
    )
}