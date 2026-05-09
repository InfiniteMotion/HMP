package com.hmp.desktop.ui.components.musiclist

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.hmp.desktop.ui.components.AlbumArt
import com.hmp.domain.music.MusicInfo

private val DefaultFullHeight = 80.dp
private val DefaultCompactHeight = 64.dp
private val DefaultGalleryHeight = 80.dp

private val RingCheckboxSize = 14.dp
private val IndexSlotWidth = 28.dp
private val RowStartPaddingWithSlot = 4.dp
private val RowStartPaddingDefault = 10.dp
private val MoreButtonWidth = 32.dp
private val RingCheckboxStrokeWidth = 1.5.dp

@Composable
private fun RingCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = RingCheckboxSize,
) {
    val density = LocalDensity.current
    val sizePx = with(density) { size.toPx() }
    val strokeWidthPx = with(density) { RingCheckboxStrokeWidth.toPx() }
    val center = Offset(sizePx / 2f, sizePx / 2f)
    val radius = (sizePx - strokeWidthPx) / 2f
    val ringColor = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

    Box(
        modifier = modifier
            .size(size)
            .clickable {
                onCheckedChange(!checked)
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                color = ringColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidthPx),
            )
            if (checked) {
                drawCircle(
                    color = ringColor,
                    radius = radius * 0.4f,
                    center = center,
                )
            }
        }
    }
}

@Composable
internal fun MusicListItem(
    musicInfo: MusicInfo,
    index: Int,
    itemConfig: ItemConfig,
    isCurrentPlaying: Boolean,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    callbacks: MusicListCallbacks,
    enableLongPressToEnterEdit: Boolean,
    editEnabled: Boolean,
    isEditMode: Boolean,
    modifier: Modifier = Modifier,
) {
    val height = itemConfig.itemHeight ?: when (itemConfig.variant) {
        ItemVariant.Full -> DefaultFullHeight
        ItemVariant.Compact -> DefaultCompactHeight
        ItemVariant.Gallery -> DefaultGalleryHeight
        ItemVariant.Custom -> DefaultFullHeight
    }

    val onItemClick = {
        callbacks.onItemClick(musicInfo, index)
    }
    val onLongClick = if (enableLongPressToEnterEdit && editEnabled) {
        {
            callbacks.onEnterEditMode()
        }
    } else null

    val onRowClick = if (isEditMode && itemConfig.showCheckbox) {
        {
            onSelectedChange(!selected)
        }
    } else {
        onItemClick
    }

    val rowClickModifier = if (onLongClick != null) {
        Modifier.combinedClickable(
            onClick = onRowClick,
            onLongClick = onLongClick,
        )
    } else {
        Modifier.clickable(onClick = onRowClick)
    }
    val rowVisualModifier = Modifier
        .fillMaxWidth()
        .height(height)
        .clip(RoundedCornerShape(12.dp))
        .padding(vertical = 4.dp)
    val rowModifier = rowVisualModifier.then(rowClickModifier)

    val showIndexSlot = itemConfig.showIndex || (itemConfig.showCheckbox && isEditMode)
    val showIndexAsCheckbox = itemConfig.showCheckbox && isEditMode

    Row(
        modifier = modifier.then(rowModifier).padding(
            start = if (showIndexSlot) RowStartPaddingWithSlot else RowStartPaddingDefault,
            end = 6.dp,
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (showIndexSlot) {
            Box(
                modifier = Modifier.size(width = IndexSlotWidth, height = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (showIndexAsCheckbox) {
                    RingCheckbox(
                        checked = selected,
                        onCheckedChange = { onSelectedChange(it) },
                    )
                } else {
                    Text(
                        text = itemConfig.indexFormat(index),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCurrentPlaying) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            }
        }
        when (itemConfig.variant) {
            ItemVariant.Full -> FullRow(
                musicInfo = musicInfo,
                index = index,
                options = itemConfig.fullOptions,
                isCurrentPlaying = isCurrentPlaying,
                callbacks = callbacks,
                modifier = Modifier.weight(1f),
            )
            ItemVariant.Compact -> CompactRow(
                musicInfo = musicInfo,
                options = itemConfig.compactOptions,
                isCurrentPlaying = isCurrentPlaying,
                callbacks = callbacks,
                modifier = Modifier.weight(1f),
            )
            ItemVariant.Gallery -> GalleryRow(
                musicInfo = musicInfo,
                options = itemConfig.galleryOptions,
                isCurrentPlaying = isCurrentPlaying,
                callbacks = callbacks,
                modifier = Modifier.weight(1f),
            )
            ItemVariant.Custom -> Box(modifier = Modifier.weight(1f)) {
                itemConfig.customContent?.invoke(musicInfo, index, selected)
            }
        }
    }
}


@Composable
private fun FullRow(
    musicInfo: MusicInfo,
    index: Int,
    options: FullItemOptions?,
    isCurrentPlaying: Boolean,
    callbacks: MusicListCallbacks,
    modifier: Modifier = Modifier,
) {
    val opts = options ?: FullItemOptions()
    var menuExpanded by remember { mutableStateOf(false) }
    val actionTint = if (isCurrentPlaying) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AlbumArt(
            path = musicInfo.music.path,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(10.dp)),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = musicInfo.music.title,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = musicInfo.music.artist,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = musicInfo.music.album,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
            if (opts.showPinButton) {
                IconButton(
                    onClick = { callbacks.onPinToTop(musicInfo) },
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "置顶",
                        tint = actionTint,
                    )
                }
            }
            if (opts.showRemoveButton) {
                IconButton(
                    onClick = { callbacks.onRemove(musicInfo) },
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "移除",
                        tint = actionTint,
                    )
                }
            }
            if (opts.showMenuButton) {
                if (opts.extraMenuItems.isEmpty() && !opts.showAddToPlaylistInMenu) {
                    IconButton(
                        onClick = { callbacks.onMenuClick(musicInfo) },
                        modifier = Modifier.size(width = MoreButtonWidth, height = 48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "更多",
                            tint = actionTint,
                        )
                    }
                } else {
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(width = MoreButtonWidth, height = 48.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "更多",
                                tint = actionTint,
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("歌曲详情") },
                                onClick = {
                                    menuExpanded = false
                                    callbacks.onMenuClick(musicInfo)
                                },
                            )
                            if (opts.showAddToPlaylistInMenu) {
                                DropdownMenuItem(
                                    text = { Text("加入播放列表") },
                                    onClick = {
                                        menuExpanded = false
                                        callbacks.onAddToPlaylist(musicInfo)
                                    },
                                )
                            }
                            opts.extraMenuItems.forEach { (label, onClick) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        menuExpanded = false
                                        onClick()
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactRow(
    musicInfo: MusicInfo,
    options: CompactItemOptions?,
    isCurrentPlaying: Boolean,
    callbacks: MusicListCallbacks,
    modifier: Modifier = Modifier,
) {
    val opts = options ?: CompactItemOptions()
    val actionTint = if (isCurrentPlaying) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AlbumArt(
            path = musicInfo.music.path,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp)),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = musicInfo.music.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = musicInfo.music.artist,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
        Row {
            if (opts.showPinButton) {
                IconButton(
                    onClick = { callbacks.onPinToTop(musicInfo) },
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "置顶",
                        modifier = Modifier.size(20.dp),
                        tint = actionTint,
                    )
                }
            }
            if (opts.showRemoveButton) {
                IconButton(
                    onClick = { callbacks.onRemove(musicInfo) },
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "移除",
                        modifier = Modifier.size(20.dp),
                        tint = actionTint,
                    )
                }
            }
            if (opts.showMenuButton) {
                if (opts.extraMenuItems.isEmpty() && !opts.showAddToPlaylistInMenu) {
                    IconButton(
                        onClick = { callbacks.onMenuClick(musicInfo) },
                        modifier = Modifier.size(width = MoreButtonWidth, height = 40.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "更多",
                            modifier = Modifier.size(20.dp),
                            tint = actionTint,
                        )
                    }
                } else {
                    var menuExpanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(width = MoreButtonWidth, height = 40.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "更多",
                                modifier = Modifier.size(20.dp),
                                tint = actionTint,
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("歌曲详情") },
                                onClick = {
                                    menuExpanded = false
                                    callbacks.onMenuClick(musicInfo)
                                },
                            )
                            if (opts.showAddToPlaylistInMenu) {
                                DropdownMenuItem(
                                    text = { Text("加入播放列表") },
                                    onClick = {
                                        menuExpanded = false
                                        callbacks.onAddToPlaylist(musicInfo)
                                    },
                                )
                            }
                            opts.extraMenuItems.forEach { (label, onClick) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        menuExpanded = false
                                        onClick()
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GalleryRow(
    musicInfo: MusicInfo,
    options: GalleryItemOptions?,
    isCurrentPlaying: Boolean,
    callbacks: MusicListCallbacks,
    modifier: Modifier = Modifier,
) {
    val opts = options ?: GalleryItemOptions()
    val actionTint = if (isCurrentPlaying) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AlbumArt(
                path = musicInfo.music.path,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp)),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = musicInfo.music.title,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = musicInfo.music.artist,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = musicInfo.music.album,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
        Row(modifier = Modifier.zIndex(1f)) {
            if (opts.showPinButton) {
                IconButton(
                    onClick = { callbacks.onPinToTop(musicInfo) },
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "置顶",
                        modifier = Modifier.size(20.dp),
                        tint = actionTint,
                    )
                }
            }
            if (opts.showRemoveButton) {
                IconButton(
                    onClick = { callbacks.onRemove(musicInfo) },
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "移除",
                        modifier = Modifier.size(20.dp),
                        tint = actionTint,
                    )
                }
            }
            if (opts.showMenuButton) {
                if (opts.extraMenuItems.isEmpty() && !opts.showAddToPlaylistInMenu) {
                    IconButton(
                        onClick = { callbacks.onMenuClick(musicInfo) },
                        modifier = Modifier.size(width = MoreButtonWidth, height = 48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "更多",
                            modifier = Modifier.size(24.dp),
                            tint = actionTint,
                        )
                    }
                } else {
                    var menuExpanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(width = MoreButtonWidth, height = 48.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "更多",
                                modifier = Modifier.size(24.dp),
                                tint = actionTint,
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("歌曲详情") },
                                onClick = {
                                    menuExpanded = false
                                    callbacks.onMenuClick(musicInfo)
                                },
                            )
                            if (opts.showAddToPlaylistInMenu) {
                                DropdownMenuItem(
                                    text = { Text("加入播放列表") },
                                    onClick = {
                                        menuExpanded = false
                                        callbacks.onAddToPlaylist(musicInfo)
                                    },
                                )
                            }
                            opts.extraMenuItems.forEach { (label, onClick) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        menuExpanded = false
                                        onClick()
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
