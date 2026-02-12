@file:androidx.annotation.OptIn(UnstableApi::class)
package com.example.hearablemusicplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.example.hearablemusicplayer.domain.music.Music
import com.example.hearablemusicplayer.domain.music.MusicInfo
import com.example.hearablemusicplayer.domain.enum.PlaybackMode
import com.example.hearablemusicplayer.ui.R
import androidx.compose.ui.res.stringResource
import com.example.hearablemusicplayer.domain.playlist.AlgorithmType
import com.example.hearablemusicplayer.domain.playlist.ExtensionConfig
import com.example.hearablemusicplayer.domain.playlist.WeightTemplate
import com.example.hearablemusicplayer.ui.dialogs.TimerDialog
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback

// 格式化时间为 mm:ss
fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun PlayContent(
    musicInfo: MusicInfo?,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    playbackMode: PlaybackMode,
    remainingTime: Long?,
    isLiked: Boolean,
    lyrics: String?,
    playlist: List<MusicInfo>,
    currentIndex: Int,
    defaultAlgorithmType: AlgorithmType?,
    defaultTemplate: WeightTemplate?,
    onBackClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onPlaybackModeChange: () -> Unit,
    onFavorite: () -> Unit,
    onTimerClick: (Int) -> Unit,
    onCancelTimer: () -> Unit,
    onHeartMode: () -> Unit,
    onGeneratePlaylist: (Long) -> Unit,
    onSaveDefaultConfig: (AlgorithmType, WeightTemplate, ExtensionConfig) -> Unit,
    onArtistClick: (String) -> Unit,
    onClearPlaylist: () -> Unit,
    onPlayItem: suspend (MusicInfo) -> Unit,
    onMoveToTop: (MusicInfo) -> Unit,
    onRemoveFromPlaylist: (MusicInfo) -> Unit
){
    val haptic = rememberHapticFeedback()

    var showTimerDialog by remember { mutableStateOf(false) }
    var playlistExpanded by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val screenHeight = maxHeight
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // 播放器主界面容器：强制填满一屏高度
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .weight(1f) // 使顶部区域占据剩余空间
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    PlayerHeader(onBackClick)
                    Spacer(modifier = Modifier.height(24.dp))
                    MusicInfo(musicInfo?.music, onArtistClick)
                    Spacer(modifier = Modifier.height(16.dp))
                    // 封面区域：使用 weight(1f) 实现弹性缩放
                    MusicInfoExtra(
                        musicInfo = musicInfo,
                        lyrics = lyrics,
                        currentPosition = currentPosition,
                        defaultAlgorithmType = defaultAlgorithmType,
                        defaultTemplate = defaultTemplate,
                        onSeek = onSeek,
                        onGeneratePlaylist = onGeneratePlaylist,
                        onSaveDefaultConfig = onSaveDefaultConfig,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SeekBar(
                        currentPosition = currentPosition,
                        duration = duration,
                        onSeek = onSeek
                    )
                    // 播放控制按钮区
                    PlaybackControlsButtons(
                        isPlaying = isPlaying,
                        playbackMode = playbackMode,
                        isLike = isLiked,
                        remainingTime = remainingTime,
                        playlistExpanded = playlistExpanded,
                        onPlayPause = {
                            haptic.performClick()
                            onPlayPause()
                        },
                        onNext = {
                            haptic.performClick()
                            onNext()
                        },
                        onPrevious = {
                            haptic.performClick()
                            onPrevious()
                        },
                        onPlaybackModeChange = {
                            haptic.performContextClick()
                            onPlaybackModeChange()
                        },
                        onFavorite = {
                            haptic.performConfirm()
                            onFavorite()
                        },
                        onTimerClick = {
                            haptic.performClick()
                            showTimerDialog = true
                        },
                        onHeartMode = {
                            haptic.performConfirm()
                            onHeartMode()
                        },
                        onPlaylistToggle = {
                            playlistExpanded = !playlistExpanded
                        }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 播放列表区域：位于主界面下方
            PlaylistArea(
                expanded = playlistExpanded,
                playlist = playlist,
                currentIndex = currentIndex,
                scrollState = scrollState,
                onClearPlaylist = onClearPlaylist,
                onPlayItem = onPlayItem,
                onMoveToTop = onMoveToTop,
                onRemoveFromPlaylist = onRemoveFromPlaylist
            )
        }
        if (showTimerDialog) {
            TimerDialog(
                onDismiss = { },
                onConfirm = { minutes: Int ->
                    if(minutes==0){
                        onCancelTimer()
                    }else{
                        onTimerClick(minutes)
                    }
                }
            )
        }
    }
}

// 顶部返回按钮
@Composable
fun PlayerHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.chevron_down),
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = stringResource(R.string.back),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

// 歌曲标题、艺术家、专辑信息
@Composable
fun MusicInfo(
    music: Music?,
    onArtistClick: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.padding(horizontal = 32.dp)
    ) {
        Text(
            text = music?.title ?: "Music Title",
            maxLines = 1,
            overflow = TextOverflow.MiddleEllipsis,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
                text = music?.artist ?: "Artist",
                maxLines = 1,
                overflow = TextOverflow.MiddleEllipsis,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable {
                    onArtistClick(music?.artist ?: "Artist")
                }
            )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = music?.album ?: "Album",
            maxLines = 1,
            overflow = TextOverflow.MiddleEllipsis,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// 标签，专辑封面，歌词
@Composable
fun MusicInfoExtra(
    musicInfo: MusicInfo?,
    lyrics: String?,
    currentPosition: Long,
    defaultAlgorithmType: AlgorithmType?,
    defaultTemplate: WeightTemplate?,
    onSeek: (Long) -> Unit,
    onGeneratePlaylist: (Long) -> Unit,
    onSaveDefaultConfig: (AlgorithmType, WeightTemplate, ExtensionConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val contents = listOf<@Composable () -> Unit>(
        {
            Column(
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                if (musicInfo?.extra != null) {
                    TechnicalInfoCard(musicInfo.extra)
                }
                if (musicInfo?.extra?.isGetExtraInfo == true){
                    GeneratePlaylistComboButtons(
                        musicInfo.music.id,
                        defaultAlgorithmType,
                        defaultTemplate,
                        onGeneratePlaylist,
                        onSaveDefaultConfig
                    )
                }
            }
        },
        {
            AlbumCover(
                musicInfo?.music?.albumArtUri,
                300.dp,
                20.dp,
                10.dp)
        },
        {
            AdvancedLyrics(
                lyrics,
                currentPosition,
                modifier = Modifier,
                onSeek = onSeek)
        }
    )
    DotPager(
        modifier = modifier.fillMaxWidth(),
        pageContent = contents,
        initialPage = 1
    )
}

// 音乐进度条和时间显示
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeekBar(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit
) {
    val haptic = rememberHapticFeedback()
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    var isSeeking by remember { mutableStateOf(false) }

    // 监听位置变化
    LaunchedEffect(currentPosition) {
        if (!isSeeking && duration > 0) {
            sliderPosition = currentPosition.toFloat() / duration
        }
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Slider(
            value = sliderPosition,
            onValueChange = { newValue ->
                isSeeking = true // 用户开始拖动，设置为true
                haptic.performDragStart()
                sliderPosition = newValue
            },
            onValueChangeFinished = {
                haptic.performGestureEnd()
                val seekPosition = (sliderPosition * duration).toLong()
                onSeek(seekPosition)
                isSeeking = false // 拖动结束，设置为false
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            enabled = true,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
            ),
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    modifier = Modifier.height(4.dp),
                    thumbTrackGapSize = 0.dp,
                    trackInsideCornerSize = 0.dp,
                    drawStopIndicator = null
                )
            },
            thumb = {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(
                            color = Transparent,
                        )
                )
            }
        )
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(currentPosition),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatTime(duration),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// 播放控制按钮（上一首、播放/暂停、下一首）
@Composable
fun PlaybackControlsButtons(
    isPlaying: Boolean,
    playbackMode: PlaybackMode,
    isLike: Boolean,
    remainingTime: Long?,
    playlistExpanded: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onPlaybackModeChange: () -> Unit,
    onFavorite: () -> Unit,
    onTimerClick: () -> Unit,
    onHeartMode: () -> Unit,
    onPlaylistToggle: () -> Unit
) {
    val haptic = rememberHapticFeedback()
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 播放控制按钮区域
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onPrevious,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.backward_end_fill),
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = stringResource(R.string.previous),
                )
            }

            IconButton(
                onClick = onPlayPause,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play_fill),
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = stringResource(R.string.play_pause),
                )
            }

            IconButton(
                onClick = onNext,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.forward_end_fill),
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = stringResource(R.string.next),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 下方额外操作按钮区
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPlaybackModeChange) {
                Icon(
                    painter = painterResource(
                        when (playbackMode) {
                            PlaybackMode.SHUFFLE -> R.drawable.shuffle
                            PlaybackMode.REPEAT_ONE -> R.drawable.repeat_1
                            PlaybackMode.SEQUENTIAL -> R.drawable.repeat
                        }
                    ),
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = stringResource(R.string.playback_mode),
                )
            }

            var isLiked by remember { mutableStateOf(false) }
            isLiked = isLike

            IconButton(
                onClick = {
                    onFavorite()
                    isLiked = !isLiked
                },
            ) {
                Icon(
                    painter = painterResource(if (isLiked) R.drawable.heart_fill else R.drawable.heart),
                    contentDescription = stringResource(R.string.favorite),
                    tint = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
            }
            IconButton(onClick = onHeartMode) {
                Icon(
                    painter = painterResource(R.drawable.identify_song),
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = stringResource(R.string.recommendation_mode),
                )
            }
            if(remainingTime == null){
                IconButton(onClick = onTimerClick) {
                    Icon(
                        painter = painterResource(R.drawable.timer),
                        tint = MaterialTheme.colorScheme.onSurface,
                        contentDescription = stringResource(R.string.sleep_timer),
                    )
                }
            } else{
                Text(
                    text = formatTime(remainingTime),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.clickable { onTimerClick() },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // 播放列表按钮
            IconButton(
                onClick = {
                    haptic.performClick()
                    onPlaylistToggle()
                }
            ) {
                Icon(
                    tint = if (playlistExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    painter = painterResource(
                        if (playlistExpanded) R.drawable.chevron_up_circle else R.drawable.music_note_list
                    ),
                    contentDescription = stringResource(R.string.playlist),
                )
            }
        }
    }
}
