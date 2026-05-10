
package com.hmp.desktop.ui.player.pages
import com.hmp.desktop.ui.common.navigation.NavController

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject



import com.hmp.desktop.ui.common.navigation.Routes
import com.hmp.desktop.ui.common.util.rememberHapticFeedback
import com.hmp.desktop.ui.common.dialogs.viewmodel.DialogViewModel
import com.hmp.desktop.ui.player.viewmodel.LyricsSettingsState
import com.hmp.desktop.ui.player.viewmodel.PlayerCallbacks
import com.hmp.desktop.ui.player.viewmodel.PlayerUiState
import com.hmp.desktop.ui.player.viewmodel.PlaybackViewModel
import com.hmp.desktop.ui.player.viewmodel.PlaylistQueueViewModel
import com.hmp.desktop.ui.playlist.viewmodel.PlaylistViewModel
import com.hmp.desktop.ui.settings.viewmodel.SettingsViewModel
import com.hmp.desktop.ui.common.viewmodel.ThemeViewModel
import com.hmp.domain.music.MusicInfo
import com.hmp.domain.playlist.AlgorithmType
import com.hmp.domain.playlist.ExtensionConfig
import com.hmp.domain.playlist.WeightTemplate
import dev.chrisbanes.haze.rememberHazeState

// 播放器主界面
@Composable
fun PlayerScreen(
    playbackViewModel: PlaybackViewModel = koinInject(),
    playlistQueueViewModel: PlaylistQueueViewModel = koinInject(),
    playlistViewModel: PlaylistViewModel = koinInject(),
    settingsViewModel: SettingsViewModel = koinInject(),
    themeViewModel: ThemeViewModel = koinInject(),
    dialogViewModel: DialogViewModel = koinInject(),
    navController: NavController
) {
    val density = LocalDensity.current
    val dismissThreshold = with(density) { 220.dp.toPx() }
    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val haptic = rememberHapticFeedback()
    val hazeState = rememberHazeState()

    // 预加载当前播放音乐信息
    LaunchedEffect(Unit) {
    }

    // 开启播放进度监督
    DisposableEffect(Unit) {
        playbackViewModel.startProgressTracking()
        onDispose {

        }
    }

    val musicInfo by playlistQueueViewModel.currentPlayingMusic.collectAsState()
    val isPlaying by playbackViewModel.isPlaying.collectAsState()
    val currentPosition by playbackViewModel.currentPosition.collectAsState()
    val duration by playbackViewModel.duration.collectAsState()
    val playbackMode by playbackViewModel.playbackMode.collectAsState()
    val remainingTime by playbackViewModel.timerRemaining.collectAsState()
    val isLiked by playlistQueueViewModel.likeStatus.collectAsState()
    val lyrics by playlistQueueViewModel.currentMusicLyrics.collectAsState()
    val playlist by playlistQueueViewModel.currentPlaylist.collectAsState()
    val currentIndex by playlistQueueViewModel.currentIndex.collectAsState()
    val defaultAlgorithmType by playlistQueueViewModel.defaultAlgorithmType.collectAsState()
    val defaultTemplate by playlistQueueViewModel.defaultWeightTemplate.collectAsState()

    // 歌词配置
    val lyricsOriginalTextSize by settingsViewModel.lyricsOriginalTextSize.collectAsState()
    val lyricsTranslatedTextSize by settingsViewModel.lyricsTranslatedTextSize.collectAsState()
    val lyricsCurrentTimeTextSize by settingsViewModel.lyricsCurrentTimeTextSize.collectAsState()
    val lyricsLineSpacing by settingsViewModel.lyricsLineSpacing.collectAsState()
    val lyricsDisplayMode by settingsViewModel.lyricsDisplayMode.collectAsState()
    val lyricsAlignment by settingsViewModel.lyricsAlignment.collectAsState()

    val playerUiState = PlayerUiState(
        musicInfo = musicInfo,
        isPlaying = isPlaying,
        currentPosition = currentPosition,
        duration = duration,
        playbackMode = playbackMode,
        remainingTime = remainingTime,
        isLiked = isLiked,
        lyrics = lyrics,
        playlist = playlist,
        currentIndex = currentIndex,
        defaultAlgorithmType = defaultAlgorithmType,
        defaultTemplate = defaultTemplate
    )

    val lyricsSettingsState = LyricsSettingsState(
        lyricsOriginalTextSize = lyricsOriginalTextSize,
        lyricsTranslatedTextSize = lyricsTranslatedTextSize,
        lyricsCurrentTimeTextSize = lyricsCurrentTimeTextSize,
        lyricsLineSpacing = lyricsLineSpacing,
        lyricsDisplayMode = lyricsDisplayMode,
        lyricsAlignment = lyricsAlignment
    )

    val playerCallbacks = object : PlayerCallbacks {
        override fun onSeek(position: Long) { playbackViewModel.seekTo(position) }
        override fun onPlayPause() { if (isPlaying) playbackViewModel.pauseMusic() else playbackViewModel.playOrResume() }
        override fun onNext() { playbackViewModel.playNext() }
        override fun onPrevious() { playbackViewModel.playPrevious() }
        override fun onPlaybackModeChange() { playbackViewModel.togglePlaybackModeByOrder() }
        override fun onFavorite() { musicInfo?.let { playlistQueueViewModel.updateMusicLikedStatus(it, !isLiked) } }
        override fun onTimerClick(minutes: Int) { playbackViewModel.startTimer(minutes) }
        override fun onShowTimerDialog() {
            dialogViewModel.showTimerDialog(
                onConfirm = { minutes ->
                    playbackViewModel.startTimer(minutes)
                },
                onDismiss = {
                    // 用户取消
                }
            )
        }
        override fun onCancelTimer() { playbackViewModel.cancelTimer() }
        override fun onHeartMode() { navController.navigate(Routes.Player.Lyrics) }
        override fun onGeneratePlaylist(seedMusicId: Long) { playlistQueueViewModel.generatePlaylist(seedMusicId) }
        override fun onSaveDefaultConfig(algorithmType: AlgorithmType, weightTemplate: WeightTemplate, extensionConfig: ExtensionConfig) { playlistQueueViewModel.saveAlgorithmConfig(algorithmType, weightTemplate, extensionConfig) }
        override fun onArtistClick(artistName: String) { playlistViewModel.getSelectedArtistMusicList(artistName); navController.navigate(Routes.Library.Artist(artistName)) }
        override fun onClearPlaylist() { playlistQueueViewModel.clearPlaylist() }
        override fun onPlayItem(musicInfo: MusicInfo) { playlistQueueViewModel.playAt(musicInfo) }
        override fun onMoveToTop(musicInfo: MusicInfo) { playlistQueueViewModel.moveToTop(musicInfo) }
        override fun onRemoveFromPlaylist(musicInfo: MusicInfo) { playlistQueueViewModel.removeFromPlaylist(musicInfo) }
        override fun onBackClick() { navController.popBackStack() }
    }

    // 监听音乐变化并加载相关信息
    LaunchedEffect(musicInfo?.music?.id) {
        musicInfo?.music?.id?.let { id ->
            playlistQueueViewModel.getLikedStatus(id)
            playlistQueueViewModel.getMusicLabels(id)
            playlistQueueViewModel.getMusicLyrics(id)
        }
    }

    // 嵌套滚动处理
    val nestedScrollConnection = rememberPlayerScreenNestedScroll(
        dismissThreshold = dismissThreshold,
        offsetY = offsetY,
        scope = scope,
        haptic = { haptic.performLightClick() },
        onDismiss = { 
            navController.popBackStack()
            haptic.performGestureEnd()
        }
    )

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
            playerUiState = playerUiState,
            lyricsSettingsState = lyricsSettingsState,
            callbacks = playerCallbacks,
            hazeState = hazeState
        )
    }
}

