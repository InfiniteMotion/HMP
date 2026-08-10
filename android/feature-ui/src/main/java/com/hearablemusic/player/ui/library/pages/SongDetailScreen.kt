package com.hearablemusic.player.ui.library.pages

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.hmp.domain.music.MusicExtra
import com.hmp.domain.music.MusicInfo
import com.hmp.domain.music.MusicLabel
import com.hmp.domain.setting.model.DailyMusicInfo
import com.hmp.domain.setting.model.PlaybackHistory
import com.hearablemusic.player.ui.R
import com.hearablemusic.player.ui.library.pages.components.AlbumCover
import com.hearablemusic.player.ui.common.components.SegmentedControl
import com.hearablemusic.player.ui.common.components.SegmentedOption
import com.hearablemusic.player.ui.common.components.base.TitleWidget
import com.hearablemusic.player.ui.common.pages.base.SubScreen
import com.hearablemusic.player.ui.common.layout.LocalWindowSizeInfo
import com.hearablemusic.player.ui.common.design.dimens.LocalHMPDimens
import com.hearablemusic.player.ui.player.pages.TechnicalInfoCard
import com.hearablemusic.player.ui.common.navigation.Routes
import com.hearablemusic.player.ui.common.util.UiState
import com.hearablemusic.player.ui.common.util.rememberHapticFeedback
import com.hearablemusic.player.ui.library.viewmodel.SongDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SongDetailScreen(
    navController: NavBackStack<NavKey>,
    musicId: Long,
    viewModel: SongDetailViewModel = koinViewModel()
) {
    // 手动调用 loadSongDetail 方法，传入 musicId
    // 同时监听返回栈变化：从标签编辑页返回后重新加载，展示最新标签
    LaunchedEffect(musicId, navController.size) {
        viewModel.loadSongDetail(musicId)
    }
    val uiState by viewModel.uiState.collectAsState()
    val haptic = rememberHapticFeedback()

    val title = when (val state = uiState) {
        is UiState.Success -> state.data.musicInfo.music.title
        else -> stringResource(R.string.title_song_detail)
    }

    SubScreen(
        onBackClick = { navController.removeLastOrNull() },
        title = title,
        trailingContent = {
            if (uiState is UiState.Success) {
                TextButton(onClick = {
                    haptic.performClick()
                    navController.add(Routes.Library.EditMusicTags(musicId))
                }) {
                    Text(stringResource(R.string.edit_music_tags))
                }
            }
        }
    ) {
        val isLandscape = LocalWindowSizeInfo.current.isLandscape
        val dimens = LocalHMPDimens.current
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(horizontal = dimens.spacing.xl, vertical = dimens.spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = uiState) {
                is UiState.Loading, UiState.Idle -> {
                    SongDetailLoading()
                }
                is UiState.Success -> {
                    val data = state.data
                    if (isLandscape) {
                        val music = data.musicInfo.music
                        val userInfo = data.musicInfo.userInfo
                        val extra = data.musicInfo.extra
                        val dailyInfo = data.dailyMusicInfo
                        val validLabels = data.labels.filterNotNull().filter { it.label.name.isNotBlank() }

                        SongDetailExpanded(
                            onOpenPlayer = {
                                haptic.performClick()
                                navController.add(Routes.Player.Player)
                            },
                            artist = music.artist,
                            album = music.album,
                            albumArtUri = music.albumArtUri,
                            musicExtra = extra,
                            userInfo = userInfo,
                            playbackHistory = data.playbackHistory,
                            dailyMusicInfo = dailyInfo,
                            validLabels = validLabels,
                        )
                    } else {
                        SongDetailPoster(
                            artist = data.musicInfo.music.artist,
                            album = data.musicInfo.music.album,
                            albumArtUri = data.musicInfo.music.albumArtUri,
                            musicExtra = data.musicInfo.extra,
                            onOpenPlayer = {
                                haptic.performClick()
                                navController.add(Routes.Player.Player)
                            }
                        )
                        SongDetailInfo(
                            musicInfo = data.musicInfo,
                            dailyMusicInfo = data.dailyMusicInfo,
                            labels = data.labels,
                            playbackHistory = data.playbackHistory
                        )
                    }
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

                else -> {}
            }
        }
    }

}



@Composable
private fun SongDetailPoster(
    artist: String,
    album: String,
    albumArtUri: String?,
    musicExtra: MusicExtra?,
    onOpenPlayer: () -> Unit
) {
    val dimens = LocalHMPDimens.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimens.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimens.spacing.md)
    ) {
        Box(
            modifier = Modifier
                .clickable(onClick = onOpenPlayer)
        ) {
            AlbumCover(
                albumArtUri,
                dimens.component.xl,
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
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = album,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        TechnicalInfoCard(extra = musicExtra)
    }
}

@Composable
private fun SongDetailExpanded(
    onOpenPlayer: () -> Unit,
    artist: String,
    album: String,
    albumArtUri: String?,
    musicExtra: MusicExtra?,
    userInfo: com.hmp.domain.music.UserInfo?,
    playbackHistory: List<PlaybackHistory>,
    dailyMusicInfo: DailyMusicInfo?,
    validLabels: List<MusicLabel>,
) {
    val dimens = LocalHMPDimens.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dimens.spacing.lg)
    ) {
        // 左栏：封面 + 技术信息 + 个人统计 + 标签 + 最近播放
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(dimens.spacing.lg)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(25.dp))
                        .clickable(onClick = onOpenPlayer)
                ) {
                    AlbumCover(albumArtUri, dimens.component.xl, corner = 25.dp, shadow = 15.dp)
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
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = album,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                TechnicalInfoCard(extra = musicExtra)
            }

            TitleWidget(title = stringResource(R.string.personal_stats)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatItem(stringResource(R.string.sort_play_count), (userInfo?.playCount ?: 0).toString(), Modifier.weight(1f))
                        StatItem(stringResource(R.string.skipped_count), (userInfo?.skippedCount ?: 0).toString(), Modifier.weight(1f))
                        StatItem(stringResource(R.string.playlist_count), (userInfo?.inCustomPlaylistCount ?: 0).toString(), Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatItem(stringResource(R.string.user_rating), (userInfo?.userRating ?: 0).toString(), Modifier.weight(1f))
                        StatItem(stringResource(R.string.last_played), formatLastPlayed(userInfo?.lastPlayed, LocalContext.current), Modifier.weight(1f))
                        StatItem(
                            stringResource(R.string.liked_status),
                            userInfo?.liked?.let { if (it) stringResource(R.string.liked_yes) else stringResource(R.string.liked_no) } ?: stringResource(R.string.liked_no),
                            Modifier.weight(1f)
                        )
                    }
                }
            }

            if (validLabels.isNotEmpty()) {
                TitleWidget(title = stringResource(R.string.labels)) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        itemVerticalAlignment = Alignment.CenterVertically
                    ) {
                        validLabels.forEach { label ->
                            AssistChip(onClick = { }, label = { Text(label.label.name) }, border = null)
                        }
                    }
                }
            }

            if (playbackHistory.isNotEmpty()) {
                TitleWidget(title = stringResource(R.string.recent_history)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        playbackHistory.forEach { history ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${stringResource(R.string.duration)}: ${formatDuration(history.playDuration)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        history.source?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(formatTimestamp(history.playedAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            if (history.isCompleted) stringResource(R.string.completed) else stringResource(R.string.incomplete),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (history.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                TitleWidget(title = stringResource(R.string.recent_history)) {
                    Text(stringResource(R.string.song_detail_empty), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // 中栏：完整歌词
        Column(Modifier.weight(1f)) {
            val lyricsFull = (musicExtra?.lyrics ?: "None Full Lyrics")
                .replace(Regex("\\[.*?]"), "")
                .lines()
                .filter { it.isNotBlank() }
                .joinToString("\n")
                .trim()

            if (lyricsFull.isNotBlank() && lyricsFull != "None Full Lyrics") {
                TitleWidget(title = stringResource(R.string.lyrics)) {
                    Text(
                        text = lyricsFull,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
                    )
                }
            }
        }

        // 右栏：歌曲介绍 + 精选歌词
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(dimens.spacing.lg)
        ) {
            if (dailyMusicInfo == null) return@Column

            if (dailyMusicInfo.errorInfo != "None") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Text(text = dailyMusicInfo.errorInfo, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(16.dp))
                }
                return@Column
            }

            val hasIntroContent = (dailyMusicInfo.backgroundIntroduce.isNotBlank() && dailyMusicInfo.backgroundIntroduce != "None")
                    || (dailyMusicInfo.description.isNotBlank() && dailyMusicInfo.description != "None")
                    || (dailyMusicInfo.singerIntroduce.isNotBlank() && dailyMusicInfo.singerIntroduce != "None")
                    || (dailyMusicInfo.rewards.isNotBlank() && dailyMusicInfo.rewards != "None")

            if (hasIntroContent) {
                TitleWidget(title = stringResource(R.string.song_description)) {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (dailyMusicInfo.backgroundIntroduce.isNotBlank() && dailyMusicInfo.backgroundIntroduce != "None") {
                            Text(dailyMusicInfo.backgroundIntroduce, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2)
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }
                        if (dailyMusicInfo.description.isNotBlank() && dailyMusicInfo.description != "None") {
                            Text(dailyMusicInfo.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2)
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }
                        if (dailyMusicInfo.singerIntroduce.isNotBlank() && dailyMusicInfo.singerIntroduce != "None") {
                            Text(dailyMusicInfo.singerIntroduce, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2)
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }
                        if (dailyMusicInfo.rewards.isNotBlank() && dailyMusicInfo.rewards != "None") {
                            Text(dailyMusicInfo.rewards, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2)
                        }
                    }
                }
            }
            if (dailyMusicInfo.relevantMusic.isNotBlank() && dailyMusicInfo.relevantMusic != "None") {
                TitleWidget(title = stringResource(R.string.similar_music)) {
                    Text(dailyMusicInfo.relevantMusic, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2)
                }
            }
            if (dailyMusicInfo.lyric.isNotBlank() && dailyMusicInfo.lyric != "None") {
                TitleWidget(title = stringResource(R.string.popular_lyrics)) {
                    Text(dailyMusicInfo.lyric, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2)
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(date)
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

private fun formatLastPlayed(timestamp: Long?, context: Context): String {
    if (timestamp == null || timestamp == 0L) return context.getString(R.string.never)
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000L -> context.getString(R.string.just_now)

        diff < 3600000L -> "${diff / 60000L}m ago"
        diff < 86400000L -> "${diff / 3600000L}h ago"
        diff < 604800000L -> "${diff / 86400000L}d ago"
        else -> {
            val sdf = SimpleDateFormat("MM-dd", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

@Composable
private fun SongDetailLoading() {
    val dimens = LocalHMPDimens.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimens.component.xl),
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
fun StatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SongDetailInfo(
    musicInfo: MusicInfo,
    dailyMusicInfo: DailyMusicInfo?,
    labels: List<MusicLabel?> = emptyList(),
    playbackHistory: List<PlaybackHistory> = emptyList()
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
        val userId = "user"

        var selectedSection by rememberSaveable { mutableStateOf(introId) }

        SegmentedControl(
            modifier = Modifier.fillMaxWidth(),
            options = listOf(
                SegmentedOption(userId, stringResource(R.string.song_detail_tab_user)),
                SegmentedOption(introId, stringResource(R.string.song_detail_tab_intro)),
                SegmentedOption(lyricsId, stringResource(R.string.song_detail_tab_lyrics))
            ),
            selectedOption = selectedSection,
            onOptionSelected = {
                selectedSection = it
                haptic.performClick()
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedSection) {
            introId -> {

                // 创作背景
                if (dailyMusicInfo.backgroundIntroduce.isNotBlank() && dailyMusicInfo.backgroundIntroduce != "None") {
                    TitleWidget(title = stringResource(R.string.creative_background)) {
                        Text(
                            text = dailyMusicInfo.backgroundIntroduce,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 描述信息
                if (dailyMusicInfo.description.isNotBlank() && dailyMusicInfo.description != "None") {
                    TitleWidget(title = stringResource(R.string.song_description)) {
                        Text(
                            text = dailyMusicInfo.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 歌手介绍
                if (dailyMusicInfo.singerIntroduce.isNotBlank() && dailyMusicInfo.singerIntroduce != "None") {
                    TitleWidget(title = stringResource(R.string.artist_introduction)) {
                        Text(
                            text = dailyMusicInfo.singerIntroduce,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 奖项成就
                if (dailyMusicInfo.rewards.isNotBlank() && dailyMusicInfo.rewards != "None") {
                    TitleWidget(title = stringResource(R.string.song_achievements)) {
                        Text(
                            text = dailyMusicInfo.rewards,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 相关音乐
                if (dailyMusicInfo.relevantMusic.isNotBlank() && dailyMusicInfo.relevantMusic != "None") {
                    TitleWidget(title = stringResource(R.string.similar_music)) {
                        Text(
                            text = dailyMusicInfo.relevantMusic,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 标签展示
                val validLabels = labels.filterNotNull().filter { it.label.name.isNotBlank() }
                if (validLabels.isNotEmpty()) {
                    TitleWidget(title = stringResource(R.string.labels)) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            itemVerticalAlignment = Alignment.CenterVertically
                        ) {
                            validLabels.forEach { label ->
                                AssistChip(
                                    onClick = { },
                                    label = { Text(label.label.name) },
                                    border = null,
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            lyricsId -> {
                val lyricsFull = (musicInfo.extra?.lyrics ?: "None Full Lyrics")
                    .replace(Regex("\\[.*?]"), "")
                    .lines()
                    .filter { it.isNotBlank() }
                    .joinToString("\n")
                    .trim()
                
                if (dailyMusicInfo.lyric.isNotBlank() && dailyMusicInfo.lyric != "None") {
                    TitleWidget(title = stringResource(R.string.popular_lyrics)) {
                        Text(
                            text = dailyMusicInfo.lyric,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (lyricsFull.isNotBlank() && lyricsFull != "None Full Lyrics") {
                    TitleWidget(title = stringResource(R.string.lyrics)) {
                        Text(
                            text = lyricsFull,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            userId -> {
                // 用户统计
                val userInfo = musicInfo.userInfo
                
                TitleWidget(title = stringResource(R.string.personal_stats)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatItem(
                                label = stringResource(R.string.sort_play_count),
                                value = (userInfo?.playCount ?: 0).toString(),
                                modifier = Modifier.weight(1f)
                            )
                            StatItem(
                                label = stringResource(R.string.skipped_count),
                                value = (userInfo?.skippedCount ?: 0).toString(),
                                modifier = Modifier.weight(1f)
                            )
                            StatItem(
                                label = stringResource(R.string.playlist_count),
                                value = (userInfo?.inCustomPlaylistCount ?: 0).toString(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatItem(
                                label = stringResource(R.string.user_rating),
                                value = (userInfo?.userRating ?: 0).toString(),
                                modifier = Modifier.weight(1f)
                            )
                            StatItem(
                                label = stringResource(R.string.last_played),
                                value = formatLastPlayed(userInfo?.lastPlayed, LocalContext.current),
                                modifier = Modifier.weight(1f)
                            )
                            StatItem(
                                label = stringResource(R.string.liked_status),
                                value = userInfo?.liked?.let { if (it) stringResource(R.string.liked_yes) else stringResource(R.string.liked_no) } ?: stringResource(R.string.liked_no),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // 最近播放历史
                if (playbackHistory.isNotEmpty()) {
                    TitleWidget(title = stringResource(R.string.recent_history)) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            playbackHistory.forEach { history ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${stringResource(R.string.duration)}: ${formatDuration(history.playDuration)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            history.source?.let {
                                                Text(
                                                    text = it,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = formatTimestamp(history.playedAt),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = if (history.isCompleted) stringResource(R.string.completed) else stringResource(R.string.incomplete),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (history.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    TitleWidget(title = stringResource(R.string.recent_history)) {
                        Text(
                            text = stringResource(R.string.song_detail_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
