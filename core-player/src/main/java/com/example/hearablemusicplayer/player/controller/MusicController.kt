package com.example.hearablemusicplayer.player.controller

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.media3.common.util.UnstableApi
import com.example.hearablemusicplayer.domain.model.AudioEffectSettings
import com.example.hearablemusicplayer.domain.model.MusicInfo
import com.example.hearablemusicplayer.domain.model.MusicLabel
import com.example.hearablemusicplayer.domain.model.PlaybackHistory
import com.example.hearablemusicplayer.domain.model.enum.PlaybackMode
import com.example.hearablemusicplayer.domain.repository.SettingsRepository
import com.example.hearablemusicplayer.domain.usecase.playback.CurrentPlaybackUseCase
import com.example.hearablemusicplayer.domain.usecase.playback.PlaybackHistoryUseCase
import com.example.hearablemusicplayer.domain.usecase.playback.PlaybackModeUseCase
import com.example.hearablemusicplayer.domain.usecase.playback.TimerUseCase
import com.example.hearablemusicplayer.domain.usecase.playlist.ManagePlaylistUseCase
import com.example.hearablemusicplayer.player.service.MusicPlayService
import com.example.hearablemusicplayer.player.service.PlayControl
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

@UnstableApi
@Singleton
class MusicController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val currentPlaybackUseCase: CurrentPlaybackUseCase,
    private val playbackModeUseCase: PlaybackModeUseCase,
    private val playbackHistoryUseCase: PlaybackHistoryUseCase,
    private val timerUseCase: TimerUseCase,
    private val managePlaylistUseCase: ManagePlaylistUseCase,
    private val settingsRepository: SettingsRepository
) : MusicPlayService.OnMusicCompleteListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var playControl: PlayControl? = null
    private var activityClass: Class<*>? = null

    fun setTargetActivityClass(clazz: Class<*>) {
        activityClass = clazz
    }
    
    // Service Connection
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val service = (binder as? MusicPlayService.MusicPlayServiceBinder)?.getService()
            if (service != null) {
                if (service is MusicPlayService) {
                    activityClass?.let { service.setMainActivityClass(it) }
                }
                bindPlayControl(service)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bindPlayControl(null)
        }
    }

    fun bindService() {
        val intent = Intent(context, MusicPlayService::class.java)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun unbindService() {
        try {
            context.unbindService(connection)
        } catch (e: Exception) {
            Log.e("MusicController", "Error unbinding service", e)
        }
        bindPlayControl(null)
    }

    fun bindPlayControl(service: PlayControl?) {
        this.playControl = service
        if (service is MusicPlayService) {
            service.setOnMusicCompleteListener(this)
        }
        // 绑定后恢复音效设置
        if (service != null) {
            restoreAudioEffectSettings()
        }
    }

    // Events
    sealed class UiEvent {
        data class ShowToast(val message: String) : UiEvent()
    }

    private val _toastEvent = MutableSharedFlow<UiEvent.ShowToast>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val toastEvent = _toastEvent.asSharedFlow()

    private fun showToast(message: String) {
        scope.launch {
            _toastEvent.emit(UiEvent.ShowToast(message))
        }
    }

    // Playback State
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // Playlist
    private var _originalPlaylist: List<MusicInfo> = emptyList()
    private val _currentPlaylist = MutableStateFlow<List<MusicInfo>>(emptyList())
    val currentPlaylist: StateFlow<List<MusicInfo>> = _currentPlaylist.asStateFlow()

    private var _shuffledPlaylist: List<MusicInfo>? = null

    // IDs
    private val currentPlayListId = settingsRepository.currentPlaylistId
    private val likedPlayListId = settingsRepository.likedPlaylistId
    private val recentPlayListId = settingsRepository.recentPlaylistId

    // Index
    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    // Current Music
    val currentPlayingMusic: StateFlow<MusicInfo?> = combine(
        currentPlaylist,
        currentIndex
    ) { playlist, index ->
        playlist.getOrNull(index)
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    // Like Status
    var likeStatus = MutableStateFlow(false)

    // Labels & Lyrics
    private val _currentMusicLabels = MutableStateFlow<List<MusicLabel?>>(emptyList())
    val currentMusicLabels: StateFlow<List<MusicLabel?>> = _currentMusicLabels

    private val _currentMusicLyrics = MutableStateFlow<String?>(null)
    val currentMusicLyrics: StateFlow<String?> = _currentMusicLyrics

    // Playback Mode
    private val _playbackMode = MutableStateFlow(PlaybackMode.SEQUENTIAL)
    val playbackMode: StateFlow<PlaybackMode> = _playbackMode.asStateFlow()

    // Position & Duration
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private var progressJob: Job? = null

    private var playStartTime: Long = 0L
    private var lastDurationRecordTime: Long = 0L
    private val durationRecordThreshold = 30000L

    // Timer
    private var timerJob: Job? = null
    val timerRemaining: StateFlow<Long?> = timerUseCase.timerRemaining

    // Audio Effects
    private val _audioEffectSettings = MutableStateFlow(AudioEffectSettings())
    val audioEffectSettings: StateFlow<AudioEffectSettings> = _audioEffectSettings.asStateFlow()

    private val _equalizerPresets = MutableStateFlow<List<String>>(emptyList())
    val equalizerPresets: StateFlow<List<String>> = _equalizerPresets.asStateFlow()

    private val _equalizerBandCount = MutableStateFlow(0)
    val equalizerBandCount: StateFlow<Int> = _equalizerBandCount.asStateFlow()

    private val _equalizerBandLevelRange = MutableStateFlow(Pair(0, 0))
    val equalizerBandLevelRange: StateFlow<Pair<Int, Int>> = _equalizerBandLevelRange.asStateFlow()

    private val _currentEqualizerBandLevels = MutableStateFlow(floatArrayOf())
    val currentEqualizerBandLevels: StateFlow<FloatArray> = _currentEqualizerBandLevels.asStateFlow()

    init {
        // Init Playlist
        scope.launch {
            loadPlaylistFromSettings()
        }

        // Watch Playlist ID
        scope.launch {
            currentPlayListId
                .filterNotNull()
                .collectLatest { playlistId ->
                    if (_currentPlaylist.value.isEmpty()) {
                        try {
                            val currentMusicId = currentPlaybackUseCase.getCurrentMusicId().first()
                            val list = managePlaylistUseCase.getMusicInfoInPlaylist(playlistId).first()
                            _originalPlaylist = list
                            _currentPlaylist.value = list
                            _currentIndex.value = list.indexOfFirst { it.music.id == currentMusicId }.takeIf { it >= 0 } ?: 0
                        } catch (e: Exception) {
                            // Ignore
                        }
                    }
                }
        }

        // Watch Playback Mode
        scope.launch {
            playbackModeUseCase.playbackMode
                .filterNotNull()
                .collectLatest { mode ->
                    _playbackMode.value = mode
                    updateCurrentPlaylist()
                }
        }
        
        // Watch Current Music for preload info
        scope.launch {
            currentPlayingMusic
                .filterNotNull()
                .collectLatest { musicInfo ->
                    preloadCurrentMusicInfo(musicInfo)
                }
        }
    }

    private suspend fun loadPlaylistFromSettings() {
        try {
            val playlistId = currentPlayListId.filterNotNull().first()
            val currentMusicId = currentPlaybackUseCase.getCurrentMusicId().first()
            val list = managePlaylistUseCase.getMusicInfoInPlaylist(playlistId).first()
            _originalPlaylist = list
            _currentPlaylist.value = list
            _currentIndex.value = list.indexOfFirst { it.music.id == currentMusicId }.takeIf { it >= 0 } ?: 0
        } catch (e: Exception) {
            // Ignore
        }
    }
    
    fun preloadCurrentMusicInfo(musicInfo: MusicInfo) {
        _duration.value = musicInfo.music.duration
        getLikedStatus(musicInfo.music.id)
        getMusicLabels(musicInfo.music.id)
        getMusicLyrics(musicInfo.music.id)
    }

    fun startProgressTracking() {
        if (progressJob?.isActive == true) return

        progressJob = scope.launch {
            while (isActive) {
                playControl?.let { svc ->
                    _currentPosition.value = svc.getCurrentPosition()
                    recordListeningDurationPeriodically()
                }
                delay(500)
            }
        }
    }

    private fun recordListeningDurationPeriodically() {
        val now = System.currentTimeMillis()
        if (_isPlaying.value && playStartTime > 0 &&
            (now - lastDurationRecordTime) >= durationRecordThreshold) {
            val duration = now - playStartTime
            scope.launch {
                playbackHistoryUseCase.recordListeningDuration(duration)
            }
            lastDurationRecordTime = now
            playStartTime = now
        }
    }

    fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }

    fun clearPlaylist() {
        val currentMusic = currentPlayingMusic.value
        if (currentMusic != null) {
            _originalPlaylist = listOf(currentMusic)
            _currentPlaylist.value = listOf(currentMusic)
            _currentIndex.value = 0
        } else {
            _originalPlaylist = emptyList()
            _currentPlaylist.value = emptyList()
            _currentIndex.value = 0
        }
        persistCurrentPlaylistToDatabase()
    }

    private suspend fun saveToPlaylist(musicInfo: MusicInfo) {
        try {
            val playlistId = currentPlayListId.filterNotNull().first()
            managePlaylistUseCase.addToPlaylist(playlistId, musicInfo.music.id, musicInfo.music.path)
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun isMusicLoaded(path: String): Boolean? {
        return playControl?.isMusicLoaded(path)
    }

    fun playOrResume() {
        if (playControl == null) {
            Log.e("MusicController", "playOrResume: playControl is null")
            return
        }
        playStartTime = System.currentTimeMillis()
        lastDurationRecordTime = playStartTime
        val path = currentMusicPath()
        if (path != null && isMusicLoaded(path) == true) {
            playControl?.proceedMusic()
        } else {
            scope.launch { playCurrentTrack("AutoPlay") }
        }
    }

    fun pauseMusic() {
        if (playControl == null) {
            Log.e("MusicController", "pauseMusic: playControl is null")
            return
        }
        if (playStartTime > 0) {
            val duration = System.currentTimeMillis() - playStartTime
            if (duration > 0) {
                scope.launch {
                    playbackHistoryUseCase.recordListeningDuration(duration)
                }
            }
        }
        playControl?.pause()
        playStartTime = 0L
        lastDurationRecordTime = 0L
    }

    fun seekTo(position: Long) {
        scope.launch {
            if (!_isPlaying.value) {
                playCurrentTrack("Player")
            }
            playControl?.seekTo(position)
        }
    }

    private fun updateCurrentPlaylist() {
        val currentTrack = currentPlayingMusic.value
        _currentPlaylist.value = when (_playbackMode.value) {
            PlaybackMode.SHUFFLE -> {
                if (_shuffledPlaylist == null) {
                    _shuffledPlaylist = _originalPlaylist.shuffled()
                }
                _shuffledPlaylist!!
            }
            else -> _originalPlaylist
        }
        _currentIndex.value = currentTrack?.let { track ->
            _currentPlaylist.value.indexOfFirst { it.music.id == track.music.id }
        }?.takeIf { it >= 0 } ?: 0
    }

    private fun togglePlaybackMode(newMode: PlaybackMode) {
        _playbackMode.value = newMode
        scope.launch {
            playbackModeUseCase.savePlaybackMode(newMode)
        }
        _shuffledPlaylist = null
        updateCurrentPlaylist()
    }

    fun togglePlaybackModeByOrder() {
        val next = when (_playbackMode.value) {
            PlaybackMode.SEQUENTIAL -> PlaybackMode.REPEAT_ONE
            PlaybackMode.REPEAT_ONE -> PlaybackMode.SHUFFLE
            PlaybackMode.SHUFFLE -> PlaybackMode.SEQUENTIAL
        }
        togglePlaybackMode(next)
    }

    fun addAllToPlaylistInOrder(playlist: List<MusicInfo>) {
        scope.launch {
            _originalPlaylist = playlist
            togglePlaybackMode(PlaybackMode.SEQUENTIAL)
            _currentPlaylist.value = _originalPlaylist
            _currentIndex.value = 0
            playCurrentTrack("In Order")
        }
        persistCurrentPlaylistToDatabase()
    }

    fun addAllToPlaylistByShuffle(playlist: List<MusicInfo>) {
        scope.launch {
            _originalPlaylist = playlist
            togglePlaybackMode(PlaybackMode.SHUFFLE)
            _currentPlaylist.value = _shuffledPlaylist!!
            _currentIndex.value = 0
            playCurrentTrack("By Shuffle")
        }
        persistCurrentPlaylistToDatabase()
    }

    fun playNext(forceChange: Boolean = true) = scope.launch {
        if (_currentPlaylist.value.isEmpty()) return@launch
        if (forceChange || _playbackMode.value != PlaybackMode.REPEAT_ONE) {
            _currentIndex.value = (_currentIndex.value + 1).mod(_currentPlaylist.value.size)
        }
        playCurrentTrack("Next")
    }

    fun playPrevious() = scope.launch {
        if (_currentPlaylist.value.isEmpty()) return@launch
        val size = _currentPlaylist.value.size
        _currentIndex.value = (_currentIndex.value - 1 + size).mod(size)
        playCurrentTrack("Previous")
    }

    override fun onPlaybackEnded() {
        if (playStartTime > 0) {
            val duration = System.currentTimeMillis() - playStartTime
            if (duration > 0) {
                scope.launch {
                    playbackHistoryUseCase.recordListeningDuration(duration)
                }
            }
        }
        playStartTime = System.currentTimeMillis()
        lastDurationRecordTime = playStartTime
        
        playNext(forceChange = false)
    }

    override fun onPlaybackNext() {
        playNext(forceChange = true)
    }

    override fun onPlaybackPrev() {
        playPrevious()
    }

    override fun onPlayStateChanged(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
    }

    private suspend fun playCurrentTrack(source: String) {
        if (playControl == null) {
            Log.e("MusicController", "playCurrentTrack: playControl is null")
            return
        }
        stopProgressTracking()
        val track = _currentPlaylist.value.getOrNull(_currentIndex.value) ?: return
        
        scope.launch {
            try {
                val recentId = withTimeoutOrNull(1000) {
                    recentPlayListId.firstOrNull()
                }
                if (recentId != null) {
                    managePlaylistUseCase.addToPlaylist(recentId, track.music.id, track.music.path)
                }
            } catch (e: Exception) {
                // Ignore
            }
        }

        persistCurrentMusic(track.music.id)
        _currentPosition.value = 0L
        playControl?.playSingleMusic(track.music)
        _duration.value = track.music.duration
        startProgressTracking()
        recordPlayback(track.music.id, source)
    }

    fun addToPlaylist(musicInfo: MusicInfo) {
        if (_originalPlaylist.none { it.music.id == musicInfo.music.id }) {
            _originalPlaylist = _originalPlaylist + musicInfo
            updateCurrentPlaylist()
            scope.launch {
                saveToPlaylist(musicInfo)
            }
            showToast("已添加:${musicInfo.music.title}")
        } else {
            showToast("已存在:${musicInfo.music.title}")
        }
    }

    private fun switchToMusicInPlaylist(musicInfo: MusicInfo) {
        val index = _currentPlaylist.value.indexOfFirst { it.music.id == musicInfo.music.id }
        _currentIndex.value = if (index != -1) index else 0
    }

    fun removeFromPlaylist(musicInfo: MusicInfo) {
        _originalPlaylist = _originalPlaylist.filter { it.music.id != musicInfo.music.id }
        updateCurrentPlaylist()
        if (_currentPlaylist.value.isNotEmpty()) {
            val current = currentPlayingMusic.value
            val idx = _currentPlaylist.value.indexOfFirst { it.music.id == current?.music?.id }
            _currentIndex.value = if (idx >= 0) idx else 0
        } else {
            _currentIndex.value = 0
        }
        persistCurrentPlaylistToDatabase()
    }
    
    fun moveToTop(musicInfo: MusicInfo) {
        val currentList = _originalPlaylist.toMutableList()
        val index = currentList.indexOfFirst { it.music.id == musicInfo.music.id }
        if (index > 0) {
            val item = currentList.removeAt(index)
            currentList.add(0, item)
            _originalPlaylist = currentList
            updateCurrentPlaylist()
            val current = currentPlayingMusic.value
            if (current != null) {
                _currentIndex.value = _currentPlaylist.value.indexOfFirst { it.music.id == current.music.id }
            }
            persistCurrentPlaylistToDatabase()
            showToast("已置顶：${musicInfo.music.title}")
        }
    }

    suspend fun playAt(musicInfo: MusicInfo) {
        switchToMusicInPlaylist(musicInfo)
        playCurrentTrack("ManualPlay")
    }

    suspend fun playWith(musicInfo: MusicInfo) {
        addToPlaylist(musicInfo)
        playAt(musicInfo)
    }

    private fun currentMusicPath(): String? {
        return _currentPlaylist.value.getOrNull(_currentIndex.value)?.music?.path
    }

    fun recordPlayback(musicId: Long, source: String?) {
        scope.launch {
            val history = PlaybackHistory(
                musicId = musicId,
                playedAt = System.currentTimeMillis(),
                playDuration = 0,
                isCompleted = true,
                source = source
            )
            playbackHistoryUseCase.insertPlayback(history)
        }
    }

    fun updateMusicLikedStatus(musicInfo: MusicInfo, liked: Boolean) {
        scope.launch {
            currentPlaybackUseCase.updateLikedStatus(musicInfo.music.id, liked)
            try {
                val likedId = likedPlayListId.filterNotNull().first()
                if (liked) {
                    managePlaylistUseCase.addToPlaylist(likedId, musicInfo.music.id, musicInfo.music.path)
                } else {
                    managePlaylistUseCase.removeItemFromPlaylist(musicInfo.music.id, likedId)
                }
            } catch (e: Exception) {
                // Ignore
            }
            getLikedStatus(musicInfo.music.id)
        }
    }

    fun getLikedStatus(musicId: Long) {
        scope.launch {
            likeStatus.value = currentPlaybackUseCase.getLikedStatus(musicId)
        }
    }

    fun playHeartMode() {
        scope.launch {
            val currentMusic = currentPlayingMusic.value ?: return@launch
            val similarSongs = currentPlaybackUseCase.getSimilarSongsByWeightedLabels(currentMusic.music.id, limit = 10)
            if (similarSongs.isNotEmpty()) {
                val newList = listOf(currentMusic) + similarSongs
                _originalPlaylist = newList
                _currentPlaylist.value = newList
                _currentIndex.value = 0
                playCurrentTrack("HeartMode")
                showToast("为你推荐${similarSongs.size}首心动歌曲")
            } else {
                showToast("未找到相似歌曲")
            }
        }
    }

    fun startTimer(minutes: Int) {
        timerUseCase.setTimerRemaining((minutes * 60 * 1000L))
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive) {
                delay(1000)
                timerUseCase.decrementTimer(1000)
                if (timerUseCase.isTimerExpired()) {
                    pauseMusic()
                    timerUseCase.cancelTimer()
                    break
                }
            }
        }
    }
    
    fun cancelTimer() {
        timerJob?.cancel()
        timerUseCase.cancelTimer()
    }

    fun getMusicLabels(musicId: Long) {
        scope.launch {
            _currentMusicLabels.value = currentPlaybackUseCase.getMusicLabels(musicId)
        }
    }

    fun getMusicLyrics(musicId: Long) {
        scope.launch {
            _currentMusicLyrics.value = currentPlaybackUseCase.getMusicLyrics(musicId)
        }
    }

    private fun persistCurrentMusic(id: Long) {
        scope.launch { currentPlaybackUseCase.saveCurrentMusicId(id) }
    }

    private fun persistCurrentPlaylistToDatabase() {
        scope.launch {
            try {
                val playlistId = currentPlayListId.filterNotNull().first()
                val playlist = _originalPlaylist
                managePlaylistUseCase.resetPlaylistItems(playlistId, playlist)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
    
    // Audio Effect Logic
    private var saveAudioEffectJob: Job? = null
    
    private fun restoreAudioEffectSettings() {
        scope.launch {
            try {
                val equalizerPreset = settingsRepository.equalizerPreset.first()
                val bassBoostLevel = settingsRepository.bassBoostLevel.first()
                val isSurroundSoundEnabled = settingsRepository.isSurroundSoundEnabled.first()
                val reverbPreset = settingsRepository.reverbPreset.first()
                val customLevels = settingsRepository.customEqualizerLevels.first()
                
                playControl?.let { control ->
                    control.setEqualizerPreset(equalizerPreset)
                    control.setBassBoost(bassBoostLevel)
                    control.setSurroundSound(isSurroundSoundEnabled)
                    control.setReverb(reverbPreset)
                    if (customLevels.isNotEmpty()) {
                        control.setCustomEqualizer(customLevels)
                    }
                }
                
                _audioEffectSettings.value = AudioEffectSettings(
                    equalizerPreset = equalizerPreset,
                    bassBoostLevel = bassBoostLevel,
                    isSurroundSoundEnabled = isSurroundSoundEnabled,
                    reverbPreset = reverbPreset,
                    customEqualizerLevels = customLevels
                )
                
            } catch (e: Exception) {
                Log.e("MusicController", "Failed to restore audio effect settings", e)
            }
        }
    }
    
    fun initializeAudioEffects() {
        playControl?.let { control ->
            _equalizerPresets.value = control.getEqualizerPresets()
            _equalizerBandCount.value = control.getEqualizerBandCount()
            _equalizerBandLevelRange.value = control.getEqualizerBandLevelRange()
            _currentEqualizerBandLevels.value = control.getCurrentEqualizerBandLevels()
            
            _audioEffectSettings.value = AudioEffectSettings(
                equalizerPreset = control.getCurrentEqualizerPreset(),
                bassBoostLevel = control.getBassBoostLevel(),
                isSurroundSoundEnabled = control.isSurroundSoundEnabled(),
                reverbPreset = control.getReverbPreset(),
                customEqualizerLevels = control.getCurrentEqualizerBandLevels()
            )
        }
    }
    
    fun setEqualizerPreset(preset: Int) {
        playControl?.let { control ->
            control.setEqualizerPreset(preset)
            _audioEffectSettings.value = _audioEffectSettings.value.copy(
                equalizerPreset = preset
            )
            saveAudioEffectSetting {
                settingsRepository.saveEqualizerPreset(preset)
            }
        }
    }
    
    fun setBassBoost(level: Int) {
        playControl?.let { control ->
            control.setBassBoost(level)
            _audioEffectSettings.value = _audioEffectSettings.value.copy(
                bassBoostLevel = level
            )
            saveAudioEffectSetting {
                settingsRepository.saveBassBoostLevel(level)
            }
        }
    }
    
    fun setSurroundSound(enabled: Boolean) {
        playControl?.let { control ->
            control.setSurroundSound(enabled)
            _audioEffectSettings.value = _audioEffectSettings.value.copy(
                isSurroundSoundEnabled = enabled
            )
            saveAudioEffectSetting {
                settingsRepository.saveSurroundSoundEnabled(enabled)
            }
        }
    }
    
    fun setReverb(preset: Int) {
        playControl?.let { control ->
            control.setReverb(preset)
            _audioEffectSettings.value = _audioEffectSettings.value.copy(
                reverbPreset = preset
            )
            saveAudioEffectSetting {
                settingsRepository.saveReverbPreset(preset)
            }
        }
    }
    
    fun setCustomEqualizer(bandLevels: FloatArray) {
        playControl?.let { control ->
            control.setCustomEqualizer(bandLevels)
            _currentEqualizerBandLevels.value = bandLevels
            _audioEffectSettings.value = _audioEffectSettings.value.copy(
                customEqualizerLevels = bandLevels
            )
            saveAudioEffectSetting {
                settingsRepository.saveCustomEqualizerLevels(bandLevels)
            }
        }
    }
    
    private fun saveAudioEffectSetting(save: suspend () -> Unit) {
        saveAudioEffectJob?.cancel()
        saveAudioEffectJob = scope.launch {
            delay(500)
            try {
                save()
            } catch (e: Exception) {
                Log.e("MusicController", "Failed to save audio effect setting", e)
            }
        }
    }
    
    fun getCurrentEqualizerPreset(): Int {
        return playControl?.getCurrentEqualizerPreset() ?: 0
    }
    
    fun getBassBoostLevel(): Int {
        return playControl?.getBassBoostLevel() ?: 0
    }
    
    fun isSurroundSoundEnabled(): Boolean {
        return playControl?.isSurroundSoundEnabled() ?: false
    }
    
    fun getReverbPreset(): Int {
        return playControl?.getReverbPreset() ?: 0
    }
    
    fun getCurrentEqualizerBandLevels(): FloatArray {
        return playControl?.getCurrentEqualizerBandLevels() ?: floatArrayOf()
    }
    
    fun release() {
        stopProgressTracking()
        unbindService()
        scope.launch {
            persistCurrentPlaylistToDatabase()
        }
    }
}
