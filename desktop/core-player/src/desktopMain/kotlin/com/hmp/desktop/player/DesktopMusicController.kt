package com.hmp.desktop.player

import com.hmp.data.database.currentTimeMillis
import com.hmp.domain.enum.PlaybackMode
import com.hmp.domain.music.MusicInfo
import com.hmp.domain.playlist.usecase.ManagePlaylistUseCase
import com.hmp.domain.setting.SettingsRepository
import com.hmp.domain.setting.usecase.CurrentPlaybackUseCase
import com.hmp.domain.setting.usecase.PlaybackHistoryUseCase
import com.hmp.domain.setting.usecase.TimerUseCase
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DesktopMusicController(
    private val audioEngine: AudioEngine,
    private val currentPlaybackUseCase: CurrentPlaybackUseCase,
    private val playbackHistoryUseCase: PlaybackHistoryUseCase,
    private val timerUseCase: TimerUseCase,
    private val managePlaylistUseCase: ManagePlaylistUseCase,
    private val settingsRepository: SettingsRepository
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

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

    private val _isMiniPlayerVisible = MutableStateFlow(true)
    val isMiniPlayerVisible: StateFlow<Boolean> = _isMiniPlayerVisible.asStateFlow()

    fun setMiniPlayerVisible(visible: Boolean) {
        _isMiniPlayerVisible.value = visible
    }

    // Playlist
    private val _currentPlaylist = MutableStateFlow<List<MusicInfo>>(emptyList())
    val currentPlaylist: StateFlow<List<MusicInfo>> = _currentPlaylist.asStateFlow()

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

    // Playback tracking
    private var currentPlaybackHistoryId: Long? = null
    private var totalPlayedDurationInSession: Long = 0L
    private val skipThresholdMs = 20000L
    private val skipThresholdPercent = 0.15f

    // Timer
    private var timerJob: Job? = null
    val timerRemaining: StateFlow<Long?> = timerUseCase.timerRemaining

    init {
        // Set up audio engine callbacks
        audioEngine.onPlaybackComplete = {
            onMusicComplete()
        }
        audioEngine.onError = { e ->
            showToast("Playback error: ${e.message}")
        }

        // Initialize playlist and progress
        scope.launch {
            loadPlaylistFromSettings()
            restoreLastPosition()
        }

        // Monitor playlist changes
        scope.launch {
            currentPlayListId
                .filterNotNull()
                .collectLatest { playlistId ->
                    managePlaylistUseCase.getMusicInfoInPlaylist(playlistId)
                        .collect { playlist ->
                            _currentPlaylist.value = playlist
                            updateCurrentIndex()
                        }
                }
        }

        // Monitor current music changes
        scope.launch {
            currentPlayingMusic
                .filterNotNull()
                .collectLatest { musicInfo ->
                    preloadCurrentMusicInfo(musicInfo)
                }
        }
    }

    private fun updateCurrentIndex() {
        val current = currentPlayingMusic.value
        if (current != null) {
            _currentIndex.value = _currentPlaylist.value.indexOfFirst { it.music.id == current.music.id }
                .takeIf { it >= 0 } ?: 0
        }
    }

    private suspend fun restoreLastPosition() {
        try {
            val lastPos = settingsRepository.currentPosition.first()
            _currentPosition.value = lastPos
        } catch (_: Exception) {}
    }

    private suspend fun loadPlaylistFromSettings() {
        try {
            val playlistId = currentPlayListId.filterNotNull().first()
            val currentMusicId = currentPlaybackUseCase.getCurrentMusicId().first()
            val list = managePlaylistUseCase.getMusicInfoInPlaylist(playlistId).first()
            _currentPlaylist.value = list
            _playbackMode.value = PlaybackMode.SEQUENTIAL
            _currentIndex.value = list.indexOfFirst { it.music.id == currentMusicId }.takeIf { it >= 0 } ?: 0
        } catch (_: Exception) {}
    }

    fun preloadCurrentMusicInfo(musicInfo: MusicInfo) {
        _duration.value = musicInfo.music.duration
    }

    // region Playback Controls

    fun play() {
        val music = currentPlayingMusic.value ?: return
        if (audioEngine.isLoaded() && !audioEngine.isPlaying()) {
            audioEngine.resume()
            startProgressTracking()
            _isPlaying.value = true
            return
        }
        playMusic(music)
    }

    fun pause() {
        audioEngine.pause()
        _isPlaying.value = false
        stopProgressTracking()
        persistCurrentPosition(_currentPosition.value)
    }

    fun togglePlayPause() {
        if (_isPlaying.value) pause() else play()
    }

    fun playMusic(musicInfo: MusicInfo) {
        scope.launch {
            // Record previous play session
            recordCurrentPlaySession(isCompleted = false)

            audioEngine.stop()
            audioEngine.play(musicInfo.music.path)

            _isPlaying.value = true
            _duration.value = musicInfo.music.duration
            _currentPosition.value = 0L
            seekPositionMs = 0L

            // Save current music ID
            currentPlaybackUseCase.saveCurrentMusicId(musicInfo.music.id)

            // Update index
            updateCurrentIndex()

            startProgressTracking()
            recordPlaybackStart()
        }
    }

    fun playNext() {
        val playlist = _currentPlaylist.value
        if (playlist.isEmpty()) return

        val nextIndex = when (_playbackMode.value) {
            PlaybackMode.REPEAT_ONE -> _currentIndex.value
            PlaybackMode.SHUFFLE -> (playlist.indices).random()
            PlaybackMode.SEQUENTIAL -> {
                val next = _currentIndex.value + 1
                if (next >= playlist.size) 0 else next
            }
        }

        _currentIndex.value = nextIndex
        val music = playlist.getOrNull(nextIndex) ?: return
        playMusic(music)
    }

    fun playPrevious() {
        val playlist = _currentPlaylist.value
        if (playlist.isEmpty()) return

        val prevIndex = when (_playbackMode.value) {
            PlaybackMode.REPEAT_ONE -> _currentIndex.value
            PlaybackMode.SHUFFLE -> (playlist.indices).random()
            PlaybackMode.SEQUENTIAL -> {
                val prev = _currentIndex.value - 1
                if (prev < 0) playlist.size - 1 else prev
            }
        }

        _currentIndex.value = prevIndex
        val music = playlist.getOrNull(prevIndex) ?: return
        playMusic(music)
    }

    fun seekTo(positionMs: Long) {
        audioEngine.seekTo(positionMs)
        _currentPosition.value = positionMs
        seekPositionMs = positionMs
    }

    fun setVolume(volume: Float) {
        audioEngine.setVolume(volume)
    }

    fun togglePlaybackMode() {
        _playbackMode.value = when (_playbackMode.value) {
            PlaybackMode.SEQUENTIAL -> PlaybackMode.REPEAT_ONE
            PlaybackMode.REPEAT_ONE -> PlaybackMode.SHUFFLE
            PlaybackMode.SHUFFLE -> PlaybackMode.SEQUENTIAL
        }
    }

    // endregion

    // region Progress Tracking

    private var seekPositionMs: Long = 0L

    private fun startProgressTracking() {
        if (progressJob?.isActive == true) return

        progressJob = scope.launch {
            while (isActive) {
                val pos = audioEngine.getCurrentPosition()
                _currentPosition.value = pos
                persistCurrentPosition(pos)
                recordListeningDurationPeriodically()
                delay(500)
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun persistCurrentPosition(position: Long) {
        scope.launch {
            try {
                settingsRepository.saveCurrentPosition(position)
            } catch (_: Exception) {}
        }
    }

    private fun recordListeningDurationPeriodically() {
        val now = currentTimeMillis()
        if (playStartTime == 0L) {
            playStartTime = now
            lastDurationRecordTime = now
            return
        }
        if (now - lastDurationRecordTime >= durationRecordThreshold) {
            val elapsed = now - lastDurationRecordTime
            scope.launch {
                try {
                    playbackHistoryUseCase.recordListeningDuration(elapsed)
                } catch (_: Exception) {}
            }
            lastDurationRecordTime = now
        }
    }

    // endregion

    // region Playback History

    private fun recordPlaybackStart() {
        scope.launch {
            try {
                val music = currentPlayingMusic.value ?: return@launch
                val historyId = playbackHistoryUseCase.insertPlayback(
                    com.hmp.domain.setting.model.PlaybackHistory(
                        musicId = music.music.id,
                        playedAt = currentTimeMillis(),
                        playDuration = 0L,
                        isCompleted = false,
                        source = "direct"
                    )
                )
                currentPlaybackHistoryId = historyId
                totalPlayedDurationInSession = 0L
                playStartTime = currentTimeMillis()
                lastDurationRecordTime = currentTimeMillis()
            } catch (_: Exception) {}
        }
    }

    private fun recordCurrentPlaySession(isCompleted: Boolean) {
        val historyId = currentPlaybackHistoryId ?: return
        val music = currentPlayingMusic.value ?: return

        scope.launch {
            try {
                val currentPos = _currentPosition.value
                val duration = music.music.duration

                val isSkip = !isCompleted && (
                    currentPos < skipThresholdMs ||
                    (duration > 0 && currentPos < duration * skipThresholdPercent)
                )

                if (isCompleted) {
                    playbackHistoryUseCase.completePlaybackSession(historyId, music.music.id, currentPos)
                } else {
                    playbackHistoryUseCase.skipPlaybackSession(historyId, music.music.id, currentPos, isSkip)
                }

                currentPlaybackHistoryId = null
                totalPlayedDurationInSession = 0L
            } catch (_: Exception) {}
        }
    }

    // endregion

    // region Music Complete

    private fun onMusicComplete() {
        scope.launch {
            val music = currentPlayingMusic.value
            if (music != null) {
                recordCurrentPlaySession(isCompleted = true)
            }

            when (_playbackMode.value) {
                PlaybackMode.REPEAT_ONE -> {
                    music?.let { playMusic(it) }
                }
                PlaybackMode.SHUFFLE, PlaybackMode.SEQUENTIAL -> {
                    playNext()
                }
            }
        }
    }

    // endregion

    // region Timer

    fun startTimer(durationMs: Long) {
        timerJob?.cancel()
        timerUseCase.setTimerRemaining(durationMs)
        timerJob = scope.launch {
            var remaining = durationMs
            while (remaining > 0) {
                delay(1000)
                remaining -= 1000
                timerUseCase.decrementTimer(1000)
            }
            pause()
            showToast("Timer ended")
        }
    }

    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        timerUseCase.cancelTimer()
    }

    // endregion

    fun setPlaylist(list: List<MusicInfo>, startIndex: Int = 0) {
        _currentPlaylist.value = list
        _currentIndex.value = startIndex.coerceIn(0, (list.size - 1).coerceAtLeast(0))
        val music = list.getOrNull(_currentIndex.value) ?: return
        playMusic(music)
    }

    fun release() {
        stopProgressTracking()
        recordCurrentPlaySession(isCompleted = false)
        persistCurrentPosition(_currentPosition.value)
        audioEngine.release()
    }
}
