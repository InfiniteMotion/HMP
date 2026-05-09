package com.hmp.desktop.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hmp.domain.config.DisplayMode
import com.hmp.domain.config.LyricsAlignment
import com.hmp.domain.enum.PlaybackMode
import com.hmp.desktop.player.DesktopMusicController
import com.hmp.desktop.ui.components.AdvancedLyrics
import com.hmp.desktop.ui.components.AlbumArt
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(modifier: Modifier = Modifier) {
    val controller: DesktopMusicController = koinInject()

    val currentMusic = controller.currentPlayingMusic.collectAsState().value
    val isPlaying = controller.isPlaying.collectAsState().value
    val currentPosition = controller.currentPosition.collectAsState().value
    val duration = controller.duration.collectAsState().value
    val playbackMode = controller.playbackMode.collectAsState().value
    val isLiked = controller.likeStatus.collectAsState().value
    val timerRemaining = controller.timerRemaining.collectAsState().value
    val playlist = controller.currentPlaylist.collectAsState().value

    var showLyrics by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Track Info
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = currentMusic?.music?.title ?: "未在播放",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = currentMusic?.music?.artist ?: "",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = currentMusic?.music?.album ?: "",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Album Art or Lyrics (toggle)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            if (showLyrics) {
                AdvancedLyrics(
                    lyrics = currentMusic?.extra?.lyrics,
                    currentPosition = currentPosition,
                    onSeek = { controller.seekTo(it) },
                    displayMode = DisplayMode.DUAL,
                    alignment = LyricsAlignment.CENTER,
                )
            } else {
                AlbumArt(
                    path = currentMusic?.music?.path ?: "",
                    modifier = Modifier
                        .size(300.dp)
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = Color.Black.copy(alpha = 0.3f),
                            spotColor = Color.Black.copy(alpha = 0.3f),
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { showLyrics = true },
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Seek Bar
        SeekBar(
            currentPosition = currentPosition,
            duration = duration,
            onSeek = { controller.seekTo(it) },
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Main Playback Controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            IconButton(
                onClick = { controller.playPrevious() },
                modifier = Modifier.size(64.dp),
            ) {
                Icon(
                    Icons.Default.SkipPrevious,
                    contentDescription = "上一首",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(36.dp),
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            IconButton(
                onClick = { controller.togglePlayPause() },
                modifier = Modifier.size(72.dp),
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "暂停" else "播放",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(48.dp),
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            IconButton(
                onClick = { controller.playNext() },
                modifier = Modifier.size(64.dp),
            ) {
                Icon(
                    Icons.Default.SkipNext,
                    contentDescription = "下一首",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(36.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Auxiliary Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Playback Mode
            IconButton(onClick = { controller.togglePlaybackMode() }) {
                Icon(
                    when (playbackMode) {
                        PlaybackMode.SHUFFLE -> Icons.Default.Shuffle
                        PlaybackMode.REPEAT_ONE -> Icons.Default.RepeatOne
                        PlaybackMode.SEQUENTIAL -> Icons.Default.Repeat
                    },
                    contentDescription = "播放模式",
                    tint = if (playbackMode != PlaybackMode.SEQUENTIAL)
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
                )
            }

            // Like
            IconButton(onClick = { /* TODO: implement toggleLike */ }) {
                Icon(
                    if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "收藏",
                    tint = if (isLiked) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
                )
            }

            // Timer
            if (timerRemaining != null) {
                Text(
                    text = formatTime(timerRemaining),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { controller.cancelTimer() },
                )
            } else {
                IconButton(onClick = { /* TODO: show timer dialog */ }) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = "定时器",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            // Lyrics toggle
            IconButton(onClick = { showLyrics = !showLyrics }) {
                Icon(
                    Icons.AutoMirrored.Filled.QueueMusic,
                    contentDescription = "歌词",
                    tint = if (showLyrics) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeekBar(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
) {
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    var isSeeking by remember { mutableStateOf(false) }

    if (!isSeeking && duration > 0) {
        sliderPosition = currentPosition.toFloat() / duration
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Slider(
            value = sliderPosition,
            onValueChange = { newValue ->
                isSeeking = true
                sliderPosition = newValue
            },
            onValueChangeFinished = {
                val seekPosition = (sliderPosition * duration).toLong()
                onSeek(seekPosition)
                isSeeking = false
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
            ),
            thumb = {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(color = Color.Transparent)
                )
            },
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = formatTime(currentPosition),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = formatTime(duration),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
