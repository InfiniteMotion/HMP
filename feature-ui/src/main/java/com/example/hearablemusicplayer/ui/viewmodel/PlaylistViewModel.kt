package com.example.hearablemusicplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hearablemusicplayer.domain.music.MusicInfo
import com.example.hearablemusicplayer.domain.enum.LabelCategory
import com.example.hearablemusicplayer.domain.enum.LabelName
import com.example.hearablemusicplayer.domain.music.usecase.GetAllMusicUseCase
import com.example.hearablemusicplayer.domain.music.usecase.MusicLabelUseCase
import com.example.hearablemusicplayer.domain.playlist.Playlist
import com.example.hearablemusicplayer.domain.playlist.usecase.ManagePlaylistUseCase
import com.example.hearablemusicplayer.domain.setting.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import androidx.lifecycle.SavedStateHandle

/** 播放列表详情页 UI 状态 */
data class PlaylistUiState(
    val playlistName: String = "",
    val playlist: List<MusicInfo> = emptyList(),
    val playlistMeta: Playlist? = null,
    val selectedPlaylistId: Long? = null,
    val isCustomPlaylist: Boolean = false
)

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val managePlaylistUseCase: ManagePlaylistUseCase,
    private val musicLabelUseCase: MusicLabelUseCase,
    private val settingsRepository: SettingsRepository,
    private val getAllMusicUseCase: GetAllMusicUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    // 标签分类列表名
    val genrePlaylistName = musicLabelUseCase.getLabelNamesByType(LabelCategory.GENRE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val moodPlaylistName = musicLabelUseCase.getLabelNamesByType(LabelCategory.MOOD)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val scenarioPlaylistName = musicLabelUseCase.getLabelNamesByType(LabelCategory.SCENARIO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val languagePlaylistName = musicLabelUseCase.getLabelNamesByType(LabelCategory.LANGUAGE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val eraPlaylistName = musicLabelUseCase.getLabelNamesByType(LabelCategory.ERA)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 当前选中的播放列表
    private val _selectedPlaylistName = MutableStateFlow("")
    val selectedPlaylistName: StateFlow<String> = _selectedPlaylistName

    private val _selectedPlaylistId = MutableStateFlow<Long?>(null)
    val selectedPlaylistId: StateFlow<Long?> = _selectedPlaylistId.asStateFlow()

    private val _selectedPlaylistMeta = MutableStateFlow<Playlist?>(null)
    val selectedPlaylistMeta: StateFlow<Playlist?> = _selectedPlaylistMeta.asStateFlow()

    private val _isCustomPlaylist = MutableStateFlow(false)
    val isCustomPlaylist: StateFlow<Boolean> = _isCustomPlaylist.asStateFlow()
    
    private val _selectedPlaylist = MutableStateFlow<List<MusicInfo>>(emptyList())
    val selectedPlaylist: StateFlow<List<MusicInfo>> = _selectedPlaylist

    /** 播放列表详情页统一 UI 状态（由各 StateFlow 合并） */
    val playlistUiState: StateFlow<PlaylistUiState> = combine(
        _selectedPlaylistName,
        _selectedPlaylist,
        _selectedPlaylistMeta,
        _selectedPlaylistId,
        _isCustomPlaylist
    ) { name, list, meta, id, isCustom ->
        PlaylistUiState(
            playlistName = name,
            playlist = list,
            playlistMeta = meta,
            selectedPlaylistId = id,
            isCustomPlaylist = isCustom
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        PlaylistUiState()
    )

    // 用户自定义播放列表（排除默认/红心/最近）
    private val _userCustomPlaylists = MutableStateFlow<List<Playlist>>(emptyList())
    val userCustomPlaylists: StateFlow<List<Playlist>> = _userCustomPlaylists.asStateFlow()
    
    // 歌手
    private val _selectedArtistName = MutableStateFlow("")
    val selectedArtistName: StateFlow<String> = _selectedArtistName
    private val _selectedArtistMusicList = MutableStateFlow<List<MusicInfo>>(emptyList())
    val selectedArtistMusicList: StateFlow<List<MusicInfo>> = _selectedArtistMusicList
    
    // 专辑
    private val _selectedAlbumName = MutableStateFlow("")
    val selectedAlbumName: StateFlow<String> = _selectedAlbumName
    private val _selectedAlbumMusicList = MutableStateFlow<List<MusicInfo>>(emptyList())
    val selectedAlbumMusicList: StateFlow<List<MusicInfo>> = _selectedAlbumMusicList

    // 初始化默认播放列表
    fun initializeDefaultPlaylists() {
        viewModelScope.launch {
            // 检查并初始化默认播放列表
            if (settingsRepository.getCurrentPlaylistId() == null) {
                managePlaylistUseCase.removePlaylist(name = "默认播放列表")
                val defaultId = managePlaylistUseCase.createPlaylist(name = "默认播放列表")
                settingsRepository.saveCurrentPlaylistId(defaultId)
            }
            
            // 检查并初始化红心列表
            if (settingsRepository.getLikedPlaylistId() == null) {
                managePlaylistUseCase.removePlaylist(name = "红心")
                val likedId = managePlaylistUseCase.createPlaylist(name = "红心")
                settingsRepository.saveLikedPlaylistId(likedId)
            }
            
            // 检查并初始化最近播放列表
            if (settingsRepository.getRecentPlaylistId() == null) {
                managePlaylistUseCase.removePlaylist(name = "最近播放")
                val recentId = managePlaylistUseCase.createPlaylist(name = "最近播放")
                settingsRepository.saveRecentPlaylistId(recentId)
            }
        }
    }

    init {
        initializeDefaultPlaylists()
        loadUserCustomPlaylists()
    }

    private fun loadRouteData() {
        try {
            // 检查是否为 CustomPlaylist 路由
            if (savedStateHandle.contains("playlistId")) {
                val playlistId = savedStateHandle.get<Long>("playlistId")
                if (playlistId != null) {
                    loadPlaylistById(playlistId)
                    return
                }
            }
            
            // 检查是否为 Playlist、Artist 或 Album 路由
            if (savedStateHandle.contains("name")) {
                val name = savedStateHandle.get<String>("name")
                if (name != null) {
                    // 尝试判断是 Playlist、Artist 还是 Album
                    val route = savedStateHandle.get<String>("nav3_route")
                    if (route?.contains("Artist") == true) {
                        getSelectedArtistMusicList(name)
                    } else if (route?.contains("Album") == true) {
                        getSelectedAlbumMusicList(name)
                    } else {
                        getSelectedPlaylist(name)
                    }
                    return
                }
            }
        } catch (e: Exception) {
            // 忽略错误，不加载特定播放列表
        }
    }

    /** 加载用户自定义播放列表列表（排除系统列表） */
    fun loadUserCustomPlaylists() {
        viewModelScope.launch {
            val all = managePlaylistUseCase.getAllPlaylists()
            val currentId = settingsRepository.getCurrentPlaylistId()
            val likedId = settingsRepository.getLikedPlaylistId()
            val recentId = settingsRepository.getRecentPlaylistId()
            val systemIds = setOfNotNull(currentId, likedId, recentId)
            _userCustomPlaylists.value = all.filter { it.id !in systemIds }
        }
    }

    private var currentPlaylistJob: kotlinx.coroutines.Job? = null

    /** 按 ID 加载播放列表（用于 CustomPlaylist 路由） */
    fun loadPlaylistById(playlistId: Long) {
        currentPlaylistJob?.cancel()
        _selectedPlaylistId.value = playlistId
        
        currentPlaylistJob = viewModelScope.launch {
            val meta = managePlaylistUseCase.getPlaylistMeta(playlistId)
            _selectedPlaylistMeta.value = meta
            _selectedPlaylistName.value = meta?.name ?: ""
            val currentId = settingsRepository.getCurrentPlaylistId()
            val likedId = settingsRepository.getLikedPlaylistId()
            val recentId = settingsRepository.getRecentPlaylistId()
            _isCustomPlaylist.value = playlistId != currentId && playlistId != likedId && playlistId != recentId
            
            _selectedPlaylist.value = emptyList()
            
            managePlaylistUseCase.getMusicInfoInPlaylist(playlistId)
                .catch { _selectedPlaylist.value = emptyList() }
                .collect { 
                    if (_selectedPlaylistId.value == playlistId) {
                        _selectedPlaylist.value = it
                    }
                }
        }
    }

    private fun refreshSelectedPlaylistMeta() {
        val id = _selectedPlaylistId.value ?: return
        viewModelScope.launch {
            val meta = managePlaylistUseCase.getPlaylistMeta(id)
            _selectedPlaylistMeta.value = meta
            meta?.name?.let { _selectedPlaylistName.value = it }
        }
    }

    /** 新建用户自定义播放列表，创建完成后回调 onCreated(新列表 ID) */
    fun createPlaylistAsync(name: String, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val id = managePlaylistUseCase.createPlaylist(name)
            loadUserCustomPlaylists()
            onCreated(id)
        }
    }

    fun renamePlaylist(id: Long, newName: String) {
        viewModelScope.launch {
            managePlaylistUseCase.renamePlaylist(id, newName)
            if (_selectedPlaylistId.value == id) {
                _selectedPlaylistName.value = newName
                refreshSelectedPlaylistMeta()
            }
            loadUserCustomPlaylists()
        }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch {
            managePlaylistUseCase.removePlaylistById(id)
            _selectedPlaylistId.value = null
            _selectedPlaylistMeta.value = null
            _selectedPlaylistName.value = ""
            _selectedPlaylist.value = emptyList()
            _isCustomPlaylist.value = false
            loadUserCustomPlaylists()
        }
    }

    fun removeItemFromPlaylist(musicId: Long, playlistId: Long) {
        viewModelScope.launch {
            managePlaylistUseCase.removeItemFromPlaylist(musicId, playlistId)
            if (playlistId == _selectedPlaylistId.value) refreshSelectedPlaylistMeta()
            loadUserCustomPlaylists()
        }
    }

    /** 向播放列表追加一首歌曲 */
    fun addItemToPlaylist(playlistId: Long, musicId: Long, musicPath: String) {
        viewModelScope.launch {
            managePlaylistUseCase.addToPlaylist(playlistId, musicId, musicPath)
            if (playlistId == _selectedPlaylistId.value) refreshSelectedPlaylistMeta()
            loadUserCustomPlaylists()
        }
    }

    /** 向播放列表批量追加歌曲，完成后统一刷新一次 */
    fun addItemsToPlaylist(
        playlistId: Long,
        items: List<Pair<Long, String>>,
        onComplete: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            items.forEach { (musicId, musicPath) ->
                managePlaylistUseCase.addToPlaylist(playlistId, musicId, musicPath)
            }
            if (playlistId == _selectedPlaylistId.value) {
                refreshSelectedPlaylistMeta()
            }
            loadUserCustomPlaylists()
            onComplete?.invoke()
        }
    }

    /** 添加歌曲选择器用：全部音乐列表 */
    private val _allMusicForAddPicker = MutableStateFlow<List<MusicInfo>>(emptyList())
    val allMusicForAddPicker: StateFlow<List<MusicInfo>> = _allMusicForAddPicker.asStateFlow()

    /** 加载全部音乐供「添加歌曲」选择器使用 */
    fun loadAllMusicForAddPicker(onLoaded: ((List<MusicInfo>) -> Unit)? = null) {
        viewModelScope.launch {
            val allMusic = getAllMusicUseCase("title", "ASC")
            _allMusicForAddPicker.value = allMusic
            onLoaded?.invoke(allMusic)
        }
    }

    fun reorderPlaylistItems(playlistId: Long, orderedMusicIds: List<Long>) {
        viewModelScope.launch {
            managePlaylistUseCase.reorderPlaylistItems(playlistId, orderedMusicIds)
            if (playlistId == _selectedPlaylistId.value) refreshSelectedPlaylistMeta()
            loadUserCustomPlaylists()
        }
    }

    fun recordPlaylistPlay(playlistId: Long) {
        viewModelScope.launch {
            managePlaylistUseCase.incrementPlaylistPlayCount(playlistId)
            managePlaylistUseCase.setPlaylistLastPlayedAt(playlistId, System.currentTimeMillis())
            if (playlistId == _selectedPlaylistId.value) refreshSelectedPlaylistMeta()
        }
    }

    fun updatePlaylistCover(id: Long, coverUri: String?) {
        viewModelScope.launch {
            managePlaylistUseCase.updatePlaylistCover(id, coverUri)
            if (id == _selectedPlaylistId.value) refreshSelectedPlaylistMeta()
            loadUserCustomPlaylists()
        }
    }

    fun updatePlaylistDescription(id: Long, description: String?) {
        viewModelScope.launch {
            managePlaylistUseCase.updatePlaylistDescription(id, description)
            if (id == _selectedPlaylistId.value) refreshSelectedPlaylistMeta()
            loadUserCustomPlaylists()
        }
    }

    fun setPlaylistPinned(id: Long, isPinned: Boolean) {
        viewModelScope.launch {
            managePlaylistUseCase.setPlaylistPinned(id, isPinned)
            if (id == _selectedPlaylistId.value) refreshSelectedPlaylistMeta()
            loadUserCustomPlaylists()
        }
    }

    // 依据标签获取音乐列表
    fun getSelectedPlaylist(label: LabelName) {
        currentPlaylistJob?.cancel()
        _selectedPlaylistId.value = null
        _selectedPlaylistMeta.value = null
        _selectedPlaylistName.value = label.name
        viewModelScope.launch {
            _selectedPlaylist.value = musicLabelUseCase.getMusicListByLabel(label)
        }
    }

    // 依据标签获取音乐列表（或默认/红心/最近）
    fun getSelectedPlaylist(label: String) {
        currentPlaylistJob?.cancel()
        _selectedPlaylistName.value = label
        viewModelScope.launch {
            val id = when (label) {
                "默认列表" -> settingsRepository.getCurrentPlaylistId()
                "红心列表" -> settingsRepository.getLikedPlaylistId()
                "最近播放" -> settingsRepository.getRecentPlaylistId()
                else -> null
            }
            if (id != null) {
                _selectedPlaylistId.value = id
                val currentId = settingsRepository.getCurrentPlaylistId()
                val likedId = settingsRepository.getLikedPlaylistId()
                val recentId = settingsRepository.getRecentPlaylistId()
                _isCustomPlaylist.value = id != currentId && id != likedId && id != recentId
                _selectedPlaylistMeta.value = managePlaylistUseCase.getPlaylistMeta(id)
                
                _selectedPlaylist.value = emptyList()
                
                managePlaylistUseCase.getMusicInfoInPlaylist(id)
                    .catch { _selectedPlaylist.value = emptyList() }
                    .collect { 
                        if (_selectedPlaylistId.value == id) {
                            _selectedPlaylist.value = it
                        }
                    }
                return@launch
            }
            _selectedPlaylistId.value = null
            _selectedPlaylistMeta.value = null
            _isCustomPlaylist.value = false
            val labelEnum = LabelName.match(label)
            if (labelEnum != null) {
                _selectedPlaylist.value = musicLabelUseCase.getMusicListByLabel(labelEnum)
            } else {
                _selectedPlaylist.value = emptyList()
            }
        }
    }
    
    // 依据歌手名获取音乐列表
    fun getSelectedArtistMusicList(artistName: String) {
        currentPlaylistJob?.cancel()
        _selectedArtistName.value = artistName
        viewModelScope.launch {
            _selectedArtistMusicList.value = getAllMusicUseCase.getMusicListByArtist(artistName)
        }
    }
    
    // 依据专辑名获取音乐列表
    fun getSelectedAlbumMusicList(albumName: String) {
        currentPlaylistJob?.cancel()
        _selectedAlbumName.value = albumName
        viewModelScope.launch {
            _selectedAlbumMusicList.value = getAllMusicUseCase.getMusicListByAlbum(albumName)
        }
    }
}
