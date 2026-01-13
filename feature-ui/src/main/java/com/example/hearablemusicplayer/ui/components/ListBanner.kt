package com.example.hearablemusicplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hearablemusicplayer.domain.model.enum.LabelName
import com.example.hearablemusicplayer.ui.util.Routes
import com.example.hearablemusicplayer.ui.viewmodel.PlaylistViewModel

@Composable
fun ListGroupName(
    bannerNameF: String,
    bannerNameS: String,
    themeColorResId: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        // 标题部分
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(start = 20.dp, top = 16.dp, bottom = 8.dp)
        ) {
            Text(
                text = bannerNameF,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(colorResource(themeColorResId))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = bannerNameS,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun ListBanner(
    listName: String = "",
    listCoverUri: Int,
    playlistViewModel: PlaylistViewModel,
    navController: NavController
) {
    val imageModifier = Modifier
        .size(100.dp)
        .shadow(elevation = 5.dp, shape = RoundedCornerShape(15.dp))
        .clip(RoundedCornerShape(15.dp))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(110.dp)
            .clickable {
                playlistViewModel.getSelectedPlaylist(listName)
                navController.navigate(Routes.PLAYLIST)
            }
    ) {
        AsyncImage(
            model = listCoverUri,
            contentDescription = "Album art",
            contentScale = ContentScale.Crop,
            modifier = imageModifier
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = listName,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.widthIn(max = 120.dp),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun ListBanner(
    listName: LabelName,
    listCoverUri: Int,
    playlistViewModel: PlaylistViewModel,
    navController: NavController
) {
    val imageModifier = Modifier
        .size(100.dp)
        .shadow(elevation = 5.dp, shape = RoundedCornerShape(15.dp))
        .clip(RoundedCornerShape(15.dp))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(110.dp)
            .clickable {
                playlistViewModel.getSelectedPlaylist(listName.name)
                navController.navigate(Routes.PLAYLIST)
            }
    ) {
        AsyncImage(
            model = listCoverUri,
            contentDescription = "Album art",
            contentScale = ContentScale.Crop,
            modifier = imageModifier
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = listName.name,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.widthIn(max = 120.dp),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
