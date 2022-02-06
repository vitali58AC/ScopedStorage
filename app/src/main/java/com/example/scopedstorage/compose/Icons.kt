package com.example.scopedstorage.compose

import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Grade
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarHalf
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.scopedstorage.R

@Composable
fun DriveFileMoveIcon(tint: Color = MaterialTheme.colors.primaryVariant) {
    Icon(
        imageVector = Icons.Filled.DriveFileMove,
        contentDescription = stringResource(R.string.set_directory_icon),
        tint = tint,
        modifier = Modifier.size(30.dp)
    )
}

@Composable
fun StarIcon(tint: Color = MaterialTheme.colors.primaryVariant) {
    Icon(
        imageVector = Icons.Filled.Star,
        contentDescription = stringResource(R.string.star_icon),
        tint = tint,
    )
}

@Composable
fun StarOutlineIcon(tint: Color = MaterialTheme.colors.primaryVariant) {
    Icon(
        imageVector = Icons.Outlined.Grade,
        contentDescription = stringResource(R.string.star_icon),
        tint = tint,
    )
}

@Composable
fun StarHalfIcon(tint: Color = MaterialTheme.colors.primaryVariant) {
    Icon(
        imageVector = Icons.Outlined.StarHalf,
        contentDescription = stringResource(R.string.star_icon),
        tint = tint,
    )
}

@Composable
fun DeleteIcon(tint: Color = MaterialTheme.colors.primaryVariant) {
    Icon(
        imageVector = Icons.Filled.Delete,
        contentDescription = stringResource(R.string.delete_icon),
        tint = tint,
    )
}