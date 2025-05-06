package com.example.hearablemusicplayer.ui.components

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hearablemusicplayer.R
import com.example.hearablemusicplayer.database.MusicInfo
import com.example.hearablemusicplayer.viewmodel.MusicViewModel
import com.example.hearablemusicplayer.viewmodel.PlayControlViewModel

@Composable
fun MusicList(
    musicViewModel: MusicViewModel,
    playControlViewModel: PlayControlViewModel,
    navController: NavController
) {
    val musicInfoList by musicViewModel.allMusic.collectAsState()
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(musicInfoList) { musicInfo ->
            MusicItem(musicInfo = musicInfo,playControlViewModel,navController)
        }
    }
}

@Composable
fun MusicItem(
    musicInfo: MusicInfo,
    playControlViewModel: PlayControlViewModel,
    navController: NavController
) {
    var isLiked by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable {
                // 跳转到音乐播放页，并传递音乐 ID
                playControlViewModel.playWith(musicInfo)
                playControlViewModel.recordPlayback(musicInfo.music.id, "Gallery")
                navController.navigate("player")
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        //专辑封面
        AsyncImage(
            model = musicInfo.extra?.albumArtUri,
            contentDescription = "Album art",
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
        )
        Spacer(modifier = Modifier.width(16.dp))
        //音乐信息
        Column(
            modifier = Modifier.width(180.dp)
        ) {
            Text(
                text = musicInfo.music.title,
                style = MaterialTheme.typography.titleSmall,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier.widthIn(max = 180.dp)
            )
            Text(
                text = "${musicInfo.music.artist} • ${musicInfo.music.album}",
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier.widthIn(max = 180.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        //点赞按钮
        Row {
            IconButton(
                onClick = {
                    isLiked = !isLiked // 切换点赞状态
                    playControlViewModel.updateMusicLikedStatus(musicInfo, isLiked)
                },
                modifier = Modifier
            ) {
                isLiked = musicInfo.userInfo?.liked ?: false
                if (isLiked) {
                    Icon(
                        painter = painterResource(R.drawable.heart_fill),
                        contentDescription = "Favorite",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }else{
                    Icon(
                        painter = painterResource(R.drawable.heart),
                        contentDescription = "Favorite",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            IconButton(
                onClick = {
                    playControlViewModel.addToPlaylist(musicInfo)
                },
                modifier = Modifier
            ) {
                Icon(
                    painter = painterResource(R.drawable.plus_square),
                    contentDescription = "Add Button",
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(
                onClick = {},
                modifier = Modifier
            ) {
                Icon(
                    painter = painterResource(R.drawable.dot_grid_1x2),
                    contentDescription = "Meum Button",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
