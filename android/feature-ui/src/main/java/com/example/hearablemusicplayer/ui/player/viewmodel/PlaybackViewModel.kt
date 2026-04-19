package com.example.hearablemusicplayer.ui.player.viewmodel

import androidx.lifecycle.ViewModel
import androidx.media3.common.util.UnstableApi
import com.example.hearablemusicplayer.domain.enum.PlaybackMode
import com.example.hearablemusicplayer.domain.music.MusicInfo
import com.example.hearablemusicplayer.player.controller.MusicController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
@UnstableApi
class PlaybackViewModel @Inject constructor(
    private val musicController: MusicController
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
}