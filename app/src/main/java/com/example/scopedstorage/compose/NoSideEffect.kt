package com.example.scopedstorage.compose

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import kotlin.reflect.KFunction2

@Composable
fun NoSideEffect(getRepositories: () -> Unit) {
    val currentCall by rememberUpdatedState(getRepositories)

    LaunchedEffect(true) {
        currentCall()
    }
}

@Composable
fun SingleToastEvent(
    toast: KFunction2<Context, String, Unit>,
    context: Context,
    message: String,
//    key: Int
) {
    val currentCall by rememberUpdatedState(toast)
    LaunchedEffect(true) {
        currentCall(context, message)
    }
}