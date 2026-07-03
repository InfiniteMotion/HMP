package com.hearablemusic.player.ui.player.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.hmp.domain.music.MusicInfo
import com.hmp.domain.music.MusicLabel
import com.hmp.domain.playlist.AlgorithmType
import com.hmp.domain.playlist.ExtensionConfig
import com.hmp.domain.playlist.WeightTemplate
import com.hmp.domain.playlist.usecase.GeneratePlaylistResult
import com.hmp.domain.playlist.usecase.GeneratePlaylistUseCase
import com.hmp.domain.setting.SettingsRepository
import com.hearablemusic.player.player.controller.MusicController
import com.hearablemusic.player.ui.common.dialogs.controller.DialogManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@UnstableApi
class PlaylistQueueViewModel(
    private val musicController: MusicController,
    private val generatePlaylistUseCase: GeneratePlaylistUseCase,
    private val settingsRepository: SettingsRepository,
    private val dialogManager: DialogManager
) : ViewModel() {

    val currentPlaylist: StateFlow<List<MusicInfo>> = musicController.currentPlaylist
    val currentIndex: StateFlow<Int> = musicController.currentIndex
    val currentPlayingMusic: StateFlow<MusicInfo?> = musicController.currentPlayingMusic
    val likeStatus: StateFlow<Boolean> = musicController.likeStatus
    val currentMusicLabels: StateFlow<List<MusicLabel?>> = musicController.currentMusicLabels
    val currentMusicLyrics: StateFlow<String?> = musicController.currentMusicLyrics

    val defaultAlgorithmType: StateFlow<AlgorithmType> =
        settingsRepository.defaultAlgorithmType.map { typeName ->
            AlgorithmType.valueOf(typeName)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = AlgorithmType.OPTIMIZED_SIMILARITY
        )

    val defaultWeightTemplate: StateFlow<WeightTemplate> = settingsRepository.defaultWeightTemplate.map { typeName ->
        WeightTemplate.valueOf(typeName)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5000),
        initialValue = WeightTemplate.BALANCED
    )

    fun playWith(musicInfo: MusicInfo) = viewModelScope.launch { musicController.playWith(musicInfo) }
    fun playAt(musicInfo: MusicInfo) = musicController.playAt(musicInfo)
    fun addToPlaylist(musicInfo: MusicInfo) = musicController.addToPlaylist(musicInfo)
    fun removeFromPlaylist(musicInfo: MusicInfo) = musicController.removeFromPlaylist(musicInfo)
    fun moveToTop(musicInfo: MusicInfo) = musicController.moveToTop(musicInfo)
    fun clearPlaylist() = musicController.clearPlaylist()
    fun addAllToPlaylistInOrder(playlist: List<MusicInfo>) = musicController.addAllToPlaylistInOrder(playlist)
    fun addAllToPlaylistByShuffle(playlist: List<MusicInfo>) = musicController.addAllToPlaylistByShuffle(playlist)
    fun playHeartMode() = musicController.playHeartMode()
    fun updateMusicLikedStatus(musicInfo: MusicInfo, liked: Boolean) = musicController.updateMusicLikedStatus(musicInfo, liked)
    fun getLikedStatus(musicId: Long) = musicController.getLikedStatus(musicId)
    fun getMusicLabels(musicId: Long) = musicController.getMusicLabels(musicId)
    fun getMusicLyrics(musicId: Long) = musicController.getMusicLyrics(musicId)

    fun generatePlaylist(
        seedMusicId: Long = currentPlayingMusic.value?.music?.id ?: 0
    ) {
        viewModelScope.launch {
            try {
                val result = generatePlaylistUseCase.execute(
                    seedMusicId = seedMusicId,
                )

                when (result) {
                    is GeneratePlaylistResult.Success -> {
                        addAllToPlaylistInOrder(result.playlist)
                        dialogManager.showMessage("已生成")
                    }

                    is GeneratePlaylistResult.Error -> {
                        dialogManager.showMessage("生成失败: ${result.message}")
                    }
                }
            } catch (e: Exception) {
                dialogManager.showMessage("生成错误: ${e.message}")
            }
        }
    }

    fun saveAlgorithmConfig(
        algorithmType: AlgorithmType,
        weightTemplate: WeightTemplate,
        extensionConfig: ExtensionConfig
    ) {
        viewModelScope.launch {
            try {
                settingsRepository.saveDefaultAlgorithmType(algorithmType.name)
                settingsRepository.saveDefaultWeightTemplate(weightTemplate.name)
                settingsRepository.saveDefaultExtensionConfig(extensionConfig.toJson())
            } catch (e: Exception) {
                dialogManager.showMessage("保存失败: ${e.message}")
            }
        }
    }
}