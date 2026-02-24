package com.example.hearablemusicplayer.ui.viewmodel

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.hearablemusicplayer.domain.setting.model.AudioEffectSettings
import com.example.hearablemusicplayer.domain.music.MusicInfo
import com.example.hearablemusicplayer.domain.music.MusicLabel
import com.example.hearablemusicplayer.domain.enum.PlaybackMode
import com.example.hearablemusicplayer.domain.playlist.AlgorithmType
import com.example.hearablemusicplayer.domain.playlist.ExtensionConfig
import com.example.hearablemusicplayer.domain.playlist.WeightTemplate
import com.example.hearablemusicplayer.domain.playlist.usecase.GeneratePlaylistUseCase
import com.example.hearablemusicplayer.domain.setting.SettingsRepository
import com.example.hearablemusicplayer.player.controller.MusicController
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
}

// 调色板颜色数据类
data class PaletteColors(
    // 主色调系列
    val dominantColor: Color = Color(0xFF121212),
    val primaryColor: Color = Color(0xFF1E1E1E),
    
    // 活力色调系列
    val vibrantColor: Color = Color(0xFF2A2A2A),
    val darkVibrantColor: Color = Color(0xFF0F0F0F),
    val lightVibrantColor: Color = Color(0xFF808080),
    
    // 柔和色调系列
    val mutedColor: Color = Color(0xFF222222),
    val darkMutedColor: Color = Color(0xFF111111),
    val lightMutedColor: Color = Color(0xFF666666),
    
    // 辅助色调
    val accentColor: Color = Color(0xFF444444)
)

@HiltViewModel
@UnstableApi
class PlayControlViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val musicController: MusicController,
    private val generatePlaylistUseCase: GeneratePlaylistUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // Delegate flows to MusicController
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

    // Events
    private val _toastEvent = MutableSharedFlow<UiEvent.ShowToast>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val toastEvent = _toastEvent.asSharedFlow()

    // Palette Colors (UI specific logic)
    private val paletteCache = mutableMapOf<String, PaletteColors>()
    private val _paletteColors = MutableStateFlow(PaletteColors())
    val paletteColors: StateFlow<PaletteColors> = _paletteColors.asStateFlow()

    // UI Visibility States
    val isMiniPlayerVisible: StateFlow<Boolean> = musicController.isMiniPlayerVisible

    init {
        // Forward controller toasts
        viewModelScope.launch {
            musicController.toastEvent.collectLatest { event ->
                _toastEvent.emit(UiEvent.ShowToast(event.message))
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

    fun recordPlayback(musicId: Long, source: String?) =
        musicController.recordPlayback(musicId, source)

    fun playHeartMode() = musicController.playHeartMode()
    fun updateMusicLikedStatus(musicInfo: MusicInfo, liked: Boolean) =
        musicController.updateMusicLikedStatus(musicInfo, liked)

    fun getLikedStatus(musicId: Long) = musicController.getLikedStatus(musicId)
    fun getMusicLabels(musicId: Long) = musicController.getMusicLabels(musicId)
    fun getMusicLyrics(musicId: Long) = musicController.getMusicLyrics(musicId)

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
    fun getCurrentEqualizerBandLevels() = musicController.getCurrentEqualizerBandLevels()

    // 提取调色板颜色（带缓存与线程分离）
    private fun extractPaletteColors(albumArtUri: String?) {
        if (albumArtUri == null) {
            _paletteColors.value = PaletteColors()
            return
        }

        // 检查缓存
        paletteCache[albumArtUri]?.let {
            _paletteColors.value = it
            return
        }

        viewModelScope.launch {
            try {
                val colors = withContext(Dispatchers.IO) {
                    val loader = ImageLoader(context)
                    val request = ImageRequest.Builder(context)
                        .data(albumArtUri)
                        .size(150, 150) // 降低采样大小，提高性能
                        .allowHardware(false)
                        .build()

                    val result = (loader.execute(request) as? SuccessResult)?.drawable
                    val bitmap = (result as? android.graphics.drawable.BitmapDrawable)?.bitmap

                    bitmap?.let {
                        // 切换到 Default 线程进行 Palette 计算
                        withContext(Dispatchers.Default) {
                            val palette = Palette.from(it)
                                .maximumColorCount(16) // 增加颜色数量
                                .generate()

                            // 提取更多颜色类型
                            val dominant = palette.getDominantColor(0xFF121212.toInt())
                            val vibrant = palette.vibrantSwatch?.rgb
                            val darkVibrant = palette.darkVibrantSwatch?.rgb
                            val lightVibrant = palette.lightVibrantSwatch?.rgb
                            val muted = palette.mutedSwatch?.rgb
                            val darkMuted = palette.darkMutedSwatch?.rgb
                            val lightMuted = palette.lightMutedSwatch?.rgb

                            PaletteColors(
                                dominantColor = Color(dominant),
                                primaryColor = Color(vibrant ?: dominant),
                                vibrantColor = Color(vibrant ?: dominant),
                                darkVibrantColor = Color(darkVibrant ?: vibrant ?: dominant),
                                lightVibrantColor = Color(lightVibrant ?: vibrant ?: dominant),
                                mutedColor = Color(muted ?: dominant),
                                darkMutedColor = Color(darkMuted ?: muted ?: dominant),
                                lightMutedColor = Color(lightMuted ?: muted ?: dominant),
                                accentColor = Color(lightVibrant ?: vibrant ?: dominant)
                            )
                        }
                    } ?: PaletteColors()
                }

                // 优化缓存机制：保持缓存大小在50以内
                if (paletteCache.size >= 50) {
                    paletteCache.remove(paletteCache.keys.first())
                }
                // 更新缓存与状态
                paletteCache[albumArtUri] = colors
                _paletteColors.value = colors
            } catch (e: Exception) {
                // 提取失败使用回退色
                _paletteColors.value = PaletteColors()
            }
        }
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
                    is com.example.hearablemusicplayer.domain.playlist.usecase.GeneratePlaylistResult.Success -> {
                        // 将生成的播放列表添加到当前播放队列
                        addAllToPlaylistInOrder(result.playlist)
                        _toastEvent.emit(UiEvent.ShowToast("已生成${result.actualLength}首歌曲的播放列表"))
                    }

                    is com.example.hearablemusicplayer.domain.playlist.usecase.GeneratePlaylistResult.Error -> {
                        _toastEvent.emit(UiEvent.ShowToast("生成播放列表失败: ${result.message}"))
                    }
                }
            } catch (e: Exception) {
                _toastEvent.emit(UiEvent.ShowToast("生成播放列表时发生错误: ${e.message}"))
            }
        }
    }

    val defaultAlgorithmType: StateFlow<AlgorithmType> = 
        settingsRepository.defaultAlgorithmType.map { typeName ->
            AlgorithmType.valueOf(typeName)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AlgorithmType.OPTIMIZED_SIMILARITY
        )

    val defaultWeightTemplate: StateFlow<WeightTemplate> = settingsRepository.defaultWeightTemplate.map { typeName ->
        WeightTemplate.valueOf(typeName)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
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
                // 使用ExtensionConfig的toJson方法进行序列化
                settingsRepository.saveDefaultExtensionConfig(extensionConfig.toJson())
                _toastEvent.emit(UiEvent.ShowToast("已保存默认配置"))
            } catch (e: Exception) {
                _toastEvent.emit(UiEvent.ShowToast("保存配置失败: ${e.message}"))
            }
        }
    }
}
