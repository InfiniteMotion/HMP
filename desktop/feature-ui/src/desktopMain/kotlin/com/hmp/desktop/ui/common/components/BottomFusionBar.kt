package com.hmp.desktop.ui.common.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hmp.desktop.generated.resources.Res
import com.hmp.desktop.generated.resources.backward_end_fill
import com.hmp.desktop.generated.resources.forward_end_fill
import com.hmp.desktop.generated.resources.pause
import com.hmp.desktop.generated.resources.play_fill
import com.hmp.desktop.ui.common.util.HapticFeedbackHelper
import com.hmp.desktop.ui.common.util.hazeStyleForIntensity
import com.hmp.desktop.ui.common.util.hazeTintAlpha
import com.hmp.desktop.ui.library.pages.components.AlbumCover
import com.hmp.domain.music.MusicInfo
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

/** 底部融合栏状态 */
enum class FusionBarState {
    /** 默认：左侧导航展开（4 Tab），右侧播放折叠（仅圆形封面） */
    NavigationExpanded,
    /** 左侧导航折叠（单图标），右侧播放展开（封面 + 信息 + 控制） */
    PlaybackExpanded
}

private data class BottomTabItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val bottomTabs = listOf(
    BottomTabItem("首页", Icons.Filled.Home, Icons.Outlined.Home),
    BottomTabItem("封面", Icons.Filled.Image, Icons.Outlined.Image),
    BottomTabItem("列表", Icons.AutoMirrored.Filled.List, Icons.AutoMirrored.Outlined.List),
    BottomTabItem("我的", Icons.Filled.Person, Icons.Outlined.Person)
)

@Composable
fun BottomFusionBar(
    musicInfo: MusicInfo?,
    isPlaying: Boolean,
    progress: Float,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onOpenPlayer: () -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    showNavText: Boolean = true,
    maxWidth: Dp? = null
) {
    val haptic = remember { HapticFeedbackHelper() }
    var fusionState by remember { mutableStateOf(FusionBarState.NavigationExpanded) }
    var timerKey by remember { mutableIntStateOf(0) }
    val hasMusic = musicInfo != null

    // 无音乐时，强制回到导航展开态
    LaunchedEffect(hasMusic) {
        if (!hasMusic) {
            fusionState = FusionBarState.NavigationExpanded
        }
    }

    // 播放展开态 5 秒无操作自动回到默认态
    LaunchedEffect(fusionState, timerKey) {
        if (fusionState == FusionBarState.PlaybackExpanded) {
            delay(5_000)
            fusionState = FusionBarState.NavigationExpanded
        }
    }

    val resetTimer: () -> Unit = { timerKey++ }

    val transitionSpec: AnimatedContentTransitionScope<FusionBarState>.() -> ContentTransform = {
        (
            fadeIn(animationSpec = tween(200)) +
            scaleIn(initialScale = 0.92f, animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f))
        ).togetherWith(
            fadeOut(animationSpec = tween(150)) +
            scaleOut(targetScale = 0.92f, animationSpec = tween(150))
        ).using(SizeTransform(clip = false))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (maxWidth != null) Modifier.widthIn(max = maxWidth) else Modifier)
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── 左侧胶囊：导航 ──
        val capsuleShape = RoundedCornerShape(36.dp)
        Card(
            shape = capsuleShape,
            colors = CardDefaults.cardColors(
                containerColor = if (hazeState != null) {
                    MaterialTheme.colorScheme.surface.copy(alpha = hazeTintAlpha())
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
                .clip(capsuleShape)
                .then(
                    // 折叠态：点击整个胶囊回到展开态
                    if (fusionState == FusionBarState.PlaybackExpanded) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            haptic.performLightClick()
                            fusionState = FusionBarState.NavigationExpanded
                            resetTimer()
                        }
                    } else Modifier
                )
                .then(
                    if (hazeState != null) Modifier.hazeEffect(
                        state = hazeState,
                        style = hazeStyleForIntensity()
                    ) else Modifier
                )
        ) {
            AnimatedContent(
                targetState = fusionState,
                transitionSpec = transitionSpec,
                label = "NavCapsule"
            ) { state ->
                when (state) {
                    FusionBarState.NavigationExpanded ->
                        NavigationExpandedContent(selectedTabIndex, onTabSelected, haptic, showNavText)
                    FusionBarState.PlaybackExpanded ->
                        NavigationCollapsedContent(
                            selectedTab = bottomTabs[selectedTabIndex]
                        )
                }
            }
        }

        // ── 右侧胶囊：播放控制（仅在有音乐时显示）──
        if (hasMusic) {
            Card(
                shape = capsuleShape,
                colors = CardDefaults.cardColors(
                    containerColor = if (hazeState != null) {
                        MaterialTheme.colorScheme.surface.copy(alpha = hazeTintAlpha())
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
                    .clip(capsuleShape)
                    .then(
                        // 折叠态：点击整个胶囊展开
                        if (fusionState == FusionBarState.NavigationExpanded) {
                            Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                haptic.performLightClick()
                                fusionState = FusionBarState.PlaybackExpanded
                                resetTimer()
                            }
                        } else Modifier
                    )
                    .then(
                        if (hazeState != null) Modifier.hazeEffect(
                            state = hazeState,
                            style = hazeStyleForIntensity()
                        ) else Modifier
                    )
            ) {
                AnimatedContent(
                    targetState = fusionState,
                    transitionSpec = transitionSpec,
                    label = "PlaybackCapsule"
                ) { state ->
                    when (state) {
                        FusionBarState.NavigationExpanded ->
                            PlaybackCollapsedContent(
                                musicInfo = musicInfo,
                                isPlaying = isPlaying
                            )
                        FusionBarState.PlaybackExpanded ->
                            PlaybackExpandedContent(
                                musicInfo = musicInfo,
                                isPlaying = isPlaying,
                                progress = progress,
                                onPlayPause = {
                                    resetTimer()
                                    onPlayPause()
                                },
                                onPrev = {
                                    resetTimer()
                                    onPrev()
                                },
                                onNext = {
                                    resetTimer()
                                    onNext()
                                },
                                onOpenPlayer = {
                                    resetTimer()
                                    onOpenPlayer()
                                },
                                haptic = haptic
                            )
                    }
                }
            }
        }
    }
}

// ── 导航展开内容 ──

@Composable
private fun NavigationExpandedContent(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    haptic: HapticFeedbackHelper,
    showNavText: Boolean = true
) {
    val density = LocalDensity.current
    val glowColor = MaterialTheme.colorScheme.primary
    var boxCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var tabBounds by remember { mutableStateOf<Map<Int, Rect>>(emptyMap()) }
    val indicatorLeft = remember { Animatable(0f) }
    val indicatorWidth = remember { Animatable(0f) }
    var indicatorReady by remember { mutableStateOf(false) }

    LaunchedEffect(selectedIndex, tabBounds[selectedIndex]) {
        val bounds = tabBounds[selectedIndex] ?: return@LaunchedEffect
        if (!indicatorReady) {
            indicatorLeft.snapTo(bounds.left)
            indicatorWidth.snapTo(bounds.width)
            indicatorReady = true
        } else {
            launch {
                indicatorLeft.animateTo(
                    bounds.left,
                    animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f)
                )
            }
            launch {
                indicatorWidth.animateTo(
                    bounds.width,
                    animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f)
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 10.dp)
            .onGloballyPositioned { boxCoords = it },
        contentAlignment = Alignment.CenterStart
    ) {
        // 滑动选中背景胶囊（shadow 边缘模糊）
        if (indicatorReady) {
            val offsetXDp = with(density) { indicatorLeft.value.toDp() }
            val widthDp = with(density) { indicatorWidth.value.toDp() }
            val heightDp = tabBounds[selectedIndex]?.let {
                with(density) { it.height.toDp() }
            } ?: 40.dp

            Box(
                modifier = Modifier
                    .offset(x = offsetXDp)
                    .size(width = widthDp, height = heightDp)
                    .shadow(12.dp, RoundedCornerShape(28.dp), clip = false,
                        spotColor = glowColor, ambientColor = glowColor.copy(alpha = 0.5f))
                    .background(glowColor.copy(alpha = 0.35f), RoundedCornerShape(28.dp))
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomTabs.forEachIndexed { index, tab ->
                val isSelected = index == selectedIndex
                val contentColor = MaterialTheme.colorScheme.onSurface

                Row(
                    modifier = Modifier
                        .onGloballyPositioned { tabCoords ->
                            val bc = boxCoords ?: return@onGloballyPositioned
                            val pos = bc.localPositionOf(tabCoords, Offset.Zero)
                            val sz = tabCoords.size
                            tabBounds = tabBounds + (index to Rect(
                                left = pos.x,
                                top = pos.y,
                                right = pos.x + sz.width,
                                bottom = pos.y + sz.height
                            ))
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            haptic.performLightClick()
                            onTabSelected(index)
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.label,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    if (showNavText) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = contentColor
                        )
                    }
                }
            }
        }
    }
}

// ── 导航折叠内容 ──

@Composable
private fun NavigationCollapsedContent(
    selectedTab: BottomTabItem
) {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .size(44.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = selectedTab.selectedIcon,
            contentDescription = selectedTab.label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
    }
}

// ── 播放折叠内容（仅圆形封面）──

@Composable
private fun PlaybackCollapsedContent(
    musicInfo: MusicInfo,
    isPlaying: Boolean
) {
    val coverRotation = remember { Animatable(0f) }

    LaunchedEffect(isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        while (true) {
            val current = coverRotation.value % 360f
            val remaining = 360f - current
            val durationMillis = ((remaining / 360f) * 8000f).toInt().coerceAtLeast(1)
            coverRotation.animateTo(
                targetValue = 360f,
                animationSpec = tween(durationMillis = durationMillis, easing = LinearEasing)
            )
            coverRotation.snapTo(0f)
        }
    }

    Box(
        modifier = Modifier
            .padding(8.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        AlbumCover(
            uri = musicInfo.music.albumArtUri,
            size = 44.dp,
            corner = 22.dp,
            shadow = 3.dp,
            modifier = Modifier.graphicsLayer {
                rotationZ = coverRotation.value
            }
        )
    }
}

// ── 播放展开内容 ──

@Composable
private fun PlaybackExpandedContent(
    musicInfo: MusicInfo,
    isPlaying: Boolean,
    progress: Float,
    onPlayPause: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onOpenPlayer: () -> Unit,
    haptic: HapticFeedbackHelper
) {
    val coverRotation = remember { Animatable(0f) }

    LaunchedEffect(isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        while (true) {
            val current = coverRotation.value % 360f
            val remaining = 360f - current
            val durationMillis = ((remaining / 360f) * 8000f).toInt().coerceAtLeast(1)
            coverRotation.animateTo(
                targetValue = 360f,
                animationSpec = tween(durationMillis = durationMillis, easing = LinearEasing)
            )
            coverRotation.snapTo(0f)
        }
    }

    Row(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                haptic.performLightClick()
                onOpenPlayer()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 封面
        AlbumCover(
            uri = musicInfo.music.albumArtUri,
            size = 48.dp,
            corner = 24.dp,
            shadow = 4.dp,
            modifier = Modifier.graphicsLayer {
                rotationZ = coverRotation.value
            }
        )

        Spacer(modifier = Modifier.width(12.dp))

        // 歌名 + 歌手
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(
                text = musicInfo.music.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = musicInfo.music.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 播放控制
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = {
                    haptic.performLightClick()
                    onPrev()
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.backward_end_fill),
                    contentDescription = "Previous",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = {
                    haptic.performLightClick()
                    onPlayPause()
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = if (isPlaying) {
                        painterResource(Res.drawable.`pause`)
                    } else {
                        painterResource(Res.drawable.play_fill)
                    },
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(
                onClick = {
                    haptic.performLightClick()
                    onNext()
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.forward_end_fill),
                    contentDescription = "Next",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
