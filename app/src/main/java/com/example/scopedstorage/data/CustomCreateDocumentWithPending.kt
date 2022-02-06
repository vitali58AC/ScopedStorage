package com.example.scopedstorage.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts

class CustomCreateDocumentWithPending: ActivityResultContracts.CreateDocument() {
    @SuppressLint("InlinedApi")
    override fun createIntent(context: Context, input: String): Intent {
        return super.createIntent(context, input)
            .putExtra(MediaStore.Video.Media.IS_PENDING, 1)
    }
}