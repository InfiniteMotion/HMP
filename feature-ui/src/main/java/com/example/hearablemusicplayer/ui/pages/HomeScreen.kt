package com.example.hearablemusicplayer.ui.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.data.database.DailyMusicInfo
import com.example.hearablemusicplayer.data.database.MusicInfo
import com.example.hearablemusicplayer.data.database.MusicLabel
import com.example.hearablemusicplayer.data.database.myenum.LabelCategory
import com.example.hearablemusicplayer.ui.components.AlbumCover
import com.example.hearablemusicplayer.ui.components.Capsule
import com.example.hearablemusicplayer.ui.viewmodel.MusicViewModel
import com.example.hearablemusicplayer.ui.viewmodel.PlayControlViewModel
import kotlinx.coroutines.launch

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun HomeScreen(
    musicViewModel: MusicViewModel,
    playControlViewModel: PlayControlViewModel,
) {
    val scope = rememberCoroutineScope()
    var visible by remember { mutableStateOf(false) }
    val dailyMusic by musicViewModel.dailyMusic.collectAsState(null)
    val dailyMusicInfo by musicViewModel.dailyMusicInfo.collectAsState()
    val dailyMusicLabel by musicViewModel.dailyMusicLabel.collectAsState()
    val currentPlayingMusic by playControlViewModel.currentPlayingMusic.collectAsState()
    val isPlaying by playControlViewModel.isPlaying.collectAsState()

    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500)),
        exit = fadeOut(animationSpec = tween(500))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                if (dailyMusic == null) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(128.dp))
                        Text(
                            text = "暂未加载到 Daily Music",
                            style = MaterialTheme.typography.displayMedium
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 16.dp, horizontal = 32.dp)
                        ) {
                            Text(
                                text = "每日一曲",
                                textAlign = TextAlign.Left,
                                style = MaterialTheme.typography.displayLarge
                            )
                            Text(
                                text = "Make Music Hearable!",
                                textAlign = TextAlign.Left,
                                style = MaterialTheme.typography.displaySmall
                            )
                        }
                        Spacer(modifier = Modifier.width(48.dp))
                        Box(
                            contentAlignment = Alignment.Center,
                        ){
                            IconButton(
                                modifier = Modifier.size(48.dp),
                                onClick = {
                                    scope.launch {
                                        dailyMusic?.let {
                                            if (currentPlayingMusic != dailyMusic!!) {
                                                playControlViewModel.playWith(dailyMusic!!)
                                            } else {
                                                if (isPlaying) playControlViewModel.pauseMusic()
                                                else playControlViewModel.playOrResume()
                                            }
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(
                                        if (currentPlayingMusic == dailyMusic!! && isPlaying) R.drawable.pause else R.drawable.play_fill
                                    ),
                                    contentDescription = "Play / Pause",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    DailyRecommendSectionOne(dailyMusic!!)
                    Spacer(modifier = Modifier.height(16.dp))
                    DailyRecommendSectionTwo(dailyMusicInfo, dailyMusicLabel)
                }
            }
        }
    }
}

@Composable
fun DailyRecommendSectionOne(
    dailyMusic: MusicInfo,
){
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
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
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = it.artist, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = it.album, style = MaterialTheme.typography.titleMedium)
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
                style = MaterialTheme.typography.displayMedium
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
                style = MaterialTheme.typography.displayMedium
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
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    border = BorderStroke(2.dp, color = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.displaySmall,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}
