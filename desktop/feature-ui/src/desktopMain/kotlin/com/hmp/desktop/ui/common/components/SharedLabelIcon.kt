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

private val iconBitmapCache = mutableMapOf<String, ImageBitmap>()

@Composable
fun SharedLabelIcon(
    iconName: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val cacheKey = iconName.lowercase()
    val cachedBitmap = iconBitmapCache[cacheKey]
    var bitmap by remember { mutableStateOf<ImageBitmap?>(cachedBitmap) }

    LaunchedEffect(iconName) {
        if (cachedBitmap == null) {
            val bytes = SharedIconLoader.loadIcon(cacheKey)
            if (bytes != null) {
                try {
                    val skiaImage = SkiaImage.makeFromEncoded(bytes)
                    val imageBitmap = skiaImage.toComposeImageBitmap()
                    iconBitmapCache[cacheKey] = imageBitmap
                    bitmap = imageBitmap
                } catch (_: Exception) {
                }
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
