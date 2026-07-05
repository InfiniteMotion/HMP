package com.hearablemusic.player.ui.common.components

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hearablemusic.player.ui.R
import com.hearablemusic.player.ui.common.util.HapticFeedbackHelper
import com.hearablemusic.player.ui.common.util.hazeStyleForIntensity
import com.hearablemusic.player.ui.common.util.hazeTintAlpha
import com.hearablemusic.player.ui.common.util.rememberHapticFeedback
import com.hearablemusic.player.ui.library.pages.components.AlbumCover
import com.hmp.domain.music.MusicInfo
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.delay
import kotlin.math.pow

/** 底部融合栏状态 */
enum class FusionBarState {
    /** 默认：左侧导航展开（4 Tab），右侧播放折叠（仅圆形封面） */
    NavigationExpanded,
    /** 左侧导航折叠（单图标），右侧播放展开（封面 + 信息 + 控制） */
    PlaybackExpanded
}

private data class BottomTabItem(
    val label: String,
    val selectedIconId: Int,
    val unselectedIconId: Int
)

private val bottomTabs = listOf(
    BottomTabItem("首页", R.drawable.house_fill, R.drawable.house),
    BottomTabItem("封面", R.drawable.square_fill_grid_2x2, R.drawable.square_grid_2x2),
    BottomTabItem("列表", R.drawable.list_bullet, R.drawable.list_bullet),
    BottomTabItem("我的", R.drawable.person_filled_viewfinder, R.drawable.person)
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
    showNavCapsule: Boolean = true,
    maxWidth: Dp? = null
) {
    val haptic = rememberHapticFeedback()
    var initialFusionState = if (showNavCapsule) FusionBarState.NavigationExpanded else FusionBarState.PlaybackExpanded
    var fusionState by remember { mutableStateOf(initialFusionState) }
    var timerKey by remember { mutableIntStateOf(0) }
    val hasMusic = musicInfo != null

    // 胶囊数量或播放状态变化时立即切换
    LaunchedEffect(showNavCapsule, hasMusic, isPlaying) {
        fusionState = if (!showNavCapsule || (hasMusic && isPlaying)) {
            FusionBarState.PlaybackExpanded
        } else {
            FusionBarState.NavigationExpanded
        }
        if (showNavCapsule && hasMusic) timerKey++
    }

    // 切换 Tab 页面时展开导航胶囊（仅双胶囊模式）
    LaunchedEffect(selectedTabIndex) {
        if (showNavCapsule) {
            fusionState = FusionBarState.NavigationExpanded
        }
    }

    // 用户手动交互后 5 秒无操作回到默认态（仅双胶囊模式）
    LaunchedEffect(fusionState, timerKey) {
        if (!showNavCapsule) return@LaunchedEffect
        delay(5_000)
        fusionState = if (hasMusic && isPlaying) {
            FusionBarState.PlaybackExpanded
        } else {
            FusionBarState.NavigationExpanded
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
            .padding(bottom = 20.dp),
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
            AnimatedVisibility(
                visible = showNavCapsule,
                enter = expandHorizontally(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = shrinkHorizontally(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)),
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
                                onResetTimer = { resetTimer() },
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
    Row(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        bottomTabs.forEachIndexed { index, tab ->
            val isSelected = index == selectedIndex
            val contentColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }

            Row(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        haptic.performLightClick()
                        onTabSelected(index)
                    }
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(if (isSelected) tab.selectedIconId else tab.unselectedIconId),
                    contentDescription = tab.label,
                    tint = contentColor,
                    modifier = Modifier.size(28.dp)
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

// ── 导航折叠内容 ──

@Composable
private fun NavigationCollapsedContent(
    selectedTab: BottomTabItem
) {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .size(56.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(selectedTab.selectedIconId),
            contentDescription = selectedTab.label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
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
            size = 56.dp,
            corner = 28.dp,
            shadow = 3.dp,
            modifier = Modifier.graphicsLayer {
                rotationZ = coverRotation.value
            }
        )
    }
}

// ── 播放展开内容 ──

@SuppressLint("AutoboxingStateCreation")
@Composable
private fun PlaybackExpandedContent(
    musicInfo: MusicInfo,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onOpenPlayer: () -> Unit,
    onResetTimer: () -> Unit,
    haptic: HapticFeedbackHelper
) {
    val coverRotation = remember { Animatable(0f) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var capsuleWidth by remember { mutableIntStateOf(0) }
    val animatedOffset by animateFloatAsState(dragOffset, animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f))
    val thresholdPx = (capsuleWidth * 0.25f).coerceAtLeast(1f)
    val thresholdReached = kotlin.math.abs(animatedOffset) >= thresholdPx
    val primaryColor = MaterialTheme.colorScheme.primary

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
            .onSizeChanged { capsuleWidth = it.width }
            .clip(RoundedCornerShape(36.dp))
            .pointerInput(thresholdPx) {
                var wasPastThreshold = false
                detectHorizontalDragGestures(
                    onDragStart = { onResetTimer() },
                    onDragEnd = {
                        if (dragOffset >= thresholdPx) {
                            onPrev()
                        } else if (dragOffset <= -thresholdPx) {
                            onNext()
                        }
                        onResetTimer()
                        dragOffset = 0f
                    },
                    onDragCancel = {
                        onResetTimer()
                        dragOffset = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        dragOffset += dragAmount
                        onResetTimer()
                        val pastThreshold = kotlin.math.abs(dragOffset) >= thresholdPx
                        if (pastThreshold && !wasPastThreshold) {
                            haptic.performLightClick()
                        }
                        wasPastThreshold = pastThreshold
                    }
                )
            }
    ) {
        // 左滑指示渐变（右边缘露出）
        if (animatedOffset < -10f) {
            val linearIntensity = (kotlin.math.abs(animatedOffset) / thresholdPx).coerceIn(0f, 1f)
            val visualIntensity = linearIntensity.pow(0.4f)
            Box(modifier = Modifier.matchParentSize()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxWidth(visualIntensity)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                0f to Color.Transparent,
                                1f to primaryColor.copy(alpha = linearIntensity * 0.4f)
                            )
                        )
                )
            }
        }

        // 右滑指示渐变（左边缘露出）
        if (animatedOffset > 10f) {
            val linearIntensity = (animatedOffset / thresholdPx).coerceIn(0f, 1f)
            val visualIntensity = linearIntensity.pow(0.4f)
            Box(modifier = Modifier.matchParentSize()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxWidth(visualIntensity)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                0f to primaryColor.copy(alpha = linearIntensity * 0.4f),
                                1f to Color.Transparent
                            )
                        )
                )
            }
        }

        // 主内容层
        Row(
            modifier = Modifier
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 封面 + 播放暂停叠加
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        haptic.performLightClick()
                        onPlayPause()
                    },
                contentAlignment = Alignment.Center
            ) {
                AlbumCover(
                    uri = musicInfo.music.albumArtUri,
                    size = 56.dp,
                    corner = 28.dp,
                    shadow = 4.dp,
                    modifier = Modifier.graphicsLayer {
                        rotationZ = coverRotation.value
                    }
                )
                Icon(
                    painter = painterResource(
                        if (isPlaying) R.drawable.pause else R.drawable.play_fill
                    ),
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 歌名 + 歌手（点击进入播放器）
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        haptic.performLightClick()
                        onOpenPlayer()
                    }
            ) {
                Text(
                    text = musicInfo.music.title,
                    style = MaterialTheme.typography.titleMedium,
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
        }
    }
}
