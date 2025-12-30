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
import com.example.hearablemusicplayer.data.model.AiProviderType
import com.example.hearablemusicplayer.data.model.AiProviderConfig
import com.example.hearablemusicplayer.domain.usecase.music.*
import com.example.hearablemusicplayer.domain.usecase.playlist.GetLabelPlaylistUseCase
import com.example.hearablemusicplayer.domain.usecase.playlist.ManagePlaylistUseCase
import com.example.hearablemusicplayer.domain.usecase.settings.PlaylistSettingsUseCase
import com.example.hearablemusicplayer.domain.usecase.settings.UserSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
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
    // 待处理音乐数量
    val pendingMusicCount: StateFlow<Int> = getAllMusicUseCase
        .getMusicWithMissingExtraCount()
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
    private suspend fun initializeDefaultPlaylists() {
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
                    // 等待播放列表初始化完成
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
    
    /**
     * 获取每日推荐音乐，并根据刷新策略决定是否需要重新获取
     */
    fun getDailyMusicInfo() {
        viewModelScope.launch {
            // 先增加启动计数（在判断之前）
            userSettingsUseCase.incrementAppLaunchCount()
            
            // 检查是否需要刷新
            val shouldRefresh = userSettingsUseCase.shouldRefreshDailyRecommendation()
            val config = userSettingsUseCase.getDailyRefreshConfig()
            
            Log.d("MusicViewModel", """
                刷新检查:
                - 刷新模式: ${config.mode}
                - 上次刷新时间: ${config.lastRefreshTimestamp}
                - 当前时间: ${System.currentTimeMillis()}
                - 启动次数: ${config.launchCountSinceRefresh}
                - 需要刷新: $shouldRefresh
            """.trimIndent())
            
            if (shouldRefresh) {
                // 需要刷新，获取新的推荐
                val recommendation = getDailyRecommendationUseCase.getRandomMusicWithExtra()
                dailyMusic.value = recommendation.musicInfo
                _dailyMusicInfo.value = recommendation.dailyMusicInfo
                _dailyMusicLabel.value = recommendation.labels
                
                // 保存新推荐的音乐ID并更新刷新时间戳
                recommendation.musicInfo?.music?.id?.let { musicId ->
                    userSettingsUseCase.saveCurrentDailyMusicId(musicId)
                }
                userSettingsUseCase.updateLastDailyRefreshTimestamp()
                Log.d("MusicViewModel", "每日推荐已刷新，新音乐ID: ${recommendation.musicInfo?.music?.id}")
            } else {
                // 不需要刷新，尝试加载上次推荐的音乐
                if (dailyMusic.value == null) {
                    // 内存中没有，尝试从持久化ID加载
                    val savedMusicId = userSettingsUseCase.getCurrentDailyMusicId()
                    if (savedMusicId != null && savedMusicId > 0) {
                        // 从数据库加载指定ID的音乐
                        val musicInfo = getAllMusicUseCase.getMusicById(savedMusicId)
                        if (musicInfo != null) {
                            dailyMusic.value = musicInfo
                            // 加载该音乐的扩展信息和标签
                            val dailyInfo = getDailyRecommendationUseCase.getRandomMusicWithExtra()
                            _dailyMusicInfo.value = dailyInfo.dailyMusicInfo
                            _dailyMusicLabel.value = dailyInfo.labels
                            Log.d("MusicViewModel", "加载上次推荐的音乐，ID: $savedMusicId")
                        } else {
                            // 找不到该音乐，重新随机获取一首
                            val recommendation = getDailyRecommendationUseCase.getRandomMusicWithExtra()
                            dailyMusic.value = recommendation.musicInfo
                            _dailyMusicInfo.value = recommendation.dailyMusicInfo
                            _dailyMusicLabel.value = recommendation.labels
                            Log.d("MusicViewModel", "上次推荐的音乐不存在，重新获取")
                        }
                    } else {
                        // 没有保存的ID，获取新的推荐
                        val recommendation = getDailyRecommendationUseCase.getRandomMusicWithExtra()
                        dailyMusic.value = recommendation.musicInfo
                        _dailyMusicInfo.value = recommendation.dailyMusicInfo
                        _dailyMusicLabel.value = recommendation.labels
                        recommendation.musicInfo?.music?.id?.let { musicId ->
                            userSettingsUseCase.saveCurrentDailyMusicId(musicId)
                        }
                        Log.d("MusicViewModel", "首次加载每日推荐")
                    }
                } else {
                    Log.d("MusicViewModel", "使用内存中的每日推荐")
                }
            }
        }
    }
    
    /**
     * 手动刷新每日推荐
     */
    fun refreshDailyMusicInfo() {
        viewModelScope.launch {
            val recommendation = getDailyRecommendationUseCase.getRandomMusicWithExtra()
            dailyMusic.value = recommendation.musicInfo
            _dailyMusicInfo.value = recommendation.dailyMusicInfo
            _dailyMusicLabel.value = recommendation.labels
            
            // 更新刷新时间戳
            userSettingsUseCase.updateLastDailyRefreshTimestamp()
            Log.d("MusicViewModel", "手动刷新每日推荐")
        }
    }
    
    /**
     * 增加应用启动计数
     */
    fun incrementAppLaunchCount() {
        viewModelScope.launch {
            userSettingsUseCase.incrementAppLaunchCount()
        }
    }

    // 添加任务状态追踪
    private val _isProcessingExtraInfo = MutableStateFlow(false)
    val isProcessingExtraInfo: StateFlow<Boolean> = _isProcessingExtraInfo
    
    // ==================== 批量处理进度相关 ====================
    
    /**
     * 批量处理进度数据类
     */
    data class BatchProcessingProgress(
        val totalCount: Int = 0,
        val processedCount: Int = 0,
        val currentMusicTitle: String = "",
        val isProcessing: Boolean = false,
        val isPaused: Boolean = false
    ) {
        val progressPercent: Float
            get() = if (totalCount > 0) processedCount.toFloat() / totalCount else 0f
    }
    
    private val _processingProgress = MutableStateFlow(BatchProcessingProgress())
    val processingProgress: StateFlow<BatchProcessingProgress> = _processingProgress
    
    /**
     * 处理结果统计
     */
    private val _processingResult = MutableStateFlow<GetDailyMusicRecommendationUseCase.ProcessingResult?>(null)
    val processingResult: StateFlow<GetDailyMusicRecommendationUseCase.ProcessingResult?> = _processingResult
    
    /**
     * 暂停处理
     */
    fun pauseProcessing() {
        getDailyRecommendationUseCase.pauseProcessing()
        _processingProgress.value = _processingProgress.value.copy(isPaused = true)
    }
    
    /**
     * 继续处理
     */
    fun resumeProcessing() {
        getDailyRecommendationUseCase.resumeProcessing()
        _processingProgress.value = _processingProgress.value.copy(isPaused = false)
    }
    
    /**
     * 取消处理
     */
    fun cancelProcessing() {
        getDailyRecommendationUseCase.cancelProcessing()
        _processingProgress.value = BatchProcessingProgress()
        _isProcessingExtraInfo.value = false
    }
    
    /**
     * 清除处理结果
     */
    fun clearProcessingResult() {
        _processingResult.value = null
    }
    
    val recentListeningDurations: StateFlow<List<ListeningDuration>> = getDailyRecommendationUseCase
        .getRecentListeningDurations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // ==================== 多 AI 服务商配置 ====================
    
    /**
     * 当前选中的 AI 服务商
     */
    val currentAiProvider: StateFlow<AiProviderType> = userSettingsUseCase.currentAiProvider
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AiProviderType.DEEPSEEK)
    
    /**
     * 当前服务商配置状态
     */
    private val _currentProviderConfig = MutableStateFlow<AiProviderConfig?>(null)
    val currentProviderConfig: StateFlow<AiProviderConfig?> = _currentProviderConfig
    
    /**
     * API 测试结果
     */
    sealed class ApiTestResult {
        data class Success(val message: String) : ApiTestResult()
        data class Error(val message: String) : ApiTestResult()
    }
    
    private val _apiTestResult = MutableStateFlow<ApiTestResult?>(null)
    val apiTestResult: StateFlow<ApiTestResult?> = _apiTestResult
    
    private val _isTestingApi = MutableStateFlow(false)
    val isTestingApi: StateFlow<Boolean> = _isTestingApi
    
    /**
     * 加载当前服务商配置
     */
    fun loadCurrentProviderConfig() {
        viewModelScope.launch {
            _currentProviderConfig.value = userSettingsUseCase.getCurrentProviderConfig()
        }
    }
    
    /**
     * 加载指定服务商配置
     */
    fun loadProviderConfig(provider: AiProviderType) {
        viewModelScope.launch {
            _currentProviderConfig.value = userSettingsUseCase.getProviderConfig(provider)
        }
    }
    
    /**
     * 切换 AI 服务商
     */
    fun switchAiProvider(provider: AiProviderType) {
        viewModelScope.launch {
            userSettingsUseCase.setCurrentProvider(provider)
            loadProviderConfig(provider)
        }
    }
    
    /**
     * 保存服务商配置
     */
    fun saveAiProviderConfig(provider: AiProviderType, apiKey: String, model: String) {
        viewModelScope.launch {
            val config = AiProviderConfig(
                type = provider,
                apiKey = apiKey,
                model = model.ifBlank { provider.defaultModel },
                isConfigured = apiKey.isNotBlank()
            )
            userSettingsUseCase.saveProviderConfig(config)
            userSettingsUseCase.setCurrentProvider(provider)
            _currentProviderConfig.value = config
        }
    }
    
    /**
     * 测试服务商 API 连接
     */
    fun testAiProviderConnection(provider: AiProviderType, apiKey: String, model: String) {
        viewModelScope.launch {
            _isTestingApi.value = true
            _apiTestResult.value = null
            
            try {
                val config = AiProviderConfig(
                    type = provider,
                    apiKey = apiKey,
                    model = model.ifBlank { provider.defaultModel },
                    isConfigured = true
                )
                
                val isValid = getDailyRecommendationUseCase.validateProviderApiKey(config)
                _apiTestResult.value = if (isValid) {
                    ApiTestResult.Success("可以访问 ${provider.displayName}")
                } else {
                    ApiTestResult.Error("API Key 无效")
                }
            } catch (e: Exception) {
                _apiTestResult.value = ApiTestResult.Error("测试失败: ${e.message}")
            } finally {
                _isTestingApi.value = false
            }
        }
    }
    
    /**
     * 清除测试结果
     */
    fun clearApiTestResult() {
        _apiTestResult.value = null
    }
    
    // ==================== AI 自动批量处理设置 ====================
    
    /**
     * 自动批量处理开关状态
     */
    val autoBatchProcess: StateFlow<Boolean> = userSettingsUseCase.autoBatchProcess
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    /**
     * 保存自动批量处理开关
     */
    fun saveAutoBatchProcess(enabled: Boolean) {
        viewModelScope.launch {
            userSettingsUseCase.saveAutoBatchProcess(enabled)
        }
    }
    
    /**
     * 使用当前服务商开始自动处理音乐信息（带进度和结果统计）
     */
    fun startAutoProcessWithCurrentProvider() {
        Log.d("MusicViewModel", "开始批量处理... isProcessing=${_isProcessingExtraInfo.value}")
        
        // 如果已经在处理中，跳过
        if (_isProcessingExtraInfo.value) {
            Log.d("MusicViewModel", "已经在处理中，跳过")
            return
        }
        
        // 立即设置状态，防止重复点击
        _isProcessingExtraInfo.value = true
        
        viewModelScope.launch {
            _processingResult.value = null
            
            try {
                // 获取待处理总数
                val totalCount = pendingMusicCount.first()
                Log.d("MusicViewModel", "待处理数量: $totalCount")
                
                _processingProgress.value = BatchProcessingProgress(
                    totalCount = totalCount,
                    processedCount = 0,
                    isProcessing = true,
                    isPaused = false
                )
                
                getDailyRecommendationUseCase.autoProcessMissingExtraInfoWithCurrentProvider(
                    onProgress = { music ->
                        Log.d("MusicViewModel", "处理中: ${music.music.title}")
                        val current = _processingProgress.value
                        _processingProgress.value = current.copy(
                            processedCount = current.processedCount + 1,
                            currentMusicTitle = music.music.title
                        )
                    },
                    onComplete = { result ->
                        Log.d("MusicViewModel", "处理完成: $result")
                        _processingResult.value = result
                    },
                    delayMillis = 500
                )
            } catch (e: Exception) {
                Log.e("MusicViewModel", "处理扩展信息时发生错误: ${e.message}", e)
            } finally {
                val current = _processingProgress.value
                _processingProgress.value = current.copy(isProcessing = false, isPaused = false)
                _isProcessingExtraInfo.value = false
                Log.d("MusicViewModel", "处理结束, isProcessing=${_isProcessingExtraInfo.value}")
            }
        }
    }
    
    init {
        // 应用启动时重置处理状态，确保之前的暂停/取消状态不会影响新的处理
        getDailyRecommendationUseCase.resetProcessingState()
        _isProcessingExtraInfo.value = false
        _processingProgress.value = BatchProcessingProgress()
        
        // 加载当前服务商配置
        loadCurrentProviderConfig()
    }
    
    // ==================== 每日推荐刷新策略设置 ====================
    
    /**
     * 每日推荐刷新模式 Flow
     */
    val dailyRefreshMode: StateFlow<String> = userSettingsUseCase.dailyRefreshMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "time")
    
    /**
     * 按小时刷新间隔 Flow
     */
    val dailyRefreshHours: StateFlow<Int> = userSettingsUseCase.dailyRefreshHours
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 24)
    
    /**
     * 按启动次数刷新 Flow
     */
    val dailyRefreshStartupCount: StateFlow<Int> = userSettingsUseCase.dailyRefreshStartupCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 3)
    
    /**
     * 保存每日推荐刷新模式
     */
    fun saveDailyRefreshMode(mode: String) {
        viewModelScope.launch {
            userSettingsUseCase.saveDailyRefreshMode(mode)
        }
    }
    
    /**
     * 保存按小时刷新的间隔
     */
    fun saveDailyRefreshHours(hours: Int) {
        viewModelScope.launch {
            userSettingsUseCase.saveDailyRefreshHours(hours)
        }
    }
    
    /**
     * 保存按启动次数刷新
     */
    fun saveDailyRefreshStartupCount(count: Int) {
        viewModelScope.launch {
            userSettingsUseCase.saveDailyRefreshStartupCount(count)
        }
    }
}
