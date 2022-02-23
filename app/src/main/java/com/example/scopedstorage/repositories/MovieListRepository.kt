package com.example.scopedstorage.repositories

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.IntentSender
import android.database.ContentObserver
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import com.example.scopedstorage.data.Networking
import com.example.scopedstorage.data.movie_list.Movie
import com.example.scopedstorage.utils.IncorrectMimeTypeException
import com.example.scopedstorage.utils.haveQ
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStream
import java.util.*

class MovieListRepository(private val context: Context) {

    private var observer: ContentObserver? = null

    fun observeMovies(onChange: () -> Unit) {
        observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                onChange()
            }
        }
        context.contentResolver.registerContentObserver(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            //Подписаться на изменения списка начиная от этого Uri и дальше по иерархии
            //Иначе только этот uri
            true,
            observer!!
        )
    }

    fun unregisterObserver() {
        observer?.let { context.contentResolver.unregisterContentObserver(it) }
    }

    @SuppressLint("Range", "InlinedApi")
    suspend fun getMovie(): List<Movie> {
        val movies = mutableListOf<Movie>()
        withContext(Dispatchers.IO) {
            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media._ID))
                    val name =
                        cursor.getString((cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)))
                    val size = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.SIZE))

                    val uri =
                        ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)

                    val isFavorite = if (haveQ()) {
                        cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.IS_FAVORITE)) == "1"
                    } else false
                    movies += Movie(id, uri, name, size, isFavorite)
                }
            }
        }
        return movies
    }

    //https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mov-file.mov

    @SuppressLint("InlinedApi")
    suspend fun saveMovie(name: String, url: String, uri: Uri?, onChange: () -> Unit) {
        withContext(Dispatchers.IO) {
            //Обработка корректности MIME type с помощью MimeTypeMap по заданию
            val mimeType = MimeTypeMap.getFileExtensionFromUrl(url)
                ?.run {
                    MimeTypeMap.getSingleton()
                        .getMimeTypeFromExtension(lowercase(Locale.getDefault()))
                } ?: "null"
            val videoMimePatterns = Regex("video/\\w+")
            if (videoMimePatterns.matches(mimeType)) {
                val movieUri = if (uri != null) {
                    //makeMovieVisibleOrNot(uri, 1)
                    //onChange()
                    uri
                } else {
                    saveMovieDetails(name, mimeType)
                }
                try {
                    downloadMovie(url, movieUri)
                    makeMovieVisibleOrNot(movieUri, 0)
                    onChange()
                } catch (t: Throwable) {
                    Log.e("movie_repository", "Error with video download ${t.message}")
                    movieUri.lastPathSegment?.toLong()?.let { deleteMovie(it) }
                }
            } else {
                throw IncorrectMimeTypeException()
            }
        }
    }

    @SuppressLint("InlinedApi")
    private fun makeMovieVisibleOrNot(movieUri: Uri, value: Int) {
        if (haveQ().not()) return
        val movieDetails = ContentValues().apply {
            put(MediaStore.Video.Media.IS_PENDING, value)
        }
        context.contentResolver.update(movieUri, movieDetails, null, null)
    }

    @SuppressLint("InlinedApi")
    private fun saveMovieDetails(name: String, mimeType: String): Uri {
        val volume = if (haveQ()) {
            MediaStore.VOLUME_EXTERNAL_PRIMARY
        } else {
            MediaStore.VOLUME_EXTERNAL
        }

        val movieCollectionUri = MediaStore.Video.Media.getContentUri(volume)
        val movieDetails = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
            put(MediaStore.Video.Media.MIME_TYPE, mimeType)
            //For get another folder
            put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
            //To hide this file about another apps
            if (haveQ()) {
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }
        Log.e(
            "repository",
            "movies directory is ${Environment.DIRECTORY_MOVIES} separator is ${File.separator}"
        )
        return context.contentResolver.insert(movieCollectionUri, movieDetails)!!
    }

    private suspend fun downloadMovie(url: String, uri: Uri) {
        context.contentResolver.openOutputStream(uri)?.use { outputStream: OutputStream ->
            Log.e("movie_repository", "uri $uri for download file")
            Networking.api.getFile(url).byteStream().use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    @SuppressLint("NewApi")
    suspend fun addToFavorite(uri: Uri, isFavorite: Boolean): IntentSender? {
        var intentSender: IntentSender? = null
        if (haveQ()) {
            withContext(Dispatchers.IO) {
                intentSender = MediaStore.createFavoriteRequest(
                    context.contentResolver,
                    listOf(uri),
                    isFavorite
                ).intentSender
            }
        }
        return intentSender
    }


    @SuppressLint("NewApi")
    suspend fun addToTrash(uri: Uri, toTrash: Boolean): IntentSender? {
        var intentSender: IntentSender? = null
        if (haveQ()) {
            withContext(Dispatchers.IO) {
                intentSender = MediaStore.createTrashRequest(
                    context.contentResolver,
                    listOf(uri),
                    toTrash
                ).intentSender
            }
        }
        return intentSender
    }

    @SuppressLint("NewApi")
    suspend fun chooseDirectory(uri: Uri): IntentSender? {
        var intentSender: IntentSender?
        withContext(Dispatchers.IO) {
            intentSender =
                MediaStore.createWriteRequest(context.contentResolver, listOf(uri)).intentSender
        }
        return intentSender
    }

    suspend fun deleteMovie(id: Long) {
        withContext(Dispatchers.IO) {
            val uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
            context.contentResolver.delete(uri, null, null)
        }
    }

}