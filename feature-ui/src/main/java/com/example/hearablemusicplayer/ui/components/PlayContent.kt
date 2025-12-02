@file:androidx.annotation.OptIn(UnstableApi::class)
package com.example.hearablemusicplayer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ScrollState
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.hearablemusicplayer.data.database.Music
import com.example.hearablemusicplayer.data.database.MusicInfo
import com.example.hearablemusicplayer.data.database.MusicLabel
import com.example.hearablemusicplayer.data.database.myenum.PlaybackMode
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.dialogs.TimerDialog
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback
import com.example.hearablemusicplayer.ui.viewmodel.PlayControlViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    viewModel: PlayControlViewModel,
    navController: NavController
){
    val haptic = rememberHapticFeedback()

    // 开启播放进度监督
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopProgressTracking()
        }
    }

    val musicInfo by viewModel.currentPlayingMusic.collectAsState()
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
            Text("当前音乐: 无", color = MaterialTheme.colorScheme.onSurface)
        }
    } else {
        viewModel.getLikedStatus(musicInfo!!.music.id)
        viewModel.getMusicLabels(musicInfo!!.music.id)
        viewModel.getMusicLyrics(musicInfo!!.music.id)
        val isLiked by viewModel.likeStatus.collectAsState()
        val labels by viewModel.currentMusicLabels.collectAsState()
        val lyrics by viewModel.currentMusicLyrics.collectAsState()
        var showTimerDialog by remember { mutableStateOf(false) }
        
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            val scrollState = rememberScrollState()
            
            // 检测是否滚动到顶部
            val isAtTop by remember {
                derivedStateOf { scrollState.value == 0 }
            }
            
            // 下拉退出的嵌套滚动处理
            var dragOffsetY by remember { mutableFloatStateOf(0f) }
            val nestedScrollConnection = remember(isAtTop) {
                object : NestedScrollConnection {
                    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                        // 只处理用户手势触发的滚动
                        if (source == NestedScrollSource.UserInput) {
                            // 向下拖动（available.y > 0）且已在顶部时，累积下拉距离
                            if (isAtTop && available.y > 0) {
                                dragOffsetY += available.y
                                return Offset.Zero // 不消耗滚动，让内容正常显示
                            } else {
                                // 不在顶部或向上拖动时，重置计数
                                dragOffsetY = 0f
                            }
                        }
                        return Offset.Zero
                    }
                    
                    override fun onPostScroll(
                        consumed: Offset,
                        available: Offset,
                        source: NestedScrollSource
                    ): Offset {
                        // 手势结束时检查是否需要返回
                        if (dragOffsetY > 200f && isAtTop) {
                            if (navController.previousBackStackEntry != null) {
                                navController.popBackStack()
                            }
                        }
                        return Offset.Zero
                    }
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(nestedScrollConnection)
                    .verticalScroll(scrollState)
            )
            {
                Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    PlayerHeader(navController)
                    Spacer(modifier = Modifier.height(24.dp))
                    MusicInfo(musicInfo!!.music)
                    Spacer(modifier = Modifier.height(16.dp))
                    MusicInfoExtra(
                        musicInfo = musicInfo!!,
                        labels = labels,
                        lyrics = lyrics,
                        currentPosition = currentPosition,
                        onSeek = { viewModel.seekTo(it) }
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SeekBar(
                        currentPosition = currentPosition,
                        duration = duration,
                        onSeek = viewModel::seekTo
                    )
                    // 播放控制按钮
                    PlaybackControls(
                        isPlaying = isPlaying,
                        playbackMode = playbackMode,
                        isLike = isLiked,
                        remainingTime = remainingTime,
                        onPlayPause = {
                            haptic.performClick()
                            if (isPlaying) viewModel.pauseMusic() else viewModel.playOrResume()
                        },
                        onNext = {
                            haptic.performClick()
                            viewModel.playNext()
                        },
                        onPrevious = {
                            haptic.performClick()
                            viewModel.playPrevious()
                        },
                        onPlaybackModeChange = {
                            haptic.performContextClick()
                            viewModel.togglePlaybackModeByOrder()
                        },
                        onFavorite = {
                            haptic.performConfirm()
                            viewModel.updateMusicLikedStatus(musicInfo!!,!isLiked)
                        },
                        onTimerClick = {
                            haptic.performClick()
                            showTimerDialog = true
                        },
                        onHeartMode = {
                            haptic.performConfirm()
                            viewModel.playHeartMode()
                        },
                        viewModel = viewModel,
                        scrollState = scrollState
                    )
                }
            }
            if (showTimerDialog) {
                TimerDialog(
                    onDismiss = { showTimerDialog = false },
                    onConfirm = { minutes: Int ->
                        if(minutes==0){
                            viewModel.cancelTimer()
                        }else{
                            viewModel.startTimer(minutes)
                        }
                        showTimerDialog = false
                    }
                )
            }
        }
    }
}

// 顶部返回按钮
@Composable
fun PlayerHeader(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.chevron_down),
                tint = MaterialTheme.colorScheme.onSurface,
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
            maxLines = 1,
            overflow = TextOverflow.MiddleEllipsis,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = music.artist,
            maxLines = 1,
            overflow = TextOverflow.MiddleEllipsis,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = music.album,
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
    musicInfo: MusicInfo,
    labels: List<MusicLabel?>,
    lyrics: String?,
    currentPosition: Long,
    onSeek: (Long) -> Unit,
) {
    val contents = listOf<@Composable () -> Unit>(
        { LabelsCapsule(musicInfo.extra,labels) },
        { AlbumCover(musicInfo.music.albumArtUri, Arrangement.Center,300) },
        { Lyrics(lyrics, currentPosition, onSeek = onSeek) }
    )
    Row (
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ){
        DotPager(
            modifier = Modifier.size(340.dp,420.dp),
            pageContent = contents,
            initialPage = 1
        )
    }
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
                            color = Color.Transparent,
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
    onHeartMode: () -> Unit,
    viewModel: PlayControlViewModel,
    scrollState: ScrollState
) {
    var playlistExpanded by remember { mutableStateOf(false) }
    val haptic = rememberHapticFeedback()
    val playlist by viewModel.currentPlaylist.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    
    // 预加载：提前准备好播放列表的初始状态，避免首次展开卡顿
    LaunchedEffect(playlist.size, currentIndex) {
        // 当播放列表数据变化时，在后台更新 listState，但不滚动
        // 这样首次展开时已经有缓存的状态
        if (playlist.isNotEmpty() && !playlistExpanded) {
            // 静默地准备好位置，不执行动画
            listState.scrollToItem(currentIndex.coerceIn(0, playlist.lastIndex))
        }
    }
    
    // 监听播放模式变化，当模式改变时重新定位当前播放歌曲
    LaunchedEffect(playbackMode, playlist.size) {
        if (playlist.isNotEmpty() && playlistExpanded) {
            // 短暂延迟后重新定位当前播放项，确保列表已更新
            delay(100)
            listState.scrollToItem(currentIndex.coerceIn(0, playlist.lastIndex))
        }
    }
    
    // 当播放列表展开时，滚动页面使播放列表底部与屏幕底部对齐
    LaunchedEffect(playlistExpanded) {
        if (playlistExpanded) {
            // 步骤1：等待播放列表区域完全展开（减少等待时间，让动画更流畅）
            delay(320) // expandVertically动画默认300ms + 20ms缓冲
            
            // 步骤2：滚动页面到底部，使播放列表底部与屏幕底部对齐
            val playlistHeightPx = with(density) { 560.dp.toPx() }
            val targetScroll = (scrollState.value + playlistHeightPx).toInt()
            scrollState.animateScrollTo(
                value = targetScroll.coerceAtMost(scrollState.maxValue),
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
                )
            )
            
            // 步骤3：页面滚动开始后稍等片刻，定位当前播放项（与滚动动画并行，体验更流畅）
            if (playlist.isNotEmpty()) {
                delay(150) // 短暂延迟后立即开始定位，与页面滚动形成流畅过渡
                listState.animateScrollToItem(
                    index = currentIndex.coerceIn(0, playlist.lastIndex),
                )
            }
        }
    }
    
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
            IconButton(onClick = onPrevious, modifier = Modifier.size(72.dp)) {
                Icon(
                    painter = painterResource(R.drawable.backward_end_fill),
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = "Previous",
                )
            }

            IconButton(onClick = onPlayPause, modifier = Modifier.size(72.dp)) {
                Icon(
                    painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play_fill),
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = "Play / Pause",
                )
            }

            IconButton(onClick = onNext, modifier = Modifier.size(72.dp)) {
                Icon(
                    painter = painterResource(R.drawable.forward_end_fill),
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = "Next",
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
                        tint = MaterialTheme.colorScheme.onSurface,
                        contentDescription = "Favorite",
                    )
                }
            }
            IconButton(onClick = onHeartMode) {
                Icon(
                    painter = painterResource(R.drawable.identify_song),
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = "RecommendationMode"
                )
            }
            if(remainingTime == null){
                IconButton(onClick = onTimerClick) {
                    Icon(
                        painter = painterResource(R.drawable.timer),
                        tint = MaterialTheme.colorScheme.onSurface,
                        contentDescription = "Timer.kt"
                    )
                }
            } else{
                Text(
                    text = formatTime(remainingTime),  // 使用 formatTime 函数
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.clickable { onTimerClick() },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // 播放列表按钮
            IconButton(
                onClick = {
                    haptic.performClick()
                    playlistExpanded = !playlistExpanded
                }
            ) {
                Icon(
                    tint = if (playlistExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    painter = painterResource(
                        if (playlistExpanded) R.drawable.chevron_up_circle else R.drawable.music_note_list
                    ),
                    contentDescription = "Playlist"
                )
            }
        }
        
        // 可展开的播放列表区域
        AnimatedVisibility(
            visible = playlistExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // 顶部显示总数和当前序号，及清空按钮
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 32.dp, top = 12.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 清空列表按钮
                    TextButton(
                        onClick = {
                            haptic.performLightClick()
                            viewModel.clearPlaylist()
                        }
                    ) {
                        Text(
                            text = "清空",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = "${currentIndex + 1}/${playlist.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                // 播放列表内容（禁用嵌套滚动，避免触发播放页下拉）
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp)
                        .padding(horizontal = 16.dp)
                        .nestedScroll(remember {
                            // 只消耗向父级传递的滚动事件，不影响 LazyColumn 内部滚动
                            object : NestedScrollConnection {
                                override fun onPostScroll(
                                    consumed: Offset,
                                    available: Offset,
                                    source: NestedScrollSource
                                ): Offset {
                                    // 消耗所有剩余的滚动事件，防止传递给父级
                                    // available 是 LazyColumn 没有消耗的部分
                                    return available
                                }
                            }
                        })
                ) {
                    itemsIndexed(
                        items = playlist,
                        key = { _, item -> item.music.id } // 使用唯一 key 优化重组性能
                    ) { index, musicInfo ->
                        PlaylistItem(
                            musicInfo = musicInfo,
                            isCurrentPlaying = index == currentIndex,
                            index = index + 1,
                            onItemClick = {
                                haptic.performClick()
                                viewModel.viewModelScope.launch {
                                    viewModel.playAt(musicInfo)
                                }
                            },
                            onPinClick = {
                                haptic.performConfirm()
                                viewModel.moveToTop(musicInfo)
                            },
                            onRemoveClick = {
                                haptic.performLightClick()
                                viewModel.removeFromPlaylist(musicInfo)
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// 播放列表项
@Composable
fun PlaylistItem(
    musicInfo: MusicInfo,
    isCurrentPlaying: Boolean,
    index: Int,
    onItemClick: () -> Unit,
    onPinClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)) // 圆角裁剪
            .clickable(onClick = onItemClick)
            .background(
                if (isCurrentPlaying) 
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else 
                    Color.Transparent,
                shape = RoundedCornerShape(12.dp) // 圆角背景
            )
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 序号
        Text(
            text = "$index",
            style = MaterialTheme.typography.bodySmall,
            color = if (isCurrentPlaying) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.width(32.dp)
        )
        
        // 专辑封面（使用简化版本，不使用 AlbumCover 以避免布局问题）
        androidx.compose.foundation.Image(
            painter = rememberAsyncImagePainter(
                model = musicInfo.music.albumArtUri,
                placeholder = painterResource(R.drawable.unknown)
            ),
            contentDescription = "Album art",
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // 标题和艺术家
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = musicInfo.music.title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isCurrentPlaying) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = musicInfo.music.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        // 操作按钮组
        Row {
            // 置顶按钮
            IconButton(
                onClick = onPinClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.chevron_up_circle),
                    contentDescription = "Pin to top",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            // 移除按钮
            IconButton(
                onClick = onRemoveClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.trash),
                    contentDescription = "Remove",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

