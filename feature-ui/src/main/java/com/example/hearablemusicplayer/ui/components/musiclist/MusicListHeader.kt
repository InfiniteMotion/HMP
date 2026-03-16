package com.example.hearablemusicplayer.ui.components.musiclist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.util.AnimationConfig
import com.example.hearablemusicplayer.ui.util.rememberHapticFeedback

/** 头部/编辑栏统一高度与 padding，保证切换时无高度变化 */
private val HeaderRowPadding = 12.dp
private val HeaderRowVerticalPadding = 12.dp
private val HeaderRowBottomPadding = 4.dp
/** 头部行内容高度（与 IconButton 一致），总高度 = VerticalPadding + ContentHeight + BottomPadding */
private val HeaderRowContentHeight = 32.dp
private val HeaderBarTotalHeight = HeaderRowVerticalPadding + HeaderRowContentHeight + HeaderRowBottomPadding
/** 数字 label 固定宽度，避免数量变化时布局偏移 */
private val CountLabelWidth = 32.dp

/**
 * 根据 [HeaderConfig] 渲染头部：None / Simple（顺序+随机）/ Full（排序筛选+播放）/ Custom。
 * 当 [showEditButton] 为 true 时显示编辑按钮；[listCount] 非 null 时显示列表数量（仅数字）。元素等间距分布。
 */
@Composable
internal fun MusicListHeader(
    config: HeaderConfig,
    modifier: Modifier = Modifier,
    showEditButton: Boolean = false,
    onEditClick: () -> Unit = {},
    listCount: Int? = null,
) {
    when (config) {
        is HeaderConfig.None -> { }
        is HeaderConfig.Simple -> SimpleHeader(
            onOrderPlay = config.onOrderPlay,
            onShufflePlay = config.onShufflePlay,
            showEditButton = showEditButton,
            onEditClick = onEditClick,
            listCount = listCount,
            trailing = config.trailing,
            modifier = modifier,
        )
        is HeaderConfig.Full -> FullHeader(
            selectedGenre = config.selectedGenre,
            selectedOrder = config.selectedOrder,
            onFilterGenreChange = config.onFilterGenreChange,
            onFilterOrderChange = config.onFilterOrderChange,
            onOrderPlay = config.onOrderPlay,
            onShufflePlay = config.onShufflePlay,
            showEditButton = showEditButton,
            onEditClick = onEditClick,
            listCount = listCount,
            modifier = modifier,
        )
        is HeaderConfig.Custom -> config.content()
    }
}

@Composable
private fun SimpleHeader(
    onOrderPlay: () -> Unit,
    onShufflePlay: () -> Unit,
    showEditButton: Boolean,
    onEditClick: () -> Unit,
    listCount: Int?,
    trailing: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val haptic = rememberHapticFeedback()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(HeaderBarTotalHeight)
            .padding(horizontal = HeaderRowPadding)
            .padding(top = HeaderRowVerticalPadding, bottom = HeaderRowBottomPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        if (listCount != null) {
            Text(
                text = "$listCount",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.width(CountLabelWidth),
            )
        }
        if (trailing != null) trailing()
        IconButton(
            onClick = {
                haptic.performClick()
                onOrderPlay()
            },
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.order_play),
                contentDescription = "Order play",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        IconButton(
            onClick = {
                haptic.performConfirm()
                onShufflePlay()
            },
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.shuffle),
                contentDescription = "Shuffle",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (showEditButton) {
            IconButton(
                onClick = {
                    haptic.performClick()
                    onEditClick()
                },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.rectangle_on_rectangle),
                    contentDescription = "Edit",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun FullHeader(
    selectedGenre: String,
    selectedOrder: String,
    onFilterGenreChange: (String) -> Unit,
    onFilterOrderChange: (String) -> Unit,
    onOrderPlay: () -> Unit,
    onShufflePlay: () -> Unit,
    showEditButton: Boolean,
    onEditClick: () -> Unit,
    listCount: Int?,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val haptic = rememberHapticFeedback()
    val transparent = Color.Transparent

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(HeaderBarTotalHeight)
                .padding(horizontal = HeaderRowPadding)
                .padding(top = HeaderRowVerticalPadding, bottom = HeaderRowBottomPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            if (listCount != null) {
                Text(
                    text = "$listCount",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.width(CountLabelWidth),
                )
            }
            IconButton(
                onClick = {
                    haptic.performClick()
                    expanded = !expanded
                },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.slider_vertical_3),
                    contentDescription = "Expand sort",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            IconButton(
                onClick = {
                    haptic.performClick()
                    onOrderPlay()
                },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.order_play),
                    contentDescription = "Order play",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            IconButton(
                onClick = {
                    haptic.performConfirm()
                    onShufflePlay()
                },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.shuffle),
                    contentDescription = "Shuffle",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (showEditButton) {
                IconButton(
                    onClick = {
                        haptic.performClick()
                        onEditClick()
                    },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.rectangle_on_rectangle),
                        contentDescription = "Edit",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = tween(durationMillis = 300, easing = AnimationConfig.EASE_OUT)) +
                fadeIn(animationSpec = tween(durationMillis = 300, easing = AnimationConfig.EASE_OUT)),
            exit = shrinkVertically(animationSpec = tween(durationMillis = 300, easing = AnimationConfig.EASE_IN)) +
                fadeOut(animationSpec = tween(durationMillis = 300, easing = AnimationConfig.EASE_IN)),
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                color = transparent,
            ) {
                val genres = listOf(
                    stringResource(R.string.sort_title) to "title",
                    stringResource(R.string.sort_artist) to "artist",
                    stringResource(R.string.sort_duration) to "duration",
                    stringResource(R.string.sort_size) to "fileSize",
                    stringResource(R.string.sort_play_count) to "playCount",
                    stringResource(R.string.sort_add_time) to "date",
                )
                val orders = listOf(
                    stringResource(R.string.order_asc) to "ASC",
                    stringResource(R.string.order_desc) to "DESC",
                )
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.sort),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                        FlowRow(
                            maxItemsInEachRow = 3,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            genres.forEach { (label, value) ->
                                FilterChip(
                                    selected = selectedGenre == value,
                                    onClick = {
                                        haptic.performLightClick()
                                        if (selectedGenre != value) onFilterGenreChange(value)
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.titleSmall,
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    ),
                                )
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.order),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            orders.forEach { (label, value) ->
                                FilterChip(
                                    selected = selectedOrder == value,
                                    onClick = {
                                        haptic.performLightClick()
                                        if (selectedOrder != value) onFilterOrderChange(value)
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.titleSmall,
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
