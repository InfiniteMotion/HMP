package com.hmp.desktop.ui.player.pages

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hmp.domain.config.DisplayMode
import com.hmp.domain.config.LyricsAlignment
import com.hmp.domain.enum.PlaybackMode
import com.hmp.domain.music.Music
import com.hmp.domain.music.MusicExtra
import com.hmp.domain.music.MusicInfo
import com.hmp.domain.music.UserInfo
import com.hmp.domain.playlist.AlgorithmType
import com.hmp.domain.playlist.ExtensionConfig
import com.hmp.domain.playlist.WeightTemplate
import com.hmp.desktop.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.painterResource
import com.hmp.desktop.ui.common.components.base.GeneratePlaylistComboButtons
import com.hmp.desktop.ui.common.dialogs.TimerDialog
import com.hmp.desktop.ui.common.layout.widthSizeClass
import com.hmp.desktop.ui.common.layout.WindowWidthSizeClass
import com.hmp.desktop.ui.common.util.rememberHapticFeedback
import com.hmp.desktop.ui.common.util.hazeStyleForIntensity
import com.hmp.desktop.ui.library.pages.components.AlbumCover
import com.hmp.desktop.ui.library.pages.components.musiclist.CurrentPlayingConfig
import com.hmp.desktop.ui.library.pages.components.musiclist.EditConfig
import com.hmp.desktop.ui.library.pages.components.musiclist.FullItemOptions
import com.hmp.desktop.ui.library.pages.components.musiclist.HeaderConfig
import com.hmp.desktop.ui.library.pages.components.musiclist.ItemConfig
import com.hmp.desktop.ui.library.pages.components.musiclist.ItemVariant
import com.hmp.desktop.ui.library.pages.components.musiclist.MusicList
import com.hmp.desktop.ui.library.pages.components.musiclist.MusicListCallbacksAdapter
import com.hmp.desktop.ui.library.pages.components.musiclist.defaultMusicListConfig
import com.hmp.desktop.ui.library.viewmodel.SongDetailData
import com.hmp.desktop.ui.library.viewmodel.SongDetailViewModel
import com.hmp.desktop.ui.player.viewmodel.LyricsSettingsState
import com.hmp.desktop.ui.player.viewmodel.PlayerCallbacks
import com.hmp.desktop.ui.common.util.UiState
import com.hmp.desktop.ui.player.viewmodel.PlayerUiState
import dev.chrisbanes.haze.HazeState
import org.koin.compose.koinInject
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource

// 格式化时间为 mm:ss
fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

@Composable
fun PlayContent(
    playerUiState: PlayerUiState,
    lyricsSettingsState: LyricsSettingsState,
    callbacks: PlayerCallbacks,
    hazeState: HazeState? = null,
    modifier: Modifier = Modifier
) {
    val haptic = rememberHapticFeedback()

    var showTimerDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("lyrics") }
    var contentPage by remember { mutableIntStateOf(1) }
    var slideDirection by remember { mutableIntStateOf(1) }
    var playlistExpanded by remember { mutableStateOf(false) }
    val songDetailViewModel: SongDetailViewModel = koinInject()
    val songDetailState by songDetailViewModel.uiState.collectAsState()

    val musicInfo = playerUiState.musicInfo
    val isPlaying = playerUiState.isPlaying
    val currentPosition = playerUiState.currentPosition
    val duration = playerUiState.duration
    val playbackMode = playerUiState.playbackMode
    val remainingTime = playerUiState.remainingTime
    val isLiked = playerUiState.isLiked
    val lyrics = playerUiState.lyrics
    val playlist = playerUiState.playlist
    val currentIndex = playerUiState.currentIndex
    val defaultAlgorithmType = playerUiState.defaultAlgorithmType
    val defaultTemplate = playerUiState.defaultTemplate

    // 加载歌曲详情数据（供文件信息/歌曲详情 Tab 使用）
    LaunchedEffect(musicInfo?.music?.id) {
        musicInfo?.music?.id?.let { songDetailViewModel.loadSongDetail(it) }
    }

    val lyricsOriginalTextSize = lyricsSettingsState.lyricsOriginalTextSize
    val lyricsTranslatedTextSize = lyricsSettingsState.lyricsTranslatedTextSize
    val lyricsCurrentTimeTextSize = lyricsSettingsState.lyricsCurrentTimeTextSize
    val lyricsLineSpacing = lyricsSettingsState.lyricsLineSpacing
    val lyricsDisplayMode = lyricsSettingsState.lyricsDisplayMode
    val lyricsAlignment = lyricsSettingsState.lyricsAlignment
    val lyricsKaraokeEnabled = lyricsSettingsState.lyricsKaraokeEnabled

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val sizeClass = widthSizeClass(maxWidth)
        val screenHeight = maxHeight
        val scrollState = rememberScrollState()
        val coverSize = when (sizeClass) {
            WindowWidthSizeClass.Compact -> minOf(maxWidth * 0.65f, 280.dp)
            WindowWidthSizeClass.Medium,
            WindowWidthSizeClass.Expanded -> minOf(maxWidth * 0.35f, 260.dp.coerceAtMost(maxHeight * 0.5f))
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (hazeState != null) Modifier.hazeSource(state = hazeState) else Modifier)
        ) {
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
                    when (sizeClass) {
                        WindowWidthSizeClass.Compact -> {
                            // ── Compact: 全屏展示（封面/歌词/信息三页切换 + PlaylistArea）──
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    PlayerHeader({ callbacks.onBackClick() })
                                }
                                MusicInfo(musicInfo?.music, { callbacks.onArtistClick(it) })

                                // 可切换内容区域：info+generate / 封面 / 歌词（带动画滑动切换）
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // 左箭头
                                    IconButton(
                                        onClick = {
                                            slideDirection = -1
                                            contentPage = (contentPage - 1 + 3) % 3
                                        },
                                        modifier = Modifier
                                            .align(Alignment.CenterStart)
                                            .size(36.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.chevron_left),
                                            contentDescription = "上一页",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    // 内容主体（滑动切换动画）
                                    AnimatedContent(
                                        targetState = contentPage,
                                        modifier = Modifier.padding(horizontal = 48.dp),
                                        transitionSpec = {
                                            val direction = slideDirection
                                            if (direction > 0) {
                                                (slideInHorizontally(animationSpec = tween(250)) { it } + fadeIn(animationSpec = tween(250))) togetherWith
                                                (slideOutHorizontally(animationSpec = tween(250)) { -it } + fadeOut(animationSpec = tween(250)))
                                            } else {
                                                (slideInHorizontally(animationSpec = tween(250)) { -it } + fadeIn(animationSpec = tween(250))) togetherWith
                                                (slideOutHorizontally(animationSpec = tween(250)) { it } + fadeOut(animationSpec = tween(250)))
                                            }.using(SizeTransform(clip = false))
                                        },
                                        label = "CompactContent"
                                    ) { page ->
                                        when (page) {
                                            0 -> {
                                                // 文件信息 + 推荐生成
                                                Column(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    if (musicInfo?.extra != null) {
                                                        TechnicalInfoCard(
                                                            extra = musicInfo.extra,
                                                            modifier = Modifier.padding(vertical = 8.dp)
                                                        )
                                                    }
                                                    if (musicInfo?.extra?.isGetExtraInfo == true) {
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        GeneratePlaylistComboButtons(
                                                            seedMusicId = musicInfo.music.id,
                                                            defaultAlgorithmType = defaultAlgorithmType,
                                                            defaultTemplate = defaultTemplate,
                                                            onGeneratePlaylist = { callbacks.onGeneratePlaylist(it) },
                                                            onSaveDefaultConfig = { a, b, c -> callbacks.onSaveDefaultConfig(a, b, c) }
                                                        )
                                                    }
                                                }
                                            }
                                            1 -> {
                                                // 封面（默认页）
                                                AlbumCover(
                                                    musicInfo?.music?.albumArtUri,
                                                    coverSize,
                                                    20.dp,
                                                    10.dp
                                                )
                                            }
                                            2 -> {
                                                // 歌词
                                                AdvancedLyrics(
                                                    modifier = Modifier,
                                                    lyrics = lyrics,
                                                    currentPosition = currentPosition,
                                                    onSeek = { callbacks.onSeek(it) },
                                                    originalTextSize = lyricsOriginalTextSize,
                                                    translatedTextSize = lyricsTranslatedTextSize,
                                                    currentTimeTextSize = lyricsCurrentTimeTextSize,
                                                    lineSpacing = lyricsLineSpacing,
                                                    displayMode = lyricsDisplayMode,
                                                    alignment = lyricsAlignment,
                                                    totalDurationMs = duration,
                                                    karaokeEnabled = lyricsKaraokeEnabled,
                                                    isPlaying = isPlaying
                                                )
                                            }
                                        }
                                    }

                                    // 右箭头
                                    IconButton(
                                        onClick = {
                                            slideDirection = 1
                                            contentPage = (contentPage + 1) % 3
                                        },
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .size(36.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.chevron_right),
                                            contentDescription = "下一页",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                                SeekBar(
                                    currentPosition = currentPosition,
                                    duration = duration,
                                    onSeek = { callbacks.onSeek(it) }
                                )
                                PlaybackControlsButtons(
                                    isPlaying = isPlaying,
                                    playbackMode = playbackMode,
                                    isLike = isLiked,
                                    remainingTime = remainingTime,
                                    isPlaylistSelected = playlistExpanded,
                                    onPlayPause = {
                                        haptic.performClick()
                                        callbacks.onPlayPause()
                                    },
                                    onNext = {
                                        haptic.performClick()
                                        callbacks.onNext()
                                    },
                                    onPrevious = {
                                        haptic.performClick()
                                        callbacks.onPrevious()
                                    },
                                    onPlaybackModeChange = {
                                        haptic.performContextClick()
                                        callbacks.onPlaybackModeChange()
                                    },
                                    onFavorite = {
                                        haptic.performConfirm()
                                        callbacks.onFavorite()
                                    },
                                    onTimerClick = { callbacks.onShowTimerDialog() },
                                    onHeartMode = {
                                        haptic.performConfirm()
                                        callbacks.onHeartMode()
                                    },
                                    onPlaylistToggle = {
                                        playlistExpanded = !playlistExpanded
                                    }
                                )
                            }
                        }
                        else -> {
                            // ── Medium / Expanded: 双栏布局 ──
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                // 左栏：返回按钮 + 音乐信息 + 封面 + 进度 + 控制
                                Column(
                                    modifier = Modifier
                                        .weight(0.4f)
                                        .fillMaxHeight()
                                ) {
                                    PlayerHeader({ callbacks.onBackClick() })

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 16.dp, end = 8.dp)
                                            .padding(vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                    MusicInfo(musicInfo?.music, { callbacks.onArtistClick(it) })
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Box(
                                        modifier = Modifier.weight(1f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AlbumCover(
                                            musicInfo?.music?.albumArtUri,
                                            280.dp,
                                            16.dp,
                                            8.dp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    SeekBar(
                                        currentPosition = currentPosition,
                                        duration = duration,
                                        onSeek = { callbacks.onSeek(it) }
                                    )
                                    PlaybackControlsButtons(
                                        isPlaying = isPlaying,
                                        playbackMode = playbackMode,
                                        isLike = isLiked,
                                        remainingTime = remainingTime,
                                        isPlaylistSelected = selectedTab == "playlist",
                                        onPlayPause = {
                                            haptic.performClick()
                                            callbacks.onPlayPause()
                                        },
                                        onNext = {
                                            haptic.performClick()
                                            callbacks.onNext()
                                        },
                                        onPrevious = {
                                            haptic.performClick()
                                            callbacks.onPrevious()
                                        },
                                        onPlaybackModeChange = {
                                            haptic.performContextClick()
                                            callbacks.onPlaybackModeChange()
                                        },
                                        onFavorite = {
                                            haptic.performConfirm()
                                            callbacks.onFavorite()
                                        },
                                        onTimerClick = { callbacks.onShowTimerDialog() },
                                        onHeartMode = {
                                            haptic.performConfirm()
                                            callbacks.onHeartMode()
                                        },
                                        onPlaylistToggle = {
                                            selectedTab = if (selectedTab == "playlist") "lyrics" else "playlist"
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                } // 内层 Column 结束
                            } // 左栏 Column 结束

                            // 分隔线
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .fillMaxHeight()
                                        .background(MaterialTheme.colorScheme.outlineVariant)
                                )

                                // 右栏：可切换内容 + 底部胶囊 Tab Bar
                                Column(
                                    modifier = Modifier
                                        .weight(0.6f)
                                        .fillMaxHeight()
                                        .padding(start = 16.dp, end = 24.dp)
                                        .padding(vertical = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // 内容区域
                                    Box(modifier = Modifier.weight(1f)) {
                                        when (selectedTab) {
                                            "playlist" -> PlaylistTabContent(
                                                playlist = playlist,
                                                currentIndex = currentIndex,
                                                seedMusicId = musicInfo?.music?.id,
                                                defaultAlgorithmType = defaultAlgorithmType,
                                                defaultTemplate = defaultTemplate,
                                                onGeneratePlaylist = { callbacks.onGeneratePlaylist(it) },
                                                onSaveDefaultConfig = { a, b, c -> callbacks.onSaveDefaultConfig(a, b, c) },
                                                onPlayItem = { callbacks.onPlayItem(it) },
                                                onMoveToTop = { callbacks.onMoveToTop(it) },
                                                onRemoveFromPlaylist = { callbacks.onRemoveFromPlaylist(it) },
                                                onClearPlaylist = { callbacks.onClearPlaylist() }
                                            )
                                            "info" -> SongDetailInfoTab(
                                                songDetailState = songDetailState,
                                                musicExtra = musicInfo?.extra,
                                                userInfo = musicInfo?.userInfo
                                            )
                                            else -> AdvancedLyrics(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(vertical = 16.dp),
                                                lyrics = lyrics,
                                                currentPosition = currentPosition,
                                                onSeek = { callbacks.onSeek(it) },
                                                originalTextSize = lyricsOriginalTextSize,
                                                translatedTextSize = lyricsTranslatedTextSize,
                                                currentTimeTextSize = lyricsCurrentTimeTextSize,
                                                lineSpacing = lyricsLineSpacing,
                                                displayMode = lyricsDisplayMode,
                                                alignment = lyricsAlignment,
                                                totalDurationMs = duration,
                                                karaokeEnabled = lyricsKaraokeEnabled,
                                                isPlaying = isPlaying
                                            )
                                        }

                                        // 悬浮底部胶囊 Tab Bar
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .padding(bottom = 8.dp)
                                        ) {
                                            PlayerTabBar(
                                                selectedTab = selectedTab,
                                                onTabSelected = { selectedTab = it },
                                                hazeState = hazeState
                                            )
                                        }
                                    }
                                }
                            } // Row end
                        }
                    }
                }
                // Compact 模式下播放列表区域（展开/收起动画）
                if (sizeClass == WindowWidthSizeClass.Compact) {
                    PlaylistArea(
                        expanded = playlistExpanded,
                        playlist = playlist,
                        currentIndex = currentIndex,
                        scrollState = scrollState,
                        onClearPlaylist = { callbacks.onClearPlaylist() },
                        onPlayItem = { callbacks.onPlayItem(it) },
                        onMoveToTop = { callbacks.onMoveToTop(it) },
                        onRemoveFromPlaylist = { callbacks.onRemoveFromPlaylist(it) }
                    )
                }
            }
        }
        if (showTimerDialog) {
            TimerDialog(
                onDismiss = { showTimerDialog = false },
                onConfirm = { minutes: Int ->
                    if (minutes == 0) {
                        callbacks.onCancelTimer()
                    } else {
                        callbacks.onTimerClick(minutes)
                    }
                    showTimerDialog = false
                },
                hazeState = hazeState
            )
        }
    }
}

// ── 右栏各标签页内容 ──

@Composable
private fun PlaylistTabContent(
    playlist: List<MusicInfo>,
    currentIndex: Int,
    seedMusicId: Long? = null,
    defaultAlgorithmType: AlgorithmType? = null,
    defaultTemplate: WeightTemplate? = null,
    onGeneratePlaylist: ((Long) -> Unit)? = null,
    onSaveDefaultConfig: ((AlgorithmType, WeightTemplate, ExtensionConfig) -> Unit)? = null,
    onPlayItem: (MusicInfo) -> Unit,
    onMoveToTop: (MusicInfo) -> Unit,
    onRemoveFromPlaylist: (MusicInfo) -> Unit,
    onClearPlaylist: () -> Unit
) {
    val haptic = rememberHapticFeedback()

    Column(
        modifier = Modifier.fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 16.dp)
    ) {
        // 推荐生成区域（在卡片外部）
        if (seedMusicId != null && onGeneratePlaylist != null) {
            GeneratePlaylistComboButtons(
                seedMusicId = seedMusicId,
                defaultAlgorithmType = defaultAlgorithmType,
                defaultTemplate = defaultTemplate,
                onGeneratePlaylist = onGeneratePlaylist,
                onSaveDefaultConfig = onSaveDefaultConfig ?: { _, _, _ -> },
                horizontalLayout = true
            )
        }

        Spacer(Modifier.height(16.dp))

        // 播放列表卡片（无标题，统一风格）
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            color = Transparent
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = { haptic.performLightClick(); onClearPlaylist() }) {
                        Text(
                            text = stringResource(Res.string.clear),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text(
                        text = "${currentIndex + 1}/${playlist.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }

                val callbacks = object : MusicListCallbacksAdapter() {
                    override fun onItemClick(musicInfo: MusicInfo, index: Int) {
                        haptic.performClick()
                        onPlayItem(musicInfo)
                    }
                    override fun onPinToTop(musicInfo: MusicInfo) {
                        haptic.performConfirm()
                        onMoveToTop(musicInfo)
                    }
                    override fun onRemove(musicInfo: MusicInfo) {
                        haptic.performLightClick()
                        onRemoveFromPlaylist(musicInfo)
                    }
                }
                val config = defaultMusicListConfig(callbacks).copy(
                    header = HeaderConfig.None,
                    item = ItemConfig(
                        showIndex = true,
                        variant = ItemVariant.Full,
                        fullOptions = FullItemOptions(
                            showPinButton = true,
                            showRemoveButton = true,
                            showMenuButton = false,
                        ),
                    ),
                    edit = EditConfig(enabled = false),
                    currentPlaying = CurrentPlayingConfig(
                        index = currentIndex.takeIf { it in playlist.indices },
                        autoScrollToCurrent = true,
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                )
                MusicList(
                    musicInfoList = playlist,
                    config = config,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    isPlaying = true,
                )
            }
        }
    }
}

// ── 文件信息/歌曲详情 Tab ──

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SongDetailInfoTab(
    songDetailState: UiState<SongDetailData>,
    musicExtra: MusicExtra?,
    userInfo: UserInfo?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 技术信息卡片
        TechnicalInfoCard(extra = musicExtra, modifier = Modifier.fillMaxWidth())

        if (songDetailState is UiState.Success) {
            val data = songDetailState.data
            val dailyInfo = data.dailyMusicInfo

            // 相关信息卡片（AI 内容）
            if (dailyInfo != null && (
                    (dailyInfo.backgroundIntroduce.isNotBlank() && dailyInfo.backgroundIntroduce != "None") ||
                    (dailyInfo.description.isNotBlank() && dailyInfo.description != "None") ||
                    (dailyInfo.singerIntroduce.isNotBlank() && dailyInfo.singerIntroduce != "None") ||
                    (dailyInfo.rewards.isNotBlank() && dailyInfo.rewards != "None")
                )
            ) {
                InfoCard(title = "相关信息") {
                    if (dailyInfo.backgroundIntroduce.isNotBlank() && dailyInfo.backgroundIntroduce != "None") {
                        InfoRow(label = stringResource(Res.string.creative_background), value = dailyInfo.backgroundIntroduce)
                    }
                    if (dailyInfo.description.isNotBlank() && dailyInfo.description != "None") {
                        InfoRow(label = stringResource(Res.string.song_description), value = dailyInfo.description)
                    }
                    if (dailyInfo.singerIntroduce.isNotBlank() && dailyInfo.singerIntroduce != "None") {
                        InfoRow(label = stringResource(Res.string.artist_introduction), value = dailyInfo.singerIntroduce)
                    }
                    if (dailyInfo.rewards.isNotBlank() && dailyInfo.rewards != "None") {
                        InfoRow(label = stringResource(Res.string.song_achievements), value = dailyInfo.rewards)
                    }
                }
            }

            // 用户信息卡片
            if (userInfo != null) {
                InfoCard(title = stringResource(Res.string.personal_stats)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatCell(stringResource(Res.string.sort_play_count), userInfo.playCount?.toString() ?: "0", Modifier.weight(1f))
                        StatCell(stringResource(Res.string.skipped_count), userInfo.skippedCount?.toString() ?: "0", Modifier.weight(1f))
                        StatCell(stringResource(Res.string.playlist_count), userInfo.inCustomPlaylistCount?.toString() ?: "0", Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatCell(stringResource(Res.string.user_rating), userInfo.userRating?.toString() ?: "0", Modifier.weight(1f))
                        StatCell(stringResource(Res.string.liked_status), if (userInfo.liked) "♥" else "♡", Modifier.weight(1f))
                    }
                }
            }

            // 标签卡片
            val validLabels = data.labels.filterNotNull().filter { it.label.name.isNotBlank() }
            if (validLabels.isNotEmpty()) {
                InfoCard(title = stringResource(Res.string.labels)) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        validLabels.forEach { label ->
                            AssistChip(
                                onClick = { },
                                label = { Text(label.label.name, style = MaterialTheme.typography.labelSmall) },
                                border = null
                            )
                        }
                    }
                }
            }
        } else if (songDetailState is UiState.Loading) {
            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        }
    }
}

// ── 统一信息卡片样式（与 TechnicalInfoCard 一致）──

@Composable
private fun InfoCard(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        color = androidx.compose.ui.graphics.Color.Companion.Transparent
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatCell(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 32.dp)
    ) {
        Text(
            text = music?.title ?: "Music Title",
            maxLines = 1,
            overflow = TextOverflow.MiddleEllipsis,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
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
                isSeeking = true
                haptic.performDragStart()
                sliderPosition = newValue
            },
            onValueChangeFinished = {
                haptic.performGestureEnd()
                val seekPosition = (sliderPosition * duration).toLong()
                onSeek(seekPosition)
                isSeeking = false
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
fun PlaybackControlsButtons(
    isPlaying: Boolean,
    playbackMode: PlaybackMode,
    isLike: Boolean,
    remainingTime: Long?,
    isPlaylistSelected: Boolean,
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
                    painter = painterResource(Res.drawable.backward_end_fill),
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = stringResource(Res.string.previous),
                )
            }

            IconButton(
                onClick = onPlayPause,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    painter = painterResource(if (isPlaying) Res.drawable.`pause` else Res.drawable.play_fill),
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = stringResource(Res.string.play_pause),
                )
            }

            IconButton(
                onClick = onNext,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.forward_end_fill),
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = stringResource(Res.string.next),
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
                            PlaybackMode.SHUFFLE -> Res.drawable.`shuffle`
                            PlaybackMode.REPEAT_ONE -> Res.drawable.repeat_1
                            PlaybackMode.SEQUENTIAL -> Res.drawable.repeat
                        }
                    ),
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = stringResource(Res.string.playback_mode),
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
                    painter = painterResource(if (isLiked) Res.drawable.heart_fill else Res.drawable.heart),
                    contentDescription = stringResource(Res.string.favorite),
                    tint = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
            }
            IconButton(onClick = onHeartMode) {
                Icon(
                    painter = painterResource(Res.drawable.identify_song),
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = stringResource(Res.string.recommendation_mode),
                )
            }
            if (remainingTime == null) {
                IconButton(onClick = { onTimerClick() }) {
                    Icon(
                        painter = painterResource(Res.drawable.`timer`),
                        tint = MaterialTheme.colorScheme.onSurface,
                        contentDescription = stringResource(Res.string.sleep_timer),
                    )
                }
            } else {
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
                    tint = if (isPlaylistSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    painter = painterResource(
                        if (isPlaylistSelected) Res.drawable.chevron_up_circle else Res.drawable.music_note_list
                    ),
                    contentDescription = stringResource(Res.string.playlist),
                )
            }
        }
    }
}

// ── 底部胶囊 Tab Bar（与 BottomFusionBar 一致风格）──

private val tabOptions = listOf(
    "lyrics" to "歌词",
    "playlist" to "播放列表",
    "info" to "文件信息"
)

@Composable
private fun PlayerTabBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    hazeState: HazeState?
) {
    Card(
        shape = RoundedCornerShape(36.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hazeState != null) {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.14f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(36.dp))
            .then(
                if (hazeState != null) Modifier.hazeEffect(
                    state = hazeState,
                    style = hazeStyleForIntensity()
                ) else Modifier
            )
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            tabOptions.forEach { (id, label) ->
                val isSelected = id == selectedTab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else Color.Transparent
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTabSelected(id) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
