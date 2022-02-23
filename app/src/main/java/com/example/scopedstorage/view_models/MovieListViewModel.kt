package com.example.scopedstorage.view_models

import android.app.Application
import android.app.RecoverableSecurityException
import android.app.RemoteAction
import android.content.IntentSender
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.scopedstorage.data.movie_list.Movie
import com.example.scopedstorage.repositories.MovieListRepository
import com.example.scopedstorage.utils.IncorrectMimeTypeException
import com.example.scopedstorage.utils.haveQ
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MovieListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MovieListRepository(application)
    private val tag = "movie_list_viewModel"

    private var isObservingStarted = false
    val movieListState = mutableStateListOf<Movie>()
    val permissionGrantedState = mutableStateOf(false)

    //Использовал live data для наблюдения.
    private val recoverableActionMutableLiveData = MutableLiveData<RemoteAction>()
    val recoverableAction: LiveData<RemoteAction>
        get() = recoverableActionMutableLiveData

    private val intentSenderMutableLiveData = MutableLiveData<IntentSender>()
    val intentSenderLiveData: LiveData<IntentSender>
        get() = intentSenderMutableLiveData

    private val intentSenderToTrashMutableLiveData = MutableLiveData<IntentSender>()
    val intentSenderToTrash: LiveData<IntentSender>
        get() = intentSenderToTrashMutableLiveData

    private val intentSenderSelectDirectoryMutableLiveData = MutableLiveData<IntentSender>()
    val intentSenderSelectDirectory: LiveData<IntentSender>
        get() = intentSenderSelectDirectoryMutableLiveData

    //Храним id для повторного запроса на удаление посел подтверждения пользователем
    private var pendingDeleteId: Long? = null

    //Для появления инндикатора прогресса при скачивании видео
    var downloadProgress by mutableStateOf(false)
    var incorrectMimeTypeState by mutableStateOf(false)

    fun updatePermissionsState(isGranted: Boolean) {
        if (isGranted) {
            permissionGranted()
        } else {
            permissionDenied()
        }
    }

    fun permissionGranted() {
        loadMovies()
        if (isObservingStarted.not()) {
            repository.observeMovies { loadMovies() }
            isObservingStarted = true
        }
        permissionGrantedState.value = true
    }

    fun permissionDenied() {
        permissionGrantedState.value = false
    }

    private fun loadMovies() {
        viewModelScope.launch {
            try {
                val movies = repository.getMovie()
                movieListState.clear()
                movieListState.addAll(movies)
            } catch (t: Throwable) {
                Log.e("movie_list_viewModel", "error with load movie ${t.message}")
            }
        }
    }

    fun downloadVideo(name: String, url: String, uri: Uri?) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    downloadProgress = true
                    if (uri != null) deleteVideoFromListState(uri)
                    repository.saveMovie(name, url, uri) {
                        loadMovies()
                    }
                } catch (t: IncorrectMimeTypeException) {
                    incorrectMimeTypeState = true
                    Log.e("movie_list_viewModel", "IncorrectMimeTypeException ${t.message}")
                } catch (t: Throwable) {
                    Log.e(
                        "movie_list_viewModel",
                        "error with download movie from url ${t.message} exception name $t"
                    )
                } finally {
                    downloadProgress = false
                    if (incorrectMimeTypeState) {
                        delay(1000L)
                        incorrectMimeTypeState = false
                    }
                }
            }
        }
    }

    private fun deleteVideoFromListState(uri: Uri) {
        try {
            val videoInList = movieListState.any { it.uri == uri }
            if (videoInList) {
                val videoToDelete = movieListState.first { it.uri == uri }
                movieListState.remove(videoToDelete)
            }
        } catch (t: Throwable) {
            Log.e(tag, "error with fix list ${t.message}")
        }
    }


    fun deleteMovie(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteMovie(id)
            } catch (t: Throwable) {
                Log.e(tag, "error with delete movie ${t.message}")
                if (haveQ() && t is RecoverableSecurityException) {
                    pendingDeleteId = id
                    recoverableActionMutableLiveData.postValue(t.userAction)
                } else {
                    //toast with error
                }
            }
        }
    }

    fun confirmDelete() {
        pendingDeleteId?.let { deleteMovie(it) }
    }

    fun declineDelete() {
        pendingDeleteId = null
    }

    fun addToFavorite(uri: Uri, isFavorite: Boolean) {
        viewModelScope.launch {
            try {
                val intentSender = repository.addToFavorite(uri, isFavorite)
                intentSender?.let { intentSenderMutableLiveData.postValue(it) }
                //loadMovies()
            } catch (t: Throwable) {
                Log.e("movie_list_viewModel", "error with add to favorite ${t.message}")
            }
        }
    }

    fun addToTrash(uri: Uri, toTrash: Boolean) {
        viewModelScope.launch {
            try {
                val intentSender = repository.addToTrash(uri, toTrash)
                intentSender?.let { intentSenderToTrashMutableLiveData.postValue(it) }
            } catch (t: Throwable) {
                Log.e("movie_list_viewModel", "error with add to trash ${t.message}")
            }
        }
    }

    fun getSelectDirectoryIntentSender(uri: Uri) {
        viewModelScope.launch {
            try {
                val intentSender = repository.chooseDirectory(uri = uri)
                intentSender?.let { intentSenderSelectDirectoryMutableLiveData.postValue(it) }
            } catch (t: Throwable) {
                Log.e("movie_list_viewModel", "error select directory ${t.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.unregisterObserver()
    }
}