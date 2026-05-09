package com.hmp.desktop.ui.components.musiclist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Stable
class MusicListState(
    initialEditMode: Boolean = false,
    initialSelectedIds: Set<Long> = emptySet(),
) {
    var isEditMode by mutableStateOf(initialEditMode)
    var selectedIds by mutableStateOf(initialSelectedIds)

    fun enterEditMode() {
        isEditMode = true
    }

    fun exitEditMode() {
        isEditMode = false
        selectedIds = emptySet()
    }

    fun toggleSelection(id: Long) {
        selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
    }

    fun selectAll(allIds: Set<Long>) {
        selectedIds = allIds
    }

    fun clearSelection() {
        selectedIds = emptySet()
    }

    fun setSelected(id: Long, selected: Boolean) {
        selectedIds = if (selected) selectedIds + id else selectedIds - id
    }
}

@Composable
fun rememberMusicListState(
    initialEditMode: Boolean = false,
    initialSelectedIds: Set<Long> = emptySet(),
): MusicListState = remember {
    MusicListState(initialEditMode, initialSelectedIds)
}
