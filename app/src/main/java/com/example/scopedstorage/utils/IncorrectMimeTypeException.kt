package com.example.scopedstorage.utils

class IncorrectMimeTypeException: Exception() {

    override val message: String
        get() = "Incorrect MIME type!"
}