package com.example.hearablemusicplayer.ui.components

import DotPager
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
import com.example.hearablemusicplayer.ui.pages.formatTime
import com.example.hearablemusicplayer.viewmodel.MusicViewModel
import com.example.hearablemusicplayer.viewmodel.PlayControlViewModel

@Composable
fun PlayContent(
    listState: LazyListState,
    musicViewModel: MusicViewModel,
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

    if (musicInfo == null) {
        // 当前没有播放的音乐时显示文字
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("当前音乐: 无")
        }
    } else {
        // 有音乐时显示播放器内容和播放列表
        musicViewModel.extractMainColor(musicInfo!!.music.path)
        viewModel.prepareMusic(musicInfo!!)
        viewModel.getLikedStatus(musicInfo!!.music.id)
        viewModel.getMusicLabels(musicInfo!!.music.id)
        viewModel.getMusicLyrics(musicInfo!!.music.id)
        val isLiked by viewModel.likeStatus.collectAsState()
        val labels by viewModel.currentMusicLabels.collectAsState()
        val lyrics by viewModel.currentMusicLyrics.collectAsState()
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            item { PlayerHeader(navController) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { MusicInfo(musicInfo!!.music) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item {
                MusicInfoExtra(musicInfo!!,labels,lyrics,currentPosition) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item {
                SeekBar(
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
                    onPlayPause = {
                        if (isPlaying) viewModel.pauseMusic() else viewModel.playOrResume()
                    },
                    onNext = viewModel::playNext,
                    onPrevious = viewModel::playPrevious,
                    onPlaybackModeChange = viewModel::togglePlaybackModeByOrder,
                    onFavorite = { viewModel.updateMusicLikedStatus(musicInfo!!,!isLiked) },
                    onTimerClick = {  },
                    onMoreOptions = {  }
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item {
                PlaylistSection(
                    currentIndex = currentIndex,
                    playlist = playlist,
                    onSelect = viewModel::playAt,
                    onClear = viewModel::clearPlaylist
                ) // 播放列表
            }
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
        { AlbumCover(musicInfo.extra?.albumArtUri, Arrangement.Center,300) },
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
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit
) {
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    val isDragging = remember { mutableStateOf(false) }

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
                // 你可以在这里自定义 Thumb 的形状、颜色、大小等
                Box(
                    modifier = Modifier
                        .size(16.dp, 16.dp)
                        .background(
                            color = Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        )
                )
            },
            value = if (duration > 0) {
                if (isDragging.value) sliderPosition else currentPosition.toFloat() / duration
            } else 0f,
            onValueChange = {
                sliderPosition = it
                isDragging.value = true
            },
            onValueChangeFinished = {
                val seekPosition = (sliderPosition * duration).toLong()
                onSeek(seekPosition)
                isDragging.value = false
            }
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
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onPlaybackModeChange: () -> Unit,
    onFavorite: () -> Unit,
    onTimerClick: () -> Unit,
    onMoreOptions: () -> Unit
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

            IconButton(onClick = onTimerClick) {
                Icon(painter = painterResource(R.drawable.timer), contentDescription = "Timer")
            }

            IconButton(onClick = onMoreOptions) {
                Icon(painter = painterResource(R.drawable.dot_grid_1x2), contentDescription = "More Options")
            }
        }
    }
}


// 播放列表组件
@Composable
fun PlaylistSection(
    currentIndex: Int,
    playlist: List<MusicInfo>,
    onSelect: (MusicInfo) -> Unit,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "播放列表",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "清空列表",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
                    .clickable { onClear() }
            )
        }
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth()
                .size(2.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        playlist.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSelect(item) }
                    .then(
                        if (index == currentIndex) { // 当前播放项的特殊样式
                            Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                        } else Modifier
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = item.music.title,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = item.music.artist,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}