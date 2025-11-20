@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
package com.example.hearablemusicplayer.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hearablemusicplayer.R
import com.example.hearablemusicplayer.database.Music
import com.example.hearablemusicplayer.database.MusicInfo
import com.example.hearablemusicplayer.database.MusicLabel
import com.example.hearablemusicplayer.database.myenum.PlaybackMode
import com.example.hearablemusicplayer.ui.dialogs.TimerDialog
import com.example.hearablemusicplayer.ui.pages.formatTime
import com.example.hearablemusicplayer.viewmodel.PlayControlViewModel

@Composable
fun PlayContent(
    listState: LazyListState,
    viewModel: PlayControlViewModel,
    navController: NavController
){
    // 开启播放进度监督
    DisposableEffect(Unit) {
        viewModel.startProgressTracking()
        onDispose {
            viewModel.stopProgressTracking()
        }
    }

    val musicInfo by viewModel.currentPlayingMusic.collectAsState()
    val playlist by viewModel.currentPlaylist.collectAsState(initial = emptyList())
    val currentIndex by viewModel.currentIndex.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val playbackMode by viewModel.playbackMode.collectAsState()
    val remainingTime by viewModel.timerRemaining.collectAsState()

    if (musicInfo == null) {
        // 当前没有播放的音乐时显示文字
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.align(Alignment.TopCenter)
            ){
                PlayerHeader(navController)
            }
            Text("当前音乐: 无")
        }
    } else {
        if(viewModel.isReady()!=true) {
            viewModel.prepareMusic(musicInfo!!)
        }
        viewModel.getLikedStatus(musicInfo!!.music.id)
        viewModel.getMusicLabels(musicInfo!!.music.id)
        viewModel.getMusicLyrics(musicInfo!!.music.id)
        val isLiked by viewModel.likeStatus.collectAsState()
        val labels by viewModel.currentMusicLabels.collectAsState()
        val lyrics by viewModel.currentMusicLyrics.collectAsState()
        var showTimerDialog by remember { mutableStateOf(false) }
        var showFullList by remember { mutableStateOf(false) }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { PlayerHeader(navController) }
            item { Spacer(modifier = Modifier.height(24.dp)) }
            item { MusicInfo(musicInfo!!.music) }
            item { Spacer(modifier = Modifier.height(24.dp)) }
            item {
                MusicInfoExtra(musicInfo!!,labels,lyrics,currentPosition) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item {
                SeekBar(
                    isPlaying = isPlaying,
                    currentPosition = currentPosition,
                    duration = duration,
                    onSeek = viewModel::seekTo
                ) // 进度条与时间显示
            }
            item {
                // 播放控制按钮
                PlaybackControls(
                    isPlaying = isPlaying,
                    playbackMode = playbackMode,
                    isLike = isLiked,
                    remainingTime = remainingTime,
                    onPlayPause = {
                        if (isPlaying) viewModel.pauseMusic() else viewModel.playOrResume()
                    },
                    onNext = viewModel::playNext,
                    onPrevious = viewModel::playPrevious,
                    onPlaybackModeChange = viewModel::togglePlaybackModeByOrder,
                    onFavorite = { viewModel.updateMusicLikedStatus(musicInfo!!,!isLiked) },
                    onTimerClick = { showTimerDialog = true },
                    onHeartMode = { viewModel.playHeartMode() }
                )
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
            item {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "播放列表 (${playlist.size})",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row {
                        if (playlist.size > 8) {
                            Text(
                                text = if (showFullList) "收起" else "展开",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier
                                    .padding(end = 16.dp, bottom = 8.dp)
                                    .clickable { showFullList = !showFullList }
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "清空",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .clickable { viewModel.clearPlaylist() }
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(2.dp)
                        .padding(horizontal = 16.dp)
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // 使用 items 显示播放列表项
            val displayItems = if (!showFullList) {
                playlist.filterIndexed { index, _ ->
                    index >= (currentIndex - 2) && index < currentIndex ||
                            index == currentIndex ||
                            index > currentIndex && index <= (currentIndex + 5)
                }
            } else {
                playlist
            }

            items(
                items = displayItems,
                key = { it.music.id }
            ) { item ->
                val index = playlist.indexOf(item)
                PlaylistItem(
                    musicInfo = item,
                    isPlaying = index == currentIndex,
                    onClick = { viewModel.playAt(item) },
                    onRemove = { viewModel.removeFromPlaylist(item) }
                )
            }

            // 显示更多提示
            if (!showFullList && playlist.size > 8) {
                item {
                    Text(
                        text = "还有 ${playlist.size - displayItems.size} 首歌曲",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 16.dp)
                            .clickable { showFullList = true }
                    )
                }
            }
        }

        if (showTimerDialog) {
            TimerDialog(
                onDismiss = { },
                onConfirm = { minutes ->
                    if(minutes==0){
                        viewModel.cancelTimer()
                    }else{
                        viewModel.startTimer(minutes)
                    }
                }
            )
        }
    }
}

// 顶部返回按钮
@Composable
fun PlayerHeader(navController: NavController) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.chevron_down),
                contentDescription = "Back",
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

// 歌曲标题、艺术家、专辑信息
@Composable
fun MusicInfo(music: Music) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.padding(horizontal = 32.dp)
    ) {
        Text(
            text = music.title,
            style = MaterialTheme.typography.displayMedium,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = music.artist, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = music.album, style = MaterialTheme.typography.titleMedium)
    }
}

// 标签，专辑封面，歌词
@Composable
fun MusicInfoExtra(
    musicInfo: MusicInfo,
    labels: List<MusicLabel?>,
    lyrics: String?,
    currentPosition: Long,
) {
    val contents = listOf<@Composable () -> Unit>(
        { LabelsCapsule(musicInfo.extra,labels) },
        { AlbumCover(musicInfo.music.albumArtUri, Arrangement.Center,300) },
        { Lyrics(lyrics,currentPosition)}
    )
    Row (
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ){
        DotPager(
            modifier = Modifier.size(340.dp),
            pageContent = contents,
            initialPage = 1
        )
    }
}

// 音乐进度条和时间显示
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeekBar(
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit
) {
    // 关键修改：独立维护滑块位置状态
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    var lastValidPosition by remember { mutableFloatStateOf(0f) }
    val isDragging = remember { mutableStateOf(false) }

    // 监听位置变化（修复点1）
    LaunchedEffect(currentPosition, isPlaying) {
        if (!isDragging.value && duration > 0) {
            val newPosition = currentPosition.toFloat() / duration
            sliderPosition = newPosition
            lastValidPosition = newPosition // 保留最后一次有效位置
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
    ) {
        Slider(
            colors = SliderDefaults.colors(
                thumbColor = Color.Transparent,       // 拇指颜色
                activeTrackColor = MaterialTheme.colorScheme.primary, // 已激活轨道颜色（左侧）
            ),
            // 形状定制
            thumb = {
                // 自定义 Thumb 的形状、颜色、大小等
                Box(
                    modifier = Modifier
                        .size(16.dp, 16.dp)
                        .background(
                            color = Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        )
                )
            },
            value = when {
                isDragging.value -> sliderPosition
                duration > 0 -> currentPosition.toFloat() / duration
                else -> lastValidPosition
            },
            onValueChange = {
                sliderPosition = it.coerceIn(0f, 1f)
                isDragging.value = true
            },
            onValueChangeFinished = {
                onSeek((sliderPosition * duration).toLong())
                isDragging.value = false
            },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatTime(currentPosition))
            Text(formatTime(duration))
        }
    }
}

// 播放控制按钮（上一首、播放/暂停、下一首）
@Composable
fun PlaybackControls(
    isPlaying: Boolean,
    playbackMode: PlaybackMode,
    isLike: Boolean,
    remainingTime:Long?,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onPlaybackModeChange: () -> Unit,
    onFavorite: () -> Unit,
    onTimerClick: () -> Unit,
    onHeartMode: () -> Unit
) {
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
            IconButton(onClick = onPrevious, modifier = Modifier.size(64.dp)) {
                Icon(
                    painter = painterResource(R.drawable.backward_end_fill),
                    contentDescription = "Previous",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onPlayPause, modifier = Modifier.size(72.dp)) {
                Icon(
                    painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play_fill),
                    contentDescription = "Play / Pause",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onNext, modifier = Modifier.size(64.dp)) {
                Icon(
                    painter = painterResource(R.drawable.forward_end_fill),
                    contentDescription = "Next",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 下方额外操作按钮区
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                    contentDescription = "Playback Mode"
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
                if (isLiked) {
                    Icon(
                        painter = painterResource(R.drawable.heart_fill),
                        contentDescription = "Favorite",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }else{
                    Icon(
                        painter = painterResource(R.drawable.heart),
                        contentDescription = "Favorite",
                    )
                }
            }
            IconButton(onClick = onHeartMode) {
                Icon(painter = painterResource(R.drawable.identify_song), contentDescription = "RecommendationMode")
            }
            if(remainingTime == null){
                IconButton(onClick = onTimerClick) {
                    Icon(painter = painterResource(R.drawable.timer), contentDescription = "Timer.kt")
                }
            }else{
                Text(
                    text = formatTime(remainingTime),  // 使用 formatTime 函数
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onTimerClick() }
                )
            }
        }
    }
}

@Composable
private fun PlaylistItem(
    musicInfo: MusicInfo,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,         // 新增
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .then(
                if (isPlaying) {
                    Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                } else Modifier
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                text = musicInfo.music.title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = musicInfo.music.artist,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 新增的移除和置顶按钮
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.padding(end = 8.dp)
        ) {

            // 移除按钮
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.minus_circle),
                    contentDescription = "Remove",
                )
            }
        }
    }
}