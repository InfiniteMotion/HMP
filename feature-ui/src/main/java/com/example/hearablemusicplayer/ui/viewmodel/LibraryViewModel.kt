package com.example.hearablemusicplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hearablemusicplayer.domain.music.MusicInfo
import com.example.hearablemusicplayer.domain.music.usecase.GetAllMusicUseCase
import com.example.hearablemusicplayer.domain.music.usecase.LoadMusicFromDeviceUseCase
import com.example.hearablemusicplayer.domain.music.usecase.SyncMusicFromDeviceIncrementalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getAllMusicUseCase: GetAllMusicUseCase,
    private val loadMusicFromDeviceUseCase: LoadMusicFromDeviceUseCase,
    private val syncMusicFromDeviceIncrementalUseCase: SyncMusicFromDeviceIncrementalUseCase
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
    
    // 统计
    val musicCount: StateFlow<Int> = getAllMusicUseCase
        .getMusicCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
        
    val musicWithExtraCount: StateFlow<Int> = getAllMusicUseCase
        .getMusicWithExtraCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
        
    // 扫描
    val isScanning = loadMusicFromDeviceUseCase.isScanning()
    
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
