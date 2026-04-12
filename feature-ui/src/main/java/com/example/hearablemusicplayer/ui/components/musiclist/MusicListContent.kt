package com.example.hearablemusicplayer.ui.components.musiclist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.hearablemusicplayer.domain.music.MusicInfo

/**
 * 列表主体：空占位 / LazyColumn（itemsIndexed + 底部 Spacer）。可选滚动条与索引条在 [MusicList] 中叠加。
 */
@Composable
internal fun MusicListContent(
    musicInfoList: List<MusicInfo>,
    config: MusicListConfig,
    listState: androidx.compose.foundation.lazy.LazyListState,
    state: MusicListState,
    isCurrentPlaying: (Int) -> Boolean,
    modifier: Modifier = Modifier,
) {
    if (musicInfoList.isEmpty() && config.emptyContent != null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(config.contentPadding),
        ) {
            config.emptyContent()
        }
        return
    }
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = config.contentPadding,
    ) {
        itemsIndexed(
            items = musicInfoList,
            key = { index, item -> config.list.key(index, item) },
        ) { index, musicInfo ->
            MusicListItem(
                musicInfo = musicInfo,
                index = index,
                itemConfig = config.item,
                isCurrentPlaying = isCurrentPlaying(index),
                selected = state.selectedIds.contains(musicInfo.music.id),
                onSelectedChange = { checked ->
                    state.setSelected(musicInfo.music.id, checked)
                    config.callbacks.onSelectionChange(state.selectedIds)
                },
                callbacks = config.callbacks,
                enableLongPressToEnterEdit = config.list.enableLongPressToEnterEdit,
                editEnabled = config.edit.enabled,
                isEditMode = state.isEditMode,
            )
        }
        item {
            Spacer(modifier = Modifier.height(config.list.bottomSpacerHeight))
        }
    }
}

/**
 * 非懒加载列表主体：空占位 / Column（逐项 compose + 底部 Spacer）。
 * 用于 [FixedMusicList]，适用于嵌入已有滚动容器的场景，不依赖 [LazyListState]，不支持滚动条与索引条。
 */
@Composable
internal fun FixedMusicListContent(
    musicInfoList: List<MusicInfo>,
    config: MusicListConfig,
    state: MusicListState,
    isCurrentPlaying: (Int) -> Boolean,
    modifier: Modifier = Modifier,
) {
    if (musicInfoList.isEmpty() && config.emptyContent != null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(config.contentPadding),
        ) {
            config.emptyContent()
        }
        return
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(config.contentPadding),
    ) {
        musicInfoList.forEachIndexed { index, musicInfo ->
            MusicListItem(
                musicInfo = musicInfo,
                index = index,
                itemConfig = config.item,
                isCurrentPlaying = isCurrentPlaying(index),
                selected = state.selectedIds.contains(musicInfo.music.id),
                onSelectedChange = { checked ->
                    state.setSelected(musicInfo.music.id, checked)
                    config.callbacks.onSelectionChange(state.selectedIds)
                },
                callbacks = config.callbacks,
                enableLongPressToEnterEdit = config.list.enableLongPressToEnterEdit,
                editEnabled = config.edit.enabled,
                isEditMode = state.isEditMode,
            )
        }
        Spacer(modifier = Modifier.height(config.list.bottomSpacerHeight))
    }
}
