package com.hearablemusic.player.ui.player.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hmp.domain.music.MusicInfo
import com.hmp.domain.music.MusicLabel
import com.hmp.domain.playlist.AlgorithmType
import com.hmp.domain.playlist.ExtensionConfig
import com.hmp.domain.playlist.WeightTemplate
import com.hmp.domain.playlist.usecase.GeneratePlaylistResult
import com.hmp.domain.playlist.usecase.GeneratePlaylistUseCase
import com.hmp.domain.setting.SettingsRepository
import com.hearablemusic.player.ui.common.dialogs.controller.DialogManager
import com.hearablemusic.player.ui.platform.PlaybackController
import com.hearablemusic.player.ui.generated.resources.Res
import com.hearablemusic.player.ui.generated.resources.generated
import com.hearablemusic.player.ui.generated.resources.generation_error
import com.hearablemusic.player.ui.generated.resources.generation_failed
import com.hearablemusic.player.ui.generated.resources.save_failed
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

/**
 * 播放队列 ViewModel（第 4 步迁入 commonMain）。
 *
 * 平台依赖处置：MusicController → PlaybackController 冻结接口；
 * Application.getString（含格式化参数）→ CMP 挂起 getString(Res..., args)。
 */
class PlaylistQueueViewModel(
    private val playbackController: PlaybackController,
    private val generatePlaylistUseCase: GeneratePlaylistUseCase,
    private val settingsRepository: SettingsRepository,
    private val dialogManager: DialogManager
) : ViewModel() {

    val currentPlaylist: StateFlow<List<MusicInfo>> = playbackController.currentPlaylist
    val currentIndex: StateFlow<Int> = playbackController.currentIndex
    val currentPlayingMusic: StateFlow<MusicInfo?> = playbackController.currentPlayingMusic
    val likeStatus: StateFlow<Boolean> = playbackController.likeStatus
    val currentMusicLabels: StateFlow<List<MusicLabel?>> = playbackController.currentMusicLabels
    val currentMusicLyrics: StateFlow<String?> = playbackController.currentMusicLyrics

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

    fun playWith(musicInfo: MusicInfo) = viewModelScope.launch { playbackController.playWith(musicInfo) }
    fun playAt(musicInfo: MusicInfo) = playbackController.playAt(musicInfo)
    fun addToPlaylist(musicInfo: MusicInfo) = playbackController.addToPlaylist(musicInfo)
    fun removeFromPlaylist(musicInfo: MusicInfo) = playbackController.removeFromPlaylist(musicInfo)
    fun moveToTop(musicInfo: MusicInfo) = playbackController.moveToTop(musicInfo)
    fun clearPlaylist() = playbackController.clearPlaylist()
    fun addAllToPlaylistInOrder(playlist: List<MusicInfo>) = playbackController.addAllToPlaylistInOrder(playlist)
    fun addAllToPlaylistByShuffle(playlist: List<MusicInfo>) = playbackController.addAllToPlaylistByShuffle(playlist)
    fun playHeartMode() = playbackController.playHeartMode()
    fun updateMusicLikedStatus(musicInfo: MusicInfo, liked: Boolean) = playbackController.updateMusicLikedStatus(musicInfo, liked)
    fun getLikedStatus(musicId: Long) = playbackController.getLikedStatus(musicId)
    fun getMusicLabels(musicId: Long) = playbackController.getMusicLabels(musicId)
    fun getMusicLyrics(musicId: Long) = playbackController.getMusicLyrics(musicId)

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
                        dialogManager.showMessage(getString(Res.string.generated))
                    }

                    is GeneratePlaylistResult.Error -> {
                        dialogManager.showMessage(getString(Res.string.generation_failed, result.message ?: ""))
                    }
                }
            } catch (e: Exception) {
                dialogManager.showMessage(getString(Res.string.generation_error, e.message ?: ""))
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
                dialogManager.showMessage(getString(Res.string.save_failed, e.message ?: ""))
            }
        }
    }
}