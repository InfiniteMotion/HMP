package com.example.hearablemusicplayer.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hearablemusicplayer.ChatRequest
import com.example.hearablemusicplayer.DeepSeekAPIWrapper
import com.example.hearablemusicplayer.DeepSeekError
import com.example.hearablemusicplayer.DeepSeekResult
import com.example.hearablemusicplayer.Message
import com.example.hearablemusicplayer.database.DailyMusicInfo
import com.example.hearablemusicplayer.database.ListeningDuration
import com.example.hearablemusicplayer.database.MusicInfo
import com.example.hearablemusicplayer.database.MusicLabel
import com.example.hearablemusicplayer.database.myenum.LabelCategory
import com.example.hearablemusicplayer.database.myenum.LabelName
import com.example.hearablemusicplayer.repository.MusicRepository
import com.example.hearablemusicplayer.repository.Result
import com.example.hearablemusicplayer.repository.SettingsRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MusicViewModel @Inject constructor(
    private val musicRepo: MusicRepository,
    private val settingsRepo: SettingsRepository,
    private val deepSeekAPIWrapper: DeepSeekAPIWrapper
) : ViewModel() {

    val isFirstLaunch = settingsRepo.isFirstLaunch
    fun saveIsFirstLaunchStatus(status:Boolean){
        viewModelScope.launch {
            settingsRepo.saveIsFirstLaunch(status)
        }
    }

    // 音乐读取状态
    val isLoadMusic = settingsRepo.isLoadMusic

    val musicWithoutExtraCount = musicRepo.musicWithoutExtraCount

    //用户名
    val userName = settingsRepo.userName
    fun saveUserName(name: String) {
        viewModelScope.launch {
            settingsRepo.saveUserName(name)
        }
    }

    // 头像
    private val _avatarUri = MutableStateFlow("")
    val avatarUri: StateFlow<String> = _avatarUri
    fun getAvatarUri() {
        viewModelScope.launch {
            _avatarUri.value= settingsRepo.getAvatarUri()?:""
        }
    }
    fun saveAvatarUri(uri: String) {
        viewModelScope.launch {
            settingsRepo.saveAvatarUri(uri)
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
            _allMusic.value = musicRepo.getAllMusicInfoAsList(_orderBy.value,_orderType.value)
        }
    }
    
    // 数据库中的音乐数量
    val musicCount:StateFlow<Int> = musicRepo
        .getMusicCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    // 数据库中已获得额外信息的音乐数量
    val musicWithExtraCount:StateFlow<Int> = musicRepo
        .getMusicWithExtraCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // 基于风格的播放列表名 List<LabelName>
    val genrePlaylistName:StateFlow<List<LabelName>> = musicRepo
        .getLabelNamesByType(LabelCategory.GENRE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    // 基于情绪的播放列表名 List<LabelName>
    val moodPlaylistName:StateFlow<List<LabelName>> = musicRepo
        .getLabelNamesByType(LabelCategory.MOOD)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    // 基于场景的播放列表名 List<LabelName>
    val scenarioPlaylistName:StateFlow<List<LabelName>> = musicRepo
        .getLabelNamesByType(LabelCategory.SCENARIO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    // 基于语言的播放列表名 List<LabelName>
    val languagePlaylistName:StateFlow<List<LabelName>> = musicRepo
        .getLabelNamesByType(LabelCategory.LANGUAGE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    // 基于年代的播放列表名 List<LabelName>
    val eraPlaylistName:StateFlow<List<LabelName>> = musicRepo
        .getLabelNamesByType(LabelCategory.ERA)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 当前播放列表
    private val _selectedPlaylistName = MutableStateFlow("")
    val selectedPlaylistName: StateFlow<String> = _selectedPlaylistName
    private val _selectedPlaylist = MutableStateFlow<List<MusicInfo>>(emptyList())
    val selectedPlaylist: StateFlow<List<MusicInfo>> = _selectedPlaylist

    // 初始化默认播放列表
    private fun initializeDefaultPlaylists() {
        viewModelScope.launch {
            musicRepo.removePlaylist(name = "默认播放列表")
            musicRepo.removePlaylist(name = "红心")
            musicRepo.removePlaylist(name = "最近播放")
            val defaultId = musicRepo.createPlaylist(name = "默认播放列表")
            val likedId = musicRepo.createPlaylist(name = "红心")
            val recentId = musicRepo.createPlaylist(name = "最近播放")

            settingsRepo.saveCurrentPlaylistId(defaultId)
            settingsRepo.saveLikedPlaylistId(likedId)
            settingsRepo.saveRecentPlaylistId(recentId)

        }
    }

    // 依据标签获取音乐列表
    fun getSelectedPlaylist(label: LabelName) {
        _selectedPlaylistName.value = label.name
        viewModelScope.launch {
            val idList = musicRepo.getMusicIdListByType(label)
            _selectedPlaylist.value = musicRepo.getPlaylistByIdList(idList)
        }
    }
    // 依据标签获取音乐列表
    fun getSelectedPlaylist(label: String) {
        _selectedPlaylistName.value = label
        viewModelScope.launch {
            val id = when(label) {
                "默认列表" -> settingsRepo.getCurrentPlaylistId()
                "红心列表" -> settingsRepo.getLikedPlaylistId()
                "最近播放" -> settingsRepo.getRecentPlaylistId()
                else -> 0
            }
            _selectedPlaylist.value = musicRepo.getPlaylistById(id?:0)
        }
    }

    // 搜索音乐的方法
    private val _searchResults = MutableStateFlow<List<MusicInfo>>(emptyList())
    val searchResults: StateFlow<List<MusicInfo>> = _searchResults
    fun searchMusic(query: String) {
        viewModelScope.launch {
            _searchResults.value = musicRepo.searchMusic(query)
        }
    }

    // 从本地读取音乐到数据库的方法
    val isScanning = musicRepo.isScanning
    
    // 错误消息状态
    private val _scanErrorMessage = MutableStateFlow<String?>(null)
    val scanErrorMessage: StateFlow<String?> = _scanErrorMessage
    
    fun refreshMusicList() {
        viewModelScope.launch(Dispatchers.IO) {
            musicRepo.clearAllDataBase()
            when (val result = musicRepo.loadMusicFromDevice()) {
                is Result.Success -> {
                    _scanErrorMessage.value = null
                    initializeDefaultPlaylists()
                    settingsRepo.saveIsLoadMusic(true)
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

    // 保存音乐标签到数据库的方法
    private fun saveMusicLabel(musicId:Long,dailyMusicInfo: DailyMusicInfo) {
        viewModelScope.launch {
            dailyMusicInfo.genre.forEach {
                musicRepo.addMusicLabel(MusicLabel(musicId,LabelCategory.GENRE,LabelName.match(it)?:LabelName.UNKNOWN))
            }
            dailyMusicInfo.mood.forEach {
                musicRepo.addMusicLabel(MusicLabel(musicId,LabelCategory.MOOD,LabelName.match(it)?:LabelName.UNKNOWN))
            }
            dailyMusicInfo.scenario.forEach {
                musicRepo.addMusicLabel(MusicLabel(musicId,LabelCategory.SCENARIO,LabelName.match(it)?:LabelName.UNKNOWN))
            }
            musicRepo.addMusicLabel(MusicLabel(musicId,LabelCategory.LANGUAGE,LabelName.match(dailyMusicInfo.language)?:LabelName.UNKNOWN))
            musicRepo.addMusicLabel(MusicLabel(musicId,LabelCategory.ERA,LabelName.match(dailyMusicInfo.era)?:LabelName.UNKNOWN))
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
            dailyMusic.value = musicRepo.getRandomMusicInfoWithExtra()
            _dailyMusicInfo.value = musicRepo.getMusicExtraById(dailyMusic.value?.music?.id?:0)
            _dailyMusicLabel.value = musicRepo.getMusicLabels(dailyMusic.value?.music?.id?:0)
        }
    }

    // 添加任务状态追踪
    private val _isProcessingExtraInfo = MutableStateFlow(false)
    val isProcessingExtraInfo: StateFlow<Boolean> = _isProcessingExtraInfo
    
    // 添加自动获取扩展信息的任务
    private val musicWithoutExtraInfo = MutableStateFlow<MusicInfo?>(null)
    fun startAutoProcessExtraInfo() {
        viewModelScope.launch {
        if (_isProcessingExtraInfo.value) return@launch
        _isProcessingExtraInfo.value = true
        try {
            while (true) {
                val music = musicRepo.getRandomMusicInfoWithMissingExtra()
                if (music == null) break
                // 挂起直到本首处理完
                getMusicExtraInfoFromLLM(music)
                delay(500) // 避免API限制
            }
        } catch (e: Exception) {
            println("处理扩展信息时发生错误: ${e.message}")
        } finally {
            _isProcessingExtraInfo.value = false
        }
    }
}
    
    // 从DeepSeek获取更多信息
    private suspend fun getMusicExtraInfoFromLLM(input: MusicInfo) {
        withContext(Dispatchers.IO) {
            val message = Message(
                "user",
                """
                        请根据由 ${input.music.artist} 演唱的歌曲《${input.music.title}》，用中文以下面的JSON格式依据提示回复相关信息（不要添加任何其他内容）：
                        {
                          "genre": [
                            "ROCK", "POP", "JAZZ", "CLASSICAL", "HIPHOP", "ELECTRONIC", "FOLK", "RNB", "METAL", "COUNTRY", "BLUES", "REGGAE", "PUNK", "FUNK", "SOUL", "INDIE"
                          ],
                          "mood": [
                            "HAPPY", "SAD", "ENERGETIC", "CALM", "ROMANTIC", "ANGRY", "LONELY", "UPLIFTING", "MYSTERIOUS", "DARK", "MELANCHOLY", "HOPEFUL"
                          ],
                          "scenario": [
                            "WORKOUT", "SLEEP", "PARTY", "DRIVING", "STUDY", "RELAX", "DINNER", "MEDITATION", "FOCUS", "TRAVEL", "MORNING", "NIGHT"
                          ],
                          "language":"ENGLISH/CHINESE/JAPANESE/KOREAN/OTHERS(单选)", 
                          "era":"SIXTIES/SEVENTIES/EIGHTIES/NINETIES/TWO_THOUSANDS/TWENTY_TENS/TWENTY_TWENTIES(单选)",
                          "rewards": "歌曲成就(若无返回-暂无)",
                          "lyric": "热门歌词(两句，若无返回-暂无)",
                          "singerIntroduce": "歌手介绍(100字左右)",
                          "backgroundIntroduce": "创作背景(出处、创作者采访等信息，100字左右)",
                          "description": "歌曲主题(主题，100字左右)",
                          "relevantMusic": "类似音乐(一到两首，若无返回-暂无)",
                          "errorInfo": "None"
                        }
                        """.trimIndent()
            )
            val messages = listOf(message)
            val chatRequest = ChatRequest(messages = messages)
            
            // 使用新的 APIWrapper
            val result = deepSeekAPIWrapper.createChatCompletion(
                authToken = settingsRepo.getDeepSeekApiKey(),
                request = chatRequest,
                useCache = true
            )
            
            when (result) {
                is DeepSeekResult.Success -> {
                    val json = result.data.choices.firstOrNull()?.message?.content
                    try {
                        val intro = Gson().fromJson(json, DailyMusicInfo::class.java)
                        musicRepo.insertMusicExtra(input.music.id, intro)
                        saveMusicLabel(input.music.id, intro)
                        Log.d("MusicViewModel", "Successfully processed music extra info")
                    } catch (e: Exception) {
                        Log.e("MusicViewModel", "Gson parsing failed", e)
                    }
                }
                is DeepSeekResult.CachedFallback -> {
                    val json = result.data.choices.firstOrNull()?.message?.content
                    try {
                        val intro = Gson().fromJson(json, DailyMusicInfo::class.java)
                        musicRepo.insertMusicExtra(input.music.id, intro)
                        saveMusicLabel(input.music.id, intro)
                        Log.d("MusicViewModel", "Using cached fallback for music extra info")
                    } catch (e: Exception) {
                        Log.e("MusicViewModel", "Gson parsing failed on cached data", e)
                    }
                }
                is DeepSeekResult.Error -> {
                    val errorMsg = when (result.error) {
                        is DeepSeekError.NetworkError -> "网络错误: ${(result.error as DeepSeekError.NetworkError).message}"
                        is DeepSeekError.RateLimitError -> "速率限制，请稍后重试"
                        is DeepSeekError.AuthError -> "认证失败，请检查API密钥"
                        is DeepSeekError.ServerError -> "服务器错误: ${(result.error as DeepSeekError.ServerError).code}"
                        is DeepSeekError.UnknownError -> "未知错误: ${(result.error as DeepSeekError.UnknownError).message}"
                    }
                    Log.e("MusicViewModel", "API call failed: $errorMsg")
                }
            }
        }
    }
    // 修改 DeepSeek API-Key
    fun saveDeepSeekApiKey(deepSeekApiKey: String) {
        viewModelScope.launch {
            settingsRepo.saveDeepSeekApiKey(deepSeekApiKey)
        }
    }

    // 检查 DeepSeek API-Key 是否有效
    suspend fun checkApiAccess(deepSeekApiKey: String): Boolean {
        val message = Message("User", """{"content": "Hello"}""".trimIndent())
        val messages = listOf(message)
        val chatRequest = ChatRequest(messages = messages)
        
        val result = deepSeekAPIWrapper.createChatCompletion(
            authToken = deepSeekApiKey,
            request = chatRequest,
            useCache = false
        )
        
        return when (result) {
            is DeepSeekResult.Success -> {
                Log.d("MusicViewModel", "API key validation successful")
                true
            }
            is DeepSeekResult.CachedFallback -> {
                Log.d("MusicViewModel", "API key validation using cache")
                true
            }
            is DeepSeekResult.Error -> {
                Log.e("MusicViewModel", "API key validation failed: ${result.error}")
                false
            }
        }
    }

    val recentListeningDurations: StateFlow<List<ListeningDuration>> = musicRepo
        .getRecentListeningDurations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}