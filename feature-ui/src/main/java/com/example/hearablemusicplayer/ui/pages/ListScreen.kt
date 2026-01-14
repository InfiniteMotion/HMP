package com.example.hearablemusicplayer.ui.pages

import androidx.activity.ComponentActivity
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hearablemusicplayer.domain.model.enum.LabelName
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.components.Capsule
import com.example.hearablemusicplayer.ui.components.ListBanner
import com.example.hearablemusicplayer.ui.components.ListGroupName
import com.example.hearablemusicplayer.ui.template.pages.TabScreen
import com.example.hearablemusicplayer.ui.util.Routes
import com.example.hearablemusicplayer.ui.util.iconResId
import com.example.hearablemusicplayer.ui.viewmodel.PlaylistViewModel

@Composable
fun ListScreen(
    playlistViewModel: PlaylistViewModel = hiltViewModel(LocalContext.current as ComponentActivity),
    navController: NavController
) {
    val genreList by playlistViewModel.genrePlaylistName.collectAsState()
    val moodList by playlistViewModel.moodPlaylistName.collectAsState()
    val scenarioList by playlistViewModel.scenarioPlaylistName.collectAsState()
    val languageList by playlistViewModel.languagePlaylistName.collectAsState()
    val eraList by playlistViewModel.eraPlaylistName.collectAsState()

    ListScreenContent(
        genreList = genreList,
        moodList = moodList,
        scenarioList = scenarioList,
        languageList = languageList,
        eraList = eraList,
        navController = navController,
        playlistViewModel = playlistViewModel
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
    navController: NavController,
    playlistViewModel: PlaylistViewModel
) {
    TabScreen(
        title = "播放列表",
        hasSearchBotton = true,
        navController = navController
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 适用场景 (Scenario) - 沉浸推荐
            Column {
                ListGroupName(
                    bannerNameF = stringResource(R.string.banner_daily_D),
                    bannerNameS = stringResource(R.string.banner_daily_DD),
                    themeColorResId = R.color.HDGreen
                )
                val scenarioListState = rememberLazyListState()
                val scenarioFlingBehavior = rememberSnapFlingBehavior(lazyListState = scenarioListState)

                LazyRow(
                    state = scenarioListState,
                    flingBehavior = scenarioFlingBehavior,
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(scenarioList) { label ->
                        ScenarioCard(
                            label = label,
                            onClick = {
                                playlistViewModel.getSelectedPlaylist(label)
                                navController.navigate(Routes.PLAYLIST)
                            }
                        )
                    }
                }
            }

            // 风格流派 (Genre) - 横向画廊
            Column {
                ListGroupName(
                    bannerNameF = stringResource(R.string.banner_daily_B),
                    bannerNameS = stringResource(R.string.banner_daily_BB),
                    themeColorResId = R.color.HDBlue
                )
                val genreListState = rememberLazyListState()
                val genreFlingBehavior = rememberSnapFlingBehavior(lazyListState = genreListState)
                
                LazyRow(
                    state = genreListState,
                    flingBehavior = genreFlingBehavior,
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(genreList) { label ->
                        GenreCard(
                            label = label,
                            onClick = {
                                playlistViewModel.getSelectedPlaylist(label)
                                navController.navigate(Routes.PLAYLIST)
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
                        listName = "默认列表",
                        listCoverUri = R.drawable.defaultlist,
                        playlistViewModel = playlistViewModel,
                        navController = navController
                    )
                    ListBanner(
                        listName = "红心列表",
                        listCoverUri = R.drawable.heartlist,
                        playlistViewModel = playlistViewModel,
                        navController = navController
                    )
                    ListBanner(
                        listName = "最近播放",
                        listCoverUri = R.drawable.historylist,
                        playlistViewModel = playlistViewModel,
                        navController = navController
                    )
                }
            }

            // 音乐情绪 (Mood) - 网格探索
            Column {
                ListGroupName(
                    bannerNameF = stringResource(R.string.banner_daily_C),
                    bannerNameS = stringResource(R.string.banner_daily_CC),
                    themeColorResId = R.color.HDOrange
                )
                // 使用 LazyHorizontalGrid 来展示2行
                Box(modifier = Modifier.height(220.dp)) {
                    LazyHorizontalGrid(
                        rows = GridCells.Fixed(2),
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(moodList) { label ->
                            MoodCard(
                                label = label,
                                onClick = {
                                    playlistViewModel.getSelectedPlaylist(label)
                                    navController.navigate(Routes.PLAYLIST)
                                }
                            )
                        }
                    }
                }
            }

            // 探索更多 (Language & Era) - 标签云
            Column {
                ListGroupName(
                    bannerNameF = "探索",
                    bannerNameS = "更多",
                    themeColorResId = R.color.HDPurple
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    val allTags = languageList + eraList
                    allTags.forEach { label ->
                        Box(
                            modifier = Modifier.clickable {
                                playlistViewModel.getSelectedPlaylist(label)
                                navController.navigate(Routes.PLAYLIST)
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
                                Color.Transparent,
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
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.6f)
                            )
                        )
                    )
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
                                Color.Transparent,
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
                    text = "适合此时此刻的音乐",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}
