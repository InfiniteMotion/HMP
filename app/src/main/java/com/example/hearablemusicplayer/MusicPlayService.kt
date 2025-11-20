package com.example.hearablemusicplayer

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.media.AudioManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionResult
import coil.imageLoader
import coil.request.ImageRequest
import com.example.hearablemusicplayer.database.Music
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

interface PlayControl {
    fun play()
    fun pause()
    fun playSingleMusic(music: Music)
    fun seekTo(position: Long)
    fun getCurrentPosition(): Long
    fun getDuration(): Long
    fun stopMusic()
    fun prepareMusic(music: Music)
    fun isMusicLoaded(path: String): Boolean
    fun isReady():Boolean
    fun proceedMusic()
}

@UnstableApi
@AndroidEntryPoint
class MusicPlayService : Service(), PlayControl {

    companion object {
        const val ACTION_PLAY = "com.example.hearablemusicplayer.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.hearablemusicplayer.ACTION_PAUSE"
        const val ACTION_NEXT = "com.example.hearablemusicplayer.ACTION_NEXT"
        const val ACTION_PREV = "com.example.hearablemusicplayer.ACTION_PREV"
    }

    // 提供给绑定组件访问 Service 的 Binder
    private val binder = MusicPlayServiceBinder()

    // ExoPlayer 实例（通过 Hilt 注入）
    @Inject
    lateinit var exoPlayer: ExoPlayer

    // 自定义 Player 包装器，让系统认为始终有上/下一首
    private lateinit var customPlayer: ForwardingPlayer

    // MediaSession 实例
    private lateinit var mediaSession: MediaSession

    // 播放完成监听器接口
    interface OnMusicCompleteListener {
        fun onPlaybackEnded()
        fun onPlaybackPrev()
        fun onPlayStateChanged(isPlaying: Boolean) // 新增
    }

    private var playbackListener: OnMusicCompleteListener? = null

    // 耳机拔插和蓝牙断开广播接收器
    private val audioBecomingNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                AudioManager.ACTION_AUDIO_BECOMING_NOISY -> {
                    // 耳机拔出，暂停播放
                    Log.d("MusicPlayService", "Audio becoming noisy, pausing playback")
                    exoPlayer.pause()
                    playbackListener?.onPlayStateChanged(false)
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    // 蓝牙断开，暂停播放
                    Log.d("MusicPlayService", "Bluetooth disconnected, pausing playback")
                    exoPlayer.pause()
                    playbackListener?.onPlayStateChanged(false)
                }
            }
        }
    }

    private var isReceiverRegistered = false

    // 绑定播放完成回调
    fun setOnMusicCompleteListener(listener: OnMusicCompleteListener) {
        playbackListener = listener
        Log.d("MusicPlayService", "OnMusicCompleteListener set: ${true}")
    }

    // 返回 Binder 实例
    override fun onBind(intent: Intent): IBinder = binder

    inner class MusicPlayServiceBinder : Binder() {
        fun getService(): MusicPlayService = this@MusicPlayService
    }


    // 创建通知频道
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "music_channel", // 通知频道 ID
            "音乐播放",         // 通知频道名称
            NotificationManager.IMPORTANCE_LOW // 重要性:低,避免打扰
        ).apply {
            description = "播放控制通知"
            setSound(null, null) // API 36 建议显式设置声音
        }

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    // 创建通知
    @SuppressLint("RestrictedApi")
    private fun buildNotification(
        music: Music,
        albumArtBitmap: Bitmap?
    ): Notification {
        val mainPendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // API 36 要求使用 PendingIntent.FLAG_IMMUTABLE
        val prevPending = PendingIntent.getBroadcast(
            this, 1, Intent(this, MusicNotificationReceiver::class.java).setAction(ACTION_PREV),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val pausePending = PendingIntent.getBroadcast(
            this, 2, Intent(this, MusicNotificationReceiver::class.java).setAction(ACTION_PAUSE),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val playPending = PendingIntent.getBroadcast(
            this, 3, Intent(this, MusicNotificationReceiver::class.java).setAction(ACTION_PLAY),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val nextPending = PendingIntent.getBroadcast(
            this, 4, Intent(this, MusicNotificationReceiver::class.java).setAction(ACTION_NEXT),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val isPlaying = exoPlayer.isPlaying

        val builder = NotificationCompat.Builder(this, "music_channel")
            .setContentTitle(music.title)
            .setContentText(music.artist)
            .setSmallIcon(R.drawable.player_d)
            .setContentIntent(mainPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.backward_end_fill, "上一首", prevPending
                ).build()
            )
            .addAction(
                if (isPlaying)
                    NotificationCompat.Action.Builder(R.drawable.pause, "暂停", pausePending).build()
                else
                    NotificationCompat.Action.Builder(R.drawable.play_fill, "播放", playPending).build()
            )
            .addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.forward_end_fill, "下一首", nextPending
                ).build()
            )
            .setStyle(
                androidx.media3.session.MediaStyleNotificationHelper.MediaStyle(mediaSession)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setOngoing(isPlaying)

        // 只在有封面时设置大图标
        if (albumArtBitmap != null) {
            builder.setLargeIcon(albumArtBitmap)
        }

        return builder.build()
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate() {
        super.onCreate()
        // ExoPlayer 已通过 Hilt 注入，这里只需配置
        exoPlayer.apply {
            // 设置重复模式，让系统知道有下一首
            repeatMode = Player.REPEAT_MODE_OFF
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        getCurrentPlayingMusic()?.let { updateNotificationWithCover(it) }
                    }
                    if (playbackState == Player.STATE_ENDED) {
                        playbackListener?.onPlaybackEnded()
                    }
                }
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    getCurrentPlayingMusic()?.let { updateNotificationPlaybackState(it) }
                    playbackListener?.onPlayStateChanged(isPlaying)
                }
            })
        }
        createNotificationChannel()

        // 注册耳机拔插和蓝牙断开广播接收器
        registerAudioDeviceReceiver()

        // 创建自定义 Player 包装器
        customPlayer = object : ForwardingPlayer(exoPlayer) {
            // 重写方法让系统认为始终有上/下一首
            override fun getAvailableCommands(): Player.Commands {
                return super.getAvailableCommands().buildUpon()
                    .add(COMMAND_SEEK_TO_NEXT)
                    .add(COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                    .add(COMMAND_SEEK_TO_PREVIOUS)
                    .add(COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                    .build()
            }

            // 重写 seekToNext 方法
            override fun seekToNext() {
                Log.d("MusicPlayService", "ForwardingPlayer.seekToNext() called")
                playbackListener?.onPlaybackEnded()
            }

            // 重写 seekToPrevious 方法
            override fun seekToPrevious() {
                Log.d("MusicPlayService", "ForwardingPlayer.seekToPrevious() called")
                playbackListener?.onPlaybackPrev()
            }

            // 告诉系统有下一首
            override fun hasNextMediaItem(): Boolean = true

            // 告诉系统有上一首
            override fun hasPreviousMediaItem(): Boolean = true
        }

        // 创建 Media3 MediaSession，使用自定义 Player
        mediaSession = MediaSession.Builder(this, customPlayer)
            .setCallback(object : MediaSession.Callback {
                override fun onConnect(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo
                ): MediaSession.ConnectionResult {
                    return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                        .setAvailableSessionCommands(
                            MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS
                        )
                        .setAvailablePlayerCommands(
                            MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS
                        )
                        .build()
                }

                // 添加命令拦截，确保系统命令被正确处理
                override fun onPlayerCommandRequest(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo,
                    playerCommand: Int
                ): Int {
                    Log.d("MusicPlayService", "onPlayerCommandRequest: $playerCommand, listener: ${playbackListener != null}")
                    when (playerCommand) {
                        Player.COMMAND_SEEK_TO_NEXT, Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM -> {
                            Log.d("MusicPlayService", "Seek to next requested")
                            // 在主线程调用 listener
                            CoroutineScope(Dispatchers.Main).launch {
                                playbackListener?.onPlaybackEnded()
                            }
                            return SessionResult.RESULT_SUCCESS
                        }
                        Player.COMMAND_SEEK_TO_PREVIOUS, Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM -> {
                            Log.d("MusicPlayService", "Seek to previous requested")
                            // 在主线程调用 listener
                            CoroutineScope(Dispatchers.Main).launch {
                                playbackListener?.onPlaybackPrev()
                            }
                            return SessionResult.RESULT_SUCCESS
                        }
                    }
                    return super.onPlayerCommandRequest(session, controller, playerCommand)
                }
            })
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterAudioDeviceReceiver()
        mediaSession.release()
        exoPlayer.release()
    }

    // 注册音频设备广播接收器
    private fun registerAudioDeviceReceiver() {
        if (!isReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
            registerReceiver(audioBecomingNoisyReceiver, filter)
            isReceiverRegistered = true
            Log.d("MusicPlayService", "Audio device receiver registered")
        }
    }

    // 注销音频设备广播接收器
    private fun unregisterAudioDeviceReceiver() {
        if (isReceiverRegistered) {
            try {
                unregisterReceiver(audioBecomingNoisyReceiver)
                isReceiverRegistered = false
                Log.d("MusicPlayService", "Audio device receiver unregistered")
            } catch (e: IllegalArgumentException) {
                Log.e("MusicPlayService", "Receiver already unregistered", e)
            }
        }
    }

    // 播放指定音乐
    override fun prepareMusic(music: Music) {
        val mediaItem = MediaItem.Builder()
            .setUri(music.path)
            .setMediaId(music.id.toString()) // 用id作为唯一标识
            .setMediaMetadata(
                androidx.media3.common.MediaMetadata.Builder()
                    .setTitle(music.title)
                    .setArtist(music.artist)
                    .setAlbumTitle(music.album)
                    .build()
            )
            .build()
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()

    }

    override fun play() { exoPlayer.play() }
    override fun pause() { exoPlayer.pause() }

    override fun playSingleMusic(music: Music) {
        cacheMusic(music)
        prepareMusic(music)
        exoPlayer.play()
    }

    // 继续播放
    override fun proceedMusic() = exoPlayer.play()

    // 停止播放并清空播放内容
    override fun stopMusic() {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
    }

    // 判断是否已加载目标音频
    override fun isMusicLoaded(path: String): Boolean {
        val current = exoPlayer.currentMediaItem?.localConfiguration?.uri?.toString()
        return current == path && exoPlayer.playbackState != Player.STATE_IDLE
    }

    // 判断是否就绪
    override fun isReady():Boolean{
        return exoPlayer.playbackState == Player.STATE_READY
    }

    // 跳转到指定位置（毫秒）
     override fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
    }

    // 获取当前播放进度（毫秒）
    override fun getCurrentPosition(): Long = exoPlayer.currentPosition

    // 获取当前音频总时长（毫秒）
    override fun getDuration(): Long = exoPlayer.duration

    // 假设你有一个 musicMap: Map<Long, Music>
    private val musicMap = mutableMapOf<Long, Music>()

    // 添加音乐时同步到 map
    private fun cacheMusic(music: Music) {
        musicMap[music.id] = music
    }

    // 获取当前播放音乐
    private fun getCurrentPlayingMusic(): Music? {
        val mediaId = exoPlayer.currentMediaItem?.mediaId?.toLongOrNull() ?: return null
        return musicMap[mediaId]
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                exoPlayer.play()
                getCurrentPlayingMusic()?.let { updateNotificationPlaybackState(it) }
                playbackListener?.onPlayStateChanged(true) // 新增
            }
            ACTION_PAUSE -> {
                exoPlayer.pause()
                getCurrentPlayingMusic()?.let { updateNotificationPlaybackState(it) }
                playbackListener?.onPlayStateChanged(false) // 新增
            }
            ACTION_NEXT -> playbackListener?.onPlaybackEnded()
            ACTION_PREV -> playbackListener?.onPlaybackPrev()
        }

        val music = getCurrentPlayingMusic()
        music?.let {
            // 切换到主线程
            CoroutineScope(Dispatchers.Main).launch {
                val notification = buildNotification(it, null)
                // API 34+ 需要指定前台服务类型
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    startForeground(
                        1,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                    )
                } else {
                    startForeground(1, notification)
                }

                // 异步加载封面
                CoroutineScope(Dispatchers.IO).launch {
                    val request = ImageRequest.Builder(this@MusicPlayService)
                        .data(music.albumArtUri)
                        .allowHardware(false)
                        .build()
                    val result = imageLoader.execute(request)
                    val bitmap = (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                    if (bitmap != null) {
                        // 回到主线程更新通知
                        CoroutineScope(Dispatchers.Main).launch {
                            val updatedNotification = buildNotification(it, bitmap)
                            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                            manager.notify(1, updatedNotification)
                        }
                    }
                }
            }
        }
        return START_STICKY
    }

    // 只用已有封面刷新通知（不重新加载封面）
    private fun updateNotificationPlaybackState(music: Music) {
        CoroutineScope(Dispatchers.Main).launch {
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val notification = buildNotification(music, currentAlbumArtBitmap)
            manager.notify(1, notification)
        }
    }

    private var currentAlbumArtBitmap: Bitmap? = null
    // 刷新通知(重新加载封面)
    private fun updateNotificationWithCover(music: Music) {
        // 先显示默认封面
        val notification = buildNotification(music, null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                1,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(1, notification)
        }

        // 异步加载封面
        CoroutineScope(Dispatchers.IO).launch {
            val request = ImageRequest.Builder(this@MusicPlayService)
                .data(music.albumArtUri)
                .size(256)
                .allowHardware(false)
                .build()
            val result = imageLoader.execute(request)
            val bitmap = (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
            if (bitmap != null) {
                CoroutineScope(Dispatchers.Main).launch {
                    currentAlbumArtBitmap = bitmap // 缓存封面
                    val updatedNotification = buildNotification(music, bitmap)
                    val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    manager.notify(1, updatedNotification)
                }
            }
        }
    }
}
