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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.hearablemusicplayer.ui.R

@Composable
fun AlbumCover(
    uri: String?,
    place: Arrangement.Horizontal, // Keep for compatibility but we might not need Row
    size: Int,
) {
    Crossfade(targetState = uri, label = "AlbumArtCrossroad") {
        AsyncImage(
            model = it,
            contentDescription = "Album art",
            modifier = Modifier
                .sizeIn(maxWidth = size.dp, maxHeight = size.dp)
                .aspectRatio(1f)
                .shadow(elevation = 10.dp, shape = RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp)),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.none),
            error = painterResource(R.drawable.none),
            fallback = painterResource(R.drawable.none)
        )
    }
}
