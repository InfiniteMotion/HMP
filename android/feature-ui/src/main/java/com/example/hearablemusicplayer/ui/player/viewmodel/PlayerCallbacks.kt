package com.example.hearablemusicplayer.ui.player.viewmodel

import com.hmp.domain.music.MusicInfo
import com.hmp.domain.playlist.AlgorithmType
import com.hmp.domain.playlist.ExtensionConfig
import com.hmp.domain.playlist.WeightTemplate

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