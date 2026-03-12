package com.example.hearablemusicplayer.ui.components.musiclist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.hearablemusicplayer.ui.R

/** 与 MusicListHeader 一致，保证编辑栏与头部高度一致（总高度 48.dp） */
private val EditToolbarHorizontalPadding = 12.dp
private val EditToolbarTopPadding = 12.dp
private val EditToolbarBottomPadding = 4.dp
private val EditToolbarTotalHeight = 48.dp
private val EditToolbarButtonSize = 32.dp
private val EditToolbarIconSize = 24.dp
/** 数字 label 固定宽度，与头部一致 */
private val EditToolbarCountLabelWidth = 28.dp

/**
 * 编辑栏：左侧显示选中数量，右侧四个按钮（全选、添加、删除、确认）。高度与头部一致。
 */
@Composable
internal fun MusicListEditToolbar(
    config: EditConfig,
    state: MusicListState,
    allIds: Set<Long>,
    callbacks: MusicListCallbacks,
    onExitEditMode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val allSelected = state.selectedIds.size == allIds.size && allIds.isNotEmpty()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(EditToolbarTotalHeight)
            .padding(horizontal = EditToolbarHorizontalPadding)
            .padding(top = EditToolbarTopPadding, bottom = EditToolbarBottomPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Box(modifier = Modifier.width(EditToolbarCountLabelWidth)) {
            Text(
                text = config.selectedCountFormat(state.selectedIds.size),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        }
        IconButton(
            onClick = {
                if (allSelected) {
                    state.clearSelection()
                } else {
                    state.selectAll(allIds)
                }
                callbacks.onSelectionChange(state.selectedIds)
            },
            modifier = Modifier.size(EditToolbarButtonSize),
        ) {
            Icon(
                painter = painterResource(
                    if (allSelected) R.drawable.checkmark_square_on_square_fill
                    else R.drawable.checkmark_square_on_square
                ),
                contentDescription = if (allSelected) "Deselect all" else "Select all",
                modifier = Modifier.size(EditToolbarIconSize),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        IconButton(
            onClick = { callbacks.onBatchAddToPlaylist(state.selectedIds) },
            modifier = Modifier.size(EditToolbarButtonSize),
        ) {
            Icon(
                painter = painterResource(R.drawable.plus),
                contentDescription = "Add to playlist",
                modifier = Modifier.size(EditToolbarIconSize),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        IconButton(
            onClick = { callbacks.onBatchDelete(state.selectedIds) },
            modifier = Modifier.size(EditToolbarButtonSize),
        ) {
            Icon(
                painter = painterResource(R.drawable.trash),
                contentDescription = "Delete selected",
                modifier = Modifier.size(EditToolbarIconSize),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        IconButton(
            onClick = onExitEditMode,
            modifier = Modifier.size(EditToolbarButtonSize),
        ) {
            Icon(
                painter = painterResource(R.drawable.checkmark_circle),
                contentDescription = "Confirm",
                modifier = Modifier.size(EditToolbarIconSize),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
