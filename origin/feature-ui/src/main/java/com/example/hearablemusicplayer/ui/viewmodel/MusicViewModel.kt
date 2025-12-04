package com.example.hearablemusicplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hearablemusicplayer.data.database.DailyMusicInfo
import com.example.hearablemusicplayer.data.database.ListeningDuration
import com.example.hearablemusicplayer.data.database.MusicInfo
import com.example.hearablemusicplayer.data.database.MusicLabel
import com.example.hearablemusicplayer.data.database.myenum.LabelCategory
import com.example.hearablemusicplayer.data.database.myenum.LabelName
import com.example.hearablemusicplayer.data.repository.Result
import com.example.hearablemusicplayer.domain.usecase.music.*
import com.example.hearablemusicplayer.domain.usecase.playlist.GetLabelPlaylistUseCase
import com.example.hearablemusicplayer.domain.usecase.playlist.ManagePlaylistUseCase
import com.example.hearablemusicplayer.domain.usecase.settings.PlaylistSettingsUseCase
import com.example.hearablemusicplayer.domain.usecase.settings.UserSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MusicViewModel @Inject constructor(
    // Use Cases - Domain Layer
    private val getAllMusicUseCase: GetAllMusicUseCase,
    private val loadMusicFromDeviceUseCase: LoadMusicFromDeviceUseCase,
    private val searchMusicUseCase: SearchMusicUseCase,
    private val getDailyRecommendationUseCase: GetDailyMusicRecommendationUseCase,
    private val musicLabelUseCase: MusicLabelUseCase,
    private val managePlaylistUseCase: ManagePlaylistUseCase,
    private val getLabelPlaylistUseCase: GetLabelPlaylistUseCase,
    private val userSettingsUseCase: UserSettingsUseCase,
    private val playlistSettingsUseCase: PlaylistSettingsUseCase

) : ViewModel() {

    // 用户设置相关
    val isFirstLaunch = userSettingsUseCase.isFirstLaunch
    fun saveIsFirstLaunchStatus(status:Boolean){
        viewModelScope.launch {
            userSettingsUseCase.saveIsFirstLaunch(status)
        }
    }

    // 音乐读取状态
    val isLoadMusic = userSettingsUseCase.isLoadMusic

    val musicWithoutExtraCount = getAllMusicUseCase.getMusicWithExtraCount()

    // 主题明暗模式
    val customMode = userSettingsUseCase.customMode
    fun saveCustomMode(mode: String) {
        viewModelScope.launch {
            userSettingsUseCase.saveThemeMode(mode)
        }
    }

    // 用户名
    val userName = userSettingsUseCase.userName
    fun saveUserName(name: String) {
        viewModelScope.launch {
            userSettingsUseCase.saveUserName(name)
        }
    }

    // 头像
    private val _avatarUri = MutableStateFlow("")
    val avatarUri: StateFlow<String> = _avatarUri
    fun getAvatarUri() {
        viewModelScope.launch {
            _avatarUri.value= userSettingsUseCase.getAvatarUri()?:""
        }
    }
    fun saveAvatarUri(uri: String) {
        viewModelScope.launch {
            userSettingsUseCase.saveAvatarUri(uri)
        }
    }

    // 所有音乐
    private val _orderBy = MutableStateFlow("title")
    val orderBy: StateFlow<String> = _orderBy
    fun updateOrderBy(orderBy: String) {
        _orderBy.value = orderBy
    }
    private val _orderType = MutableStateFlow("ASC")
    val orderType: StateFlow<String> = _orderType
    fun updateOrderType(orderType: String) {
        _orderType.value = orderType
    }
    private val _allMusic = MutableStateFlow<List<MusicInfo>>(emptyList())
    val allMusic: StateFlow<List<MusicInfo>> = _allMusic
    fun getAllMusic() {
        viewModelScope.launch {
            _allMusic.value = getAllMusicUseCase(_orderBy.value,_orderType.value)
        }
    }
    
    // 数据库中的音乐数量
    val musicCount:StateFlow<Int> = getAllMusicUseCase
        .getMusicCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    // 数据库中已获得额外信息的音乐数量
    val musicWithExtraCount:StateFlow<Int> = getAllMusicUseCase
        .getMusicWithExtraCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // 基于风格的播放列表名 List<LabelName>
    val genrePlaylistName:StateFlow<List<LabelName>> = musicLabelUseCase
        .getLabelNamesByType(LabelCategory.GENRE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    // 基于情绪的播放列表名 List<LabelName>
    val moodPlaylistName:StateFlow<List<LabelName>> = musicLabelUseCase
        .getLabelNamesByType(LabelCategory.MOOD)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    // 基于场景的播放列表名 List<LabelName>
    val scenarioPlaylistName:StateFlow<List<LabelName>> = musicLabelUseCase
        .getLabelNamesByType(LabelCategory.SCENARIO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    // 基于语言的播放列表名 List<LabelName>
    val languagePlaylistName:StateFlow<List<LabelName>> = musicLabelUseCase
        .getLabelNamesByType(LabelCategory.LANGUAGE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    // 基于年代的播放列表名 List<LabelName>
    val eraPlaylistName:StateFlow<List<LabelName>> = musicLabelUseCase
        .getLabelNamesByType(LabelCategory.ERA)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 当前播放列表
    private val _selectedPlaylistName = MutableStateFlow("")
    val selectedPlaylistName: StateFlow<String> = _selectedPlaylistName
    private val _selectedPlaylist = MutableStateFlow<List<MusicInfo>>(emptyList())
    val selectedPlaylist: StateFlow<List<MusicInfo>> = _selectedPlaylist
    
    // 当前歌手
    private val _selectedArtistName = MutableStateFlow("")
    val selectedArtistName: StateFlow<String> = _selectedArtistName
    private val _selectedArtistMusicList = MutableStateFlow<List<MusicInfo>>(emptyList())
    val selectedArtistMusicList: StateFlow<List<MusicInfo>> = _selectedArtistMusicList

    // 初始化默认播放列表
    private fun initializeDefaultPlaylists() {
        viewModelScope.launch {
            managePlaylistUseCase.removePlaylist(name = "默认播放列表")
            managePlaylistUseCase.removePlaylist(name = "红心")
            managePlaylistUseCase.removePlaylist(name = "最近播放")
            val defaultId = managePlaylistUseCase.createPlaylist(name = "默认播放列表")
            val likedId = managePlaylistUseCase.createPlaylist(name = "红心")
            val recentId = managePlaylistUseCase.createPlaylist(name = "最近播放")

            playlistSettingsUseCase.saveCurrentPlaylistId(defaultId)
            playlistSettingsUseCase.saveLikedPlaylistId(likedId)
            playlistSettingsUseCase.saveRecentPlaylistId(recentId)

        }
    }

    // 依据标签获取音乐列表
    fun getSelectedPlaylist(label: LabelName) {
        _selectedPlaylistName.value = label.name
        viewModelScope.launch {
            _selectedPlaylist.value = musicLabelUseCase.getMusicListByLabel(label)
        }
    }
    // 依据标签获取音乐列表
    fun getSelectedPlaylist(label: String) {
        _selectedPlaylistName.value = label
        viewModelScope.launch {
            val id = when(label) {
                "默认列表" -> playlistSettingsUseCase.getCurrentPlaylistId()
                "红心列表" -> playlistSettingsUseCase.getLikedPlaylistId()
                "最近播放" -> playlistSettingsUseCase.getRecentPlaylistId()
                else -> 0
            }
            _selectedPlaylist.value = managePlaylistUseCase.getPlaylistById(id?:0)
        }
    }
    
    // 依据歌手名获取音乐列表
    fun getSelectedArtistMusicList(artistName: String) {
        _selectedArtistName.value = artistName
        viewModelScope.launch {
            _selectedArtistMusicList.value = getAllMusicUseCase.getMusicListByArtist(artistName)
        }
    }

    // 搜索音乐的方法
    private val _searchResults = MutableStateFlow<List<MusicInfo>>(emptyList())
    val searchResults: StateFlow<List<MusicInfo>> = _searchResults
    fun searchMusic(query: String) {
        viewModelScope.launch {
            _searchResults.value = searchMusicUseCase(query)
        }
    }

    // 从本地读取音乐到数据库的方法
    val isScanning = loadMusicFromDeviceUseCase.isScanning()
    
    // 错误消息状态
    private val _scanErrorMessage = MutableStateFlow<String?>(null)
    val scanErrorMessage: StateFlow<String?> = _scanErrorMessage
    
    fun refreshMusicList() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = loadMusicFromDeviceUseCase()) {
                is Result.Success -> {
                    _scanErrorMessage.value = null
                    initializeDefaultPlaylists()
                    userSettingsUseCase.saveIsLoadMusic(true)
                }
                is Result.Error -> {
                    _scanErrorMessage.value = result.exception.message ?: "扫描失败"
                }
                is Result.Loading -> {
                    // 加载中
                }
            }
        }
    }

    // 每日推荐歌曲
    val dailyMusic = MutableStateFlow<MusicInfo?>(null)
    private val _dailyMusicInfo = MutableStateFlow<DailyMusicInfo?>(null)
    val dailyMusicInfo: StateFlow<DailyMusicInfo?> = _dailyMusicInfo
    private val _dailyMusicLabel = MutableStateFlow<List<MusicLabel?>>(emptyList())
    val dailyMusicLabel: StateFlow<List<MusicLabel?>> = _dailyMusicLabel
    fun getDailyMusicInfo() {
        viewModelScope.launch {
            val recommendation = getDailyRecommendationUseCase.getRandomMusicWithExtra()
            dailyMusic.value = recommendation.musicInfo
            _dailyMusicInfo.value = recommendation.dailyMusicInfo
            _dailyMusicLabel.value = recommendation.labels
        }
    }

    // 添加任务状态追踪
    private val _isProcessingExtraInfo = MutableStateFlow(false)
    val isProcessingExtraInfo: StateFlow<Boolean> = _isProcessingExtraInfo
    
    // 添加自动获取扩展信息的任务
    fun startAutoProcessExtraInfo() {
        viewModelScope.launch {
            if (_isProcessingExtraInfo.value) return@launch
            _isProcessingExtraInfo.value = true
            try {
                getDailyRecommendationUseCase.autoProcessMissingExtraInfo(
                    onProgress = { music ->
                        // 可以更新UI显示当前处理的音乐
                    },
                    delayMillis = 500
                )
            } catch (e: Exception) {
                println("处理扩展信息时发生错误: ${e.message}")
            } finally {
                _isProcessingExtraInfo.value = false
            }
        }
    }
    
    // 修改 DeepSeek API-Key
    fun saveDeepSeekApiKey(deepSeekApiKey: String) {
        viewModelScope.launch {
            userSettingsUseCase.saveDeepSeekApiKey(deepSeekApiKey)
        }
    }

    // 检查 DeepSeek API-Key 是否有效
    suspend fun checkApiAccess(deepSeekApiKey: String): Boolean {
        return getDailyRecommendationUseCase.validateApiKey(deepSeekApiKey)
    }

    val recentListeningDurations: StateFlow<List<ListeningDuration>> = getDailyRecommendationUseCase
        .getRecentListeningDurations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
