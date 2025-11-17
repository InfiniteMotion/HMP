package com.example.hearablemusicplayer.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun AlbumCover(
    uri: String?,
    place: Arrangement.Horizontal,
    size: Int,
) {
    val imageModifier = Modifier
        .size(size.dp)
        .shadow(elevation = 10.dp, shape = RoundedCornerShape(20.dp))
        .clip(RoundedCornerShape(20.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement =place
    ) {
        Crossfade(targetState = uri, label = "AlbumArtCrossroad") { targetUri ->
            AsyncImage(
                model = targetUri,
                contentDescription = "Album art",
                modifier = imageModifier
            )
        }
    }
}