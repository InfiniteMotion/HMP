package com.hmp.desktop.ui.library.pages
import com.hmp.desktop.ui.common.navigation.NavController

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import com.hmp.desktop.ui.common.layout.widthSizeClass
import com.hmp.desktop.ui.common.layout.WindowWidthSizeClass
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo


import com.hmp.desktop.ui.common.navigation.Routes as NavRoutes
import coil3.compose.AsyncImage
import com.hmp.domain.music.MusicInfo
import com.hmp.desktop.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.painterResource
import com.hmp.desktop.ui.library.pages.components.musiclist.CurrentPlayingConfig
import com.hmp.desktop.ui.library.pages.components.musiclist.EditConfig
import com.hmp.desktop.ui.library.pages.components.musiclist.FixedMusicList
import com.hmp.desktop.ui.library.pages.components.musiclist.FullItemOptions
import com.hmp.desktop.ui.library.pages.components.musiclist.HeaderConfig
import com.hmp.desktop.ui.library.pages.components.musiclist.ItemConfig
import com.hmp.desktop.ui.library.pages.components.musiclist.ItemVariant
import com.hmp.desktop.ui.library.pages.components.musiclist.MusicListCallbacksAdapter
import com.hmp.desktop.ui.library.pages.components.musiclist.defaultMusicListConfig
import com.hmp.desktop.ui.common.pages.base.TabScreen
import com.hmp.desktop.ui.common.util.rememberHapticFeedback
import com.hmp.desktop.ui.common.dialogs.viewmodel.DialogViewModel
import com.hmp.desktop.ui.player.viewmodel.PlaybackViewModel
import com.hmp.desktop.ui.player.viewmodel.PlaylistQueueViewModel
import com.hmp.desktop.ui.settings.viewmodel.RecommendationViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    recommendationViewModel: RecommendationViewModel = koinInject(),
    playbackViewModel: PlaybackViewModel,
    playlistQueueViewModel: PlaylistQueueViewModel,
    dialogViewModel: DialogViewModel,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    val dailyMusic by recommendationViewModel.dailyMusic.collectAsState(null)
    val currentPlayingMusic by playlistQueueViewModel.currentPlayingMusic.collectAsState(null)
    val haptic = rememberHapticFeedback()
    val isPlaying by playbackViewModel.isPlaying.collectAsState()

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
                if (dailyMusic == null) {
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
                            onClick = { navController.navigate(NavRoutes.AI.AI) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(stringResource(Res.string.go_to_ai_config))
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                } else {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                            .padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.today_recommendation),
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
                                        playlistQueueViewModel.playWith(dailyMusic!!)
                                        navController.navigate(NavRoutes.Player.Player)
                                    }
                            },
                            onDetail = {
                                haptic.performClick()
                                navController.navigate(NavRoutes.Library.SongDetail(dailyMusic!!.music.id))
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
                                text = stringResource(Res.string.today_heartbeat_playlist),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                            if (heartbeatList.isNotEmpty()){
                                FilledIconButton(
                                    onClick = {
                                        playlistQueueViewModel.addAllToPlaylistInOrder(heartbeatList)
                                        navController.navigate(NavRoutes.Player.Player)
                                    },
                                    modifier = Modifier
                                        .size(24.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                ) {
                                    Icon(
                                        painter = painterResource(Res.drawable.music_note_list),
                                        contentDescription = stringResource(Res.string.play_heartbeat_playlist),
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
                                    text = stringResource(Res.string.generating_heartbeat_playlist),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            val currentPlayingIndex = heartbeatList.indexOfFirst { it.music.id == currentPlayingMusic?.music?.id }.takeIf { it >= 0 }
                            val callbacks = object : MusicListCallbacksAdapter() {
                                override fun onItemClick(musicInfo: MusicInfo, index: Int) {
                                    haptic.performClick()
                                    scope.launch {
                                        playlistQueueViewModel.playWith(musicInfo)
                                    }
                                    navController.navigate(NavRoutes.Player.Player)
                                }
                                override fun onMenuClick(musicInfo: MusicInfo) {
                                    val menuConfig = DialogViewModel.MusicDetailMenuConfig(
                                        showAddToPlaylist = true,
                                        showAddToSpecificPlaylist = true,
                                        showShare = true,
                                        showViewDetail = true,
                                        showPlayNext = false,
                                        showRemoveFromCurrentPlaylist = false,
                                        showDelete = false
                                    )
                                    dialogViewModel.showMusicDetailDialog(musicInfo, menuConfig)
                                }
                            }
                            val config = defaultMusicListConfig(callbacks).copy(
                                header = HeaderConfig.None,
                                item = ItemConfig(
                                    variant = ItemVariant.Full,
                                    showIndex = true,
                                    fullOptions = FullItemOptions(
                                        showPinButton = false,
                                        showRemoveButton = false,
                                        showMenuButton = true
                                    ),
                                ),
                                edit = EditConfig(enabled = false),
                                currentPlaying = CurrentPlayingConfig(
                                    index = currentPlayingIndex,
                                    autoScrollToCurrent = false
                                ),
                            )
                            Column(modifier = Modifier.fillMaxWidth()) {
                                FixedMusicList(
                                    musicInfoList = heartbeatList,
                                    config = config,
                                    modifier = Modifier.fillMaxWidth(),
                                    isPlaying = isPlaying,
                                )
                            }
                        }
                    }
                }
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
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val windowWidthDp = with(density) { windowInfo.containerSize.width.toDp() }
    val sizeClass = widthSizeClass(windowWidthDp)

    // 扩展布局使用更宽的卡片比例，紧凑布局使用正方形
    val aspectRatio = when (sizeClass) {
        WindowWidthSizeClass.Expanded -> 16f / 9f
        WindowWidthSizeClass.Medium -> 4f / 3f
        WindowWidthSizeClass.Compact -> 1f
    }
    val horizontalPadding = when (sizeClass) {
        WindowWidthSizeClass.Expanded -> 48.dp
        WindowWidthSizeClass.Medium -> 32.dp
        WindowWidthSizeClass.Compact -> 32.dp
    }

    Card(
        modifier = Modifier
            .padding(horizontal = horizontalPadding, vertical = 16.dp)
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
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
                        painter = painterResource(Res.drawable.media_center),
                        contentDescription = "Play",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
