package com.example.hearablemusicplayer.ui.components

import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hearablemusicplayer.data.database.MusicInfo
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback
import com.example.hearablemusicplayer.ui.viewmodel.PlayControlViewModel
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun MusicList(
    musicInfoList: List<MusicInfo>,
    playControlViewModel: PlayControlViewModel,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = musicInfoList,
        ) { musicInfo ->
            MusicItem(
                musicInfo = musicInfo,
                playControlViewModel = playControlViewModel,
                navController = navController,
                modifier = Modifier
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun MusicItem(
    musicInfo: MusicInfo,
    playControlViewModel: PlayControlViewModel,
    navController: NavController,
    modifier: Modifier
) {
    val scope = rememberCoroutineScope()
    val haptic = rememberHapticFeedback()
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable {
                haptic.performClick()
                // 在协程中等待播放准备完成后再导航
                scope.launch {
                    playControlViewModel.playWith(musicInfo)
                    playControlViewModel.recordPlayback(musicInfo.music.id, "Gallery")
                    navController.navigate("player")
                }
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        //专辑封面
        Spacer(modifier = Modifier.width(8.dp))
        AsyncImage(
            model = musicInfo.music.albumArtUri,
            contentDescription = "Album art",
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
        )
        Spacer(modifier = Modifier.width(24.dp))
        //音乐信息
        Column(
            modifier = Modifier.fillMaxWidth()
                .weight(1f)
        ) {
            Text(
                text = musicInfo.music.title,
                style = MaterialTheme.typography.titleSmall,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "${musicInfo.music.artist} • ${musicInfo.music.album}",
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(32.dp))
        //点赞按钮
        Row {
            IconButton(
                onClick = {
                    haptic.performConfirm()
                    scope.launch {
                        playControlViewModel.addToPlaylist(musicInfo)
                    }
                },
                modifier = Modifier
            ) {
                Icon(
                    painter = painterResource(R.drawable.plus_square),
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = "Add Button",
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(
                onClick = {
                    haptic.performLightClick()
                },
                modifier = Modifier
            ) {
                Icon(
                    painter = painterResource(R.drawable.dot_grid_1x2),
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = "Meum Button",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
