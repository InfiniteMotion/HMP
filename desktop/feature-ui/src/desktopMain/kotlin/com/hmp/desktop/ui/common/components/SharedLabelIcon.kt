package com.hmp.desktop.ui.common.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.hmp.shared.resource.SharedIconLoader
import org.jetbrains.skia.Image as SkiaImage

@Composable
fun SharedLabelIcon(
    iconName: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(iconName) {
        val bytes = SharedIconLoader.loadIcon(iconName.lowercase())
        if (bytes != null) {
            try {
                val skiaImage = SkiaImage.makeFromEncoded(bytes)
                bitmap = skiaImage.toComposeImageBitmap()
            } catch (_: Exception) {
                // Failed to decode icon, show nothing
            }
        }
    }

    bitmap?.let {
        Image(
            bitmap = it,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}
