package com.hearablemusic.player.ui.library.pages.components.musiclist

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.hmp.domain.music.Music
import com.hmp.domain.music.MusicInfo
import com.hearablemusic.player.ui.common.design.theme.HearableMusicPlayerTheme

private fun previewMusicInfo(
    id: Long,
    title: String,
    artist: String,
    album: String = "专辑",
) = MusicInfo(
    music = Music(
        id = id,
        title = title,
        artist = artist,
        album = album,
        duration = 180_000L,
        path = "",
        albumArtUri = "",
    ),
    extra = null,
    userInfo = null,
)

private val previewList = listOf(
    previewMusicInfo(1, "晴天", "周杰伦", "叶惠美"),
    previewMusicInfo(2, "稻香", "周杰伦", "魔杰座"),
    previewMusicInfo(3, "七里香", "周杰伦", "七里香"),
    previewMusicInfo(4, "告白气球", "周杰伦", "周杰伦的床边故事"),
    previewMusicInfo(5, "青花瓷", "周杰伦", "我很忙"),
)

/** 较长且标题首字多样，用于索引条与滚动条等完整功能预览 */
private val previewListFull = listOf(
    previewMusicInfo(1, "晴天", "周杰伦", "叶惠美"),
    previewMusicInfo(2, "稻香", "周杰伦", "魔杰座"),
    previewMusicInfo(3, "七里香", "周杰伦", "七里香"),
    previewMusicInfo(4, "告白气球", "周杰伦", "周杰伦的床边故事"),
    previewMusicInfo(5, "青花瓷", "周杰伦", "我很忙"),
    previewMusicInfo(6, "以父之名", "周杰伦", "叶惠美"),
    previewMusicInfo(7, "夜曲", "周杰伦", "十一月的萧邦"),
    previewMusicInfo(8, "简单爱", "周杰伦", "范特西"),
    previewMusicInfo(9, "东风破", "周杰伦", "叶惠美"),
    previewMusicInfo(10, "轨迹", "周杰伦", "寻找周杰伦"),
    previewMusicInfo(11, "搁浅", "周杰伦", "七里香"),
    previewMusicInfo(12, "彩虹", "周杰伦", "我很忙"),
    previewMusicInfo(13, "不能说的秘密", "周杰伦", "不能说的秘密"),
    previewMusicInfo(14, "枫", "周杰伦", "十一月的萧邦"),
    previewMusicInfo(15, "爱在西元前", "周杰伦", "范特西"),
    previewMusicInfo(16, "Back to December", "Taylor Swift", "Speak Now"),
    previewMusicInfo(17, "Blinding Lights", "The Weeknd", "After Hours"),
)

@Preview(name = "MusicList Simple", showBackground = true)
@Composable
private fun MusicListPreviewSimple() {
    HearableMusicPlayerTheme(darkTheme = false) {
        MusicList(
            musicInfoList = previewList,
            config = defaultMusicListConfig().copy(
                header = HeaderConfig.Simple(
                    onOrderPlay = {},
                    onShufflePlay = {},
                ),
                item = ItemConfig(
                    variant = ItemVariant.Full,
                    fullOptions = FullItemOptions(showPinButton = true, showRemoveButton = true, showMenuButton = true),
                ),
            ),
        )
    }
}

@Preview(name = "MusicList Compact", showBackground = true)
@Composable
private fun MusicListPreviewCompact() {
    HearableMusicPlayerTheme(darkTheme = false) {
        MusicList(
            musicInfoList = previewList,
            config = defaultMusicListConfig().copy(
                header = HeaderConfig.None,
                item = ItemConfig(
                    showIndex = true,
                    variant = ItemVariant.Compact,
                    compactOptions = CompactItemOptions(),
                ),
                currentPlaying = CurrentPlayingConfig(index = 1),
            ),
        )
    }
}

@Preview(name = "MusicList Gallery", showBackground = true)
@Composable
private fun MusicListPreviewGallery() {
    HearableMusicPlayerTheme(darkTheme = false) {
        MusicList(
            musicInfoList = previewList,
            config = defaultMusicListConfig().copy(
                header = HeaderConfig.None,
                item = ItemConfig(variant = ItemVariant.Gallery),
            ),
        )
    }
}

@Preview(name = "MusicList Full Header", showBackground = true)
@Composable
private fun MusicListPreviewFullHeader() {
    HearableMusicPlayerTheme(darkTheme = false) {
        MusicList(
            musicInfoList = previewList,
            config = defaultMusicListConfig().copy(
                header = HeaderConfig.Full(
                    selectedGenre = "title",
                    selectedOrder = "ASC",
                    onFilterGenreChange = {},
                    onFilterOrderChange = {},
                    onOrderPlay = {},
                    onShufflePlay = {},
                ),
                item = ItemConfig(
                    variant = ItemVariant.Full,
                    fullOptions = FullItemOptions(showPinButton = true, showRemoveButton = true, showMenuButton = true),
                ),
                edit = EditConfig(enabled = true),
            ),
        )
    }
}

/** 展示最多信息与最完整功能：Full 头部、序号、置顶/移除/更多、编辑、索引条、滚动条、当前播放高亮、内容边距、长列表 */
@Preview(name = "MusicList 完整功能", showBackground = true)
@Composable
private fun MusicListPreviewFull() {
    HearableMusicPlayerTheme(darkTheme = false) {
        MusicList(
            musicInfoList = previewListFull,
            config = defaultMusicListConfig().copy(
                header = HeaderConfig.Full(
                    selectedGenre = "title",
                    selectedOrder = "ASC",
                    onFilterGenreChange = {},
                    onFilterOrderChange = {},
                    onOrderPlay = {},
                    onShufflePlay = {},
                ),
                item = ItemConfig(
                    showIndex = true,
                    indexFormat = { "${it + 1}" },
                    showCheckbox = true,
                    variant = ItemVariant.Full,
                    fullOptions = FullItemOptions(
                        showPinButton = true,
                        showRemoveButton = true,
                        showMenuButton = true,
                        extraMenuItems = listOf(stringResource(R.string.remove_from_list) to {}),
                    ),
                ),
                list = ListConfig(
                    bottomSpacerHeight = 64.dp,
                    enableLongPressToEnterEdit = true,
                ),
                edit = EditConfig(
                    enabled = true,
                    showToolbar = true,
                    toolbarActions = listOf(
                        EditToolbarAction.SelectAll,
                        EditToolbarAction.DeselectAll,
                        EditToolbarAction.Delete,
                        EditToolbarAction.AddToPlaylist,
                    ),
                    selectAllLabel = stringResource(R.string.select_all),
                    deselectAllLabel = stringResource(R.string.deselect_all),
                    selectedCountFormat = { stringResource(R.string.selected_count_format, it) },
                ),
                indexJump = IndexJumpConfig(
                    enabled = true,
                    letters = ('A'..'Z').toList() + listOf('#'),
                    letterToIndex = ::defaultLetterToIndex,
                ),
                scrollbar = ScrollbarConfig(
                    enabled = true,
                    width = 4.dp,
                    thumbMinHeight = 24.dp,
                ),
                currentPlaying = CurrentPlayingConfig(
                    index = 2,
                    autoScrollToCurrent = true,
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            ),
        )
    }
}

@Preview(name = "MusicList Dark", showBackground = true)
@Composable
private fun MusicListPreviewDark() {
    HearableMusicPlayerTheme(darkTheme = true) {
        MusicList(
            musicInfoList = previewList,
            config = defaultMusicListConfig().copy(
                header = HeaderConfig.Simple(onOrderPlay = {}, onShufflePlay = {}),
                item = ItemConfig(variant = ItemVariant.Full),
            ),
        )
    }
}
