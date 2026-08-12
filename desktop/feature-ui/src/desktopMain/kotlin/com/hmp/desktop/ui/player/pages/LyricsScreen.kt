package com.hmp.desktop.ui.player.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import com.hmp.domain.config.DisplayMode
import com.hmp.domain.config.LyricsAlignment
import com.hmp.desktop.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.painterResource
import com.hmp.desktop.ui.common.components.SegmentedOption
import com.hmp.desktop.ui.common.components.VerticalSegmentedControl
import com.hmp.desktop.ui.common.viewmodel.ThemeViewModel
import com.hmp.desktop.ui.common.util.rememberHapticFeedback
import com.hmp.desktop.ui.player.viewmodel.PlaybackViewModel
import com.hmp.desktop.ui.player.viewmodel.PlaylistQueueViewModel
import com.hmp.desktop.ui.settings.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay

/**
 * 独立歌词页面
 * 提供全屏歌词展示和实时参数调节功能
 */
@Composable
fun LyricsScreen(
    playbackViewModel: PlaybackViewModel = koinInject(),
    playlistQueueViewModel: PlaylistQueueViewModel = koinInject(),
    settingsViewModel: SettingsViewModel = koinInject(),
    navController: com.hmp.desktop.ui.common.navigation.NavController? = null
) {
    val lyrics by playlistQueueViewModel.currentMusicLyrics.collectAsState()
    val currentPosition by playbackViewModel.currentPosition.collectAsState()
    val duration by playbackViewModel.duration.collectAsState()
    val isPlaying by playbackViewModel.isPlaying.collectAsState()

    // 歌词参数 - 从设置中获取
    val originalTextSize by settingsViewModel.lyricsOriginalTextSize.collectAsState()
    val translatedTextSize by settingsViewModel.lyricsTranslatedTextSize.collectAsState()
    val currentTimeTextSize by settingsViewModel.lyricsCurrentTimeTextSize.collectAsState()
    val lineSpacing by settingsViewModel.lyricsLineSpacing.collectAsState()
    val displayMode by settingsViewModel.lyricsDisplayMode.collectAsState()
    val alignment by settingsViewModel.lyricsAlignment.collectAsState()
    val karaokeEnabled by settingsViewModel.lyricsKaraokeEnabled.collectAsState()
    val themeViewModel: ThemeViewModel = koinInject()
    val paletteColors by themeViewModel.paletteColors.collectAsState()

    var isSettingsPanelVisible by remember { mutableStateOf(false) }
    val haptic = rememberHapticFeedback()

    // 统一悬浮控制：鼠标在页面上时显示，离开 3 秒后隐藏
    val hoverInteractionSource = remember { MutableInteractionSource() }
    val isHovered by hoverInteractionSource.collectIsHoveredAsState()
    var areControlsVisible by remember { mutableStateOf(true) }

    LaunchedEffect(isHovered) {
        if (!isHovered) {
            delay(3000L)
            areControlsVisible = false
        } else {
            areControlsVisible = true
        }
    }

    // 设置面板打开时始终显示控件
    LaunchedEffect(isSettingsPanelVisible) {
        if (isSettingsPanelVisible) {
            areControlsVisible = true
        }
    }

    // 开启播放进度跟踪
    DisposableEffect(Unit) {
        playbackViewModel.startProgressTracking()
        onDispose {
            playbackViewModel.setMiniPlayerVisible(true)
        }
    }

    // 同步控制 MiniPlayerBar 的显示隐藏
    LaunchedEffect(areControlsVisible) {
        playbackViewModel.setMiniPlayerVisible(areControlsVisible)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .hoverable(hoverInteractionSource)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent()
                        areControlsVisible = true
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 歌词展示区域
            AdvancedLyrics(
                modifier = Modifier.weight(1f),
                lyrics = lyrics,
                currentPosition = currentPosition,
                onSeek = {
                    areControlsVisible = true
                    playbackViewModel.seekTo(it)
                },
                originalTextSize = originalTextSize,
                translatedTextSize = translatedTextSize,
                currentTimeTextSize = currentTimeTextSize,
                lineSpacing = lineSpacing,
                displayMode = displayMode,
                alignment = alignment,
                totalDurationMs = duration,
                karaokeEnabled = karaokeEnabled,
                paletteColors = paletteColors,
                isPlaying = isPlaying
            )
        }
        // 悬浮设置面板（右下角，控制卡片上方）
        AnimatedVisibility(
            visible = isSettingsPanelVisible,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 72.dp, end = 16.dp),
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                border = BorderStroke(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.14f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.size(width = 400.dp, height = 216.dp)
            ) {
                LyricsSettingsPanel(
                    originalTextSize = originalTextSize,
                    translatedTextSize = translatedTextSize,
                    currentTimeTextSize = currentTimeTextSize,
                    lineSpacing = lineSpacing,
                    displayMode = displayMode,
                    alignment = alignment,
                    karaokeEnabled = karaokeEnabled,
                    onKaraokeEnabledChange = { settingsViewModel.saveLyricsKaraokeEnabled(it) },
                    onOriginalTextSizeChange = { settingsViewModel.saveLyricsOriginalTextSize(it) },
                    onTranslatedTextSizeChange = { settingsViewModel.saveLyricsTranslatedTextSize(it) },
                    onCurrentTimeTextSizeChange = { settingsViewModel.saveLyricsCurrentTimeTextSize(it) },
                    onLineSpacingChange = { settingsViewModel.saveLyricsLineSpacing(it) },
                    onDisplayModeChange = { settingsViewModel.saveLyricsDisplayMode(it) },
                    onAlignmentChange = { settingsViewModel.saveLyricsAlignment(it) }
                )
            }
        }

        // 悬浮控制卡片（右下角）
        AnimatedVisibility(
            visible = areControlsVisible,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp),
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it }
        ) {
            Card(
                shape = RoundedCornerShape(36.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ),
                border = BorderStroke(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.14f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = { navController?.popBackStack() },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.chevron_left),
                            tint = MaterialTheme.colorScheme.onSurface,
                            contentDescription = stringResource(Res.string.back),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    IconButton(
                        onClick = {
                            haptic.performLightClick()
                            isSettingsPanelVisible = !isSettingsPanelVisible
                        },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_gallery_search_things),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            contentDescription = "设置",
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}

/**
 * 歌词参数设置面板
 */
@Composable
private fun LyricsSettingsPanel(
    originalTextSize: Int,
    translatedTextSize: Int,
    currentTimeTextSize: Int,
    lineSpacing: Int,
    displayMode: DisplayMode,
    alignment: LyricsAlignment,
    karaokeEnabled: Boolean,
    onKaraokeEnabledChange: (Boolean) -> Unit,
    onOriginalTextSizeChange: (Int) -> Unit,
    onTranslatedTextSizeChange: (Int) -> Unit,
    onCurrentTimeTextSizeChange: (Int) -> Unit,
    onLineSpacingChange: (Int) -> Unit,
    onDisplayModeChange: (DisplayMode) -> Unit,
    onAlignmentChange: (LyricsAlignment) -> Unit
) {
    val haptic = rememberHapticFeedback()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
        VerticalSegmentedControl(
            modifier = Modifier.weight(1f),
            options = listOf(
                SegmentedOption(id = "LANG1", label = "原文", icon = painterResource(Res.drawable.translate_c2e)),
                SegmentedOption(id = "LANG2", label = "译文", icon = painterResource(Res.drawable.translate_e2c)),
                SegmentedOption(id = "DUAL", label = "双语", icon = painterResource(Res.drawable.translate))
            ),
            selectedOption = displayMode.name,
            onOptionSelected = { name ->
                haptic.performLightClick()
                onDisplayModeChange(DisplayMode.valueOf(name))
            }
        )
        VerticalSegmentedControl(
            modifier = Modifier.weight(1f),
            options = listOf(
                SegmentedOption(id = "LEFT", label = "左对齐", icon = painterResource(Res.drawable.text_alignleft)),
                SegmentedOption(id = "CENTER", label = "居中", icon = painterResource(Res.drawable.text_aligncenter)),
                SegmentedOption(id = "RIGHT", label = "右对齐", icon = painterResource(Res.drawable.text_alignright))
            ),
            selectedOption = alignment.name,
            onOptionSelected = { name ->
                haptic.performLightClick()
                onAlignmentChange(LyricsAlignment.valueOf(name))
            }
        )
        if(displayMode == DisplayMode.DUAL || displayMode == DisplayMode.LANG1){
            // 原文字体大小
            SizeControl(
                modifier = Modifier.weight(1f),
                label = "原 文",
                value = originalTextSize,
                minValue = 10,
                maxValue = 28,
                onValueChange = onOriginalTextSizeChange
            )
        }
        if(displayMode == DisplayMode.DUAL || displayMode == DisplayMode.LANG2){
            // 译文字体大小
            SizeControl(
                modifier = Modifier.weight(1f),
                label = "译 文",
                value = translatedTextSize,
                minValue = 10,
                maxValue = 28,
                onValueChange = onTranslatedTextSizeChange
            )
        }
        // 当前行字体大小
        SizeControl(
            modifier = Modifier.weight(1f),
            label = "强调行",
            value = currentTimeTextSize,
            minValue = 14,
            maxValue = 32,
            onValueChange = onCurrentTimeTextSizeChange
        )
        // 行间距
        SizeControl(
            modifier = Modifier.weight(1f),
            label = "行间距",
            value = lineSpacing,
            minValue = 2,
            maxValue = 20,
            onValueChange = onLineSpacingChange
        )

        }
        // 逐字显示开关
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "逐字显示",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked = karaokeEnabled,
                    onCheckedChange = onKaraokeEnabledChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                )
            }
        }
        }
    }
}

/**
 * 数字调节块组件
 * 包含属性标题和调节块，后者包括当前值与 up/down 按钮
 */
@Composable
private fun SizeControl(
    modifier: Modifier = Modifier,
    label: String,
    value: Int,
    minValue: Int,
    maxValue: Int,
    onValueChange: (Int) -> Unit
) {
    val haptic = rememberHapticFeedback()
    Column(
        modifier = modifier
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), shape = RoundedCornerShape(16.dp)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp) // 按钮整体尺寸
                    .clip(RoundedCornerShape(6.dp)) // 圆角设为尺寸的一半，形成正圆
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp) // 边框圆角与外层保持一致
                    )
                    .clickable {
                            haptic.performLightClick()
                            if (value < maxValue) {
                                onValueChange(value + 1)
                            }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.plus),
                    contentDescription = "增大",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp) // 图标尺寸适配按钮
                )
            }
            Column (
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Text(
                    text = label.toCharArray().joinToString("\n"),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .size(24.dp) // 按钮整体尺寸
                    .clip(RoundedCornerShape(6.dp)) // 圆角设为尺寸的一半，形成正圆
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp) // 边框圆角与外层保持一致
                    )
                    .clickable {
                            haptic.performLightClick()
                            if (value > minValue) {
                                onValueChange(value - 1)
                            }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.minus),
                    contentDescription = "减小",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp) // 图标尺寸适配按钮
                )
            }
        }
    }
}
