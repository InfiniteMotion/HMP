package com.hmp.desktop.ui.library.pages
import com.hmp.desktop.ui.common.navigation.NavController

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import org.jetbrains.compose.resources.DrawableResource
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject


import coil3.compose.AsyncImage
import com.hmp.domain.enum.LabelName
import com.hmp.domain.playlist.Playlist
import com.hmp.desktop.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.painterResource
import com.hmp.desktop.ui.common.components.Capsule
import com.hmp.desktop.ui.library.pages.components.ListBanner
import com.hmp.desktop.ui.library.pages.components.ListGroupName
import com.hmp.desktop.ui.common.components.base.NewPlaylistButton
import com.hmp.desktop.ui.common.pages.base.TabScreen
import com.hmp.desktop.ui.common.navigation.Routes
import com.hmp.desktop.ui.common.util.UiState
import com.hmp.desktop.ui.common.components.SharedLabelIcon
import com.hmp.desktop.ui.common.util.iconName
import com.hmp.desktop.ui.common.util.rememberHapticFeedback
import com.hmp.desktop.ui.common.dialogs.viewmodel.DialogViewModel
import com.hmp.desktop.ui.playlist.viewmodel.PlaylistViewModel

@Composable
fun ListScreen(
    playlistViewModel: PlaylistViewModel = koinInject(),
    dialogViewModel: DialogViewModel = koinInject(),
    navController: NavController
) {
    val genreList by playlistViewModel.genrePlaylistName.collectAsState()
    val moodList by playlistViewModel.moodPlaylistName.collectAsState()
    val scenarioList by playlistViewModel.scenarioPlaylistName.collectAsState()
    val languageList by playlistViewModel.languagePlaylistName.collectAsState()
    val eraList by playlistViewModel.eraPlaylistName.collectAsState()
    val userCustomPlaylistsState by playlistViewModel.userCustomPlaylistsState.collectAsState()
    val userCustomPlaylists = (userCustomPlaylistsState as? UiState.Success)?.data ?: emptyList()

    ListScreenContent(
        genreList = genreList,
        moodList = moodList,
        scenarioList = scenarioList,
        languageList = languageList,
        eraList = eraList,
        userCustomPlaylists = userCustomPlaylists,
        playlistViewModel = playlistViewModel,
        dialogViewModel = dialogViewModel,
        navController = navController
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ListScreenContent(
    genreList: List<LabelName>,
    moodList: List<LabelName>,
    scenarioList: List<LabelName>,
    languageList: List<LabelName>,
    eraList: List<LabelName>,
    userCustomPlaylists: List<Playlist>,
    playlistViewModel: PlaylistViewModel,
    dialogViewModel: DialogViewModel,
    navController: NavController
) {
    val haptic = rememberHapticFeedback()

    // 与列表管理页一致：置顶优先，再按最近播放、更新时间
    val sortedUserPlaylists = remember(userCustomPlaylists) {
        userCustomPlaylists.sortedWith(
            compareByDescending<Playlist> { it.isPinned }
                .thenByDescending { it.lastPlayedAt ?: 0L }
                .thenByDescending { it.updatedAt }
        )
    }

    TabScreen(
        title = stringResource(Res.string.title_playlist),
        hasSearchBotton = false,
        navController = navController,
        trailing = {
            NewPlaylistButton(
                onClick = {
                    dialogViewModel.showCreatePlaylistDialog { id ->
                        playlistViewModel.loadUserCustomPlaylists()
                        navController.navigate(Routes.Playlist.CustomPlaylist(id))
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 用户自定义播放列表
            Column {
                ListGroupName(
                    bannerNameF = stringResource(Res.string.user_custom_playlists),
                    bannerNameS = stringResource(Res.string.banner_daily_GG),
                    themeColor = Color(0xFF6200EE),
                    trailing = {
                        TextButton(
                            onClick = {
                                haptic.performClick()
                                navController.navigate(Routes.Playlist.UserPlaylistManage)
                            }
                        ) {
                            Text(
                                text = stringResource(Res.string.manage_playlists),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(sortedUserPlaylists) { playlist ->
                        UserListCard(
                            playlist = playlist,
                            onClick = {
                                haptic.performClick()
                                navController.navigate(Routes.Playlist.CustomPlaylist(playlist.id))
                            }
                        )
                    }
                }
            }

            // 常用列表 (Common Playlists) - 保留原有样式
            Column {
                ListGroupName(
                    bannerNameF = stringResource(Res.string.banner_daily_A),
                    bannerNameS = stringResource(Res.string.banner_daily_AA),
                    themeColor = Color(0xFFE53935)
                )
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ListBanner(
                        listName = stringResource(Res.string.banner_default),
                        listCoverUri = Res.drawable.defaultlist,
                        navController = navController
                    )
                    ListBanner(
                        listName = stringResource(Res.string.banner_heart),
                        listCoverUri = Res.drawable.heartlist,
                        navController = navController
                    )
                    ListBanner(
                        listName = stringResource(Res.string.banner_history),
                        listCoverUri = Res.drawable.historylist,
                        navController = navController
                    )
                }
            }

            // 适用场景 (Scenario) - 沉浸推荐
            LabelListGroup(
                data = scenarioList,
                bannerNameF = stringResource(Res.string.banner_daily_D),
                bannerNameS = stringResource(Res.string.banner_daily_DD),
                themeColor = Color(0xFF43A047)
            ) { list ->
                val scenarioListState = rememberLazyListState()
                val scenarioFlingBehavior = rememberSnapFlingBehavior(lazyListState = scenarioListState)

                LazyRow(
                    state = scenarioListState,
                    flingBehavior = scenarioFlingBehavior,
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(list) { label ->
                        ScenarioCard(
                            label = label,
                            onClick = {
                                haptic.performClick()
                                navController.navigate(Routes.Playlist.Playlist(label.name))
                            }
                        )
                    }
                }
            }

            // 风格流派 (Genre) - 横向画廊
            LabelListGroup(
                data = genreList,
                bannerNameF = stringResource(Res.string.banner_daily_B),
                bannerNameS = stringResource(Res.string.banner_daily_BB),
                themeColor = Color(0xFF1976D2)
            ) { list ->
                val genreListState = rememberLazyListState()
                val genreFlingBehavior = rememberSnapFlingBehavior(lazyListState = genreListState)
                
                LazyRow(
                    state = genreListState,
                    flingBehavior = genreFlingBehavior,
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(list) { label ->
                        GenreCard(
                            label = label,
                            onClick = {
                                haptic.performClick()
                                navController.navigate(Routes.Playlist.Playlist(label.name))
                            }
                        )
                    }
                }
            }

            // 音乐情绪 (Mood) - 网格探索
            LabelListGroup(
                data = moodList,
                bannerNameF = stringResource(Res.string.banner_daily_C),
                bannerNameS = stringResource(Res.string.banner_daily_CC),
                themeColor = Color(0xFFFF9800)
            ) { list ->
                Box(modifier = Modifier.height(220.dp)) {
                    LazyHorizontalGrid(
                        rows = GridCells.Fixed(2),
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(list) { label ->
                            MoodCard(
                                label = label,
                                onClick = {
                                    haptic.performClick()
                                    navController.navigate(Routes.Playlist.Playlist(label.name))
                                }
                            )
                        }
                    }
                }
            }

            // 探索更多 (Language & Era) - 标签云
            LabelListGroup(
                data = languageList + eraList,
                bannerNameF = stringResource(Res.string.explore),
                bannerNameS = stringResource(Res.string.more),
                themeColor = Color(0xFF6200EE)
            ) { list ->
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    list.forEach { label ->
                        Box(
                            modifier = Modifier.clickable {
                                haptic.performClick()
                                navController.navigate(Routes.Playlist.Playlist(label.name))
                            }
                        ) {
                            Capsule(
                                text = label.name,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

// ================== 局部组件定义 ==================

@Composable
fun <T> LabelListGroup(
    data: List<T>,
    bannerNameF: String,
    bannerNameS: String,
    themeColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable (List<T>) -> Unit
) {
    if (data.isNotEmpty()) {
        Column {
            ListGroupName(
                bannerNameF = bannerNameF,
                bannerNameS = bannerNameS,
                themeColor = themeColor
            )
            content(data)
        }
    }
}

@Composable
private fun GenreCard(
    label: LabelName,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 背景图
            SharedLabelIcon(
                iconName = label.iconName,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // 渐变遮罩
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )
            // 文字
            Text(
                text = label.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                )
        }
    }
}

@Composable
private fun MoodCard(
    label: LabelName,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            SharedLabelIcon(
                iconName = label.iconName,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}

@Composable
private fun ScenarioCard(
    label: LabelName,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(160.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            SharedLabelIcon(
                iconName = label.iconName,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Transparent,
                                Color.Black.copy(alpha = 0.4f),
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = label.name,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.suitable_for_now),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

private const val CARD_WIDTH_DP = 280
private const val CARD_HEIGHT_DP = 360
private const val CORNER_RADIUS_DP = 20

@Composable
fun UserListCard(
    playlist: Playlist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(CARD_WIDTH_DP.dp)
            .height(CARD_HEIGHT_DP.dp)
            .clip(RoundedCornerShape(CORNER_RADIUS_DP.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(CORNER_RADIUS_DP.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (playlist.coverUri != null && playlist.coverUri!!.isNotBlank()) {
                AsyncImage(
                    model = playlist.coverUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.music_note_list),
                        contentDescription = null,
                        modifier = Modifier
                            .width(72.dp)
                            .height(72.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Transparent,
                                0.5f to Transparent,
                                1.0f to Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (playlist.songCount > 0 || playlist.playCount > 0 || playlist.totalDurationMs > 0) {
                    val parts = mutableListOf<String>()
                    if (playlist.songCount > 0) {
                        parts.add(stringResource(Res.string.songs_count, playlist.songCount))
                    }
                    if (playlist.totalDurationMs > 0) {
                        val minutes = (playlist.totalDurationMs / 1000 / 60).toInt()
                        parts.add(stringResource(Res.string.minutes_format, minutes))
                    }
                    if (playlist.playCount > 0 && parts.size < 2) {
                        parts.add(stringResource(Res.string.play_count_display, playlist.playCount))
                    }
                    if (parts.isNotEmpty()) {
                        Text(
                            text = parts.take(2).joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
