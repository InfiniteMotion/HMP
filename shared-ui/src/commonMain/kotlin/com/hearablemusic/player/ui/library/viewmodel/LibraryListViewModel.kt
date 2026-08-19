package com.hearablemusic.player.ui.library.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hmp.domain.music.MusicInfo
import com.hmp.domain.music.usecase.GetAllMusicUseCase
import com.hearablemusic.player.ui.common.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 列表主路径 ViewModel（commonMain，第 2b 步）。
 *
 * 仅为新层首页列表提供「全量音乐 + 排序」读取；点击播放为占位空实现（第 3 步接 PlaybackController）。
 * 完整库管理（扫描/隐藏文件夹/恢复）仍由 androidMain 的 LibraryViewModel 承担。
 */
class LibraryListViewModel(
    private val getAllMusicUseCase: GetAllMusicUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<MusicInfo>>>(UiState.Loading)
    val state: StateFlow<UiState<List<MusicInfo>>> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching { getAllMusicUseCase("title", "ASC") }
                .onSuccess { list ->
                    _state.value = if (list.isEmpty()) UiState.Empty else UiState.Success(list)
                }
                .onFailure { _state.value = UiState.Error(it.message ?: "load failed") }
        }
    }
}
