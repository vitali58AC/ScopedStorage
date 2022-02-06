package com.example.scopedstorage

import android.annotation.SuppressLint
import android.app.Activity
import android.app.RemoteAction
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.scopedstorage.compose.navigation.Navigation
import com.example.scopedstorage.ui.theme.ScopedStorageTheme
import com.example.scopedstorage.utils.haveQ
import com.example.scopedstorage.view_models.MovieListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : ComponentActivity() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var recoverableActionListener: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var openDocumentLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var createDocumentLauncher: ActivityResultLauncher<String>
    private lateinit var selectDocumentLauncher: ActivityResultLauncher<Uri?>
    private lateinit var addToFavoriteRequestLauncher: ActivityResultLauncher<IntentSenderRequest>

    private val
            movieListViewModel: MovieListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initPermissionResultListener()
        initRecoverableActionListener()
        initOpenDocumentLauncher()
        initCreateDocumentLauncher()
        initSelectDocumentFolderLauncher()
        initAddToFavoriteRequest()
        setContent {
            val navController = rememberNavController()
            val remoteAction by movieListViewModel.recoverableAction.observeAsState()
            val intentSender by movieListViewModel.intentSenderLiveData.observeAsState()
            val toTrash by movieListViewModel.intentSenderToTrash.observeAsState()
            val toTrashLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartIntentSenderForResult(),
                onResult = { Log.e("main_activity", "Result test launcher $it") })
            ScopedStorageTheme {
                Surface(color = MaterialTheme.colors.background) {
                    Navigation(
                        navController = navController,
                        movieListViewModel = movieListViewModel
                    ) { requestPermission() }
                }
            }
            if (haveQ()) {
                remoteAction?.let {
                    handleRecoverableAction(it)
                }
            }
            intentSender?.let {
                if (haveQ()) {
                    val request = IntentSenderRequest.Builder(it).build()
                    toTrashLauncher.launch(request)
                }
            }
            toTrash?.let {
                if (haveQ()) {
                    val request = IntentSenderRequest.Builder(it).build()
                    toTrashLauncher.launch(request)
                }
            }
        }
        if (hasPermissions().not()) {
            //Запрос разрешений
            requestPermission()
        }
    }

    override fun onResume() {
        super.onResume()
        movieListViewModel.updatePermissionsState(hasPermissions())
    }


    //Т.к. по заданию минимальное API 21, то диалог, поясняющий необходимость разрешений, добавлять не стал
    private fun initPermissionResultListener() {
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissionToGrantedMap: Map<String, Boolean> ->
            if (permissionToGrantedMap.values.all { it }) {
                movieListViewModel.permissionGranted()
            } else {
                movieListViewModel.permissionDenied()
            }
        }
    }

    private fun initRecoverableActionListener() {
        recoverableActionListener = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { activityResult ->
            Log.e("main_activity", "init recoverable action launcher ${activityResult.resultCode}")
            val isConfirmed = activityResult.resultCode == Activity.RESULT_OK
            if (isConfirmed) {
                movieListViewModel.confirmDelete()
            } else {
                movieListViewModel.declineDelete()
            }
        }
    }

    private fun requestPermission() {
        requestPermissionLauncher.launch(PERMISSION.toTypedArray())
    }

    private fun hasPermissions() = PERMISSION.all {
        ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleRecoverableAction(action: RemoteAction) {
        val request = IntentSenderRequest.Builder(action.actionIntent.intentSender).build()
        recoverableActionListener.launch(request)
    }


    //Open text file example
    fun readFile() {
        openDocumentLauncher.launch(arrayOf("text/plain"))
    }

    private fun initOpenDocumentLauncher() {
        openDocumentLauncher = registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) {
            handleOpenDocument(it)
        }
    }

    private fun handleOpenDocument(uri: Uri?) {
        if (uri == null) {
            //toast
            return
        }
        //Scope is wrong?
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                this@MainActivity.contentResolver.openInputStream(uri)
                    ?.bufferedReader()
                    ?.use {
                        val content = it.readLines().joinToString { "\n" }
                        //example output
                        Log.e("Activity", "example output text file $content")
                    }
            }
        }
    }

    private fun createDocument() {
        createDocumentLauncher.launch("text.txt")
    }

    private fun initCreateDocumentLauncher() {
        createDocumentLauncher = registerForActivityResult(
            ActivityResultContracts.CreateDocument()
        ) {
            handleCreateFile(it)
        }
    }

    private fun handleCreateFile(uri: Uri?) {
        if (uri == null) {
            //toast
            return
        }
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                this@MainActivity.contentResolver.openOutputStream(uri)?.bufferedWriter()
                    ?.use { it.write("Example text create") }
            }
        }
    }

    //Просто как напоминание для работы с pendingIntent
    @SuppressLint("NewApi")
    fun initAddToFavoriteRequest() {
        addToFavoriteRequestLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    Log.e("main_activity", "init favorite launcher ${it.resultCode}")
                }

            }
    }

    private fun handleAddToFavorite(intentSender: IntentSender) {
        val request = IntentSenderRequest.Builder(intentSender).build()
        addToFavoriteRequestLauncher.launch(request)
    }

    fun selectDirectory() {
        //null is default folder path
        selectDocumentLauncher.launch(null)
    }

    private fun initSelectDocumentFolderLauncher() {
        selectDocumentLauncher = registerForActivityResult(
            ActivityResultContracts.OpenDocumentTree()
        ) {
            handleSelectDirectory(it)
        }
    }

    private fun handleSelectDirectory(uri: Uri?) {
        if (uri == null) {
            //toast
            return
        }
        //Here we can work with folder, i need find out how it do
        //DocumentsContract
        //toast
    }


    companion object {
        private val PERMISSION = listOfNotNull(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                .takeIf { haveQ().not() }
        )
    }
}