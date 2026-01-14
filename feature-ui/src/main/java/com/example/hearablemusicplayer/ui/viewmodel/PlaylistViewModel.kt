package com.example.hearablemusicplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hearablemusicplayer.domain.model.MusicInfo
import com.example.hearablemusicplayer.domain.model.enum.LabelCategory
import com.example.hearablemusicplayer.domain.model.enum.LabelName
import com.example.hearablemusicplayer.domain.usecase.music.GetAllMusicUseCase
import com.example.hearablemusicplayer.domain.usecase.music.MusicLabelUseCase
import com.example.hearablemusicplayer.domain.usecase.playlist.GetLabelPlaylistUseCase
import com.example.hearablemusicplayer.domain.usecase.playlist.ManagePlaylistUseCase
import com.example.hearablemusicplayer.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val managePlaylistUseCase: ManagePlaylistUseCase,
    private val getLabelPlaylistUseCase: GetLabelPlaylistUseCase,
    private val musicLabelUseCase: MusicLabelUseCase,
    private val settingsRepository: SettingsRepository,
    private val getAllMusicUseCase: GetAllMusicUseCase
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
    
    private val _selectedPlaylist = MutableStateFlow<List<MusicInfo>>(emptyList())
    val selectedPlaylist: StateFlow<List<MusicInfo>> = _selectedPlaylist
    
    // 歌手
    private val _selectedArtistName = MutableStateFlow("")
    val selectedArtistName: StateFlow<String> = _selectedArtistName
    private val _selectedArtistMusicList = MutableStateFlow<List<MusicInfo>>(emptyList())
    val selectedArtistMusicList: StateFlow<List<MusicInfo>> = _selectedArtistMusicList

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
                "默认列表" -> settingsRepository.getCurrentPlaylistId()
                "红心列表" -> settingsRepository.getLikedPlaylistId()
                "最近播放" -> settingsRepository.getRecentPlaylistId()
                else -> 0L
            }
            _selectedPlaylist.value = managePlaylistUseCase.getPlaylistById(id ?: 0L)
        }
    }
    
    // 依据歌手名获取音乐列表
    fun getSelectedArtistMusicList(artistName: String) {
        _selectedArtistName.value = artistName
        viewModelScope.launch {
            _selectedArtistMusicList.value = getAllMusicUseCase.getMusicListByArtist(artistName)
        }
    }
}
