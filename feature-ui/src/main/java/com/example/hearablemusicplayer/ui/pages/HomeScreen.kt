package com.example.hearablemusicplayer.ui.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.example.hearablemusicplayer.domain.model.DailyMusicInfo
import com.example.hearablemusicplayer.domain.model.MusicInfo
import com.example.hearablemusicplayer.domain.model.MusicLabel
import com.example.hearablemusicplayer.domain.model.enum.LabelCategory
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.components.AlbumCover
import com.example.hearablemusicplayer.ui.components.Capsule
import com.example.hearablemusicplayer.ui.template.components.TitleWidget
import com.example.hearablemusicplayer.ui.template.pages.TabScreen
import com.example.hearablemusicplayer.ui.util.Routes
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback
import com.example.hearablemusicplayer.ui.viewmodel.PlaylistViewModel
import com.example.hearablemusicplayer.ui.viewmodel.PlayControlViewModel
import com.example.hearablemusicplayer.ui.viewmodel.RecommendationViewModel
import kotlinx.coroutines.launch

import androidx.hilt.navigation.compose.hiltViewModel

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun HomeScreen(
    recommendationViewModel: RecommendationViewModel = hiltViewModel(),
    playlistViewModel: PlaylistViewModel = hiltViewModel(),
    playControlViewModel: PlayControlViewModel = hiltViewModel(),
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    val dailyMusic by recommendationViewModel.dailyMusic.collectAsState(null)
    val dailyMusicInfo by recommendationViewModel.dailyMusicInfo.collectAsState()
    val dailyMusicLabel by recommendationViewModel.dailyMusicLabel.collectAsState()
    val haptic = rememberHapticFeedback()

    HomeScreenContent(
        dailyMusic = dailyMusic,
        dailyMusicInfo = dailyMusicInfo,
        dailyMusicLabel = dailyMusicLabel,
        onRefreshDailyMusic = {
            haptic.performClick()
            recommendationViewModel.refreshDailyMusicInfo()
        },
        onNavigateToAI = {
            haptic.performClick()
            navController.navigate(Routes.AI)
        },
        onPlayDailyMusic = { musicInfo ->
            haptic.performClick()
            scope.launch {
                playControlViewModel.playWith(musicInfo)
                playControlViewModel.recordPlayback(musicInfo.music.id, "Home")
                navController.navigate(Routes.PLAYER)
            }
        },
        onNavigateToDailyArtists = { artistName ->
            haptic.performClick()
            playlistViewModel.getSelectedArtistMusicList(artistName)
            navController.navigate(Routes.ARTIST)
        },
        navController = navController
    )
}

@Composable
fun HomeScreenContent(
    dailyMusic: MusicInfo?,
    dailyMusicInfo: DailyMusicInfo?,
    dailyMusicLabel: List<MusicLabel?>,
    onRefreshDailyMusic: () -> Unit,
    onNavigateToAI: () -> Unit,
    onPlayDailyMusic: (MusicInfo) -> Unit,
    onNavigateToDailyArtists: (String) -> Unit,
    navController: NavController
) {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val screenHeight = with(density) { windowInfo.containerSize.height.toDp() }

    TabScreen(
        title = "每日推荐",
        trailing = {
            // 手动刷新按钮
            IconButton(
                onClick = onRefreshDailyMusic
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
                .verticalScroll(rememberScrollState())
        ) {
            if (dailyMusic == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(screenHeight * 0.7f)
                        .padding(bottom = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Text(
                            text = "暂未加载到 Daily Music",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "请配置 AI 服务以启用每日推荐功能",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = onNavigateToAI,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(horizontal = 32.dp)
                        ) {
                            Text(
                                text = "前往 AI 配置",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            } else {
                DailyMusicBaseInfo(
                    dailyMusic,
                    playDailyMusic = { onPlayDailyMusic(dailyMusic) },
                    navigateToDailyArtists = { onNavigateToDailyArtists(dailyMusic.music.artist) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                DailyMusicExtraInfo(dailyMusicInfo, dailyMusicLabel)

                Spacer(modifier = Modifier.height(16.dp))

            }
        }
    }
}

@Composable
fun DailyMusicBaseInfo(
    dailyMusic: MusicInfo,
    playDailyMusic: () -> Unit,
    navigateToDailyArtists: () -> Unit
){
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(0.5f)
                    .clickable(onClick = playDailyMusic),
                contentAlignment = Alignment.Center
            ) {
                AlbumCover(
                    dailyMusic.music.albumArtUri,
                    Arrangement.Start,
                    150
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(0.5f),
            ){
                dailyMusic.music.let {
                    Text(
                        text = it.title,
                        style = MaterialTheme.typography.displayMedium,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.clickable(onClick = playDailyMusic)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it.artist,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable(onClick = navigateToDailyArtists)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = it.album,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DailyMusicExtraInfo(
    dailyMusicInfo: DailyMusicInfo?,
    dailyMusicLabel: List<MusicLabel?>,
){
    if(dailyMusicInfo==null) {
        Column (
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(
                text = "加载中...",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }else if(dailyMusicInfo.errorInfo!="None"){
        Column (
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(
                text = dailyMusicInfo.errorInfo,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    } else {
        val labelListOne = listOf(
            "曲风" to dailyMusicLabel.filter { (it?.type ?: "0") == LabelCategory.GENRE }.map{ it?.label?: "" },
            "情绪" to dailyMusicLabel.filter { (it?.type ?: "0") == LabelCategory.MOOD }.map{ it?.label?: "" },
            "场景" to dailyMusicLabel.filter { (it?.type ?: "0") == LabelCategory.SCENARIO }.map{ it?.label?: "" },
            "语言" to dailyMusicLabel.filter { (it?.type ?: "0") == LabelCategory.LANGUAGE }.map{ it?.label?: "" },
            "年代" to dailyMusicLabel.filter { (it?.type ?: "0") == LabelCategory.ERA }.map{ it?.label?: "" },
        )
        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            FlowRow(
                maxItemsInEachRow = 2,
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                labelListOne.forEach { (category, labels) ->
                    var labelStr = ""
                    labels.forEach { labelStr += "$it " }
                    Capsule(text = "$category: $labelStr")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            val labelListTwo = listOf(
                "歌曲介绍" to dailyMusicInfo.description,
                "歌手介绍" to dailyMusicInfo.singerIntroduce,
                "创作背景" to dailyMusicInfo.backgroundIntroduce,
                "热门歌词" to dailyMusicInfo.lyric,
                "歌曲成就" to dailyMusicInfo.rewards,
                "类似音乐" to dailyMusicInfo.relevantMusic
            )
            labelListTwo.forEach { (category, label) ->
                TitleWidget(
                    title = category
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

