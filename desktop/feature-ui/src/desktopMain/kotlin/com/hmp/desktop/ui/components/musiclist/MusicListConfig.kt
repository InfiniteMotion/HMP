package com.hmp.desktop.ui.components.musiclist

import java.util.Calendar
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hmp.domain.music.MusicInfo
import com.hmp.data.util.stringToPinyinSortKey
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

sealed class HeaderConfig {
    data object None : HeaderConfig()
    data class Simple(
        val onOrderPlay: () -> Unit,
        val onShufflePlay: () -> Unit,
        val trailing: (@Composable () -> Unit)? = null,
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

data class FullItemOptions(
    val showPinButton: Boolean = true,
    val showRemoveButton: Boolean = true,
    val showMenuButton: Boolean = true,
    val showAddToPlaylistInMenu: Boolean = false,
    val extraMenuItems: List<Pair<String, () -> Unit>> = emptyList(),
)

data class CompactItemOptions(
    val showPinButton: Boolean = true,
    val showRemoveButton: Boolean = true,
    val showMenuButton: Boolean = true,
    val showAddToPlaylistInMenu: Boolean = false,
    val extraMenuItems: List<Pair<String, () -> Unit>> = emptyList(),
)

data class GalleryItemOptions(
    val showPinButton: Boolean = true,
    val showRemoveButton: Boolean = true,
    val showMenuButton: Boolean = true,
    val showAddToPlaylistInMenu: Boolean = false,
    val extraMenuItems: List<Pair<String, () -> Unit>> = emptyList(),
)

// ---------- List ----------

data class ListConfig(
    val key: (Int, MusicInfo) -> Any = { i, m -> "${m.music.id}_$i" },
    val bottomSpacerHeight: Dp = 88.dp,
    val enableLongPressToEnterEdit: Boolean = false,
)

// ---------- Edit ----------

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

data class IndexJumpConfig(
    val enabled: Boolean = false,
    val mode: IndexJumpMode = IndexJumpMode.FirstLetter,
    val letters: List<Char> = ('A'..'Z').toList() + listOf('#'),
    val letterToIndex: (List<MusicInfo>) -> Map<Char, Int>,
    val smartAnchor: ((List<MusicInfo>) -> Pair<List<String>, Map<Int, Int>>)? = null,
    val orderType: String = "ASC",
) {
    val isAnchorMode: Boolean get() = smartAnchor != null
}

enum class IndexJumpMode {
    FirstLetter,
    PinyinInitial,
}

private fun titleToIndexLetter(title: String): Char {
    val c = title.firstOrNull() ?: return '#'
    return when {
        c in 'a'..'z' -> c.uppercaseChar()
        c in 'A'..'Z' -> c
        c in '0'..'9' -> '#'
        else -> {
            val pinyinKey = stringToPinyinSortKey(title)
            pinyinKey.firstOrNull()?.uppercaseChar() ?: '#'
        }
    }
}

fun defaultLetterToIndex(list: List<MusicInfo>): Map<Char, Int> =
    letterToIndexByString(list) { it.music.title }

fun letterToIndexByString(list: List<MusicInfo>, getKey: (MusicInfo) -> String): Map<Char, Int> {
    val map = mutableMapOf<Char, Int>()
    list.forEachIndexed { index, info ->
        val char = titleToIndexLetter(getKey(info))
        if (!map.containsKey(char)) map[char] = index
    }
    return map
}

private const val SMART_ANCHOR_MIN = 8
private const val SMART_ANCHOR_MAX = 16

private fun lettersForOrderType(orderType: String): List<Char> =
    if (orderType.uppercase() == "DESC") ('Z' downTo 'A').toList() + listOf('#')
    else ('A'..'Z').toList() + listOf('#')

fun indexJumpConfigForOrderBy(orderBy: String, orderType: String = "ASC"): IndexJumpConfig {
    val letters = lettersForOrderType(orderType)
    return when (orderBy) {
        "title" -> IndexJumpConfig(
            enabled = true,
            letters = letters,
            letterToIndex = ::defaultLetterToIndex,
            orderType = orderType,
        )
        "artist" -> IndexJumpConfig(
            enabled = true,
            letters = letters,
            letterToIndex = { list -> letterToIndexByString(list) { it.music.artist } },
            orderType = orderType,
        )
        "album" -> IndexJumpConfig(
            enabled = true,
            letters = letters,
            letterToIndex = { list -> letterToIndexByString(list) { it.music.album } },
            orderType = orderType,
        )
        "duration" -> IndexJumpConfig(
            enabled = true,
            letterToIndex = { emptyMap() },
            smartAnchor = { list ->
                computeSmartAnchors(list, { it.music.duration }, ::formatDurationMs)
            },
            orderType = orderType,
        )
        "fileSize" -> IndexJumpConfig(
            enabled = true,
            letterToIndex = { emptyMap() },
            smartAnchor = { list ->
                computeSmartAnchors(
                    list,
                    { it.extra?.fileSize ?: 0L },
                    ::formatFileSize,
                )
            },
            orderType = orderType,
        )
        "playCount" -> IndexJumpConfig(
            enabled = true,
            letterToIndex = { emptyMap() },
            smartAnchor = { list ->
                computeSmartAnchors(
                    list,
                    { (it.userInfo?.playCount ?: 0).toLong() },
                    { it.toString() },
                )
            },
            orderType = orderType,
        )
        "id" -> IndexJumpConfig(
            enabled = true,
            letterToIndex = { emptyMap() },
            smartAnchor = { list ->
                val dateResult = computeDateSmartAnchors(list, orderType)
                val (labels, _) = dateResult
                if (labels.size <= 1 && labels.getOrNull(0) == "未知") computeDateStylePositionAnchors(list, orderType)
                else dateResult
            },
            orderType = orderType,
        )
        "date" -> IndexJumpConfig(
            enabled = true,
            letterToIndex = { emptyMap() },
            smartAnchor = { list -> computeDateSmartAnchors(list, orderType) },
            orderType = orderType,
        )
        "year" -> IndexJumpConfig(
            enabled = true,
            letterToIndex = { emptyMap() },
            smartAnchor = { list -> computeDateSmartAnchors(list, orderType) },
            orderType = orderType,
        )
        else -> IndexJumpConfig(
            enabled = true,
            letters = letters,
            letterToIndex = ::defaultLetterToIndex,
            orderType = orderType,
        )
    }
}

private fun formatDurationMs(ms: Long): String {
    val totalSeconds = (ms / 1000).toInt()
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%d:%02d".format(m, s)
}

private fun buildAnchorToIndex(
    list: List<MusicInfo>,
    anchorValues: List<Long>,
    getValue: (MusicInfo) -> Long,
): Map<Int, Int> {
    if (anchorValues.isEmpty()) return emptyMap()
    val result = mutableMapOf<Int, Int>()
    for ((listIndex, info) in list.withIndex()) {
        val v = getValue(info)
        val bucket = when {
            v < anchorValues.first() -> 0
            v >= anchorValues.last() -> anchorValues.lastIndex
            else -> {
                val i = anchorValues.indexOfFirst { it > v }
                if (i <= 0) 0 else i - 1
            }
        }
        if (bucket !in result) result[bucket] = listIndex
    }
    return result
}

private fun computeSmartAnchors(
    list: List<MusicInfo>,
    getValue: (MusicInfo) -> Long,
    formatLabel: (Long) -> String,
    minAnchors: Int = SMART_ANCHOR_MIN,
    maxAnchors: Int = SMART_ANCHOR_MAX,
): Pair<List<String>, Map<Int, Int>> {
    if (list.isEmpty()) return Pair(emptyList(), emptyMap())
    val values = list.map(getValue).filter { it >= 0 }
    if (values.isEmpty()) return Pair(emptyList(), emptyMap())
    val sorted = values.sorted()
    val n = sorted.size
    if (n == 0) return Pair(emptyList(), emptyMap())
    val anchorCount = if (list.size > 100) maxAnchors else (minAnchors + (list.size / 200).coerceAtMost(maxAnchors - minAnchors)).coerceIn(minAnchors, maxAnchors)
    val anchorValues = if (n == 1) {
        listOf(sorted[0])
    } else {
        (0 until anchorCount).map { i ->
            val idx = (i.toLong() * (n - 1) / (anchorCount - 1).coerceAtLeast(1)).toInt().coerceIn(0, n - 1)
            sorted[idx]
        }.distinct()
    }
    if (anchorValues.isEmpty()) return Pair(emptyList(), emptyMap())
    val labels = anchorValues.map(formatLabel)
    val map = buildAnchorToIndex(list, anchorValues, getValue)
    return Pair(labels, map)
}

private fun formatFileSize(bytes: Long): String = when {
    bytes < 1024 -> "${bytes}B"
    bytes < 1024 * 1024 -> "${bytes / 1024}KB"
    else -> "${bytes / (1024 * 1024)}MB"
}

private fun computeIdSmartAnchors(list: List<MusicInfo>, orderType: String = "ASC"): Pair<List<String>, Map<Int, Int>> {
    if (list.isEmpty()) return Pair(emptyList(), emptyMap())
    val n = list.size
    if (n <= 1) return Pair(listOf("早"), mapOf(0 to 0))
    val anchorCount = if (n > 100) SMART_ANCHOR_MAX else when {
        n < 20 -> 8
        n < 40 -> 10
        n < 60 -> 12
        else -> SMART_ANCHOR_MAX
    }.coerceIn(SMART_ANCHOR_MIN, SMART_ANCHOR_MAX)
    val positionLabels = listOf("早", "1/4", "1/2", "3/4", "末")
    val labels = (0 until anchorCount).map { i ->
        positionLabels.getOrElse(i) { "${i + 1}/${anchorCount}" }
    }
    val desc = orderType.uppercase() == "DESC"
    val map = (0 until anchorCount).associate { i ->
        val pos = (i.toLong() * (n - 1) / (anchorCount - 1).coerceAtLeast(1)).toInt().coerceIn(0, n - 1)
        val listIndex = if (desc) n - 1 - pos else pos
        i to listIndex
    }
    return Pair(labels, map)
}

private fun computeDateSmartAnchors(
    list: List<MusicInfo>,
    orderType: String,
): Pair<List<String>, Map<Int, Int>> {
    if (list.isEmpty()) return Pair(emptyList(), emptyMap())
    val cal = Calendar.getInstance()
    fun getYearMonth(info: MusicInfo): Pair<Int, Int> {
        val ts = info.extra?.date ?: return -1 to 1
        cal.timeInMillis = ts
        return cal.get(Calendar.YEAR) to (cal.get(Calendar.MONTH) + 1)
    }
    val pairs = list.map(::getYearMonth).distinct()
    if (pairs.isEmpty()) return Pair(emptyList(), emptyMap())
    val sorted = if (orderType.uppercase() == "DESC") {
        pairs.sortedWith(compareBy({ -it.first }, { -it.second }))
    } else {
        pairs.sortedWith(compareBy({ it.first }, { it.second }))
    }
    val ordered = sorted.filter { it.first >= 0 }.let { known ->
        if (sorted.any { it.first == -1 }) known + (-1 to 1) else known
    }
    if (ordered.isEmpty()) return Pair(emptyList(), emptyMap())
    val labels = ordered.map { (y, m) ->
        if (m == 1) (if (y == -1) "未知" else y.toString()) else "%02d".format(m)
    }
    if (labels.size <= 1 && labels.getOrNull(0) == "未知") {
        return computeDateStylePositionAnchors(list, orderType)
    }
    val map = ordered.mapIndexed { index, pair ->
        index to list.indexOfFirst { getYearMonth(it) == pair }
    }.filter { it.second >= 0 }.toMap()
    return Pair(labels, map)
}

private fun computeAddOrderYearMonthAnchors(
    list: List<MusicInfo>,
    orderType: String,
): Pair<List<String>, Map<Int, Int>> {
    if (list.isEmpty()) return Pair(emptyList(), emptyMap())
    val n = list.size
    val cal = Calendar.getInstance()
    val endMs = cal.timeInMillis
    cal.add(Calendar.YEAR, -2)
    val startMs = cal.timeInMillis
    val spanMs = (endMs - startMs).toDouble().coerceAtLeast(1.0)
    val desc = orderType.uppercase() == "DESC"
    fun virtualYearMonth(listIndex: Int): Pair<Int, Int> {
        val ratio = if (n <= 1) 1.0 else {
            val r = listIndex.toDouble() / (n - 1).coerceAtLeast(1)
            if (desc) 1.0 - r else r
        }
        val ts = (startMs + ratio * spanMs).toLong()
        cal.timeInMillis = ts
        return cal.get(Calendar.YEAR) to (cal.get(Calendar.MONTH) + 1)
    }
    val indexToYm = (0 until n).map { i -> i to virtualYearMonth(i) }
    val ordered = indexToYm.map { it.second }.distinct().let { distinct ->
        if (desc) distinct.sortedWith(compareBy({ -it.first }, { -it.second }))
        else distinct.sortedWith(compareBy({ it.first }, { it.second }))
    }
    if (ordered.isEmpty()) return Pair(emptyList(), emptyMap())
    val labels = ordered.map { (y, m) -> if (m == 1) y.toString() else "%02d".format(m) }
    val map = ordered.mapIndexed { anchorIndex, pair ->
        anchorIndex to indexToYm.indexOfFirst { it.second == pair }
    }.filter { it.second >= 0 }.toMap()
    return Pair(labels, map)
}

private fun computeDateStylePositionAnchors(
    list: List<MusicInfo>,
    orderType: String,
): Pair<List<String>, Map<Int, Int>> {
    if (list.isEmpty()) return Pair(emptyList(), emptyMap())
    val n = list.size
    if (n <= 1) return Pair(listOf(Calendar.getInstance().get(Calendar.YEAR).toString()), mapOf(0 to 0))
    val anchorCount = 13
    val cal = Calendar.getInstance()
    val currentYear = cal.get(Calendar.YEAR)
    val labels = listOf(currentYear.toString()) + (1..12).map { "%02d".format(it) }
    val desc = orderType.uppercase() == "DESC"
    val map = (0 until anchorCount).associate { i ->
        val pos = (i.toLong() * (n - 1) / (anchorCount - 1).coerceAtLeast(1)).toInt().coerceIn(0, n - 1)
        val listIndex = if (desc) n - 1 - pos else pos
        i to listIndex
    }
    return Pair(labels, map)
}

data class ScrollbarConfig(
    val enabled: Boolean = false,
    val width: Dp = 4.dp,
    val thumbMinHeight: Dp = 24.dp,
    val cornerRadius: Dp = 2.dp,
    val trackColor: Color? = null,
    val thumbColor: Color? = null,
)

// ---------- CurrentPlaying ----------

data class CurrentPlayingConfig(
    val index: Int? = null,
    val autoScrollToCurrent: Boolean = true,
    val scrollOffsetForCenter: Dp? = null,
)

// ---------- Callbacks ----------

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

open class MusicListCallbacksAdapter : MusicListCallbacks

// ---------- 默认与预设 ----------

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

fun playlistPresetMusicListConfig(
    onOrderPlay: () -> Unit,
    onShufflePlay: () -> Unit,
    callbacks: MusicListCallbacks,
    headerTrailing: (@Composable () -> Unit)? = null,
): MusicListConfig = defaultMusicListConfig(callbacks).copy(
    header = HeaderConfig.Simple(onOrderPlay = onOrderPlay, onShufflePlay = onShufflePlay, trailing = headerTrailing),
    item = ItemConfig(
        showIndex = true,
        showCheckbox = true,
        variant = ItemVariant.Full,
        fullOptions = FullItemOptions(showPinButton = true, showRemoveButton = true, showMenuButton = true),
    ),
    list = ListConfig(enableLongPressToEnterEdit = true),
    edit = EditConfig(enabled = true),
)

fun galleryPresetMusicListConfig(callbacks: MusicListCallbacks): MusicListConfig =
    defaultMusicListConfig(callbacks).copy(
        header = HeaderConfig.None,
        item = ItemConfig(variant = ItemVariant.Gallery, galleryOptions = GalleryItemOptions()),
    )

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
