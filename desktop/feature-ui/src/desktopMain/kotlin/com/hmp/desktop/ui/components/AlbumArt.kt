package com.hmp.desktop.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.jaudiotagger.audio.AudioFileIO
import java.io.File

@Composable
fun AlbumArt(
    path: String,
    modifier: Modifier = Modifier
) {
    val imageBitmap: ImageBitmap? = remember(path) {
        loadAlbumArt(path)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = "Album Art",
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

private fun loadAlbumArt(path: String): ImageBitmap? {
    if (path.isBlank()) return null
    return try {
        val audioFile = AudioFileIO.read(File(path))
        val artwork = audioFile.tag?.firstArtwork?.binaryData
        if (artwork != null) {
            org.jetbrains.skia.Image.makeFromEncoded(artwork).toComposeImageBitmap()
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }
}
