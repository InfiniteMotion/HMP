package com.example.hearablemusicplayer.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.hearablemusicplayer.ui.R

@Composable
fun Avatar(
    aSize: Int,
    imageUri: String?,
){
    if (imageUri != "") {
        AsyncImage(
            model = imageUri,
            contentDescription = "User Avatar",
            modifier = Modifier
                .size(aSize.dp)
                .clip(CircleShape)
        )
    } else {
        Image(
            painter = painterResource(R.drawable.none),
            contentDescription = "User Avatar",
            modifier = Modifier
                .size(aSize.dp)
                .clip(CircleShape)
        )
    }
}
