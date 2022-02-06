package com.example.scopedstorage.compose

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scopedstorage.data.movie_list.Movie
import com.example.scopedstorage.ui.theme.ScopedStorageTheme
import com.example.scopedstorage.utils.haveQ
import com.skydoves.landscapist.glide.GlideImage
import java.math.RoundingMode
import java.text.DecimalFormat
import androidx.compose.runtime.*


@Composable
fun MovieItem(
    movie: Movie,
    onDeleteClick: () -> Unit,
    onAddToFavorite: (Uri, Boolean) -> Unit,
    onNonAndroidQStarClick: () -> Unit
) {
    val df = DecimalFormat("#.##")
    df.roundingMode = RoundingMode.CEILING
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(vertical = 4.dp, horizontal = 8.dp),
        elevation = 4.dp,
        backgroundColor = MaterialTheme.colors.background,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clickable { /*Click*/ },
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlideImage(
                imageModel = movie.uri,
                modifier = Modifier.size(100.dp)
            )
            Text(
                text = movie.name,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                fontWeight = FontWeight.Light,
                fontSize = 16.sp,
            )
            Text(
                text = "${df.format(movie.size.toDouble() / 1024)} Mb",
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                textAlign = TextAlign.End,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Light
            )
            Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
                if (haveQ()) {
                    IconButton(onClick = {
                        onAddToFavorite(movie.uri, movie.isFavorite.not())
                    }) {
                        if (movie.isFavorite) StarIcon() else StarOutlineIcon()
                    }
                } else {
                    IconButton(onClick = { onNonAndroidQStarClick() }) {
                        StarHalfIcon()
                    }
                }
                IconButton(onClick = { onDeleteClick() }) {
                    DeleteIcon()
                }
            }
        }
    }
}


@Preview(showBackground = true, widthDp = 320)
@Composable
fun MovieItemPreview() {
    ScopedStorageTheme {
        MovieItem(
            Movie(
                id = 0,
                uri = Uri.EMPTY,
                name = "Home video 13132131312313",
                size = 22
            ),
            onDeleteClick = { },
            onAddToFavorite = { _, _ -> },
            onNonAndroidQStarClick = {}
        )
    }
}