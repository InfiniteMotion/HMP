package com.hmp.desktop.ui.library.pages
import com.hmp.desktop.ui.common.navigation.NavController

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.key
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
import com.hmp.desktop.ui.common.util.HapticFeedbackHelper
import com.hmp.desktop.ui.common.util.rememberHapticFeedback
import com.hmp.desktop.ui.common.dialogs.viewmodel.DialogViewModel
import com.hmp.desktop.ui.player.viewmodel.PlaybackViewModel
import com.hmp.desktop.ui.player.viewmodel.PlaylistQueueViewModel
import com.hmp.desktop.ui.settings.viewmodel.RecommendationViewModel
import kotlinx.coroutines.CoroutineScope
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
    val dailyMusic by recommendationViewModel.dailyMusic.collectAsState(recommendationViewModel.dailyMusic.value)
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
                    val windowInfo = LocalWindowInfo.current
                    val density = LocalDensity.current
                    val windowWidthDp = with(density) { windowInfo.containerSize.width.toDp() }
                    val sizeClass = widthSizeClass(windowWidthDp)
                    val heartbeatList by recommendationViewModel.heartbeatList.collectAsState()

                    val onPlay: () -> Unit = {
                        haptic.performClick()
                        scope.launch {
                            playlistQueueViewModel.playWith(dailyMusic!!)
                            navController.navigate(NavRoutes.Player.Player)
                        }
                    }
                    val onDetail: () -> Unit = {
                        haptic.performClick()
                        navController.navigate(NavRoutes.Library.SongDetail(dailyMusic!!.music.id))
                    }

                    if (sizeClass != WindowWidthSizeClass.Expanded) {
                        // ── Compact / Medium: 纵向堆叠 ──
                        key(dailyMusic!!.music.id) {
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
                                DailyHeroCard(music = dailyMusic!!, onPlay = onPlay, onDetail = onDetail)

                                Spacer(modifier = Modifier.height(24.dp))

                                HeartbeatSection(
                                    heartbeatList = heartbeatList,
                                    currentPlayingMusic = currentPlayingMusic,
                                    isPlaying = isPlaying,
                                    haptic = haptic,
                                    scope = scope,
                                    playlistQueueViewModel = playlistQueueViewModel,
                                    navController = navController,
                                    dialogViewModel = dialogViewModel
                                )
                            }
                        }
                    } else {
                        // ── Expanded: 横向布局 ──
                        key(dailyMusic!!.music.id) {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(horizontal = 48.dp),
                                horizontalArrangement = Arrangement.spacedBy(32.dp)
                            ) {
                                // 左侧：标题 + HeroCard
                                Column(modifier = Modifier.weight(0.4f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        Text(
                                            text = stringResource(Res.string.today_recommendation),
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        FilledIconButton(
                                            onClick = {
                                                haptic.performClick()
                                                recommendationViewModel.refreshDailyMusicInfo()
                                            },
                                            modifier = Modifier.size(28.dp),
                                            colors = IconButtonDefaults.filledIconButtonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = MaterialTheme.colorScheme.onPrimary
                                            ),
                                        ) {
                                            Icon(
                                                painter = painterResource(Res.drawable.player_d),
                                                contentDescription = stringResource(Res.string.refresh_daily_recommendation),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    DailyHeroCardContent(
                                        music = dailyMusic!!,
                                        onPlay = onPlay,
                                        onDetail = onDetail,
                                        showExtraInfo = true,
                                        modifier = Modifier.fillMaxWidth()
                                            .padding(top = 16.dp, bottom = 96.dp)
                                    )
                                }
                                // 右侧：标题 + 推荐列表
                                Column(modifier = Modifier.weight(0.6f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        Text(
                                            text = stringResource(Res.string.today_heartbeat_playlist),
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        if (heartbeatList.isNotEmpty()) {
                                            FilledIconButton(
                                                onClick = {
                                                    playlistQueueViewModel.addAllToPlaylistInOrder(heartbeatList)
                                                    navController.navigate(NavRoutes.Player.Player)
                                                },
                                                modifier = Modifier.size(28.dp),
                                                colors = IconButtonDefaults.filledIconButtonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary,
                                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                                ),
                                            ) {
                                                Icon(
                                                    painter = painterResource(Res.drawable.music_note_list),
                                                    contentDescription = stringResource(Res.string.play_heartbeat_playlist),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                    // 右侧可滚动列表
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .verticalScroll(rememberScrollState())
                                    ) {
                                        HeartbeatSection(
                                            heartbeatList = heartbeatList,
                                            currentPlayingMusic = currentPlayingMusic,
                                            isPlaying = isPlaying,
                                            haptic = haptic,
                                            scope = scope,
                                            playlistQueueViewModel = playlistQueueViewModel,
                                            navController = navController,
                                            dialogViewModel = dialogViewModel,
                                            showTitle = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun HeartbeatSection(
    heartbeatList: List<MusicInfo>,
    currentPlayingMusic: MusicInfo?,
    isPlaying: Boolean,
    haptic: HapticFeedbackHelper,
    scope: CoroutineScope,
    playlistQueueViewModel: PlaylistQueueViewModel,
    navController: NavController,
    dialogViewModel: DialogViewModel,
    showTitle: Boolean = true
) {
    if (showTitle) {
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
        if (heartbeatList.isNotEmpty()) {
            FilledIconButton(
                onClick = {
                    playlistQueueViewModel.addAllToPlaylistInOrder(heartbeatList)
                    navController.navigate(NavRoutes.Player.Player)
                },
                modifier = Modifier.size(24.dp),
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
    }

    if (heartbeatList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().height(100.dp),
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

    val aspectRatio = when (sizeClass) {
        WindowWidthSizeClass.Expanded -> 1f
        WindowWidthSizeClass.Medium -> 2f
        WindowWidthSizeClass.Compact -> 1f
    }
    val horizontalPadding = when (sizeClass) {
        WindowWidthSizeClass.Expanded -> 48.dp
        WindowWidthSizeClass.Medium -> 32.dp
        WindowWidthSizeClass.Compact -> 32.dp
    }

    DailyHeroCardContent(
        music = music,
        onPlay = onPlay,
        onDetail = onDetail,
        modifier = Modifier
            .padding(horizontal = horizontalPadding, vertical = 16.dp)
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
    )
}

@Composable
private fun DailyHeroCardContent(
    music: MusicInfo,
    onPlay: () -> Unit,
    onDetail: () -> Unit,
    showExtraInfo: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .clickable(onClick = onDetail),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        if (showExtraInfo) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 上半部分：封面 + 标题/歌手叠加
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
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
                                .size(48.dp),
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

                // 下半部分：描述信息
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val extra = music.extra
                        val bgIntro = extra?.backgroundIntroduce
                        val desc = extra?.description
                        val combined = listOfNotNull(bgIntro, desc).filter { it.isNotBlank() }
                            .joinToString("\n")
                        if (combined.isNotBlank()) {
                            Text(
                                text = combined,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                maxLines = 6,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        } else {
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
                            .size(48.dp),
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
}
