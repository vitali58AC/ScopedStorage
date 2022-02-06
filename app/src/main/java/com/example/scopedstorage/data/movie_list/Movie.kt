package com.example.scopedstorage.data.movie_list

import android.net.Uri

data class Movie(
    val id: Long,
    val uri: Uri,
    val name: String,
    val size: Int,
    val isFavorite: Boolean = false
) {
    companion object {
        val exampleList = listOf(
            Movie(
                id = 0L,
                uri = Uri.EMPTY,
                name = "Home video 13132131312313",
                size = 22
            ),
            Movie(
                id = 10L,
                uri = Uri.EMPTY,
                name = "Work video 13132131312313",
                size = 2222
            ),
            Movie(
                id = 100L,
                uri = Uri.EMPTY,
                name = "Street video",
                size = 22222
            ),
            Movie(
                id = 20L,
                uri = Uri.EMPTY,
                name = "New Home video 13132131312313",
                size = 222
            ),
            Movie(
                id = 0,
                uri = Uri.EMPTY,
                name = "Video #13132131312313",
                size = 999
            )
        )
    }
}
