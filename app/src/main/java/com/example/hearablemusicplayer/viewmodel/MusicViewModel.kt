package com.example.hearablemusicplayer.viewmodel

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hearablemusicplayer.R
import com.example.hearablemusicplayer.database.Music
import com.example.hearablemusicplayer.database.Playlist
import com.example.hearablemusicplayer.repository.MusicRepository
import com.example.hearablemusicplayer.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MusicViewModel(
    private val musicRepo: MusicRepository,
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    // 当前播放列表
    private val _avatarUri = MutableStateFlow<Int>(0)
    val avatarUri: StateFlow<Int> = _avatarUri
    fun getAvatarUri() {
        viewModelScope.launch {
            _avatarUri.value= settingsRepo.getAvatarUri()!!
        }
    }

    val allMusic: StateFlow<List<Music>> = musicRepo
        .getAllMusic()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 从本地读取音乐到数据库的方法
    fun refreshMusicList() {
        viewModelScope.launch {
            val musicList = musicRepo.loadMusicFromDevice()
            musicRepo.saveMusicToDatabase(musicList)
        }
    }

    // 依据id获得音乐的方法
    fun getMusicById(musicId: Long): Flow<Music?> {
        return musicRepo.getMusicById(musicId)
    }

    // 当前播放列表
    private val _currentPlaylist = MutableStateFlow<List<Music>>(emptyList())
    val currentPlaylist: StateFlow<List<Music>> = _currentPlaylist
    // 红心播放列表
    private val _likedPlaylist = MutableStateFlow<List<Music>>(emptyList())
    val likedPlaylist: StateFlow<List<Music>> = _likedPlaylist
    // 最近播放列表
    private val _recentPlaylist = MutableStateFlow<List<Music>>(emptyList())
    val recentPlaylist: StateFlow<List<Music>> = _recentPlaylist

    // 获取展示的音乐列表
    fun getMusicList(musicListId:Long): Flow<List<Music>> {
        return musicRepo.getMusicInPlaylist(musicListId)
    }


    private val _searchResults = MutableStateFlow<List<Music>>(emptyList())
    val searchResults: StateFlow<List<Music>> = _searchResults
    // 搜索音乐的方法
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
            _currentPlaylist.value = musicRepo.getMusicInPlaylist(currentId,7).first()
            _likedPlaylist.value = musicRepo.getMusicInPlaylist(likedId,7).first()
            _recentPlaylist.value = musicRepo.getMusicInPlaylist(recentId,7).first()
        }
    }
}