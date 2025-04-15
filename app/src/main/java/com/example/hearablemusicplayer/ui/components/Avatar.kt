package com.example.hearablemusicplayer.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.hearablemusicplayer.R
import com.example.hearablemusicplayer.database.AppPreferences

@Composable
fun Avatar(
    aSize: Int
){
    val imageUri=AppPreferences.getAvatarUri()
    if (imageUri != null) {
        AsyncImage(
            model = imageUri,
            contentDescription = "User Avatar",
            modifier = Modifier
                .size(aSize.dp)
        )
    } else {
        Image(
            painter = painterResource(R.drawable.avatar),
            contentDescription = "User Avatar",
            modifier = Modifier
                .size(aSize.dp)
                .clip(CircleShape)
                .shadow(elevation = 8.dp, shape = CircleShape, clip = true)
                .border(width = 4.dp, color = colorResource(R.color.HDBlue), shape = CircleShape)
        )
    }
}