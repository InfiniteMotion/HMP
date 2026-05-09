package com.hmp.desktop.ui.components.musiclist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val EditToolbarHorizontalPadding = 12.dp
private val EditToolbarTopPadding = 12.dp
private val EditToolbarBottomPadding = 4.dp
private val EditToolbarTotalHeight = 48.dp
private val EditToolbarButtonSize = 32.dp
private val EditToolbarIconSize = 24.dp
private val EditToolbarCountLabelWidth = 28.dp

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
                imageVector = Icons.Default.SelectAll,
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
                imageVector = Icons.Default.LibraryAdd,
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
                imageVector = Icons.Default.Delete,
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
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Confirm",
                modifier = Modifier.size(EditToolbarIconSize),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
