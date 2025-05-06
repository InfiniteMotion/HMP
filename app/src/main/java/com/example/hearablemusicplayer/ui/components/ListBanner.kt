package com.example.hearablemusicplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.hearablemusicplayer.database.MusicInfo
import com.example.hearablemusicplayer.viewmodel.PlayControlViewModel

@Composable
fun ListBanner(
    bannerNameF: String,
    bannerNameS: String,
    musicList: List<MusicInfo>,
    themeColorResId: Int,
    playControlViewModel: PlayControlViewModel,
    navController: NavController,
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
                style = MaterialTheme.typography.headlineMedium
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
                style = MaterialTheme.typography.headlineMedium
            )
        }
        Spacer(modifier = Modifier.height(10.dp))

        val listState = rememberLazyListState()
        val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
        // 横向滚动列表部分
        LazyRow(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(musicList) { musicInfo ->
                Banner(
                    musicInfo = musicInfo,
                    viewModel = playControlViewModel,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun Banner(
    musicInfo: MusicInfo,
    viewModel: PlayControlViewModel,
    navController: NavController
) {
    val imageModifier = Modifier
        .size(100.dp)
        .shadow(elevation = 5.dp, shape = RoundedCornerShape(10.dp))
        .clip(RoundedCornerShape(10.dp))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(110.dp)
            .clickable {
                viewModel.playWith(musicInfo)
                viewModel.recordPlayback(musicInfo.music.id, "Banner")
                navController.navigate("player")
            }
    ) {
        AsyncImage(
            model = musicInfo.extra?.albumArtUri,
            contentDescription = "Album art",
            contentScale = ContentScale.Crop,
            modifier = imageModifier
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = musicInfo.music.title,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.widthIn(max = 120.dp),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}
