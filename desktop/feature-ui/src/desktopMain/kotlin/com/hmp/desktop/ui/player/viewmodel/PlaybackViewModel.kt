package com.hmp.desktop.ui.player.viewmodel

import androidx.lifecycle.ViewModel
import com.hmp.domain.enum.PlaybackMode
import com.hmp.domain.music.MusicInfo
import com.hmp.desktop.player.DesktopMusicController
import kotlinx.coroutines.flow.StateFlow

class PlaybackViewModel(
    private val musicController:DesktopMusicController
) : ViewModel() {

    val isPlaying: StateFlow<Boolean> = musicController.isPlaying
    val currentPosition: StateFlow<Long> = musicController.currentPosition
    val duration: StateFlow<Long> = musicController.duration
    val playbackMode: StateFlow<PlaybackMode> = musicController.playbackMode
    val currentIndex: StateFlow<Int> = musicController.currentIndex
    val currentPlayingMusic: StateFlow<MusicInfo?> = musicController.currentPlayingMusic
    val timerRemaining: StateFlow<Long?> = musicController.timerRemaining
    val isMiniPlayerVisible: StateFlow<Boolean> = musicController.isMiniPlayerVisible

    fun playOrResume() = musicController.playOrResume()
    fun pauseMusic() = musicController.pauseMusic()
    fun playNext() = musicController.playNext()
    fun playPrevious() = musicController.playPrevious()
    fun seekTo(position: Long) = musicController.seekTo(position)
    fun togglePlaybackModeByOrder() = musicController.togglePlaybackModeByOrder()
    fun startTimer(minutes: Int) = musicController.startTimer(minutes)
    fun cancelTimer() = musicController.cancelTimer()
    fun startProgressTracking() = musicController.startProgressTracking()
    fun stopProgressTracking() = musicController.stopProgressTracking()
    fun setMiniPlayerVisible(visible: Boolean) = musicController.setMiniPlayerVisible(visible)
    fun setPlaylist(list: List<MusicInfo>, startIndex: Int = 0) = musicController.setPlaylist(list, startIndex)
}