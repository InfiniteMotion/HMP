package com.example.hearablemusicplayer.ui.pages

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.annotation.ColorRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.NavBackStack
import coil.compose.AsyncImage
import com.example.hearablemusicplayer.domain.enum.LabelName
import com.example.hearablemusicplayer.domain.playlist.Playlist
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.components.Capsule
import com.example.hearablemusicplayer.ui.components.ListBanner
import com.example.hearablemusicplayer.ui.components.ListGroupName
import com.example.hearablemusicplayer.ui.components.NewPlaylistButton
import com.example.hearablemusicplayer.ui.pages.base.TabScreen
import com.example.hearablemusicplayer.ui.util.Routes
import com.example.hearablemusicplayer.ui.util.iconResId
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback
import com.example.hearablemusicplayer.ui.viewmodel.PlaylistViewModel

@SuppressLint("ContextCastToActivity")
@Composable
fun ListScreen(
    playlistViewModel: PlaylistViewModel = hiltViewModel(LocalContext.current as ComponentActivity),
    navController: NavBackStack
) {
    val genreList by playlistViewModel.genrePlaylistName.collectAsState()
    val moodList by playlistViewModel.moodPlaylistName.collectAsState()
    val scenarioList by playlistViewModel.scenarioPlaylistName.collectAsState()
    val languageList by playlistViewModel.languagePlaylistName.collectAsState()
    val eraList by playlistViewModel.eraPlaylistName.collectAsState()
    val userCustomPlaylists by playlistViewModel.userCustomPlaylists.collectAsState()

    ListScreenContent(
        genreList = genreList,
        moodList = moodList,
        scenarioList = scenarioList,
        languageList = languageList,
        eraList = eraList,
        userCustomPlaylists = userCustomPlaylists,
        playlistViewModel = playlistViewModel,
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
    navController: NavBackStack
) {
    val haptic = rememberHapticFeedback()
    var showNewPlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    // 与列表管理页一致：置顶优先，再按最近播放、更新时间
    val sortedUserPlaylists = remember(userCustomPlaylists) {
        userCustomPlaylists.sortedWith(
            compareByDescending<Playlist> { it.isPinned }
                .thenByDescending { it.lastPlayedAt ?: 0L }
                .thenByDescending { it.updatedAt }
        )
    }

    if (showNewPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showNewPlaylistDialog = false; newPlaylistName = "" },
            title = { Text(stringResource(R.string.new_playlist_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text(stringResource(R.string.playlist_name_hint)) },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = newPlaylistName.trim()
                        if (name.isNotEmpty()) {
                            playlistViewModel.createPlaylistAsync(name) { id ->
                                showNewPlaylistDialog = false
                                newPlaylistName = ""
                                navController.add(Routes.CustomPlaylist(id))
                            }
                        }
                    }
                ) {
                    Text(stringResource(R.string.ok), color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewPlaylistDialog = false; newPlaylistName = "" }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    TabScreen(
        title = stringResource(R.string.title_playlist),
        hasSearchBotton = false,
        navController = navController,
        trailing = {
            NewPlaylistButton(onClick = { showNewPlaylistDialog = true })
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
                    bannerNameF = stringResource(R.string.user_custom_playlists),
                    bannerNameS = stringResource(R.string.banner_daily_GG),
                    themeColorResId = R.color.HDPurple,
                    trailing = {
                        TextButton(
                            onClick = {
                                haptic.performClick()
                                navController.add(Routes.UserPlaylistManage)
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.manage_playlists),
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
                                navController.add(Routes.CustomPlaylist(playlist.id))
                            }
                        )
                    }
                }
            }

            // 常用列表 (Common Playlists) - 保留原有样式
            Column {
                ListGroupName(
                    bannerNameF = stringResource(R.string.banner_daily_A),
                    bannerNameS = stringResource(R.string.banner_daily_AA),
                    themeColorResId = R.color.HDRed
                )
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ListBanner(
                        listName = stringResource(R.string.banner_default),
                        listCoverUri = R.drawable.defaultlist,
                        navController = navController
                    )
                    ListBanner(
                        listName = stringResource(R.string.banner_heart),
                        listCoverUri = R.drawable.heartlist,
                        navController = navController
                    )
                    ListBanner(
                        listName = stringResource(R.string.banner_history),
                        listCoverUri = R.drawable.historylist,
                        navController = navController
                    )
                }
            }

            // 适用场景 (Scenario) - 沉浸推荐
            LabelListGroup(
                data = scenarioList,
                bannerNameF = stringResource(R.string.banner_daily_D),
                bannerNameS = stringResource(R.string.banner_daily_DD),
                themeColorResId = R.color.HDGreen
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
                                navController.add(Routes.Playlist(label.name))
                            }
                        )
                    }
                }
            }

            // 风格流派 (Genre) - 横向画廊
            LabelListGroup(
                data = genreList,
                bannerNameF = stringResource(R.string.banner_daily_B),
                bannerNameS = stringResource(R.string.banner_daily_BB),
                themeColorResId = R.color.HDBlue
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
                                navController.add(Routes.Playlist(label.name))
                            }
                        )
                    }
                }
            }

            // 音乐情绪 (Mood) - 网格探索
            LabelListGroup(
                data = moodList,
                bannerNameF = stringResource(R.string.banner_daily_C),
                bannerNameS = stringResource(R.string.banner_daily_CC),
                themeColorResId = R.color.HDOrange
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
                                    navController.add(Routes.Playlist(label.name))
                                }
                            )
                        }
                    }
                }
            }

            // 探索更多 (Language & Era) - 标签云
            LabelListGroup(
                data = languageList + eraList,
                bannerNameF = stringResource(R.string.explore),
                bannerNameS = stringResource(R.string.more),
                themeColorResId = R.color.HDPurple
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
                                navController.add(Routes.Playlist(label.name))
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
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

// ================== 局部组件定义 ==================

@Composable
fun <T> LabelListGroup(
    data: List<T>,
    bannerNameF: String,
    bannerNameS: String,
    @ColorRes themeColorResId: Int,
    content: @Composable (List<T>) -> Unit
) {
    if (data.isNotEmpty()) {
        Column {
            ListGroupName(
                bannerNameF = bannerNameF,
                bannerNameS = bannerNameS,
                themeColorResId = themeColorResId
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
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 背景图
            AsyncImage(
                model = label.iconResId,
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
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = label.iconResId,
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
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = label.iconResId,
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
                    text = stringResource(R.string.suitable_for_now),
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
                        painter = painterResource(R.drawable.music_note_list),
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
                                0.0f to Color.Transparent,
                                0.5f to Color.Transparent,
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
                        parts.add(stringResource(R.string.songs_count, playlist.songCount))
                    }
                    if (playlist.totalDurationMs > 0) {
                        val minutes = (playlist.totalDurationMs / 1000 / 60).toInt()
                        parts.add(stringResource(R.string.minutes_format, minutes))
                    }
                    if (playlist.playCount > 0 && parts.size < 2) {
                        parts.add(stringResource(R.string.play_count_display, playlist.playCount))
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
