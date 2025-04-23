package com.example.hearablemusicplayer.ui.pages

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hearablemusicplayer.R
import com.example.hearablemusicplayer.database.Music
import com.example.hearablemusicplayer.database.myClass.PlaybackMode
import com.example.hearablemusicplayer.viewmodel.PlayControlViewModel

// 格式化时间为 mm:ss
fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

// 播放器主界面
@Composable
fun PlayerScreen(viewModel: PlayControlViewModel, navController: NavController) {

    // 开启播放进度监督
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(Unit) {
        viewModel.startProgressTracking()
        onDispose {
            viewModel.stopProgressTracking()
        }
    }

    val music by viewModel.currentPlayingMusic.collectAsState()
    val playlist by viewModel.currentPlaylist.collectAsState(initial = emptyList())
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val playbackMode by viewModel.playbackMode.collectAsState()
    val isLiked = music?.liked ?: false

    if (music == null) {
        // 当前没有播放的音乐时显示文字
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("当前音乐: 无")
        }
    } else {
        // 有音乐时显示播放器内容和播放列表
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            item { PlayerHeader(navController) } // 顶部返回按钮
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { MusicInfo(music!!) } // 显示歌曲信息
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { AlbumArt(music!!.albumArtUri) } // 显示专辑封面
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item {
                SeekBar(
                    currentPosition = currentPosition,
                    duration = duration,
                    onSeek = viewModel::seekTo
                ) // 进度条与时间显示
            }
            item {
                PlaybackControls(
                    isPlaying = isPlaying,
                    playbackMode = playbackMode,
                    isLiked = isLiked,
                    onPlayPause = {
                        if (isPlaying) viewModel.pauseMusic() else viewModel.playOrResume()
                    },
                    onNext = viewModel::playNext,
                    onPrevious = viewModel::playPrevious,
                    onPlaybackModeChange = viewModel::togglePlaybackModeByOrder,
                    onFavorite = { viewModel.updateMusicLikedStatus(music!!,!isLiked) },
                    onTimerClick = {  },
                    onMoreOptions = {  }
                ) // 播放控制按钮
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item {
                PlaylistSection(
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
        modifier = Modifier.fillMaxWidth(),
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

// 显示专辑封面
@Composable
fun AlbumArt(uri: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Crossfade(targetState = uri, label = "AlbumArtCrossfade") { targetUri ->
            AsyncImage(
                model = targetUri,
                contentDescription = "Album art",
                modifier = Modifier
                    .size(300.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
        }
    }
}

// 音乐进度条和时间显示
@Composable
fun SeekBar(currentPosition: Long, duration: Long, onSeek: (Long) -> Unit) {
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    val isDragging = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
    ) {
        Slider(
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
    isLiked: Boolean,
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
                Icon(painter = painterResource(R.drawable.backward_end_fill), contentDescription = "Previous")
            }

            IconButton(onClick = onPlayPause, modifier = Modifier.size(72.dp)) {
                Icon(
                    painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play_fill),
                    contentDescription = "Play / Pause"
                )
            }

            IconButton(onClick = onNext, modifier = Modifier.size(64.dp)) {
                Icon(painter = painterResource(R.drawable.forward_end_fill), contentDescription = "Next")
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

            IconButton(onClick = onFavorite) {
                Icon(
                    painter = painterResource(
                        if(isLiked) R.drawable.heart_fill
                        else R.drawable.heart
                    ),
                    contentDescription = "Favorite")
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
    playlist: List<Music>,
    onSelect: (Music) -> Unit,
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
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "清空列表",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
                    .clickable { onClear() }
            )
        }
        playlist.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { onSelect(item) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = item.albumArtUri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = item.title, style = MaterialTheme.typography.bodyLarge)
                    Text(text = item.artist, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
