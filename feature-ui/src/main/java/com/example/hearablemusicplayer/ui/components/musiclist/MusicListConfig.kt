package com.example.hearablemusicplayer.ui.components.musiclist

import net.sourceforge.pinyin4j.PinyinHelper
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.hearablemusicplayer.domain.music.MusicInfo
import androidx.compose.foundation.layout.PaddingValues

/**
 * 增强版音乐列表的入口配置，驱动头部、单项形态、编辑模式、索引跳转、滚动条与当前播放等行为。
 * 所有子配置在此汇总，调用方通过 copy 或预设方法按场景定制。
 */
data class MusicListConfig(
    val header: HeaderConfig,
    val item: ItemConfig,
    val list: ListConfig,
    val edit: EditConfig,
    val indexJump: IndexJumpConfig,
    val scrollbar: ScrollbarConfig,
    val currentPlaying: CurrentPlayingConfig,
    val callbacks: MusicListCallbacks,
    val contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp),
    val emptyContent: (@Composable () -> Unit)? = null,
    val loadingContent: (@Composable () -> Unit)? = null,
)

// ---------- Header ----------

/**
 * 头部区域配置：不显示 / 简单顺序+随机 / 完整排序筛选+播放 / 自定义内容。
 * 典型场景：None 用于内嵌列表；Simple 用于播放列表页；Full 用于曲库/列表页；Custom 由调用方完全自定义。
 */
sealed class HeaderConfig {
    data object None : HeaderConfig()
    data class Simple(
        val onOrderPlay: () -> Unit,
        val onShufflePlay: () -> Unit,
    ) : HeaderConfig()
    data class Full(
        val selectedGenre: String,
        val selectedOrder: String,
        val onFilterGenreChange: (String) -> Unit,
        val onFilterOrderChange: (String) -> Unit,
        val onOrderPlay: () -> Unit,
        val onShufflePlay: () -> Unit,
    ) : HeaderConfig()
    data class Custom(
        val content: @Composable () -> Unit,
    ) : HeaderConfig()
}

// ---------- Item ----------

/**
 * 单项配置：是否显示序号/选择框、形态（Full/Compact/Gallery/Custom）及各形态选项。
 * - [showIndex] 为 true 时在左侧显示序号，[indexFormat] 控制文案（默认 1-based）。
 * - [showCheckbox] 与编辑模式配合，用于多选。
 * - [variant] 决定行布局：Full/Compact/Gallery 均仅支持三种按钮（置顶、移除、更多）；Custom 使用 [customContent]。
 */
data class ItemConfig(
    val showIndex: Boolean = false,
    val indexFormat: (Int) -> String = { "${it + 1}" },
    val showCheckbox: Boolean = false,
    val variant: ItemVariant = ItemVariant.Full,
    val fullOptions: FullItemOptions? = null,
    val compactOptions: CompactItemOptions? = null,
    val galleryOptions: GalleryItemOptions? = null,
    val customContent: (@Composable (MusicInfo, Int, Boolean) -> Unit)? = null,
    val itemHeight: Dp? = null,
)

sealed class ItemVariant {
    data object Full : ItemVariant()
    data object Compact : ItemVariant()
    data object Gallery : ItemVariant()
    data object Custom : ItemVariant()
}

/** Item 仅支持三种按钮：置顶、移除、更多。 */
data class FullItemOptions(
    val showPinButton: Boolean = true,
    val showRemoveButton: Boolean = true,
    val showMenuButton: Boolean = true,
    val extraMenuItems: List<Pair<String, () -> Unit>> = emptyList(),
)

data class CompactItemOptions(
    val showPinButton: Boolean = true,
    val showRemoveButton: Boolean = true,
    val showMenuButton: Boolean = true,
    val extraMenuItems: List<Pair<String, () -> Unit>> = emptyList(),
)

data class GalleryItemOptions(
    val showPinButton: Boolean = true,
    val showRemoveButton: Boolean = true,
    val showMenuButton: Boolean = true,
    val extraMenuItems: List<Pair<String, () -> Unit>> = emptyList(),
)

// ---------- List ----------

/**
 * 列表整体配置：LazyColumn 的 key、底部占位高度、是否支持长按列表项进入编辑模式。
 */
data class ListConfig(
    val key: (Int, MusicInfo) -> Any = { i, m -> "${m.music.id}_$i" },
    val bottomSpacerHeight: Dp = 64.dp,
    val enableLongPressToEnterEdit: Boolean = false,
)

// ---------- Edit ----------

/**
 * 编辑模式配置：是否启用、是否显示工具栏、[toolbarActions] 决定显示的批量操作、文案可本地化。
 */
data class EditConfig(
    val enabled: Boolean = false,
    val showToolbar: Boolean = true,
    val toolbarActions: List<EditToolbarAction> = defaultEditToolbarActions(),
    val selectAllLabel: String = "全选",
    val deselectAllLabel: String = "取消全选",
    val confirmLabel: String = "确认",
    val selectedCountFormat: (Int) -> String = { it.toString() },
)

enum class EditToolbarAction {
    SelectAll,
    DeselectAll,
    Delete,
    AddToPlaylist,
    RemoveFromPlaylist,
    MoveToTop,
    Share,
}

fun defaultEditToolbarActions(): List<EditToolbarAction> = listOf(
    EditToolbarAction.SelectAll,
    EditToolbarAction.DeselectAll,
    EditToolbarAction.Delete,
    EditToolbarAction.AddToPlaylist,
)

// ---------- IndexJump ----------

/**
 * 索引跳转配置：是否启用、[mode] 预留拼音等、[letters] 为右侧显示的字符、[letterToIndex] 由调用方或 [defaultLetterToIndex] 提供。
 *
 * **列表排序时**：索引条和单项序号均基于**当前传入的 [musicInfoList] 顺序**。排序后调用方应传入重新排序后的列表；
 * 组件会通过 [remember(musicInfoList)] 重新计算字母→首项下标，索引条与 1、2、3… 序号会自然跟随新顺序。
 */
data class IndexJumpConfig(
    val enabled: Boolean = false,
    val mode: IndexJumpMode = IndexJumpMode.FirstLetter,
    val letters: List<Char> = ('A'..'Z').toList() + listOf('#'),
    val letterToIndex: (List<MusicInfo>) -> Map<Char, Int>,
)

enum class IndexJumpMode {
    FirstLetter,
    PinyinInitial,
}

/**
 * 标题首字符对应的索引字母：英文字母取大写；中文取首字默认拼音的首字母；数字及其他归为 '#'。
 */
private fun titleToIndexLetter(title: String): Char {
    val c = title.firstOrNull() ?: return '#'
    return when {
        c in 'a'..'z' -> c.uppercaseChar()
        c in 'A'..'Z' -> c
        c in '0'..'9' -> '#'
        else -> {
            // 中文等：取首字默认拼音的首字母
            val pyArray = PinyinHelper.toHanyuPinyinStringArray(c) ?: return '#'
            val firstPy = pyArray.firstOrNull() ?: return '#'
            firstPy.firstOrNull()?.takeIf { it.isLetter() }?.uppercaseChar() ?: '#'
        }
    }
}

/**
 * 按标题首字母/首字分组，返回「字母 -> 该组第一项在列表中的 index」映射。
 * 英文取首字母；中文取首字默认拼音首字母；数字及其他归为 #。依赖传入 [list] 的当前顺序。
 */
fun defaultLetterToIndex(list: List<MusicInfo>): Map<Char, Int> {
    val map = mutableMapOf<Char, Int>()
    list.forEachIndexed { index, info ->
        val char = titleToIndexLetter(info.music.title)
        if (!map.containsKey(char)) map[char] = index
    }
    return map
}

// ---------- Scrollbar ----------

/**
 * 垂直滚动条样式与显隐；颜色为 null 时使用主题或半透明默认色。
 */
data class ScrollbarConfig(
    val enabled: Boolean = false,
    val width: Dp = 4.dp,
    val thumbMinHeight: Dp = 24.dp,
    val cornerRadius: Dp = 2.dp,
    val trackColor: Color? = null,
    val thumbColor: Color? = null,
)

// ---------- CurrentPlaying ----------

/**
 * 当前播放项：索引、是否自动滚动、滚动偏移。
 * 当前播放项始终高亮背景（不可配置）。列表排序后，调用方应将 [index] 更新为当前播放曲目在新列表中的下标，以便高亮与自动滚动正确。
 */
data class CurrentPlayingConfig(
    val index: Int? = null,
    val autoScrollToCurrent: Boolean = true,
    val scrollOffsetForCenter: Dp? = null,
)

// ---------- Callbacks ----------

/**
 * 单项与批量操作回调汇总。使用 [MusicListCallbacksAdapter] 可只重写需要的回调。
 */
interface MusicListCallbacks {
    fun onItemClick(musicInfo: MusicInfo, index: Int) {}
    fun onAddToPlaylist(musicInfo: MusicInfo) {}
    fun onMenuClick(musicInfo: MusicInfo) {}
    fun onRemoveFromPlaylist(musicInfo: MusicInfo) {}
    fun onMoveUp(index: Int) {}
    fun onMoveDown(index: Int) {}
    fun onPinToTop(musicInfo: MusicInfo) {}
    fun onRemove(musicInfo: MusicInfo) {}
    fun onEnterEditMode() {}
    fun onExitEditMode() {}
    fun onSelectionChange(selectedIds: Set<Long>) {}
    fun onBatchDelete(selectedIds: Set<Long>) {}
    fun onBatchAddToPlaylist(selectedIds: Set<Long>) {}
    fun onBatchRemoveFromPlaylist(selectedIds: Set<Long>) {}
    fun onBatchMoveToTop(selectedIds: Set<Long>) {}
    fun onBatchShare(selectedIds: Set<Long>) {}
}

/**
 * 默认空实现的适配器，调用方仅 override 需要的回调。
 */
open class MusicListCallbacksAdapter : MusicListCallbacks

// ---------- 默认与预设 ----------

/**
 * 返回默认 [MusicListConfig]：无头部、Full 单项、无编辑/索引/滚动条、[defaultLetterToIndex] 用于索引跳转。
 * 调用方可通过 [MusicListConfig.copy] 按需覆盖。
 */
fun defaultMusicListConfig(
    callbacks: MusicListCallbacks = MusicListCallbacksAdapter(),
): MusicListConfig = MusicListConfig(
    header = HeaderConfig.None,
    item = ItemConfig(),
    list = ListConfig(),
    edit = EditConfig(),
    indexJump = IndexJumpConfig(letterToIndex = ::defaultLetterToIndex),
    scrollbar = ScrollbarConfig(),
    currentPlaying = CurrentPlayingConfig(),
    callbacks = callbacks,
)

/**
 * 播放列表场景预设：Simple 头部、Full 单项、可编辑、可显示序号与选择框、支持长按进入编辑。
 */
fun playlistPresetMusicListConfig(
    onOrderPlay: () -> Unit,
    onShufflePlay: () -> Unit,
    callbacks: MusicListCallbacks,
): MusicListConfig = defaultMusicListConfig(callbacks).copy(
    header = HeaderConfig.Simple(onOrderPlay = onOrderPlay, onShufflePlay = onShufflePlay),
    item = ItemConfig(
        showIndex = false,
        showCheckbox = true,
        variant = ItemVariant.Full,
        fullOptions = FullItemOptions(showPinButton = true, showRemoveButton = true, showMenuButton = true),
    ),
    list = ListConfig(enableLongPressToEnterEdit = true),
    edit = EditConfig(enabled = true),
)

/**
 * 图库/简洁列表预设：无头部、Gallery 单项、无编辑。
 */
fun galleryPresetMusicListConfig(callbacks: MusicListCallbacks): MusicListConfig =
    defaultMusicListConfig(callbacks).copy(
        header = HeaderConfig.None,
        item = ItemConfig(variant = ItemVariant.Gallery, galleryOptions = GalleryItemOptions()),
    )

/**
 * 曲库/列表页预设：Full 头部（排序筛选）、Full 单项、可编辑、可选索引与滚动条。
 */
fun libraryPresetMusicListConfig(
    selectedGenre: String,
    selectedOrder: String,
    onFilterGenreChange: (String) -> Unit,
    onFilterOrderChange: (String) -> Unit,
    onOrderPlay: () -> Unit,
    onShufflePlay: () -> Unit,
    callbacks: MusicListCallbacks,
): MusicListConfig = defaultMusicListConfig(callbacks).copy(
    header = HeaderConfig.Full(
        selectedGenre = selectedGenre,
        selectedOrder = selectedOrder,
        onFilterGenreChange = onFilterGenreChange,
        onFilterOrderChange = onFilterOrderChange,
        onOrderPlay = onOrderPlay,
        onShufflePlay = onShufflePlay,
    ),
    item = ItemConfig(
        showCheckbox = true,
        variant = ItemVariant.Full,
        fullOptions = FullItemOptions(),
    ),
    list = ListConfig(enableLongPressToEnterEdit = true),
    edit = EditConfig(enabled = true),
    indexJump = IndexJumpConfig(enabled = true, letterToIndex = ::defaultLetterToIndex),
    scrollbar = ScrollbarConfig(enabled = true),
)
