package com.example.hearablemusicplayer.ui.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.hearablemusicplayer.domain.music.MusicInfo
import com.example.hearablemusicplayer.domain.setting.model.DailyMusicInfo
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.components.AlbumCover
import com.example.hearablemusicplayer.ui.components.SegmentedControl
import com.example.hearablemusicplayer.ui.components.SegmentedOption
import com.example.hearablemusicplayer.ui.components.TitleWidget
import com.example.hearablemusicplayer.ui.pages.base.SubScreen
import com.example.hearablemusicplayer.ui.util.Routes
import com.example.hearablemusicplayer.ui.util.UiState
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback
import com.example.hearablemusicplayer.ui.viewmodel.SongDetailViewModel

@Composable
fun SongDetailScreen(
    navController: NavController,
    viewModel: SongDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = rememberHapticFeedback()

    val title = when (val state = uiState) {
        is UiState.Success -> state.data.musicInfo.music.title
        else -> stringResource(R.string.title_song_detail)
    }

    SubScreen(
        onBackClick = { navController.popBackStack() },
        title = title,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = uiState) {
                is UiState.Loading, UiState.Idle -> {
                    SongDetailLoading()
                }
                is UiState.Success -> {
                    val data = state.data
                    SongDetailPoster(
                        artist = data.musicInfo.music.artist,
                        album = data.musicInfo.music.album,
                        albumArtUri = data.musicInfo.music.albumArtUri,
                        onOpenPlayer = {
                            haptic.performClick()
                            navController.navigate(Routes.Player)
                        }
                    )

                    SongDetailInfo(data.musicInfo, data.dailyMusicInfo)

                    Spacer(modifier = Modifier.height(24.dp))
                }
                is UiState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth().height(200.dp)
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.retry() }) {
                            Text(stringResource(R.string.loading))
                        }
                    }
                }
            }
        }
    }

}



@Composable
private fun SongDetailPoster(
    artist: String,
    album: String,
    albumArtUri: String?,
    onOpenPlayer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .clickable(onClick = onOpenPlayer)
        ) {
            AlbumCover(
                albumArtUri,
                280.dp,
                corner = 25.dp,
                shadow = 15.dp
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = artist,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.86f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = album,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SongDetailLoading() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.38f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.loading),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SongDetailInfo(
    musicInfo: MusicInfo,
    dailyMusicInfo: DailyMusicInfo?,
) {
    val haptic = rememberHapticFeedback()

    if (dailyMusicInfo == null) return

    if (dailyMusicInfo.errorInfo != "None") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Text(
                text = dailyMusicInfo.errorInfo,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(16.dp)
            )
        }
        return
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        val introId = "intro"
        val lyricsId = "lyrics"
        val moreId = "more"

        var selectedSection by rememberSaveable { mutableStateOf(introId) }

        SegmentedControl(
            modifier = Modifier.fillMaxWidth(),
            options = listOf(
                SegmentedOption(introId, stringResource(R.string.song_detail_tab_intro)),
                SegmentedOption(lyricsId, stringResource(R.string.song_detail_tab_lyrics)),
                SegmentedOption(moreId, stringResource(R.string.song_detail_tab_more))
            ),
            selectedOption = selectedSection,
            onOptionSelected = {
                selectedSection = it
                haptic.performClick()
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        val lyricsFull = (musicInfo.extra?.lyrics ?: "None Full Lyrics")
            .replace(Regex("\\[.*?]"), "")
            .lines()
            .filter { it.isNotBlank() }
            .joinToString("\n")
            .trim()
        val sections = when (selectedSection) {
            lyricsId -> listOf(
                stringResource(R.string.popular_lyrics) to dailyMusicInfo.lyric,
                stringResource(R.string.lyrics) to lyricsFull
            )

            moreId -> listOf(
                stringResource(R.string.song_achievements) to dailyMusicInfo.rewards,
                stringResource(R.string.similar_music) to dailyMusicInfo.relevantMusic
            )

            else -> listOf(
                stringResource(R.string.song_description) to dailyMusicInfo.description,
                stringResource(R.string.artist_introduction) to dailyMusicInfo.singerIntroduce,
                stringResource(R.string.creative_background) to dailyMusicInfo.backgroundIntroduce
            )
        }
        
        val visibleSections = sections.filter { (_, label) -> label.isNotBlank() && label != "None" }

        if (visibleSections.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.46f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = stringResource(R.string.song_detail_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
            return@Column
        }

        visibleSections.forEach { (category, label) ->
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
