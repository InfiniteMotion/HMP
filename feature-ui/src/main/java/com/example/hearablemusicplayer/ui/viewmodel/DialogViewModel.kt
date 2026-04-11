package com.example.hearablemusicplayer.ui.viewmodel

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hearablemusicplayer.domain.music.MusicInfo
import com.example.hearablemusicplayer.domain.music.UserInfo
import com.example.hearablemusicplayer.player.controller.MusicController
import com.example.hearablemusicplayer.ui.util.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.media3.common.util.UnstableApi
import javax.inject.Inject

@OptIn(UnstableApi::class)
@HiltViewModel
class DialogViewModel @Inject constructor(
    private val musicController: MusicController
) : ViewModel() {
    
    // 音乐详情弹窗状态
    private val _musicDetailState = MutableStateFlow<MusicDetailState?>(null)
    val musicDetailState: StateFlow<MusicDetailState?> = _musicDetailState
    
    // 收藏状态
    private val _isLiked = MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> = _isLiked
    
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
            
            _musicDetailState.value = MusicDetailState(
                musicInfo = updatedMusicInfo,
                isVisible = true
            )
        }
    }
    
    // 关闭音乐详情弹窗
    fun dismissMusicDetailDialog() {
        _musicDetailState.value = null
    }
    
    // 切换收藏状态
    fun toggleFavorite() {
        val currentState = _musicDetailState.value ?: return
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
        _musicDetailState.value = currentState.copy(
            musicInfo = updatedMusicInfo
        )
        _isLiked.value = newLiked
        
        // 调用MusicController更新收藏状态
        musicController.updateMusicLikedStatus(musicInfo, newLiked)
    }
    
    // 播放音乐
    fun playMusic(onPlayComplete: () -> Unit) {
        val currentState = _musicDetailState.value ?: return
        val musicInfo = currentState.musicInfo
        
        viewModelScope.launch {
            musicController.playWith(musicInfo)
            onPlayComplete()
        }
    }
    
    // 添加到播放列表
    fun addToPlaylist(onAddComplete: () -> Unit) {
        val currentState = _musicDetailState.value ?: return
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
        val currentState = _musicDetailState.value ?: return
        val musicInfo = currentState.musicInfo
        
        navController.add(Routes.SongDetail(musicInfo.music.id))
        dismissMusicDetailDialog()
    }
    
    // 移除音乐
    fun removeMusic() {
        // 这里可以添加移除逻辑
        dismissMusicDetailDialog()
    }
    
    // 音乐详情弹窗状态
    data class MusicDetailState(
        val musicInfo: MusicInfo,
        val isVisible: Boolean
    )
}
