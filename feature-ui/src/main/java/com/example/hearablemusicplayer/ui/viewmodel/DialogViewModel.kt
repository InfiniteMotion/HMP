package com.example.hearablemusicplayer.ui.viewmodel

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hearablemusicplayer.domain.music.MusicInfo
import com.example.hearablemusicplayer.domain.music.usecase.GetAllMusicUseCase
import com.example.hearablemusicplayer.domain.music.UserInfo
import com.example.hearablemusicplayer.domain.playlist.usecase.ManagePlaylistUseCase
import com.example.hearablemusicplayer.player.controller.MusicController
import com.example.hearablemusicplayer.ui.controller.DialogManager
import com.example.hearablemusicplayer.ui.util.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.media3.common.util.UnstableApi
import javax.inject.Inject

@OptIn(UnstableApi::class)
@HiltViewModel
class DialogViewModel @Inject constructor(
    private val musicController: MusicController,
    private val getAllMusicUseCase: GetAllMusicUseCase,
    private val managePlaylistUseCase: ManagePlaylistUseCase,
    private val dialogManager: DialogManager
) : ViewModel() {
    
    // 统一弹窗状态
    private val _activeDialog = MutableStateFlow<DialogUiState?>(null)
    val activeDialog: StateFlow<DialogUiState?> = _activeDialog.asStateFlow()

    // 向后兼容：按类型导出子状态，供现有组件复用
    val musicDetailState: StateFlow<MusicDetailState?> = activeDialog
        .map { (it as? DialogUiState.MusicDetail)?.state }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val createPlaylistState: StateFlow<CreatePlaylistDialogState?> = activeDialog
        .map { (it as? DialogUiState.CreatePlaylist)?.state }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val musicPickerState: StateFlow<MusicPickerDialogState?> = activeDialog
        .map { (it as? DialogUiState.MusicPicker)?.state }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // 收藏状态
    private val _isLiked = MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> = _isLiked

    private var onPlaylistCreated: ((Long) -> Unit)? = null
    private var existingPlaylistNames: Set<String> = emptySet()
    private var onMusicPickerConfirm: ((Set<Long>) -> Unit)? = null
    private var pendingCreatePlaylistStateForPicker: CreatePlaylistDialogState? = null
    private var createDialogMusicLookup: Map<Long, MusicInfo> = emptyMap()

    data class CreatePlaylistDialogState(
        val name: String = "",
        val description: String = "",
        val pinAfterCreate: Boolean = false,
        val selectedSongIds: Set<Long> = emptySet(),
        val nameError: String? = null,
        val submitError: String? = null,
        val isSubmitting: Boolean = false
    ) {
        val canSubmit: Boolean
            get() = !isSubmitting && nameError == null && name.trim().isNotEmpty()
    }

    data class MusicPickerDialogState(
        val allMusic: List<MusicInfo>,
        val selectedIds: Set<Long>,
        val title: String
    )

    sealed class DialogUiState {
        data class MusicDetail(val state: MusicDetailState) : DialogUiState()
        data class CreatePlaylist(val state: CreatePlaylistDialogState) : DialogUiState()
        data class MusicPicker(val state: MusicPickerDialogState) : DialogUiState()
    }
    
    // 显示音乐详情弹窗
    fun showMusicDetailDialog(musicInfo: MusicInfo) {
        // 获取最新的收藏状态
        viewModelScope.launch {
            val isLiked = musicController.getCurrentLikedStatus(musicInfo.music.id)
            _isLiked.value = isLiked
            
            val updatedMusicInfo = musicInfo.copy(
                userInfo = musicInfo.userInfo?.copy(
                    liked = isLiked
                ) ?: UserInfo(
                    id = musicInfo.music.id,
                    liked = isLiked
                )
            )
            
            _activeDialog.value = DialogUiState.MusicDetail(
                MusicDetailState(
                    musicInfo = updatedMusicInfo,
                    isVisible = true
                )
            )
        }
    }
    
    // 关闭音乐详情弹窗
    fun dismissMusicDetailDialog() {
        if (_activeDialog.value is DialogUiState.MusicDetail) {
            _activeDialog.value = null
        }
    }
    
    // 切换收藏状态
    fun toggleFavorite() {
        val currentState = (_activeDialog.value as? DialogUiState.MusicDetail)?.state ?: return
        val musicInfo = currentState.musicInfo
        
        val currentLiked = musicInfo.userInfo?.liked ?: false
        val newLiked = !currentLiked
        
        // 更新本地状态
        val updatedMusicInfo = musicInfo.copy(
            userInfo = musicInfo.userInfo?.copy(
                liked = newLiked
            ) ?: UserInfo(
                id = musicInfo.music.id,
                liked = newLiked
            )
        )
        
        // 更新ViewModel状态
        updateMusicDetailState { it.copy(musicInfo = updatedMusicInfo) }
        _isLiked.value = newLiked
        
        // 调用MusicController更新收藏状态
        musicController.updateMusicLikedStatus(musicInfo, newLiked)
    }
    
    // 播放音乐
    fun playMusic(onPlayComplete: () -> Unit) {
        val currentState = (_activeDialog.value as? DialogUiState.MusicDetail)?.state ?: return
        val musicInfo = currentState.musicInfo
        
        viewModelScope.launch {
            musicController.playWith(musicInfo)
            onPlayComplete()
        }
    }
    
    // 添加到播放列表
    fun addToPlaylist(onAddComplete: () -> Unit) {
        val currentState = (_activeDialog.value as? DialogUiState.MusicDetail)?.state ?: return
        val musicInfo = currentState.musicInfo
        
        musicController.addToPlaylist(musicInfo)
        onAddComplete()
    }
    
    // 分享音乐
    fun shareMusic() {
        // 这里可以添加分享逻辑
        dismissMusicDetailDialog()
    }
    
    // 查看详情
    fun viewDetail(navController: NavBackStack<NavKey>) {
        val currentState = (_activeDialog.value as? DialogUiState.MusicDetail)?.state ?: return
        val musicInfo = currentState.musicInfo
        
        navController.add(Routes.SongDetail(musicInfo.music.id))
        dismissMusicDetailDialog()
    }
    
    // 移除音乐
    fun removeMusic() {
        // 这里可以添加移除逻辑
        dismissMusicDetailDialog()
    }

    fun showCreatePlaylistDialog(onCreated: (Long) -> Unit) {
        onPlaylistCreated = onCreated
        _activeDialog.value = DialogUiState.CreatePlaylist(CreatePlaylistDialogState())
        viewModelScope.launch {
            existingPlaylistNames = managePlaylistUseCase.getAllPlaylists()
                .map { it.name.trim().lowercase() }
                .toSet()
            updateCreatePlaylistName("")
        }
    }

    fun dismissCreatePlaylistDialog() {
        if (_activeDialog.value is DialogUiState.CreatePlaylist) {
            _activeDialog.value = null
        }
        onPlaylistCreated = null
        createDialogMusicLookup = emptyMap()
    }

    fun updateCreatePlaylistName(input: String) {
        updateCreatePlaylistState {
            it.copy(
                name = input,
                nameError = validatePlaylistName(input),
                submitError = null
            )
        }
    }

    fun updateCreatePlaylistDescription(input: String) {
        updateCreatePlaylistState {
            it.copy(
                description = input,
                submitError = null
            )
        }
    }

    fun setCreatePlaylistPinned(pinned: Boolean) {
        updateCreatePlaylistState {
            it.copy(pinAfterCreate = pinned)
        }
    }

    fun setCreatePlaylistSelectedSongs(selectedIds: Set<Long>) {
        updateCreatePlaylistState {
            it.copy(
                selectedSongIds = selectedIds,
                submitError = null
            )
        }
    }

    fun onCreatePlaylistAddSongsClick() {
        val current = (_activeDialog.value as? DialogUiState.CreatePlaylist)?.state ?: return
        pendingCreatePlaylistStateForPicker = current
        viewModelScope.launch {
            try {
                val allMusic = getAllMusicUseCase("title", "ASC")
                createDialogMusicLookup = allMusic.associateBy { it.music.id }
                showMusicPickerDialog(
                    allMusic = allMusic,
                    selectedIds = current.selectedSongIds,
                    title = "添加歌曲到歌单",
                    onConfirm = { selectedIds ->
                        _activeDialog.value = DialogUiState.CreatePlaylist(
                            current.copy(
                                selectedSongIds = selectedIds,
                                submitError = null
                            )
                        )
                    }
                )
            } catch (e: Exception) {
                dialogManager.showMessage(e.message ?: "加载歌曲失败")
            }
        }
    }

    fun showMusicPickerDialog(
        allMusic: List<MusicInfo>,
        selectedIds: Set<Long> = emptySet(),
        title: String,
        onConfirm: (Set<Long>) -> Unit
    ) {
        onMusicPickerConfirm = onConfirm
        _activeDialog.value = DialogUiState.MusicPicker(
            MusicPickerDialogState(
                allMusic = allMusic,
                selectedIds = selectedIds,
                title = title
            )
        )
    }

    fun confirmMusicPickerDialog(selectedIds: Set<Long>) {
        onMusicPickerConfirm?.invoke(selectedIds)
        dismissMusicPickerDialog()
    }

    fun dismissMusicPickerDialog() {
        if (_activeDialog.value is DialogUiState.MusicPicker) {
            val pendingCreateState = pendingCreatePlaylistStateForPicker
            _activeDialog.value = if (pendingCreateState != null) {
                DialogUiState.CreatePlaylist(pendingCreateState)
            } else {
                null
            }
        }
        pendingCreatePlaylistStateForPicker = null
        onMusicPickerConfirm = null
    }

    fun submitCreatePlaylist() {
        val current = (_activeDialog.value as? DialogUiState.CreatePlaylist)?.state ?: return
        val validationError = validatePlaylistName(current.name)
        if (validationError != null) {
            updateCreatePlaylistState { it.copy(nameError = validationError) }
            return
        }

        updateCreatePlaylistState { it.copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            try {
                val id = managePlaylistUseCase.createPlaylist(current.name.trim())
                val desc = current.description.trim().takeIf { it.isNotEmpty() }
                if (desc != null) {
                    managePlaylistUseCase.updatePlaylistDescription(id, desc)
                }
                if (current.pinAfterCreate) {
                    managePlaylistUseCase.setPlaylistPinned(id, true)
                }
                addSelectedSongsToPlaylist(
                    playlistId = id,
                    selectedSongIds = current.selectedSongIds
                )
                onPlaylistCreated?.invoke(id)
                dialogManager.showMessage("歌单已创建")
                dismissCreatePlaylistDialog()
            } catch (e: Exception) {
                updateCreatePlaylistState {
                    it.copy(
                        isSubmitting = false,
                        submitError = e.message ?: "创建歌单失败"
                    )
                }
            }
        }
    }

    private inline fun updateMusicDetailState(
        transform: (MusicDetailState) -> MusicDetailState
    ) {
        val current = (_activeDialog.value as? DialogUiState.MusicDetail)?.state ?: return
        _activeDialog.value = DialogUiState.MusicDetail(transform(current))
    }

    private inline fun updateCreatePlaylistState(
        transform: (CreatePlaylistDialogState) -> CreatePlaylistDialogState
    ) {
        val current = (_activeDialog.value as? DialogUiState.CreatePlaylist)?.state ?: return
        _activeDialog.value = DialogUiState.CreatePlaylist(transform(current))
    }

    private fun validatePlaylistName(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return "歌单名不能为空"
        }
        if (trimmed.length > 30) {
            return "歌单名最多 30 个字符"
        }
        if (existingPlaylistNames.contains(trimmed.lowercase())) {
            return "歌单名已存在"
        }
        return null
    }

    private suspend fun addSelectedSongsToPlaylist(
        playlistId: Long,
        selectedSongIds: Set<Long>
    ) {
        if (selectedSongIds.isEmpty()) return
        selectedSongIds.forEach { songId ->
            val path = createDialogMusicLookup[songId]?.music?.path
                ?: getAllMusicUseCase.getMusicById(songId)?.music?.path
            if (!path.isNullOrBlank()) {
                managePlaylistUseCase.addToPlaylist(
                    playlistId = playlistId,
                    musicId = songId,
                    musicPath = path
                )
            }
        }
    }
    
    // 音乐详情弹窗状态
    data class MusicDetailState(
        val musicInfo: MusicInfo,
        val isVisible: Boolean
    )
}
