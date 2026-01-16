package com.example.hearablemusicplayer.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hearablemusicplayer.domain.model.MusicInfo
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.components.MusicList
import com.example.hearablemusicplayer.ui.template.pages.TabScreen
import com.example.hearablemusicplayer.ui.util.Routes
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback
import com.example.hearablemusicplayer.ui.viewmodel.PlayControlViewModel
import com.example.hearablemusicplayer.ui.viewmodel.RecommendationViewModel
import kotlinx.coroutines.launch

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun HomeScreen(
    recommendationViewModel: RecommendationViewModel = hiltViewModel(),
    playControlViewModel: PlayControlViewModel = hiltViewModel(),
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    val dailyMusic by recommendationViewModel.dailyMusic.collectAsState(null)
    val haptic = rememberHapticFeedback()
    val isPlaying by playControlViewModel.isPlaying.collectAsState()

    TabScreen(
        title = "每日推荐",
        trailing = {
            IconButton(
                onClick = {
                    haptic.performClick()
                    recommendationViewModel.refreshDailyMusicInfo()
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.player_d),
                    contentDescription = "刷新每日推荐",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "今日推荐",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            if (dailyMusic == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "暂未加载到 Daily Music",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { navController.navigate(Routes.AI) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("前往 AI 配置")
                        }
                    }
                }
            } else {
                DailyHeroCard(
                    music = dailyMusic!!,
                    onPlay = {
                        haptic.performClick()
                        scope.launch {
                            playControlViewModel.playWith(dailyMusic!!)
                            navController.navigate(Routes.Player)
                        }
                    },
                    onDetail = {
                        haptic.performClick()
                        navController.navigate(Routes.SongDetail(dailyMusic!!.music.id))
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 2: Heartbeat Playlist (Music List)
            Text(
                text = "今日心动歌单",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            val heartbeatList by recommendationViewModel.heartbeatList.collectAsState()

            if (heartbeatList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "正在生成心动歌单...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                haptic.performClick()
                                scope.launch {
                                    val list = heartbeatList
                                    if (list.isNotEmpty()) {
                                        playControlViewModel.clearPlaylist()
                                        playControlViewModel.addAllToPlaylistInOrder(list)
                                        playControlViewModel.playWith(list.first())
                                        navController.navigate(Routes.Player)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = "播放心动歌单")
                        }
                    }
                    MusicList(
                        musicInfoList = heartbeatList,
                        onItemClick = { music ->
                            haptic.performClick()
                            navController.navigate(Routes.SongDetail(music.music.id))
                        },
                        onAddToPlaylist = { _ -> },
                        onMenuClick = { _ -> },
                        showAddButton = false,
                        showMenuButton = true,
                        isPlaying = isPlaying,
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

 

@Composable
fun DailyHeroCard(
    music: MusicInfo,
    onPlay: () -> Unit,
    onDetail: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(240.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = music.music.albumArtUri,
                contentDescription = null,
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = music.music.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = music.music.artist,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = music.music.album,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onPlay,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("播放")
                    }
                    Button(
                        onClick = onDetail,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("详情")
                    }
                }
            }
        }
    }
}

