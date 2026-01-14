@file:OptIn(androidx.media3.common.util.UnstableApi::class)
package com.example.hearablemusicplayer.ui.pages

import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hearablemusicplayer.ui.components.PlayContent
import com.example.hearablemusicplayer.ui.util.AnimationConfig
import com.example.hearablemusicplayer.ui.util.Routes
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback
import com.example.hearablemusicplayer.ui.viewmodel.PlayControlViewModel
import com.example.hearablemusicplayer.ui.viewmodel.PlaylistViewModel
import kotlinx.coroutines.launch

import androidx.hilt.navigation.compose.hiltViewModel

import androidx.activity.ComponentActivity

// 播放器主界面
@Composable
fun PlayerScreen(
    viewModel: PlayControlViewModel = hiltViewModel(),
    playlistViewModel: PlaylistViewModel = hiltViewModel(LocalContext.current as ComponentActivity),
    navController: NavController
) {
    val density = LocalDensity.current
    val dismissThreshold = with(density) { 220.dp.toPx() }
    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var visible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val haptic = rememberHapticFeedback()

    LaunchedEffect(Unit) {
        visible = true
        viewModel.toastEvent.collect { event ->
            Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
        }
    }

    // 预加载当前播放音乐信息
    LaunchedEffect(Unit) {
        viewModel.preloadCurrentMusicInfo()
    }

    // 开启播放进度监督
    DisposableEffect(Unit) {
        viewModel.startProgressTracking()
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
    val isLiked by viewModel.likeStatus.collectAsState()
    val labels by viewModel.currentMusicLabels.collectAsState()
    val lyrics by viewModel.currentMusicLyrics.collectAsState()
    val playlist by viewModel.currentPlaylist.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()

    // 监听音乐变化并加载相关信息
    LaunchedEffect(musicInfo?.music?.id) {
        musicInfo?.music?.id?.let { id ->
            viewModel.getLikedStatus(id)
            viewModel.getMusicLabels(id)
            viewModel.getMusicLyrics(id)
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(initialScale = 0.95f, animationSpec = tween(durationMillis = AnimationConfig.TRANSITION, easing = AnimationConfig.EASE_IN_OUT)) +
                fadeIn(animationSpec = tween(durationMillis = AnimationConfig.TRANSITION, easing = AnimationConfig.EASE_IN_OUT)),
        exit = scaleOut(targetScale = 0.95f, animationSpec = tween(durationMillis = AnimationConfig.TRANSITION, easing = AnimationConfig.EASE_IN_OUT)) +
                fadeOut(animationSpec = tween(durationMillis = AnimationConfig.TRANSITION, easing = AnimationConfig.EASE_IN_OUT))
    ){
        // 实现嵌套滚动连接，处理下滑返回
        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                // 预先消耗滚动事件：当已有偏移量时，向上拖动应先消耗偏移量
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    // 当有向上滚动（available.y < 0）且当前有下拉偏移时，先消耗偏移量
                    if (available.y < 0 && offsetY.value > 0f && source == NestedScrollSource.UserInput) {
                        val consumed = available.y.coerceAtLeast(-offsetY.value)
                        scope.launch {
                            val newOffset = (offsetY.value + consumed).coerceAtLeast(0f)
                            offsetY.snapTo(newOffset)
                        }
                        return Offset(0f, consumed)
                    }
                    return Offset.Zero
                }

                override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                    // 只有当子组件没有消耗向下滚动事件，且处于拖动状态时，才处理返回
                    if (available.y > 0 && consumed.y <= 0 && source == NestedScrollSource.UserInput) {
                        // 子组件没有消耗向下滚动事件，处理返回逻辑
                        val delta = available.y
                        scope.launch {
                            val newOffset = (offsetY.value + delta).coerceAtLeast(0f)
                            offsetY.snapTo(newOffset)
                            // 当拖动到一定程度时给予触觉反馈
                            if (newOffset > dismissThreshold * 0.5f && newOffset < dismissThreshold * 0.6f) {
                                haptic.performLightClick()
                            }
                        }
                        // 返回已消耗的偏移量
                        return Offset(0f, available.y)
                    }
                    return Offset.Zero
                }

                override suspend fun onPreFling(available: Velocity): Velocity {
                    // 处理快速滑动结束时的逻辑
                    if (offsetY.value > 0f) {
                        if (offsetY.value > dismissThreshold) {
                            // 达到阈值，执行退出
                            navController.popBackStack()
                            haptic.performGestureEnd()
                            offsetY.animateTo(
                                targetValue = with(density) { 1000.dp.toPx() },
                                animationSpec = tween(durationMillis = 300)
                            )
                        } else {
                            // 未达到阈值，执行回弹
                            haptic.performLightClick()
                            offsetY.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                        // 消耗垂直方向的 fling 速度
                        return Velocity(0f, available.y)
                    }
                    return Velocity.Zero
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, offsetY.value.toInt()) }
                .graphicsLayer {
                    alpha = 1f - (offsetY.value / (2 * dismissThreshold)).coerceIn(0f, 1f)
                }
                .nestedScroll(nestedScrollConnection) // 添加嵌套滚动支持
        ) {
            PlayContent(
                musicInfo = musicInfo,
                isPlaying = isPlaying,
                currentPosition = currentPosition,
                duration = duration,
                playbackMode = playbackMode,
                remainingTime = remainingTime,
                isLiked = isLiked,
                labels = labels,
                lyrics = lyrics,
                playlist = playlist,
                currentIndex = currentIndex,
                onBackClick = { navController.popBackStack() },
                onSeek = viewModel::seekTo,
                onPlayPause = { if (isPlaying) viewModel.pauseMusic() else viewModel.playOrResume() },
                onNext = viewModel::playNext,
                onPrevious = viewModel::playPrevious,
                onPlaybackModeChange = viewModel::togglePlaybackModeByOrder,
                onFavorite = {
                    musicInfo?.let { viewModel.updateMusicLikedStatus(it, !isLiked) }
                },
                onTimerClick = viewModel::startTimer,
                onCancelTimer = viewModel::cancelTimer,
                onHeartMode = viewModel::playHeartMode,
                onArtistClick = { artistName ->
                    playlistViewModel.getSelectedArtistMusicList(artistName)
                    navController.navigate(Routes.ARTIST)
                },
                onClearPlaylist = viewModel::clearPlaylist,
                onPlayItem = viewModel::playAt,
                onMoveToTop = viewModel::moveToTop,
                onRemoveFromPlaylist = viewModel::removeFromPlaylist
            )
        }
    }
}

