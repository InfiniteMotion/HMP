package com.hearablemusic.player.ui.library.pages.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import com.hearablemusic.player.ui.R

@Composable
fun AlbumCover(
    uri: String?,
    size: Dp,
    corner: Dp,
    shadow: Dp,
    modifier: Modifier = Modifier,
) {
    Crossfade(targetState = uri, label = "AlbumArtCrossroad") {
        AsyncImage(
            model = it,
            contentDescription = stringResource(R.string.album_art_desc),
            modifier = modifier
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
