package com.hmp.desktop.ui.library.pages.components.musiclist

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.hmp.domain.music.MusicInfo
import com.hmp.desktop.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.painterResource
import com.hmp.desktop.ui.library.pages.components.AlbumCover
import com.hmp.desktop.ui.common.util.rememberHapticFeedback

private val DefaultFullHeight = 80.dp
private val DefaultCompactHeight = 64.dp
private val DefaultGalleryHeight = 80.dp

/** 圆环形自定义复选框尺寸 */
private val RingCheckboxSize = 14.dp
/** 序号/复选框位固定宽度，保证三位数完整显示；序号与复选框共用此宽度避免切换时错位 */
private val IndexSlotWidth = 28.dp
/** 有序号或复选框时的左侧 padding（进一步缩小） */
private val RowStartPaddingWithSlot = 4.dp
/** 无序号且无复选框时的左侧 padding */
private val RowStartPaddingDefault = 10.dp
/** 更多按钮缩小左右占位（固定宽度，小于默认 48.dp） */
private val MoreButtonWidth = 32.dp
private val RingCheckboxStrokeWidth = 1.5.dp

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

/**
 * 圆环形复选框：未选为空心圆环，选中为圆环+实心圆心。
 */
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

/**
 * 单行音乐项：可选序号、可选选择框、按 [ItemConfig.variant] 渲染 Full/Compact/Gallery/Custom 内容。
 */
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
    val haptic = rememberHapticFeedback()
    val height = itemConfig.itemHeight ?: when (itemConfig.variant) {
        ItemVariant.Full -> DefaultFullHeight
        ItemVariant.Compact -> DefaultCompactHeight
        ItemVariant.Gallery -> DefaultGalleryHeight
        ItemVariant.Custom -> DefaultFullHeight
    }

    val onItemClick = {
        haptic.performClick()
        callbacks.onItemClick(musicInfo, index)
    }
    val onLongClick = if (enableLongPressToEnterEdit && editEnabled) {
        {
            haptic.performClick()
            callbacks.onEnterEditMode()
        }
    } else null

    // 编辑模式下点击整行切换选中状态，否则播放
    val onRowClick = if (isEditMode && itemConfig.showCheckbox) {
        {
            haptic.performClick()
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
        .clip(RoundedCornerShape(16.dp))
        .then(rowClickModifier)
        .padding(vertical = 4.dp)

    // 编辑模式且启用复选框时，复选框取代序号位；否则有序号则显示序号
    val showIndexSlot = itemConfig.showIndex || (itemConfig.showCheckbox && isEditMode)
    val showIndexAsCheckbox = itemConfig.showCheckbox && isEditMode

    Row(
        modifier = modifier.then(rowVisualModifier).padding(
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
    val haptic = rememberHapticFeedback()
    val actionTint = if (isCurrentPlaying) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AlbumCover(
            uri = musicInfo.music.albumArtUri,
            size = 56.dp,
            corner = 10.dp,
            shadow = 3.dp
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = musicInfo.music.title,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground,
            )
            val parts = buildList {
                add(musicInfo.music.artist)
                if (musicInfo.music.album.isNotBlank()) add(musicInfo.music.album)
                if (musicInfo.music.duration > 0) add(formatDuration(musicInfo.music.duration))
            }
            Text(
                text = parts.joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
            if (opts.showPinButton) {
                IconButton(
                    onClick = { haptic.performConfirm(); callbacks.onPinToTop(musicInfo) },
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.chevron_up_circle),
                        contentDescription = stringResource(Res.string.pin_to_top),
                        tint = actionTint,
                    )
                }
            }
            if (opts.showRemoveButton) {
                IconButton(
                    onClick = { haptic.performLightClick(); callbacks.onRemove(musicInfo) },
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.trash),
                        contentDescription = stringResource(Res.string.remove),
                        tint = actionTint,
                    )
                }
            }
            if (opts.showMenuButton) {
                if (opts.extraMenuItems.isEmpty() && !opts.showAddToPlaylistInMenu) {
                    IconButton(
                        onClick = { haptic.performLightClick(); callbacks.onMenuClick(musicInfo) },
                        modifier = Modifier.size(width = MoreButtonWidth, height = 48.dp),
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.dot_grid_1x2),
                            contentDescription = stringResource(Res.string.more),
                            tint = actionTint,
                        )
                    }
                } else {
                    Box {
                        IconButton(
                            onClick = { haptic.performLightClick(); menuExpanded = true },
                            modifier = Modifier.size(width = MoreButtonWidth, height = 48.dp),
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.dot_grid_1x2),
                                contentDescription = stringResource(Res.string.more),
                                tint = actionTint,
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.title_song_detail)) },
                                onClick = {
                                    menuExpanded = false
                                    callbacks.onMenuClick(musicInfo)
                                },
                            )
                            if (opts.showAddToPlaylistInMenu) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(Res.string.add_to_playlist)) },
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
    val haptic = rememberHapticFeedback()
    val actionTint = if (isCurrentPlaying) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AsyncImage(
            model = musicInfo.music.albumArtUri,
            contentDescription = stringResource(Res.string.album_art),
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(Res.drawable.`none`),
            error = painterResource(Res.drawable.`none`),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = musicInfo.music.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )
            val parts = buildList {
                add(musicInfo.music.artist)
                if (musicInfo.music.album.isNotBlank()) add(musicInfo.music.album)
                if (musicInfo.music.duration > 0) add(formatDuration(musicInfo.music.duration))
            }
            Text(
                text = parts.joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
        Row {
            if (opts.showPinButton) {
                IconButton(
                    onClick = { haptic.performConfirm(); callbacks.onPinToTop(musicInfo) },
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.chevron_up_circle),
                        contentDescription = stringResource(Res.string.pin_to_top),
                        modifier = Modifier.size(20.dp),
                        tint = actionTint,
                    )
                }
            }
            if (opts.showRemoveButton) {
                IconButton(
                    onClick = { haptic.performLightClick(); callbacks.onRemove(musicInfo) },
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.trash),
                        contentDescription = stringResource(Res.string.remove),
                        modifier = Modifier.size(20.dp),
                        tint = actionTint,
                    )
                }
            }
            if (opts.showMenuButton) {
                if (opts.extraMenuItems.isEmpty() && !opts.showAddToPlaylistInMenu) {
                    IconButton(
                        onClick = { haptic.performLightClick(); callbacks.onMenuClick(musicInfo) },
                        modifier = Modifier.size(width = MoreButtonWidth, height = 40.dp),
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.dot_grid_1x2),
                            contentDescription = stringResource(Res.string.more),
                            modifier = Modifier.size(20.dp),
                            tint = actionTint,
                        )
                    }
                } else {
                    var menuExpanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(
                            onClick = { haptic.performLightClick(); menuExpanded = true },
                            modifier = Modifier.size(width = MoreButtonWidth, height = 40.dp),
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.dot_grid_1x2),
                                contentDescription = stringResource(Res.string.more),
                                modifier = Modifier.size(20.dp),
                                tint = actionTint,
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.title_song_detail)) },
                                onClick = {
                                    menuExpanded = false
                                    callbacks.onMenuClick(musicInfo)
                                },
                            )
                            if (opts.showAddToPlaylistInMenu) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(Res.string.add_to_playlist)) },
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
    val haptic = rememberHapticFeedback()
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
        AlbumCover(
            uri = musicInfo.music.albumArtUri,
            size = 56.dp,
            corner = 10.dp,
            shadow = 3.dp,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = musicInfo.music.title,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground,
            )
            val parts = buildList {
                add(musicInfo.music.artist)
                if (musicInfo.music.album.isNotBlank()) add(musicInfo.music.album)
                if (musicInfo.music.duration > 0) add(formatDuration(musicInfo.music.duration))
            }
            Text(
                text = parts.joinToString(" · "),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            )
        }
        }
        Row(modifier = Modifier.zIndex(1f)) {
            if (opts.showPinButton) {
                IconButton(
                    onClick = { haptic.performConfirm(); callbacks.onPinToTop(musicInfo) },
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.chevron_up_circle),
                        contentDescription = stringResource(Res.string.pin_to_top),
                        modifier = Modifier.size(20.dp),
                        tint = actionTint,
                    )
                }
            }
            if (opts.showRemoveButton) {
                IconButton(
                    onClick = { haptic.performLightClick(); callbacks.onRemove(musicInfo) },
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.trash),
                        contentDescription = stringResource(Res.string.remove),
                        modifier = Modifier.size(20.dp),
                        tint = actionTint,
                    )
                }
            }
            if (opts.showMenuButton) {
                if (opts.extraMenuItems.isEmpty() && !opts.showAddToPlaylistInMenu) {
                    IconButton(
                        onClick = { haptic.performLightClick(); callbacks.onMenuClick(musicInfo) },
                        modifier = Modifier.size(width = MoreButtonWidth, height = 48.dp),
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.dot_grid_1x2),
                            contentDescription = stringResource(Res.string.more),
                            modifier = Modifier.size(24.dp),
                            tint = actionTint,
                        )
                    }
                } else {
                    var menuExpanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(
                            onClick = { haptic.performLightClick(); menuExpanded = true },
                            modifier = Modifier.size(width = MoreButtonWidth, height = 48.dp),
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.dot_grid_1x2),
                                contentDescription = stringResource(Res.string.more),
                                modifier = Modifier.size(24.dp),
                                tint = actionTint,
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.title_song_detail)) },
                                onClick = {
                                    menuExpanded = false
                                    callbacks.onMenuClick(musicInfo)
                                },
                            )
                            if (opts.showAddToPlaylistInMenu) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(Res.string.add_to_playlist)) },
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
