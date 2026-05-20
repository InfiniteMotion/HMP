package com.hmp.desktop.ui.common.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.hmp.desktop.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun Avatar(
    aSize: Int,
    imageUri: String?,
){
    if (!imageUri.isNullOrEmpty()) {
        AsyncImage(
            model = imageUri,
            contentDescription = "User Avatar",
            modifier = Modifier
                .size(aSize.dp)
                .clip(CircleShape)
        )
    } else {
        Image(
            painter = painterResource(Res.drawable.`none`),
            contentDescription = "User Avatar",
            modifier = Modifier
                .size(aSize.dp)
                .clip(CircleShape)
        )
    }
}
