package com.example.hearablemusicplayer.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hearablemusicplayer.domain.model.MusicInfo
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.components.FixedMusicList
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
        title = stringResource(R.string.title_home),
        trailing = {
            IconButton(
                onClick = {
                    haptic.performClick()
                    recommendationViewModel.refreshDailyMusicInfo()
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.player_d),
                    contentDescription = stringResource(R.string.refresh_daily_recommendation),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    ) {
        if (dailyMusic == null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.no_data_loaded),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.relaunch_or_config_ai),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.navigate(Routes.AI) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.go_to_ai_config))
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        } else {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
                    .fillMaxSize()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.today_recommendation),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
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

                Spacer(modifier = Modifier.height(24.dp))

                // Section 2: Heartbeat Playlist (Music List)
                val heartbeatList by recommendationViewModel.heartbeatList.collectAsState()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.today_heartbeat_playlist),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                    if (heartbeatList.isNotEmpty()){
                        FilledIconButton(
                            onClick = {
                                playControlViewModel.clearPlaylist()
                                playControlViewModel.addAllToPlaylistInOrder(heartbeatList)
                                playControlViewModel.playWith(heartbeatList.first())
                                navController.navigate(Routes.Player)
                            },
                            modifier = Modifier
                                .size(24.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.music_note_list),
                                contentDescription = stringResource(R.string.play_heartbeat_playlist),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                if (heartbeatList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.generating_heartbeat_playlist),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        FixedMusicList(
                            musicInfoList = heartbeatList,
                            onItemClick = {
                                haptic.performClick()
                                playControlViewModel.playWith(it)
                                },
                            onAddToPlaylist = { _ -> },
                            onMenuClick = {navController.navigate(Routes.SongDetail(it.music.id))},
                            showAddButton = false,
                            showMenuButton = true,
                            isPlaying = isPlaying,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(64.dp))
            }
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
            .padding(horizontal = 32.dp, vertical = 16.dp)
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onDetail),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = music.music.albumArtUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.5f to Color.Transparent,
                                1.0f to Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.align(Alignment.BottomStart)
                ) {
                    Text(
                        text = music.music.title,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = music.music.artist,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                FilledIconButton(
                    onClick = onPlay,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(48.dp), // Larger touch target
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.media_center),
                        contentDescription = "Play",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
