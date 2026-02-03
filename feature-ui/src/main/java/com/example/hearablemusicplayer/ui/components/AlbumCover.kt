package com.example.hearablemusicplayer.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.hearablemusicplayer.ui.R

@Composable
fun AlbumCover(
    uri: String?,
    size: Dp,
    corner: Dp,
    shadow: Dp
) {
    Crossfade(targetState = uri, label = "AlbumArtCrossroad") {
        AsyncImage(
            model = it,
            contentDescription = "Album art",
            modifier = Modifier
                .sizeIn(maxWidth = size, maxHeight = size)
                .aspectRatio(1f)
                .shadow(elevation = shadow, shape = RoundedCornerShape(corner))
                .clip(RoundedCornerShape(corner)),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.none),
            error = painterResource(R.drawable.none),
            fallback = painterResource(R.drawable.none)
        )
    }
}
