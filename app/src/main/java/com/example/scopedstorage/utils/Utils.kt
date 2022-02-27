package com.example.scopedstorage.utils

import android.os.Build
import android.webkit.MimeTypeMap
import java.util.*

fun haveQ() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

fun getMimeType(url: String) = MimeTypeMap.getFileExtensionFromUrl(url)
    ?.run {
        MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(lowercase(Locale.getDefault()))
    } ?: "null"
