package com.example.hearablemusicplayer.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hearablemusicplayer.MusicPlayService
import com.example.hearablemusicplayer.database.MusicInfo
import com.example.hearablemusicplayer.database.MusicLabel
import com.example.hearablemusicplayer.database.PlaybackHistory
import com.example.hearablemusicplayer.database.myenum.PlaybackMode
import com.example.hearablemusicplayer.repository.MusicRepository
import com.example.hearablemusicplayer.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlayControlViewModel(
    application: Application,
    private val musicRepo: MusicRepository,
    private val settingsRepo: SettingsRepository
) : AndroidViewModel(application), MusicPlayService.OnMusicCompleteListener {

    private val context: Context = application.applicationContext
    private var musicService: MusicPlayService? = null

    // 播放状态（播放/暂停）
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // 原始播放列表（用于顺序或打乱）
    private var _originalPlaylist: List<MusicInfo> = emptyList()
    private val _currentPlaylist = MutableStateFlow<List<MusicInfo>>(emptyList())
    val currentPlaylist: StateFlow<List<MusicInfo>> = _currentPlaylist.asStateFlow()

    // SHUFFLE 模式缓存打乱后的列表
    private var _shuffledPlaylist: List<MusicInfo>? = null

    // 当前播放列表ID和收藏/最近播放列表ID
    private val currentPlayListId = settingsRepo.currentPlaylistId
    private val likedPlayListId = settingsRepo.likedPlaylistId
    private val recentPlayListId = settingsRepo.recentPlaylistId

    // 当前播放索引
    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    // 当前播放的音乐（自动更新）
    val currentPlayingMusic: StateFlow<MusicInfo?> = combine(
        currentPlaylist,
        currentIndex
    ) { playlist, index ->
        playlist.getOrNull(index)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // 喜爱状态
    var likeStatus = MutableStateFlow(false)

    // 歌曲标签
    private val _currentMusicLabels = MutableStateFlow<List<MusicLabel?>>(emptyList())
    val currentMusicLabels: StateFlow<List<MusicLabel?>> = _currentMusicLabels

    // 歌曲歌词
    private val _currentMusicLyrics = MutableStateFlow<String?>(null)
    val currentMusicLyrics: StateFlow<String?> = _currentMusicLyrics

    // 播放模式（顺序、单曲循环、随机）
    private val _playbackMode = MutableStateFlow(PlaybackMode.SEQUENTIAL)
    val playbackMode: StateFlow<PlaybackMode> = _playbackMode.asStateFlow()

    // 当前播放进度与总时长
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    private var progressJob: Job? = null

    // 与 MusicPlayService 的连接管理
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            musicService = (binder as MusicPlayService.MusicPlayServiceBinder).getService().also {
                it.setOnMusicCompleteListener(this@PlayControlViewModel)
            }
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
        }
    }

    init {
        // 启动并绑定音乐播放服务
        Intent(context, MusicPlayService::class.java).also { intent ->
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        // 初始化播放列表和当前播放歌曲索引
        viewModelScope.launch {
            val playlistId = currentPlayListId.first() ?: return@launch
            val currentMusicId = settingsRepo.currentMusicId.first()
            val list = musicRepo.getMusicInfoInPlaylist(playlistId).first()
            _originalPlaylist = list
            _currentPlaylist.value = list
            _currentIndex.value = list.indexOfFirst { it.music.id == currentMusicId }.takeIf { it >= 0 } ?: 0
        }

        // 监听播放模式变更并更新播放列表
        viewModelScope.launch {
            settingsRepo.playbackMode
                .filterNotNull()
                .collectLatest { mode ->
                    _playbackMode.value = mode
                    updateCurrentPlaylist()
                }
        }
    }

    // 开始监听播放进度
    fun startProgressTracking() {
        if (progressJob?.isActive == true) return // 避免重复启动

        progressJob = viewModelScope.launch {
            while (isActive) {
                musicService?.let { svc ->
                    _currentPosition.value = svc.getCurrentPosition()
                    _duration.value = svc.getDuration()
                }
                delay(500)
            }
        }
    }

    // 停止监听播放进度
    fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }

    // 清空播放列表
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


    // 向播放列表添加歌曲
    private fun saveToPlaylist(musicInfo: MusicInfo) {
        viewModelScope.launch {
            currentPlayListId.firstOrNull()?.let { playlistId ->
                musicRepo.addToPlaylist(playlistId, musicInfo.music.id, musicInfo.music.path)
            }
        }
    }

    // 播放或恢复当前歌曲
    fun playOrResume() {
        val path = currentMusicPath()
        if (path != null && musicService?.isMusicLoaded(path) == true) {
            musicService?.proceedMusic()
        } else {
            viewModelScope.launch { playCurrentTrack("AutoPlay") }
        }
        _isPlaying.value = true
    }

    // 暂停播放
    fun pauseMusic() {
        musicService?.pauseMusic()
        _isPlaying.value = false
    }

    // 跳转到指定进度
    fun seekTo(position: Long) {
        musicService?.seekTo(position)
    }

    // 根据播放模式更新当前播放列表
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
        // 更新当前播放索引
        _currentIndex.value = currentTrack?.let { track ->
            _currentPlaylist.value.indexOfFirst { it.music.id == track.music.id }
        }?.takeIf { it >= 0 } ?: 0
    }

    private fun togglePlaybackMode(newMode: PlaybackMode) {
        _playbackMode.value = newMode
        persistPlaybackMode(newMode)
        // 切换播放模式时：清空旧的 SHUFFLE 列表（确保每次进入 SHUFFLE 是新打乱）
        _shuffledPlaylist = null
        updateCurrentPlaylist()
    }

    // 切换播放模式
    fun togglePlaybackModeByOrder() {
        val next = when (_playbackMode.value) {
            PlaybackMode.SEQUENTIAL -> PlaybackMode.REPEAT_ONE
            PlaybackMode.REPEAT_ONE -> PlaybackMode.SHUFFLE
            PlaybackMode.SHUFFLE -> PlaybackMode.SEQUENTIAL
        }
        togglePlaybackMode(next)
    }


    // 播放全部歌曲（顺序）
    fun addAllToPlaylistInOrder() {
        viewModelScope.launch {
            _originalPlaylist = musicRepo.getAllMusicInfoAsList()
            togglePlaybackMode(PlaybackMode.SEQUENTIAL)
            _currentPlaylist.value = _originalPlaylist
            _currentIndex.value = 0
            playCurrentTrack("In Order")
        }
        persistCurrentPlaylistToDatabase()
    }

    // 播放全部歌曲（随机）
    fun addAllToPlaylistByShuffle() {
        viewModelScope.launch {
            _originalPlaylist = musicRepo.getAllMusicInfoAsList()
            togglePlaybackMode(PlaybackMode.SHUFFLE)
            _currentPlaylist.value = _shuffledPlaylist!!
            _currentIndex.value = 0
            playCurrentTrack("By Shuffle")
        }
        persistCurrentPlaylistToDatabase()
    }

    // 播放下一首
    fun playNext() = viewModelScope.launch {
        if (_currentPlaylist.value.isEmpty()) return@launch
        if (_playbackMode.value != PlaybackMode.REPEAT_ONE) {
            _currentIndex.value = (_currentIndex.value + 1).mod(_currentPlaylist.value.size)
        }
        playCurrentTrack("Next")
    }

    // 播放上一首
    fun playPrevious() = viewModelScope.launch {
        if (_currentPlaylist.value.isEmpty()) return@launch
        if (_playbackMode.value != PlaybackMode.REPEAT_ONE) {
            val size = _currentPlaylist.value.size
            _currentIndex.value = (_currentIndex.value - 1 + size).mod(size)
        }
        playCurrentTrack("Previous")
    }

    // 播放完成自动播放下一首
    override fun onPlaybackEnded() {
        viewModelScope.launch {
            if (_playbackMode.value != PlaybackMode.REPEAT_ONE) {
                _currentIndex.value = (_currentIndex.value + 1).mod(_currentPlaylist.value.size)
            }
            playCurrentTrack("AutoPlay")
        }
    }

    // 将音乐添加到准备
    fun  prepareMusic(musicInfo: MusicInfo){
        musicService?.prepareMusic(musicInfo.music)
    }

    // 播放当前索引对应的歌曲
    private suspend fun playCurrentTrack(source: String) {
        val track = _currentPlaylist.value.getOrNull(_currentIndex.value) ?: return
        recentPlayListId.first()?.let {
            musicRepo.addToPlaylist(it, track.music.id, track.music.path)
        }
        persistCurrentMusic(track.music.id)
        musicService?.playSingleMusic(track.music)
        _isPlaying.value = true
        recordPlayback(track.music.id, source)
    }

    // 将音乐加入播放队列(不确定是否在播放列表中)
    fun addToPlaylist(musicInfo: MusicInfo) {
        viewModelScope.launch {
            if (_originalPlaylist.none { it.music.id == musicInfo.music.id }) {
                _originalPlaylist = _originalPlaylist + musicInfo
                updateCurrentPlaylist()
                saveToPlaylist(musicInfo)
                Toast.makeText(context, "歌曲${musicInfo.music.title}已被添加至播放列表", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(context, "该歌曲已在播放列表中", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun switchToMusicInPlaylist(musicInfo: MusicInfo) {
        val index = _currentPlaylist.value.indexOfFirst { it.music.id == musicInfo.music.id }
        _currentIndex.value = if (index != -1) index else 0
    }

    // 从指定音乐开始播放(已经在播放列表中)
    fun playAt(musicInfo: MusicInfo) {
        viewModelScope.launch {
            switchToMusicInPlaylist(musicInfo)
            playCurrentTrack("ManualPlay")
        }
    }

    // 从指定音乐开始播放(不确定是否在播放列表中)
    fun playWith(musicInfo: MusicInfo) {
        viewModelScope.launch {
            addToPlaylist(musicInfo)
            playAt(musicInfo)
        }
    }

    // 获取当前音乐路径
    private fun currentMusicPath(): String? {
        return _currentPlaylist.value.getOrNull(_currentIndex.value)?.music?.path
    }

    // 记录播放历史
    fun recordPlayback(musicId: Long, source: String?) {
        viewModelScope.launch {
            val history = PlaybackHistory(
                musicId = musicId,
                playedAt = System.currentTimeMillis(),
                playDuration = 0,
                isCompleted = true,
                source = source
            )
            musicRepo.insertPlayback(history)
        }
    }

    // 更改音乐喜爱状态
    fun updateMusicLikedStatus(musicInfo: MusicInfo,liked: Boolean) {
        viewModelScope.launch {
            musicRepo.updateLikedStatus(musicInfo.music.id, liked)
            likedPlayListId.filterNotNull().collectLatest {
                if (liked) musicRepo.addToPlaylist(it, musicInfo.music.id, musicInfo.music.path)
                else musicRepo.removeItemFromPlaylist(musicInfo.music.id, it)
            }
            getLikedStatus(musicInfo.music.id)
        }
    }

    // 获取音乐喜爱状态
    fun getLikedStatus(musicId: Long) {
        viewModelScope.launch {
            likeStatus.value = musicRepo.getLikedStatus(musicId)
        }
    }

    // 获取音乐标签
    fun getMusicLabels(musicId: Long) {
        viewModelScope.launch {
            _currentMusicLabels.value=musicRepo.getMusicLabels(musicId)
        }
    }

    // 获取音乐歌词
    fun getMusicLyrics(musicId: Long) {
        viewModelScope.launch {
            _currentMusicLyrics.value=musicRepo.getMusicLyrics(musicId)
        }
    }

    // 初始化默认播放列表（首次安装时）
    fun initializeDefaultPlaylists() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentId = settingsRepo.currentPlaylistId.first()
            if (currentId == null) {
                val defaultId = musicRepo.createPlaylist(name = "默认播放列表")
                val likedId = musicRepo.createPlaylist(name = "红心")
                val recentId = musicRepo.createPlaylist(name = "最近播放")

                settingsRepo.saveCurrentPlaylistId(defaultId)
                settingsRepo.saveLikedPlaylistId(likedId)
                settingsRepo.saveRecentPlaylistId(recentId)
            }
        }
    }

    // 保存当前播放的音乐ID
    private fun persistCurrentMusic(id: Long) {
        viewModelScope.launch { settingsRepo.saveCurrentMusicId(id) }
    }

    // 保存播放模式
    private fun persistPlaybackMode(mode: PlaybackMode) {
        viewModelScope.launch { settingsRepo.savePlaybackMode(mode) }
    }

    // 保存默认播放列表
    private fun persistCurrentPlaylistToDatabase() {
        viewModelScope.launch {
            currentPlayListId.filterNotNull().collectLatest {
                val playlist = _currentPlaylist.value
                musicRepo.resetPlaylistItems(it, playlist)
            }
        }
    }

    // ViewModel销毁时解绑服务
    override fun onCleared() {
        super.onCleared()
        stopProgressTracking()
        context.unbindService(serviceConnection)
        viewModelScope.launch {
            persistCurrentPlaylistToDatabase()
        }
    }
}