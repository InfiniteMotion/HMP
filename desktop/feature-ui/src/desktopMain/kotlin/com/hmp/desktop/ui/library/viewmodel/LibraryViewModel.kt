package com.hmp.desktop.ui.library.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hmp.domain.music.MusicInfo
import com.hmp.domain.music.usecase.GetAllMusicUseCase
import com.hmp.domain.music.usecase.GetDeletedMusicIdsGroupedByFolderUseCase
import com.hmp.domain.music.usecase.LoadMusicFromDeviceUseCase
import com.hmp.domain.music.usecase.RemoveFromLibraryUseCase
import com.hmp.domain.music.usecase.RestoreToLibraryUseCase
import com.hmp.domain.music.usecase.SyncMusicFromDeviceIncrementalUseCase
import com.hmp.domain.setting.SettingsRepository
import com.hmp.domain.setting.model.ScanDirectoryConfig

import com.hmp.desktop.ui.common.util.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File

data class FolderInfo(
    val path: String,
    val songCount: Int
)

data class HiddenFolderInfo(
    val path: String,
    val songCount: Int,
    val musicIds: List<Long>
)

data class ScanResult(
    val musicList: List<MusicInfo> = emptyList(),
    val scannedFolderCount: Int = 0
)

class LibraryViewModel(
    private val getAllMusicUseCase: GetAllMusicUseCase,
    private val loadMusicFromDeviceUseCase: LoadMusicFromDeviceUseCase,
    private val syncMusicFromDeviceIncrementalUseCase: SyncMusicFromDeviceIncrementalUseCase,
    private val removeFromLibraryUseCase: RemoveFromLibraryUseCase,
    private val restoreToLibraryUseCase: RestoreToLibraryUseCase,
    private val getDeletedMusicIdsGroupedByFolderUseCase: GetDeletedMusicIdsGroupedByFolderUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

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

    private val _allMusic = MutableStateFlow<List<MusicInfo>>(emptyList())
    val allMusic: StateFlow<List<MusicInfo>> = _allMusic

    private val _scanState = MutableStateFlow<UiState<ScanResult>>(UiState.Idle)
    val scanState: StateFlow<UiState<ScanResult>> = _scanState

    fun getAllMusic() {
        viewModelScope.launch {
            _allMusic.value = getAllMusicUseCase(_orderBy.value, _orderType.value)
        }
    }

    fun removeFromLibrary(ids: List<Long>) {
        viewModelScope.launch {
            removeFromLibraryUseCase(ids)
            getAllMusic()
            loadHiddenFolders()
        }
    }

    fun restoreToLibrary(ids: List<Long>) {
        viewModelScope.launch {
            restoreToLibraryUseCase(ids)
            getAllMusic()
            loadHiddenFolders()
        }
    }

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

    private val _hiddenFolders = MutableStateFlow<List<HiddenFolderInfo>>(emptyList())
    val hiddenFolders: StateFlow<List<HiddenFolderInfo>> = _hiddenFolders

    private val _scanDirectoryConfig = MutableStateFlow(ScanDirectoryConfig())
    val scanDirectoryConfig: StateFlow<ScanDirectoryConfig> = _scanDirectoryConfig

    fun loadHiddenFolders() {
        viewModelScope.launch {
            val grouped = getDeletedMusicIdsGroupedByFolderUseCase()
            _hiddenFolders.value = grouped.map { (path, ids) ->
                HiddenFolderInfo(path = path, songCount = ids.size, musicIds = ids)
            }
        }
    }

    val musicCount: StateFlow<Int> = getAllMusicUseCase
        .getMusicCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val musicWithExtraCount: StateFlow<Int> = getAllMusicUseCase
        .getMusicWithExtraCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val isScanning = loadMusicFromDeviceUseCase.isScanning()

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

    fun refreshMusicList() {
        viewModelScope.launch(Dispatchers.IO) {
            _scanState.value = UiState.Loading
            syncMusicFromDeviceIncrementalUseCase()
                .onSuccess {
                    val musicList = getAllMusicUseCase(_orderBy.value, _orderType.value)
                    _allMusic.value = musicList
                    val folderCount = musicList.mapNotNull { music ->
                        try { File(music.music.path).parent } catch (e: Exception) { null }
                    }.distinct().size
                    _scanState.value = UiState.Success(ScanResult(musicList, folderCount))
                }
                .onFailure { e ->
                    _scanState.value = UiState.Error(e.message ?: "Scan failed")
                }
        }
    }

    fun fullRescan() {
        viewModelScope.launch(Dispatchers.IO) {
            _scanState.value = UiState.Loading
            loadMusicFromDeviceUseCase()
                .onSuccess {
                    val musicList = getAllMusicUseCase(_orderBy.value, _orderType.value)
                    _allMusic.value = musicList
                    val folderCount = musicList.mapNotNull { music ->
                        try { File(music.music.path).parent } catch (e: Exception) { null }
                    }.distinct().size
                    _scanState.value = UiState.Success(ScanResult(musicList, folderCount))
                }
                .onFailure { e ->
                    _scanState.value = UiState.Error(e.message ?: "Scan failed")
                }
        }
    }

    init {
        getAllMusic()
        loadScanDirectoryConfig()
    }

    private fun loadScanDirectoryConfig() {
        viewModelScope.launch {
            settingsRepository.scanDirectoryConfig.collect {
                _scanDirectoryConfig.value = it
            }
        }
    }

    fun addScanDirectory(path: String) {
        viewModelScope.launch {
            val current = _scanDirectoryConfig.value
            if (path !in current.scanDirectories) {
                val updated = current.copy(scanDirectories = current.scanDirectories + path)
                settingsRepository.saveScanDirectoryConfig(updated)
            }
        }
    }

    fun removeScanDirectory(path: String) {
        viewModelScope.launch {
            val current = _scanDirectoryConfig.value
            val updated = current.copy(scanDirectories = current.scanDirectories - path)
            settingsRepository.saveScanDirectoryConfig(updated)
        }
    }

    fun addBlockedDirectory(path: String) {
        viewModelScope.launch {
            val current = _scanDirectoryConfig.value
            if (path !in current.blockedDirectories) {
                val updated = current.copy(blockedDirectories = current.blockedDirectories + path)
                settingsRepository.saveScanDirectoryConfig(updated)
            }
        }
    }

    fun removeBlockedDirectory(path: String) {
        viewModelScope.launch {
            val current = _scanDirectoryConfig.value
            val updated = current.copy(blockedDirectories = current.blockedDirectories - path)
            settingsRepository.saveScanDirectoryConfig(updated)
        }
    }
}
