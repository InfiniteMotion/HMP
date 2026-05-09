package com.hmp.desktop.ui.components.musiclist

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.unit.dp
import com.hmp.desktop.ui.design.AnimationTokens

private val HeaderRowPadding = 12.dp
private val HeaderRowVerticalPadding = 12.dp
private val HeaderRowBottomPadding = 4.dp
private val HeaderRowContentHeight = 32.dp
private val HeaderBarTotalHeight = HeaderRowVerticalPadding + HeaderRowContentHeight + HeaderRowBottomPadding
private val CountLabelWidth = 32.dp

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
            onClick = { onOrderPlay() },
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Order play",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        IconButton(
            onClick = { onShufflePlay() },
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = "Shuffle",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (showEditButton) {
            IconButton(
                onClick = { onEditClick() },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
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
                onClick = { expanded = !expanded },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Expand sort",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            IconButton(
                onClick = { onOrderPlay() },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Order play",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            IconButton(
                onClick = { onShufflePlay() },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (showEditButton) {
                IconButton(
                    onClick = { onEditClick() },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = tween(durationMillis = 300, easing = AnimationTokens.EASE_OUT)) +
                fadeIn(animationSpec = tween(durationMillis = 300, easing = AnimationTokens.EASE_OUT)),
            exit = shrinkVertically(animationSpec = tween(durationMillis = 300, easing = AnimationTokens.EASE_IN)) +
                fadeOut(animationSpec = tween(durationMillis = 300, easing = AnimationTokens.EASE_IN)),
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                color = transparent,
            ) {
                val genres = listOf(
                    "标题" to "title",
                    "艺术家" to "artist",
                    "时长" to "duration",
                    "大小" to "fileSize",
                    "播放次数" to "playCount",
                    "添加时间" to "date",
                )
                val orders = listOf(
                    "升序" to "ASC",
                    "降序" to "DESC",
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
                            text = "排序",
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
                            text = "顺序",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            orders.forEach { (label, value) ->
                                FilterChip(
                                    selected = selectedOrder == value,
                                    onClick = {
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
