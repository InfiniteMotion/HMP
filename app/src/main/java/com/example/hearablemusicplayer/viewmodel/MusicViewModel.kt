package com.example.hearablemusicplayer.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hearablemusicplayer.ChatRequest
import com.example.hearablemusicplayer.DeepSeekService
import com.example.hearablemusicplayer.Message
import com.example.hearablemusicplayer.database.DailyMusicInfo
import com.example.hearablemusicplayer.database.MusicInfo
import com.example.hearablemusicplayer.database.MusicLabel
import com.example.hearablemusicplayer.database.myenum.LabelCategory
import com.example.hearablemusicplayer.database.myenum.LabelName
import com.example.hearablemusicplayer.repository.MusicRepository
import com.example.hearablemusicplayer.repository.SettingsRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MusicViewModel(
    private val musicRepo: MusicRepository,
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    // 音乐读取状态
    val isLoadMusic = settingsRepo.isLoadMusic

    // 头像
    private val _avatarUri = MutableStateFlow(0)
    val avatarUri: StateFlow<Int> = _avatarUri
    fun getAvatarUri() {
        viewModelScope.launch {
            _avatarUri.value= settingsRepo.getAvatarUri()!!
        }
    }

    // 所有音乐
    val allMusic: StateFlow<List<MusicInfo>> = musicRepo
        .getAllMusic()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 当前播放列表
    private val _currentPlaylist = MutableStateFlow<List<MusicInfo>>(emptyList())
    val currentPlaylist: StateFlow<List<MusicInfo>> = _currentPlaylist
    // 红心播放列表
    private val _likedPlaylist = MutableStateFlow<List<MusicInfo>>(emptyList())
    val likedPlaylist: StateFlow<List<MusicInfo>> = _likedPlaylist
    // 最近播放列表
    private val _recentPlaylist = MutableStateFlow<List<MusicInfo>>(emptyList())
    val recentPlaylist: StateFlow<List<MusicInfo>> = _recentPlaylist

    // 搜索音乐的方法
    private val _searchResults = MutableStateFlow<List<MusicInfo>>(emptyList())
    val searchResults: StateFlow<List<MusicInfo>> = _searchResults
    fun searchMusic(query: String) {
        viewModelScope.launch {
            _searchResults.value = musicRepo.searchMusic(query)
        }
    }

    // 初始化默认音乐列表
    fun initPlaylists() {
        viewModelScope.launch {
            val currentId = settingsRepo.getCurrentPlaylistId()?:1
            val likedId = settingsRepo.getLikedPlaylistId()?:2
            val recentId = settingsRepo.getRecentPlaylistId()?:3
            _currentPlaylist.value = musicRepo.getMusicInfoInPlaylist(currentId,7).first()
            _likedPlaylist.value = musicRepo.getMusicInfoInPlaylist(likedId,7).first()
            _recentPlaylist.value = musicRepo.getMusicInfoInPlaylist(recentId,7).first()
        }
    }

    // 从本地读取音乐到数据库的方法
    val isScanning = musicRepo.isScanning
    fun refreshMusicList() {
        viewModelScope.launch(Dispatchers.IO) {
           musicRepo.loadMusicFromDevice()
            settingsRepo.saveIsLoadMusic(true)
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

    // 获取音乐标签的方法
    fun getMusicLabel(musicId: Long): List<MusicLabel> {
        var labels = mutableListOf<MusicLabel>()
        viewModelScope.launch {
            labels= musicRepo.getMusicLabels(musicId).toMutableList()
        }
        return labels
    }

    // 每日推荐歌曲
    // 新获取的dailyMusicInfo存储起来
    private val dailyMusic = MutableStateFlow<MusicInfo?>(null)
    fun getDailyMusic() {
        viewModelScope.launch {
            dailyMusic.value = musicRepo.getRandomMusicInfo()
            parseQuestionAndAnswer(dailyMusic.value)
        }
    }
    private val dailyMusicInfo = MutableStateFlow<DailyMusicInfo?>(null)
    // 上次获取的dailyMusicInfo供ui使用
    private val _dailyMusicCache = MutableStateFlow<MusicInfo?>(null)
    val dailyMusicCache: StateFlow<MusicInfo?> = _dailyMusicCache
    fun getDailyMusicCache() {
        viewModelScope.launch {
            val musicId = settingsRepo.getDaliyMusicInfoId()
            _dailyMusicCache.value = musicId?.let { musicRepo.getMusicInfoById(it).first() }
        }
    }
    private val _dailyMusicInfoCache = MutableStateFlow<DailyMusicInfo?>(null)
    val dailyMusicInfoCache: StateFlow<DailyMusicInfo?> = _dailyMusicInfoCache
    fun getDailyMusicInfoCache() {
        viewModelScope.launch {
            _dailyMusicInfoCache.value = settingsRepo.getDaliyMusicInfo()
        }
    }
    private val _dailyMusicLabel = MutableStateFlow<List<MusicLabel?>>(emptyList())
    val dailyMusicLabel: StateFlow<List<MusicLabel?>> = _dailyMusicLabel
    fun getDailyMusicLabel() {
        viewModelScope.launch {
            val musicId = settingsRepo.getDaliyMusicInfoId()
            if (musicId != null) {
                _dailyMusicLabel.value=musicRepo.getMusicLabels(musicId)
            }
        }
    }
    // 从DeepSeek获取更多信息
    private fun parseQuestionAndAnswer(input: MusicInfo?) {
        viewModelScope.launch {
            if (input != null) {
                val message = Message(
                    "user",
                    """
                        请根据由 ${input.music.artist} 演唱的歌曲《${input.music.title}》，用中文以下面的JSON格式回复相关信息（不要添加任何其他内容）：
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
                          "language":"ENGLISH/CHINESE/JAPANESE/KOREAN/OTHERS", 
                          "era":"SIXTIES/SEVENTIES/EIGHTIES/NINETIES,TWO_THOUSANDS/TWENTY_TENS/TWENTY_TWENTIES",
                          "rewards": "歌曲成就",
                          "lyric": "热门歌词",
                          "singerIntroduce": "歌手介绍",
                          "backgroundIntroduce": "创作背景",
                          "description": "歌曲介绍",
                          "relevantMusic": "类似音乐",
                          "errorInfo": "None"
                        }
                        """.trimIndent()
                )
                val messages = listOf(message)
                val chatRequest = ChatRequest(messages = messages)
                val response = DeepSeekService.createChatCompletion(
                    authToken = "" , //DeepSeek Key
                    request = chatRequest
                )

                if (response.isSuccessful) {
                    val json = response.body()?.choices?.firstOrNull()?.message?.content
                    try {
                        val intro = Gson().fromJson(json, DailyMusicInfo::class.java)
                        dailyMusicInfo.value = intro
                        settingsRepo.saveDaliyMusicInfoId(input.music.id)
                        settingsRepo.saveDaliyMusicInfo(intro)
                        saveMusicLabel(input.music.id,intro)
                    } catch (e: Exception) {
                        dailyMusicInfo.value = DailyMusicInfo(
                            genre = emptyList(),
                            mood = emptyList(),
                            scenario = emptyList(),
                            language = "",
                            era = "",
                            rewards = "",
                            lyric = "",
                            singerIntroduce = "",
                            backgroundIntroduce = "",
                            description = "",
                            relevantMusic = "",
                            errorInfo = "Exception: ${e.message}"
                        )
                    }
                } else {
                    dailyMusicInfo.value = DailyMusicInfo(
                        genre = emptyList(),
                        mood = emptyList(),
                        scenario = emptyList(),
                        language = "",
                        era = "",
                        rewards = "",
                        lyric = "",
                        singerIntroduce = "",
                        backgroundIntroduce = "",
                        description = "",
                        relevantMusic = "",
                        errorInfo = "Error: ${response.errorBody()?.string()}"
                    )
                }
            }
        }
    }

    fun initializeDefaultDailyMusic(){
        viewModelScope.launch(Dispatchers.IO) {
            val currentDailyMusicId = settingsRepo.getDaliyMusicInfoId()
            if (currentDailyMusicId == null) {
                val defaultMusic = musicRepo.getRandomMusicInfo()
                if (defaultMusic != null) {
                    settingsRepo.saveDaliyMusicInfoId(defaultMusic.music.id)
                }
            }
        }
    }
}
