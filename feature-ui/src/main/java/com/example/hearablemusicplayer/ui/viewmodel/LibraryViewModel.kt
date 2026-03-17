package com.example.hearablemusicplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hearablemusicplayer.domain.music.MusicInfo
import com.example.hearablemusicplayer.domain.music.usecase.GetAllMusicUseCase
import com.example.hearablemusicplayer.domain.music.usecase.GetDeletedMusicIdsGroupedByFolderUseCase
import com.example.hearablemusicplayer.domain.music.usecase.LoadMusicFromDeviceUseCase
import com.example.hearablemusicplayer.domain.music.usecase.RemoveFromLibraryUseCase
import com.example.hearablemusicplayer.domain.music.usecase.RestoreToLibraryUseCase
import com.example.hearablemusicplayer.domain.music.usecase.SyncMusicFromDeviceIncrementalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File

data class FolderInfo(
    val path: String,
    val songCount: Int
)

/** 已隐藏的文件夹：路径、歌曲数、用于恢复的 music id 列表。 */
data class HiddenFolderInfo(
    val path: String,
    val songCount: Int,
    val musicIds: List<Long>
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getAllMusicUseCase: GetAllMusicUseCase,
    private val loadMusicFromDeviceUseCase: LoadMusicFromDeviceUseCase,
    private val syncMusicFromDeviceIncrementalUseCase: SyncMusicFromDeviceIncrementalUseCase,
    private val removeFromLibraryUseCase: RemoveFromLibraryUseCase,
    private val restoreToLibraryUseCase: RestoreToLibraryUseCase,
    private val getDeletedMusicIdsGroupedByFolderUseCase: GetDeletedMusicIdsGroupedByFolderUseCase
) : ViewModel() {

    // 排序
    private val _orderBy = MutableStateFlow("title")
    val orderBy: MutableStateFlow<String> = _orderBy
    fun updateOrderBy(orderBy: String) {
        _orderBy.value = orderBy
    }
    
    private val _orderType = MutableStateFlow("ASC")
    val orderType: StateFlow<String> = _orderType
    fun updateOrderType(orderType: String) {
        _orderType.value = orderType
    }
    
    // 列表
    private val _allMusic = MutableStateFlow<List<MusicInfo>>(emptyList())
    val allMusic: StateFlow<List<MusicInfo>> = _allMusic
    
    fun getAllMusic() {
        viewModelScope.launch {
            _allMusic.value = getAllMusicUseCase(_orderBy.value, _orderType.value)
        }
    }

    /** 从曲库软删除指定 id 的歌曲，并刷新列表。 */
    fun removeFromLibrary(ids: List<Long>) {
        viewModelScope.launch {
            removeFromLibraryUseCase(ids)
            getAllMusic()
            loadHiddenFolders()
        }
    }

    /** 恢复已移除的歌曲到曲库，并刷新列表与已隐藏文件夹。 */
    fun restoreToLibrary(ids: List<Long>) {
        viewModelScope.launch {
            restoreToLibraryUseCase(ids)
            getAllMusic()
            loadHiddenFolders()
        }
    }

    /** 隐藏文件夹：将该路径下当前曲库中的歌曲批量软删除。 */
    fun hideFolder(folderPath: String) {
        viewModelScope.launch {
            val ids = _allMusic.value
                .filter { try { File(it.music.path).parent == folderPath } catch (e: Exception) { false } }
                .map { it.music.id }
            if (ids.isNotEmpty()) {
                removeFromLibraryUseCase(ids)
                getAllMusic()
                loadHiddenFolders()
            }
        }
    }

    /** 已隐藏的文件夹（含路径、歌曲数、id 列表，用于恢复）。 */
    private val _hiddenFolders = MutableStateFlow<List<HiddenFolderInfo>>(emptyList())
    val hiddenFolders: StateFlow<List<HiddenFolderInfo>> = _hiddenFolders

    /** 刷新已隐藏文件夹列表，设置页进入时或隐藏/恢复后调用。 */
    fun loadHiddenFolders() {
        viewModelScope.launch {
            val grouped = getDeletedMusicIdsGroupedByFolderUseCase()
            _hiddenFolders.value = grouped.map { (path, ids) ->
                HiddenFolderInfo(path = path, songCount = ids.size, musicIds = ids)
            }
        }
    }
    
    // 统计
    val musicCount: StateFlow<Int> = getAllMusicUseCase
        .getMusicCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
        
    val musicWithExtraCount: StateFlow<Int> = getAllMusicUseCase
        .getMusicWithExtraCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
        
    // 扫描
    val isScanning = loadMusicFromDeviceUseCase.isScanning()
    
    // 文件夹统计
    val scannedFolders: StateFlow<List<FolderInfo>> = _allMusic.map { list ->
        list.groupBy { music ->
            try {
                File(music.music.path).parent ?: "Unknown"
            } catch (e: Exception) {
                "Unknown"
            }
        }.map { (path, songs) ->
            FolderInfo(path, songs.size)
        }.sortedByDescending { it.songCount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    private val _scanErrorMessage = MutableStateFlow<String?>(null)
    val scanErrorMessage: StateFlow<String?> = _scanErrorMessage
    
    /**
     * 增量刷新音乐列表（推荐的日常操作）
     */
    fun refreshMusicList() {
        viewModelScope.launch(Dispatchers.IO) {
            syncMusicFromDeviceIncrementalUseCase()
                .onSuccess {
                    _scanErrorMessage.value = null
                    // 扫描完成后刷新列表
                    getAllMusic()
                }
                .onFailure { e ->
                    _scanErrorMessage.value = e.message ?: "扫描失败"
                }
        }
    }

    /**
     * 全量重建音乐库（清空并重新扫描）
     */
    fun fullRescan() {
        viewModelScope.launch(Dispatchers.IO) {
            loadMusicFromDeviceUseCase()
                .onSuccess {
                    _scanErrorMessage.value = null
                    getAllMusic()
                }
                .onFailure { e ->
                    _scanErrorMessage.value = e.message ?: "扫描失败"
                }
        }
    }
    
    init {
        getAllMusic()
    }
}
