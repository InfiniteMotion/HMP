package com.example.hearablemusicplayer.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.hearablemusicplayer.R
import com.example.hearablemusicplayer.viewmodel.MusicViewModel

@Composable
fun Avatar(
    aSize: Int,
    viewModel: MusicViewModel
){
    val imageUri by viewModel.avatarUri.collectAsState(initial = 0)
    if (imageUri != 0) {
        AsyncImage(
            model = imageUri,
            contentDescription = "User Avatar",
            modifier = Modifier
                .size(aSize.dp)
                .clip(CircleShape)
                .border(width = 4.dp, color = MaterialTheme.colorScheme.primary, shape = CircleShape)
        )
    } else {
        Image(
            painter = painterResource(R.drawable.avatar),
            contentDescription = "User Avatar",
            modifier = Modifier
                .size(aSize.dp)
                .clip(CircleShape)
                .shadow(elevation = 8.dp, shape = CircleShape, clip = true)
                .border(width = 4.dp, color = MaterialTheme.colorScheme.primary, shape = CircleShape)

        )
    }
}