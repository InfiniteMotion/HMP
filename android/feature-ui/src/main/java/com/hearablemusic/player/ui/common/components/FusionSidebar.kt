package com.hearablemusic.player.ui.common.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.hearablemusic.player.ui.R
import com.hearablemusic.player.ui.common.design.dimens.LocalHMPDimens
import com.hearablemusic.player.ui.common.util.rememberHapticFeedback
import com.hmp.domain.music.MusicInfo

private data class SidebarTabItem(
    val label: String,
    val selectedIconId: Int,
    val unselectedIconId: Int
)

private val sidebarTabs = listOf(
    SidebarTabItem("首页", R.drawable.house_fill, R.drawable.house),
    SidebarTabItem("封面", R.drawable.square_fill_grid_2x2, R.drawable.square_grid_2x2),
    SidebarTabItem("列表", R.drawable.list_bullet, R.drawable.list_bullet),
    SidebarTabItem("我的", R.drawable.person_filled_viewfinder, R.drawable.person)
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
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
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
        verticalArrangement = Arrangement.Top
    ) {
        // 导航 Tab 区域
        sidebarTabs.forEachIndexed { index, tab ->
            val isSelected = index == selectedTabIndex
            val contentColor by animateColorAsState(
                targetValue = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(200)
            )
            val containerColor by animateColorAsState(
                targetValue = MaterialTheme.colorScheme.surface,
                animationSpec = tween(200)
            )

            Column(
                modifier = Modifier
                    .width(64.dp)
                    .clip(RoundedCornerShape(dimens.corner.sm))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        haptic.performClick()
                        onTabSelected(index)
                    }
                    .padding(vertical = dimens.spacing.xs),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(dimens.corner.sm))
                        .background(containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(
                            if (isSelected) tab.selectedIconId else tab.unselectedIconId
                        ),
                        contentDescription = tab.label,
                        tint = contentColor,
                        modifier = Modifier.size(dimens.icon.sm)
                    )
                }
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = dimens.type.sm,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 分隔线
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(1.dp)
                .background(
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
        )

        Spacer(modifier = Modifier.height(dimens.spacing.sm))

        // 迷你播放控制区域
        Column(
            modifier = Modifier
                .width(64.dp)
                .clip(RoundedCornerShape(dimens.corner.sm))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onOpenPlayer() }
                .padding(vertical = dimens.spacing.xs),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 专辑封面缩略图
            AsyncImage(
                model = currentMusic?.music?.albumArtUri,
                contentDescription = currentMusic?.music?.title,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(dimens.corner.sm))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )
            Text(
                text = currentMusic?.music?.title ?: "",
                style = MaterialTheme.typography.labelSmall,
                fontSize = dimens.type.sm,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = dimens.spacing.xs)
            )
        }

        // 播放控制按钮
        Icon(
            painter = painterResource(R.drawable.backward_end_fill),
            contentDescription = "上一首",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    haptic.performClick()
                    onPrev()
                }
                .padding(dimens.spacing.xs)
        )

        Icon(
            painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play_fill),
            contentDescription = if (isPlaying) "暂停" else "播放",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    haptic.performClick()
                    onPlayPause()
                }
                .padding(dimens.spacing.xs)
        )

        Icon(
            painter = painterResource(R.drawable.forward_end_fill),
            contentDescription = "下一首",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    haptic.performClick()
                    onNext()
                }
                .padding(dimens.spacing.xs)
        )

        Spacer(modifier = Modifier.height(dimens.spacing.xs))
    }
}
