package com.example.hearablemusicplayer.ui.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import com.example.hearablemusicplayer.database.Music
import com.example.hearablemusicplayer.viewmodel.MusicViewModel
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hearablemusicplayer.R
import com.example.hearablemusicplayer.database.AppPreferences
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(
    viewModel: MusicViewModel,
    navController: NavController
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val music = remember { mutableStateOf<Music?>(null) }
        val musicId = AppPreferences.getCurrentPlayingMusicId()
        LaunchedEffect(Unit) {
            musicId?.let {
                // 根据音乐 ID 获取音乐详情
                viewModel.getMusicById(it).collect { musicDetail ->
                    music.value = musicDetail
                }
            }
        }

        if (music.value == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // 显示音乐播放界面
            MusicPlayerContent(music = music.value!!,viewModel,navController)
        }
    }
}

@Composable
fun MusicPlayerContent(
    music: Music,
    viewModel: MusicViewModel,
    navController: NavController
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
    ) {
        val isPlaying by viewModel.isPlaying.collectAsState()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .size(32.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.chevron_down),
                    contentDescription = "select Button",
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Text(
                text = music.title,
                style = MaterialTheme.typography.displayMedium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1, // 设置最大行数为1，强制水平滚动
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${music.artist} ",
                style = MaterialTheme.typography.titleMedium
                )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${music.album}",
                style = MaterialTheme.typography.titleMedium
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center
        ){
            AsyncImage(
                model = music.albumArtUri,
                contentDescription = "Album art",
                modifier = Modifier
                    .size(300.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ){
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.backward_end_fill),
                    contentDescription = "pre"
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(72.dp)
                    .clickable { viewModel.togglePlaying() }
            ) {
                Icon(
                    painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play_fill),
                    contentDescription = "on"
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.forward_end_fill),
                    contentDescription = "next"
                )
            }
        }
    }
}