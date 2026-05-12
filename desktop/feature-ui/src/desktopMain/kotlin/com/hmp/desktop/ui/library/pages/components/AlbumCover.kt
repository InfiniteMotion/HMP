package com.hmp.desktop.ui.library.pages.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage
import com.hmp.desktop.generated.resources.*
import org.jetbrains.compose.resources.painterResource

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
            contentDescription = "Album art",
            modifier = modifier
                .sizeIn(maxWidth = size, maxHeight = size)
                .aspectRatio(1f)
                .shadow(elevation = shadow, shape = RoundedCornerShape(corner))
                .clip(RoundedCornerShape(corner)),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(Res.drawable.`none`),
            error = painterResource(Res.drawable.`none`),
            fallback = painterResource(Res.drawable.`none`)
        )
    }
}
