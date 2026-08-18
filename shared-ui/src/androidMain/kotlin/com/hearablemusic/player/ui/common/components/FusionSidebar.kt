package com.hearablemusic.player.ui.common.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.hearablemusic.player.ui.R
import androidx.annotation.StringRes
import com.hearablemusic.player.ui.common.design.dimens.LocalHMPDimens
import com.hearablemusic.player.ui.common.util.rememberHapticFeedback
import com.hmp.domain.music.MusicInfo

private data class SidebarTabItem(
    @StringRes val label: Int,
    val selectedIconId: Int,
    val unselectedIconId: Int
)

private val sidebarTabs = listOf(
    SidebarTabItem(R.string.tab_home, R.drawable.house_fill, R.drawable.house),
    SidebarTabItem(R.string.tab_gallery, R.drawable.square_fill_grid_2x2, R.drawable.square_grid_2x2),
    SidebarTabItem(R.string.tab_list, R.drawable.list_bullet, R.drawable.list_bullet),
    SidebarTabItem(R.string.tab_user, R.drawable.person_filled_viewfinder, R.drawable.person)
)

/**
 * 融合侧边栏：NavigationRail + 迷你播放控制
 * 仅在 Medium (600–840dp) 手机横屏模式下使用，替代 BottomFusionBar，
 * 将所有操作集中到左侧，最大化内容区域的垂直空间。
 */
@Composable
fun FusionSidebar(
    selectedTabIndex: Int,
    currentMusic: MusicInfo?,
    isPlaying: Boolean,
    onTabSelected: (Int) -> Unit,
    onOpenPlayer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = LocalHMPDimens.current
    val haptic = rememberHapticFeedback()

    Column(
        modifier = modifier
            .width(80.dp)
            .fillMaxHeight()
            .padding(vertical = dimens.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // 导航 Tab 区域
        sidebarTabs.forEachIndexed { index, tab ->
            val isSelected = index == selectedTabIndex
            val iconTint = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(dimens.corner.sm))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        haptic.performClick()
                        onTabSelected(index)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        if (isSelected) tab.selectedIconId else tab.unselectedIconId
                    ),
                    contentDescription = stringResource(tab.label),
                    tint = iconTint,
                    modifier = Modifier.size(dimens.icon.md)
                )
            }
        }

        // 迷你播放封面（无播放时隐藏）
        if (currentMusic != null) {
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
                .size(40.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onOpenPlayer() },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = currentMusic.music.albumArtUri,
                contentDescription = currentMusic.music.title,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    .graphicsLayer { rotationZ = coverRotation.value },
                contentScale = ContentScale.Crop
            )
        }
        }
    }
}
