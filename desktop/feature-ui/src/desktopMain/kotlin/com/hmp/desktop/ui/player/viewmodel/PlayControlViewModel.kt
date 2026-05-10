package com.hmp.desktop.ui.player.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hmp.domain.enum.PlaybackMode
import com.hmp.domain.music.MusicInfo
import com.hmp.domain.music.MusicLabel
import com.hmp.domain.playlist.AlgorithmType
import com.hmp.domain.playlist.ExtensionConfig
import com.hmp.domain.playlist.WeightTemplate
import com.hmp.domain.playlist.usecase.GeneratePlaylistResult
import com.hmp.domain.playlist.usecase.GeneratePlaylistUseCase
import com.hmp.domain.setting.SettingsRepository
import com.hmp.domain.setting.model.AudioEffectSettings
import com.hmp.desktop.player.DesktopMusicController
import com.hmp.desktop.ui.common.viewmodel.PaletteColors
import com.hmp.desktop.ui.common.util.PaletteExtractor
import com.hmp.desktop.ui.common.dialogs.controller.DialogManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Deprecated("Use PlaybackViewModel, PlaylistQueueViewModel, AudioEffectViewModel, ThemeViewModel instead")
class PlayControlViewModel(
    private val musicController:DesktopMusicController,
    private val generatePlaylistUseCase: GeneratePlaylistUseCase,
    private val settingsRepository: SettingsRepository,
    private val dialogManager: DialogManager
) : ViewModel() {

    // Delegate flows to DesktopMusicController
    val isPlaying: StateFlow<Boolean> = musicController.isPlaying
    val currentPlaylist: StateFlow<List<MusicInfo>> = musicController.currentPlaylist
    val currentIndex: StateFlow<Int> = musicController.currentIndex
    val currentPlayingMusic: StateFlow<MusicInfo?> = musicController.currentPlayingMusic
    val likeStatus: StateFlow<Boolean> = musicController.likeStatus
    val currentMusicLabels: StateFlow<List<MusicLabel?>> = musicController.currentMusicLabels
    val currentMusicLyrics: StateFlow<String?> = musicController.currentMusicLyrics
    val playbackMode: StateFlow<PlaybackMode> = musicController.playbackMode
    val currentPosition: StateFlow<Long> = musicController.currentPosition
    val duration: StateFlow<Long> = musicController.duration
    val timerRemaining: StateFlow<Long?> = musicController.timerRemaining

    // Audio Effect States
    val audioEffectSettings: StateFlow<AudioEffectSettings> = musicController.audioEffectSettings
    val equalizerPresets: StateFlow<List<String>> = musicController.equalizerPresets
    val equalizerBandCount: StateFlow<Int> = musicController.equalizerBandCount
    val equalizerBandLevelRange: StateFlow<Pair<Int, Int>> = musicController.equalizerBandLevelRange
    val currentEqualizerBandLevels: StateFlow<FloatArray> =
        musicController.currentEqualizerBandLevels

    // Palette Colors (UI specific logic)
    private val paletteCache = mutableMapOf<String, PaletteColors>()
    private val _paletteColors = MutableStateFlow(PaletteColors())
    val paletteColors: StateFlow<PaletteColors> = _paletteColors.asStateFlow()

    // UI Visibility States
    val isMiniPlayerVisible: StateFlow<Boolean> = musicController.isMiniPlayerVisible

    init {
        // Forward controller toasts to DialogManager
        viewModelScope.launch {
            musicController.toastEvent.collectLatest { event ->
                dialogManager.showMessage(event.message)
            }
        }

        // Palette extraction
        viewModelScope.launch {
            currentPlayingMusic
                .filterNotNull()
                .collectLatest { musicInfo ->
                    extractPaletteColors(musicInfo.music.albumArtUri)
                }
        }
    }

    // Delegated Methods
    fun playOrResume() = musicController.playOrResume()
    fun pauseMusic() = musicController.pauseMusic()
    fun playNext() = musicController.playNext()
    fun playPrevious() = musicController.playPrevious()
    fun seekTo(position: Long) = musicController.seekTo(position)
    fun togglePlaybackModeByOrder() = musicController.togglePlaybackModeByOrder()

    fun playWith(musicInfo: MusicInfo) =
        viewModelScope.launch { musicController.playWith(musicInfo) }

    fun playAt(musicInfo: MusicInfo) = viewModelScope.launch { musicController.playAt(musicInfo) }

    fun addToPlaylist(musicInfo: MusicInfo) = musicController.addToPlaylist(musicInfo)
    fun removeFromPlaylist(musicInfo: MusicInfo) = musicController.removeFromPlaylist(musicInfo)
    fun moveToTop(musicInfo: MusicInfo) = musicController.moveToTop(musicInfo)
    fun clearPlaylist() = musicController.clearPlaylist()
    fun addAllToPlaylistInOrder(playlist: List<MusicInfo>) =
        musicController.addAllToPlaylistInOrder(playlist)

    fun addAllToPlaylistByShuffle(playlist: List<MusicInfo>) =
        musicController.addAllToPlaylistByShuffle(playlist)

    fun playHeartMode() = musicController.playHeartMode()
    fun updateMusicLikedStatus(musicInfo: MusicInfo, liked: Boolean) =
        musicController.updateMusicLikedStatus(musicInfo, liked)

    suspend fun getLikedStatus(musicId: Long) = musicController.getLikedStatus(musicId)
    suspend fun getMusicLabels(musicId: Long) = musicController.getMusicLabels(musicId)
    suspend fun getMusicLyrics(musicId: Long) = musicController.getMusicLyrics(musicId)

    fun startTimer(minutes: Int) = musicController.startTimer(minutes)
    fun cancelTimer() = musicController.cancelTimer()

    fun startProgressTracking() = musicController.startProgressTracking()
    fun stopProgressTracking() = musicController.stopProgressTracking()

    fun setMiniPlayerVisible(visible: Boolean) {
        musicController.setMiniPlayerVisible(visible)
    }

    fun preloadCurrentMusicInfo() {
        val music = currentPlayingMusic.value
        if (music != null) {
            musicController.preloadCurrentMusicInfo(music)
        }
    }

    // Audio Effects Delegates
    fun initializeAudioEffects() = musicController.initializeAudioEffects()
    fun setEqualizerPreset(preset: Int) = musicController.setEqualizerPreset(preset)
    fun setBassBoost(level: Int) = musicController.setBassBoost(level)
    fun setSurroundSound(enabled: Boolean) = musicController.setSurroundSound(enabled)
    fun setReverb(preset: Int) = musicController.setReverb(preset)
    fun setCustomEqualizer(bandLevels: FloatArray) = musicController.setCustomEqualizer(bandLevels)

    // Getters for Audio Effect UI
    fun getCurrentEqualizerPreset() = musicController.getCurrentEqualizerPreset()
    fun getBassBoostLevel() = musicController.getBassBoostLevel()
    fun isSurroundSoundEnabled() = musicController.isSurroundSoundEnabled()
    fun getReverbPreset() = musicController.getReverbPreset()
    fun getCurrentEqualizerBandLevels() = musicController.currentEqualizerBandLevels.value

    // 提取调色板颜色（带缓存）
    private suspend fun extractPaletteColors(albumArtUri: String?) {
        if (albumArtUri == null) {
            _paletteColors.value = PaletteColors()
            return
        }

        paletteCache[albumArtUri]?.let {
            _paletteColors.value = it
            return
        }

        val colors = withContext(Dispatchers.IO) {
            PaletteExtractor.extract(albumArtUri)
        }
        paletteCache[albumArtUri] = colors
        _paletteColors.value = colors
    }

    // ==================== 播放列表生成方法 ====================
    /**
     * 生成播放列表并添加到当前播放队列
     */
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
                        // 将生成的播放列表添加到当前播放队列
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

    /**
     * 保存默认算法配置
     */
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