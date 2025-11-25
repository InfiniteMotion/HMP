package com.example.hearablemusicplayer.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.hearablemusicplayer.player.service.MusicPlayService
import com.example.hearablemusicplayer.player.service.PlayControl
import com.example.hearablemusicplayer.data.database.MusicInfo
import com.example.hearablemusicplayer.data.database.MusicLabel
import com.example.hearablemusicplayer.data.database.PlaybackHistory
import com.example.hearablemusicplayer.data.database.myenum.PlaybackMode
import com.example.hearablemusicplayer.domain.usecase.playback.*
import com.example.hearablemusicplayer.domain.usecase.playlist.ManagePlaylistUseCase
import com.example.hearablemusicplayer.domain.usecase.settings.PlaylistSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
}

// 调色板颜色数据类
data class PaletteColors(
    val dominantColor: Color = Color(0xFF121212),
    val vibrantColor: Color = Color(0xFF1E1E1E)
)

@HiltViewModel
@UnstableApi
class PlayControlViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    // Use Cases - Domain Layer
    private val currentPlaybackUseCase: CurrentPlaybackUseCase,
    private val playbackModeUseCase: PlaybackModeUseCase,
    private val playbackHistoryUseCase: PlaybackHistoryUseCase,
    private val timerUseCase: TimerUseCase,
    private val managePlaylistUseCase: ManagePlaylistUseCase,
    private val playlistSettingsUseCase: PlaylistSettingsUseCase
) : ViewModel(), MusicPlayService.OnMusicCompleteListener {

    private var playControl: PlayControl? = null

    @OptIn(UnstableApi::class)
    fun bindPlayControl(service: PlayControl?) {
        this.playControl = service
        if (service is MusicPlayService) {
            service.setOnMusicCompleteListener(this)
        }
    }

    // 事件管理
    private val _toastEvent = MutableSharedFlow<UiEvent.ShowToast>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val toastEvent = _toastEvent.asSharedFlow()

    private fun showToast(message: String) {
        viewModelScope.launch {
            _toastEvent.emit(UiEvent.ShowToast(message))
        }
    }

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
    private val currentPlayListId = playlistSettingsUseCase.currentPlaylistId
    private val likedPlayListId = playlistSettingsUseCase.likedPlaylistId
    private val recentPlayListId = playlistSettingsUseCase.recentPlaylistId

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

    // 调色板缓存与状态
    private val paletteCache = mutableMapOf<String, PaletteColors>()
    private val _paletteColors = MutableStateFlow(PaletteColors())
    val paletteColors: StateFlow<PaletteColors> = _paletteColors.asStateFlow()

    // 播放模式（顺序、单曲循环、随机）
    private val _playbackMode = MutableStateFlow(PlaybackMode.SEQUENTIAL)
    val playbackMode: StateFlow<PlaybackMode> = _playbackMode.asStateFlow()

    // 当前播放进度与总时长
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    private var progressJob: Job? = null

    private var playStartTime: Long = 0L
    private var lastDurationRecordTime: Long = 0L
    private val durationRecordThreshold = 30000L // 30秒节流，避免频繁写入

    // 添加定时器相关状态
    private var timerJob: Job? = null
    val timerRemaining: StateFlow<Long?> = timerUseCase.timerRemaining

    init {
        // 初始化播放列表和当前播放歌曲索引
        viewModelScope.launch {
            val playlistId = currentPlayListId.first() ?: return@launch
            val currentMusicId = currentPlaybackUseCase.getCurrentMusicId().first()
            val list = managePlaylistUseCase.getMusicInfoInPlaylist(playlistId).first()
            _originalPlaylist = list
            _currentPlaylist.value = list
            _currentIndex.value = list.indexOfFirst { it.music.id == currentMusicId }.takeIf { it >= 0 } ?: 0
        }

        // 监听播放模式变更并更新播放列表
        viewModelScope.launch {
            playbackModeUseCase.playbackMode
                .filterNotNull()
                .collectLatest { mode ->
                    _playbackMode.value = mode
                    updateCurrentPlaylist()
                }
        }

        // 监听当前曲目变化并提取调色板
        viewModelScope.launch {
            currentPlayingMusic
                .filterNotNull()
                .collectLatest { musicInfo ->
                    extractPaletteColors(musicInfo.music.albumArtUri)
                }
        }
    }

    // 开始监听播放进度
    fun startProgressTracking() {
        if (progressJob?.isActive == true) return // 避免重复启动

        progressJob = viewModelScope.launch {
            while (isActive) {
                playControl?.let { svc ->
                    _currentPosition.value = svc.getCurrentPosition()
                    _duration.value = svc.getDuration()
                    
                    // 周期性记录播放时长（节流策略）
                    recordListeningDurationPeriodically()
                }
                delay(500)
            }
        }
    }

    // 周期性记录播放时长（节流策略：每30秒记录一次）
    private fun recordListeningDurationPeriodically() {
        val now = System.currentTimeMillis()
        if (_isPlaying.value && playStartTime > 0 && 
            (now - lastDurationRecordTime) >= durationRecordThreshold) {
            val duration = now - playStartTime
            viewModelScope.launch {
                playbackHistoryUseCase.recordListeningDuration(duration)
            }
            lastDurationRecordTime = now
            playStartTime = now // 重置起始时间以累计下一个周期
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
                managePlaylistUseCase.addToPlaylist(playlistId, musicInfo.music.id, musicInfo.music.path)
            }
        }
    }

    // 是否加载音乐
    private fun isMusicLoaded(path:String): Boolean? {
        return playControl?.isMusicLoaded(path)
    }

    // 是否就绪
    fun isReady():Boolean?{
        return playControl?.isReady()
    }

    // 播放或恢复当前歌曲
    fun playOrResume() {
        playStartTime = System.currentTimeMillis()
        lastDurationRecordTime = playStartTime // 重置节流计时器
        val path = currentMusicPath()
        if (path != null && isMusicLoaded(path) == true) {
            playControl?.proceedMusic()
        } else {
            viewModelScope.launch { playCurrentTrack("AutoPlay") }
        }
        _isPlaying.value = true
    }

    // 暂停播放
    fun pauseMusic() {
        if (playStartTime > 0) {
            val duration = System.currentTimeMillis() - playStartTime
            // 立即记录剩余时长，不等待节流器
            if (duration > 0) {
                viewModelScope.launch {
                    playbackHistoryUseCase.recordListeningDuration(duration)
                }
            }
        }
        playControl?.pause()
        _isPlaying.value = false
        playStartTime = 0L // 重置
        lastDurationRecordTime = 0L
    }

    // 跳转到指定进度
    fun seekTo(position: Long) {
        playControl?.seekTo(position)
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
        viewModelScope.launch {
            playbackModeUseCase.savePlaybackMode(newMode)
        }
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
    fun addAllToPlaylistInOrder(playlist:List<MusicInfo>) {
        viewModelScope.launch {
            _originalPlaylist = playlist
            togglePlaybackMode(PlaybackMode.SEQUENTIAL)
            _currentPlaylist.value = _originalPlaylist
            _currentIndex.value = 0
            playCurrentTrack("In Order")
        }
        persistCurrentPlaylistToDatabase()
    }

    // 播放全部歌曲（随机）
    fun addAllToPlaylistByShuffle(playlist:List<MusicInfo>) {
        viewModelScope.launch {
            _originalPlaylist = playlist
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

    // 播放完成回调播放下一首
    override fun onPlaybackEnded() {
        if (playStartTime > 0) {
            val duration = System.currentTimeMillis() - playStartTime
            if (duration > 0) {
                viewModelScope.launch {
                    playbackHistoryUseCase.recordListeningDuration(duration)
                }
            }
        }
        playStartTime = System.currentTimeMillis() // 为下一首重置
        lastDurationRecordTime = playStartTime
        playNext()
    }

    // 回调播放上一首
    override fun onPlaybackPrev() {
        playPrevious()
    }

    // 回调播放状态变化
    override fun onPlayStateChanged(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
    }

    // 将音乐添加到准备
    fun  prepareMusic(musicInfo: MusicInfo){
        playControl?.prepareMusic(musicInfo.music)
    }

    // 播放当前索引对应的歌曲
    private suspend fun playCurrentTrack(source: String) {
        val track = _currentPlaylist.value.getOrNull(_currentIndex.value) ?: return
        recentPlayListId.first()?.let {
            managePlaylistUseCase.addToPlaylist(it, track.music.id, track.music.path)
        }
        persistCurrentMusic(track.music.id)
        playControl?.playSingleMusic(track.music)
        _isPlaying.value = true
        recordPlayback(track.music.id, source)
    }

    // 将音乐加入播放队列(不确定是否在播放列表中)
    suspend fun addToPlaylist(musicInfo: MusicInfo) {
        if (_originalPlaylist.none { it.music.id == musicInfo.music.id }) {
            _originalPlaylist = _originalPlaylist + musicInfo
            updateCurrentPlaylist()
            viewModelScope.launch {
                saveToPlaylist(musicInfo)
            }
            showToast("已添加：${musicInfo.music.title}")
        }
        else {
            showToast("已存在：${musicInfo.music.title}")
        }
    }

    // 切换到播放列表中的音乐
    private fun switchToMusicInPlaylist(musicInfo: MusicInfo) {
        val index = _currentPlaylist.value.indexOfFirst { it.music.id == musicInfo.music.id }
        _currentIndex.value = if (index != -1) index else 0
    }

    // 从播放列表移除指定歌曲
    fun removeFromPlaylist(musicInfo: MusicInfo) {
        // 移除后更新原始和当前播放列表
        _originalPlaylist = _originalPlaylist.filter { it.music.id != musicInfo.music.id }
        updateCurrentPlaylist()
        // 如果当前播放的被移除，重置索引
        if (_currentPlaylist.value.isNotEmpty()) {
            val current = currentPlayingMusic.value
            val idx = _currentPlaylist.value.indexOfFirst { it.music.id == current?.music?.id }
            _currentIndex.value = if (idx >= 0) idx else 0
        } else {
            _currentIndex.value = 0
        }
        persistCurrentPlaylistToDatabase()
    }
    
    // 将指定歌曲移动到队列首位
    fun moveToTop(musicInfo: MusicInfo) {
        val currentList = _originalPlaylist.toMutableList()
        val index = currentList.indexOfFirst { it.music.id == musicInfo.music.id }
        if (index > 0) {
            // 移除原位置
            val item = currentList.removeAt(index)
            // 插入到首位
            currentList.add(0, item)
            _originalPlaylist = currentList
            updateCurrentPlaylist()
            // 更新当前索引
            val current = currentPlayingMusic.value
            if (current != null) {
                _currentIndex.value = _currentPlaylist.value.indexOfFirst { it.music.id == current.music.id }
            }
            persistCurrentPlaylistToDatabase()
            showToast("已置顶：${musicInfo.music.title}")
        }
    }

    // 从指定音乐开始播放(已经在播放列表中)
    suspend fun playAt(musicInfo: MusicInfo) {
        switchToMusicInPlaylist(musicInfo)
        playCurrentTrack("ManualPlay")
    }

    // 从指定音乐开始播放(不确定是否在播放列表中)
    suspend fun playWith(musicInfo: MusicInfo) {
        addToPlaylist(musicInfo)
        playAt(musicInfo)
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
            playbackHistoryUseCase.insertPlayback(history)
        }
    }

    // 更改音乐喜爱状态
    fun updateMusicLikedStatus(musicInfo: MusicInfo,liked: Boolean) {
        viewModelScope.launch {
            currentPlaybackUseCase.updateLikedStatus(musicInfo.music.id, liked)
            likedPlayListId.filterNotNull().collectLatest {
                if (liked) managePlaylistUseCase.addToPlaylist(it, musicInfo.music.id, musicInfo.music.path)
                else managePlaylistUseCase.removeItemFromPlaylist(musicInfo.music.id, it)
            }
            getLikedStatus(musicInfo.music.id)
        }
    }

    // 获取音乐喜爱状态
    fun getLikedStatus(musicId: Long) {
        viewModelScope.launch {
            likeStatus.value = currentPlaybackUseCase.getLikedStatus(musicId)
        }
    }

    fun playHeartMode() {
        viewModelScope.launch {
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

    // 设置定时器
    fun startTimer(minutes: Int) {
        timerUseCase.setTimerRemaining((minutes * 60 * 1000L))
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
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
    
    // 取消定时器
    fun cancelTimer() {
        timerJob?.cancel()
        timerUseCase.cancelTimer()
    }

    // 获取音乐标签
    fun getMusicLabels(musicId: Long) {
        viewModelScope.launch {
            _currentMusicLabels.value=currentPlaybackUseCase.getMusicLabels(musicId)
        }
    }

    // 获取音乐歌词
    fun getMusicLyrics(musicId: Long) {
        viewModelScope.launch {
            _currentMusicLyrics.value=currentPlaybackUseCase.getMusicLyrics(musicId)
        }
    }

    // 保存当前播放的音乐ID
    private fun persistCurrentMusic(id: Long) {
        viewModelScope.launch { currentPlaybackUseCase.saveCurrentMusicId(id) }
    }

    // 保存默认播放列表
    private fun persistCurrentPlaylistToDatabase() {
        viewModelScope.launch {
            currentPlayListId.filterNotNull().collectLatest {
                val playlist = _currentPlaylist.value
                managePlaylistUseCase.resetPlaylistItems(it, playlist)
            }
        }
    }

    // 提取调色板颜色（带缓存与线程分离）
    private fun extractPaletteColors(albumArtUri: String?) {
        if (albumArtUri == null) {
            _paletteColors.value = PaletteColors()
            return
        }

        // 检查缓存
        paletteCache[albumArtUri]?.let {
            _paletteColors.value = it
            return
        }

        viewModelScope.launch {
            try {
                val colors = withContext(Dispatchers.IO) {
                    val loader = ImageLoader(context)
                    val request = ImageRequest.Builder(context)
                        .data(albumArtUri)
                        .size(200, 200)
                        .allowHardware(false)
                        .build()

                    val result = (loader.execute(request) as? SuccessResult)?.drawable
                    val bitmap = (result as? android.graphics.drawable.BitmapDrawable)?.bitmap

                    bitmap?.let {
                        // 切换到 Default 线程进行 Palette 计算
                        withContext(Dispatchers.Default) {
                            val palette = Palette.from(it)
                                .maximumColorCount(12)
                                .generate()

                            val darkMuted = palette.darkMutedSwatch?.rgb
                            val vibrant = palette.vibrantSwatch?.rgb
                            val darkVibrant = palette.darkVibrantSwatch?.rgb
                            val dominant = palette.getDominantColor(0xFF121212.toInt())

                            PaletteColors(
                                dominantColor = Color(darkMuted ?: darkVibrant ?: dominant),
                                vibrantColor = Color(vibrant ?: darkVibrant ?: 0xFF1E1E1E.toInt())
                            )
                        }
                    } ?: PaletteColors()
                }

                // 更新缓存与状态
                paletteCache[albumArtUri] = colors
                _paletteColors.value = colors
            } catch (e: Exception) {
                // 提取失败使用回退色
                _paletteColors.value = PaletteColors()
            }
        }
    }

    // ViewModel销毁时解绑服务
    override fun onCleared() {
        super.onCleared()
        stopProgressTracking()
        viewModelScope.launch {
            persistCurrentPlaylistToDatabase()
        }
    }

}
