package com.hearablemusic.player.ui.common.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.hmp.shared.resource.SharedIconLoader
import com.hearablemusic.player.ui.common.util.decodeToImageBitmap

/**
 * 标签图标展示（第 4 步随 ListScreen 迁入 commonMain）。
 * 平台依赖处置：BitmapFactory 解码 → ByteArray.decodeToImageBitmap() expect/actual。
 */
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
            bitmap = bytes.decodeToImageBitmap()
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
