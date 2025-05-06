package com.example.hearablemusicplayer

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.hearablemusicplayer.database.Music

class MusicPlayService : Service() {

    // 提供给绑定组件访问 Service 的 Binder
    private val binder = MusicPlayServiceBinder()

    // ExoPlayer 实例
    private lateinit var exoPlayer: ExoPlayer

    // 播放完成监听器接口
    interface OnMusicCompleteListener {
        fun onPlaybackEnded()
    }

    private var playbackListener: OnMusicCompleteListener? = null

    // 绑定播放完成回调
    fun setOnMusicCompleteListener(listener: OnMusicCompleteListener) {
        playbackListener = listener
    }

    // 返回 Binder 实例
    override fun onBind(intent: Intent): IBinder = binder

    inner class MusicPlayServiceBinder : Binder() {
        fun getService(): MusicPlayService = this@MusicPlayService
    }

    override fun onCreate() {
        super.onCreate()
        exoPlayer = ExoPlayer.Builder(this).build().apply {
            addListener(object : androidx.media3.common.Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == androidx.media3.common.Player.STATE_ENDED) {
                        playbackListener?.onPlaybackEnded()
                    }
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
    }

    // 播放指定音乐
    fun prepareMusic(music: Music) {
        val mediaItem = MediaItem.Builder()
            .setUri(music.path)
            .setMediaId(music.id.toString())
            .setTag(music)
            .build()
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }

    fun playSingleMusic(music: Music) {
        prepareMusic(music)
        exoPlayer.play()
    }

    // 暂停播放
    fun pauseMusic() = exoPlayer.pause()

    // 继续播放
    fun proceedMusic() = exoPlayer.play()

    // 停止播放并清空播放内容
    fun stopMusic() {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
    }

    // 判断是否已加载目标音频
    fun isMusicLoaded(path: String): Boolean {
        val current = exoPlayer.currentMediaItem?.localConfiguration?.uri?.toString()
        return current == path && exoPlayer.playbackState != androidx.media3.common.Player.STATE_IDLE
    }

    // 跳转到指定位置（毫秒）
    fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
    }

    // 获取当前播放进度（毫秒）
    fun getCurrentPosition(): Long = exoPlayer.currentPosition

    // 获取当前音频总时长（毫秒）
    fun getDuration(): Long = exoPlayer.duration
}
