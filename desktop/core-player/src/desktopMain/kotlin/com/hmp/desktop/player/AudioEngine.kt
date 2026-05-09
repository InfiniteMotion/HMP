package com.hmp.desktop.player

interface AudioEngine {
    fun play(path: String)
    fun pause()
    fun resume()
    fun stop()
    fun seekTo(positionMs: Long)
    fun getCurrentPosition(): Long
    fun getDuration(): Long
    fun isPlaying(): Boolean
    fun isLoaded(): Boolean
    fun setVolume(volume: Float)
    fun release()

    var onPlaybackComplete: (() -> Unit)?
    var onError: ((Exception) -> Unit)?
}
