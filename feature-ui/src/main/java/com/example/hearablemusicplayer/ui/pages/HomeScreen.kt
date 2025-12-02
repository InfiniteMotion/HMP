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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.example.hearablemusicplayer.data.database.DailyMusicInfo
import com.example.hearablemusicplayer.data.database.MusicInfo
import com.example.hearablemusicplayer.data.database.MusicLabel
import com.example.hearablemusicplayer.data.database.myenum.LabelCategory
import com.example.hearablemusicplayer.ui.components.AlbumCover
import com.example.hearablemusicplayer.ui.components.Capsule
import com.example.hearablemusicplayer.ui.template.components.TitleWidget
import com.example.hearablemusicplayer.ui.template.pages.TabScreen
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback
import com.example.hearablemusicplayer.ui.viewmodel.MusicViewModel
import com.example.hearablemusicplayer.ui.viewmodel.PlayControlViewModel
import kotlinx.coroutines.launch

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun HomeScreen(
    musicViewModel: MusicViewModel,
    playControlViewModel: PlayControlViewModel,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    val dailyMusic by musicViewModel.dailyMusic.collectAsState(null)
    val dailyMusicInfo by musicViewModel.dailyMusicInfo.collectAsState()
    val dailyMusicLabel by musicViewModel.dailyMusicLabel.collectAsState()
    val haptic = rememberHapticFeedback()

    TabScreen(
        title = "每日推荐",
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (dailyMusic == null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "暂未加载到 Daily Music",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            } else {
                DailyRecommendSectionOne(
                    dailyMusic!!,
                    playDailyMusic = {
                        haptic.performClick()
                        scope.launch {
                            playControlViewModel.playWith(dailyMusic!!)
                            playControlViewModel.recordPlayback(dailyMusic!!.music.id, "Home")
                            navController.navigate("player")
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                DailyRecommendSectionTwo(dailyMusicInfo, dailyMusicLabel)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun DailyRecommendSectionOne(
    dailyMusic: MusicInfo,
    playDailyMusic: () -> Unit,
){
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clickable(onClick = playDailyMusic)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(0.5f),
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
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = it.artist, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = it.album, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DailyRecommendSectionTwo(
    dailyMusicInfo: DailyMusicInfo?,
    dailyMusicLabel: List<MusicLabel?>
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
