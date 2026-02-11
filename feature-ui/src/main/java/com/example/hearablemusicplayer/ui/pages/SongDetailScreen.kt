package com.example.hearablemusicplayer.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.example.hearablemusicplayer.domain.setting.model.DailyMusicInfo
import com.example.hearablemusicplayer.domain.music.MusicLabel
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.components.AlbumCover
import com.example.hearablemusicplayer.ui.components.Capsule
import com.example.hearablemusicplayer.ui.template.components.TitleWidget
import com.example.hearablemusicplayer.ui.template.pages.SubScreen
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback
import com.example.hearablemusicplayer.ui.viewmodel.PlayControlViewModel
import com.example.hearablemusicplayer.ui.viewmodel.RecommendationViewModel

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun SongDetailScreen(
    navController: NavController,
    recommendationViewModel: RecommendationViewModel = hiltViewModel(),
    playControlViewModel: PlayControlViewModel = hiltViewModel()
) {
    val dailyMusic by recommendationViewModel.dailyMusic.collectAsState(null)
    val dailyMusicInfo by recommendationViewModel.dailyMusicInfo.collectAsState()
    val dailyMusicLabel by recommendationViewModel.dailyMusicLabel.collectAsState()
    val isPlaying by playControlViewModel.isPlaying.collectAsState()
    val currentPlayingMusic by playControlViewModel.currentPlayingMusic.collectAsState()
    val haptic = rememberHapticFeedback()

    SubScreen(
        onBackClick = { navController.popBackStack() },
        title = stringResource(R.string.title_song_detail)
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            dailyMusic?.let { music ->
                // Large Album Cover
                AlbumCover(
                    music.music.albumArtUri,
                    280.dp,
                    corner = 20.dp,
                    shadow = 10.dp
                )
                // Title and Artist
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = music.music.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = music.music.artist,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = music.music.album,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                }

                // Playback Control
                val isCurrentSong = currentPlayingMusic?.music?.id == music.music.id
                val showPause = isCurrentSong && isPlaying

                IconButton(
                    onClick = {
                        haptic.performClick()
                        if (isCurrentSong) {
                            playControlViewModel.playOrResume()
                        } else {
                            playControlViewModel.playWith(music)
                        }
                    },
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        painter = if (showPause) painterResource(R.drawable.pause) else painterResource(
                            R.drawable.play_fill
                        ),
                        contentDescription = if (showPause) "Pause" else "Play",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Detailed Info
                SongDetailInfo(dailyMusicInfo, dailyMusicLabel)

                Spacer(modifier = Modifier.height(32.dp))
            } ?: run {
                // Loading or Empty State
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.loading), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
    

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SongDetailInfo(
    dailyMusicInfo: DailyMusicInfo?,
    dailyMusicLabel: List<MusicLabel?>,
) {
    if (dailyMusicInfo == null) return

    if (dailyMusicInfo.errorInfo != "None") {
        Text(
            text = dailyMusicInfo.errorInfo,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(
                space = 8.dp,
                alignment = Alignment.CenterHorizontally
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            dailyMusicLabel.forEach { label ->
                label?.let {
                    Capsule(text = label.label.toString())
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        val labelListTwo = listOf(
            stringResource(R.string.song_description) to dailyMusicInfo.description,
            stringResource(R.string.artist_introduction) to dailyMusicInfo.singerIntroduce,
            stringResource(R.string.creative_background) to dailyMusicInfo.backgroundIntroduce,
            stringResource(R.string.popular_lyrics) to dailyMusicInfo.lyric,
            stringResource(R.string.song_achievements) to dailyMusicInfo.rewards,
            stringResource(R.string.similar_music) to dailyMusicInfo.relevantMusic
        )
        
        labelListTwo.forEach { (category, label) ->
            if (label.isNotBlank() && label != "None") {
                TitleWidget(title = category) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
