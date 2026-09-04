package com.hearablemusic.player.ui.library.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.hearablemusic.player.ui.common.layout.LocalWindowSizeInfo
import com.hearablemusic.player.ui.common.pages.base.TabScreen
import com.hearablemusic.player.ui.common.util.rememberHapticFeedback
import com.hearablemusic.player.ui.generated.resources.Res
import com.hearablemusic.player.ui.generated.resources.go_to_ai_config
import com.hearablemusic.player.ui.generated.resources.music_note_list
import com.hearablemusic.player.ui.generated.resources.no_data_loaded
import com.hearablemusic.player.ui.generated.resources.player_d
import com.hearablemusic.player.ui.generated.resources.processing_ai_recommendation
import com.hearablemusic.player.ui.generated.resources.refresh_daily_recommendation
import com.hearablemusic.player.ui.generated.resources.relaunch_or_config_ai
import com.hearablemusic.player.ui.generated.resources.title_home
import com.hearablemusic.player.ui.library.pages.components.FeatureEntryRow
import com.hearablemusic.player.ui.library.pages.components.HelloSlideCardStack
import com.hearablemusic.player.ui.library.pages.components.PlaylistEntryCard
import com.hearablemusic.player.ui.library.pages.components.RadioCard
import com.hearablemusic.player.ui.player.viewmodel.PlaylistQueueViewModel
import com.hearablemusic.player.ui.settings.viewmodel.RecommendationViewModel
import kotlinx.coroutines.launch
import com.hearablemusic.player.ui.common.util.activityViewModel
import com.hearablemusic.player.ui.common.navigation.Routes as NavRoutes
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(
    recommendationViewModel: RecommendationViewModel = activityViewModel(),
    playlistQueueViewModel: PlaylistQueueViewModel = activityViewModel(),
    navController: NavBackStack<NavKey>
) {
    val scope = rememberCoroutineScope()
    val dailyMusic by recommendationViewModel.dailyMusic.collectAsState(null)
    val isProcessingExtraInfo by recommendationViewModel.isProcessingExtraInfo.collectAsState()
    val heartbeatList by recommendationViewModel.heartbeatList.collectAsState()
    val haptic = rememberHapticFeedback()
    val isLandscape = LocalWindowSizeInfo.current.isLandscape

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            TabScreen(
                title = stringResource(Res.string.title_home),
                trailing = {
                    IconButton(
                        onClick = {
                            haptic.performClick()
                            recommendationViewModel.refreshDailyMusicInfo()
                        }
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.player_d),
                            contentDescription = stringResource(Res.string.refresh_daily_recommendation),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            ) {
                if (dailyMusic == null && !isProcessingExtraInfo) {
                    // ── 空状态 ──
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(Res.string.no_data_loaded),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(Res.string.relaunch_or_config_ai),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { navController.add(NavRoutes.AI.AI) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(stringResource(Res.string.go_to_ai_config))
                            }
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                } else if (dailyMusic == null && isProcessingExtraInfo) {
                    // ── AI 补全中 ──
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(Res.string.processing_ai_recommendation),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            CircularProgressIndicator()
                        }
                    }
                } else if (isLandscape) {
                    // ═══════════════════════════════════════
                    // Expanded 横向布局
                    // ① HelloSlideCardStack (32:7 横条)
                    // ② 左 RadioCard(1:1) + 右 { 今日推荐卡, 最近收藏卡 }
                    // ③ FeatureEntryRow
                    // ═══════════════════════════════════════
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 区域①
                        HelloSlideCardStack(
                            modifier = Modifier.fillMaxWidth().aspectRatio(32f / 7f),
                        )

                        // 区域②：Row { RadioCard; Column { 今日推荐; 最近收藏 } }
                        Row(
                            modifier = Modifier.fillMaxWidth().height(128.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            RadioCard(
                                modifier = Modifier.weight(0.5f),
                            )
                            Column(
                                modifier = Modifier.weight(0.5f).fillMaxHeight(),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                PlaylistEntryCard(
                                    icon = Res.drawable.music_note_list,
                                    title = "🎵 今日推荐",
                                    subtitle = "AI 为你精选",
                                    count = heartbeatList.size,
                                    onClickPlay = {
                                        if (heartbeatList.isNotEmpty()) {
                                            playlistQueueViewModel.clearPlaylist()
                                            playlistQueueViewModel.addAllToPlaylistInOrder(heartbeatList)
                                            playlistQueueViewModel.playWith(heartbeatList.first())
                                            navController.add(NavRoutes.Player.Player)
                                        }
                                    },
                                    onClickDetails = {
                                        navController.add(NavRoutes.Playlist.Playlist("今日推荐"))
                                    },
                                )
                                PlaylistEntryCard(
                                    icon = Res.drawable.music_note_list,
                                    title = "❤️ 最近收藏",
                                    subtitle = "WIP · 后续接 MusicRepository",
                                    count = heartbeatList.size, // 批次 B 占位：复用 heartbeatList
                                    onClickPlay = {
                                        if (heartbeatList.isNotEmpty()) {
                                            playlistQueueViewModel.clearPlaylist()
                                            playlistQueueViewModel.addAllToPlaylistInOrder(heartbeatList)
                                            playlistQueueViewModel.playWith(heartbeatList.first())
                                            navController.add(NavRoutes.Player.Player)
                                        }
                                    },
                                    onClickDetails = {
                                        navController.add(NavRoutes.Playlist.Playlist("最近收藏"))
                                    },
                                )
                            }
                        }

                        // 区域③
                        FeatureEntryRow(navController = navController)
                    }
                } else {
                    // ═══════════════════════════════════════
                    // Compact / Medium 纵向布局
                    // ① HelloSlideCardStack (16:9)
                    // ② Row { RadioCard(1:1); Column { 今日推荐; 最近收藏 } }
                    // ③ FeatureEntryRow
                    // ═══════════════════════════════════════
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                            .padding(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // 区域①
                        HelloSlideCardStack(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        )

                        // 区域②
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            RadioCard(
                                modifier = Modifier.weight(1f),
                            )
                            Column(
                                modifier = Modifier.weight(2f),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                PlaylistEntryCard(
                                    icon = Res.drawable.music_note_list,
                                    title = "🎵 今日推荐",
                                    subtitle = "AI 为你精选",
                                    count = heartbeatList.size,
                                    onClickPlay = {
                                        if (heartbeatList.isNotEmpty()) {
                                            playlistQueueViewModel.clearPlaylist()
                                            playlistQueueViewModel.addAllToPlaylistInOrder(heartbeatList)
                                            playlistQueueViewModel.playWith(heartbeatList.first())
                                            navController.add(NavRoutes.Player.Player)
                                        }
                                    },
                                    onClickDetails = {
                                        navController.add(NavRoutes.Playlist.Playlist("今日推荐"))
                                    },
                                )
                                PlaylistEntryCard(
                                    icon = Res.drawable.music_note_list,
                                    title = "❤️ 最近收藏",
                                    subtitle = "WIP · 后续接 MusicRepository",
                                    count = heartbeatList.size, // 批次 B 占位
                                    onClickPlay = {
                                        if (heartbeatList.isNotEmpty()) {
                                            playlistQueueViewModel.clearPlaylist()
                                            playlistQueueViewModel.addAllToPlaylistInOrder(heartbeatList)
                                            playlistQueueViewModel.playWith(heartbeatList.first())
                                            navController.add(NavRoutes.Player.Player)
                                        }
                                    },
                                    onClickDetails = {
                                        navController.add(NavRoutes.Playlist.Playlist("最近收藏"))
                                    },
                                )
                            }
                        }

                        // 区域③
                        FeatureEntryRow(navController = navController)
                    }
                }
            }
        }
    }
}
