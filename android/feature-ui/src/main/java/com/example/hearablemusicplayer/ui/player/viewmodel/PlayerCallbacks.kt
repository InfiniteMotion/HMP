package com.example.hearablemusicplayer.ui.player.viewmodel

import com.example.hearablemusicplayer.domain.music.MusicInfo
import com.example.hearablemusicplayer.domain.playlist.AlgorithmType
import com.example.hearablemusicplayer.domain.playlist.ExtensionConfig
import com.example.hearablemusicplayer.domain.playlist.WeightTemplate

interface PlayerCallbacks {
    fun onSeek(position: Long)
    fun onPlayPause()
    fun onNext()
    fun onPrevious()
    fun onPlaybackModeChange()
    fun onFavorite()
    fun onTimerClick(minutes: Int)
    fun onShowTimerDialog()
    fun onCancelTimer()
    fun onHeartMode()
    fun onGeneratePlaylist(seedMusicId: Long)
    fun onSaveDefaultConfig(algorithmType: AlgorithmType, weightTemplate: WeightTemplate, extensionConfig: ExtensionConfig)
    fun onArtistClick(artistName: String)
    fun onClearPlaylist()
    fun onPlayItem(musicInfo: MusicInfo)
    fun onMoveToTop(musicInfo: MusicInfo)
    fun onRemoveFromPlaylist(musicInfo: MusicInfo)
    fun onBackClick()
}