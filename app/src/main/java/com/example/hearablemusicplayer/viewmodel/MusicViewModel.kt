package com.example.hearablemusicplayer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hearablemusicplayer.database.Music
import com.example.hearablemusicplayer.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MusicViewModel(
    private val repository: MusicRepository,
) : ViewModel() {
    val allMusic: LiveData<List<Music>> = repository.getAllMusic()
    // 从本地读取音乐到数据库的方法
    fun refreshMusicList() {
        viewModelScope.launch {
            val musicList = repository.loadMusicFromDevice()
            repository.saveMusicToDatabase(musicList)
        }
    }

    // 依据id获得音乐的方法
    fun getMusicById(musicId: String): Flow<Music?> {
        return repository.getMusicById(musicId)
    }

    private val _randomMusic = MutableStateFlow<List<Music>?>(null)
    val musicList: StateFlow<List<Music>?> = _randomMusic
    private val _randomMusicS = MutableStateFlow<List<Music>?>(null)
    val musicListS: StateFlow<List<Music>?> = _randomMusicS
    private val _randomMusicT = MutableStateFlow<List<Music>?>(null)
    val musicListT: StateFlow<List<Music>?> = _randomMusicT
    // 获得随机音乐的方法
    fun getRandomMusic() {
        viewModelScope.launch {
            _randomMusic.value = repository.getRandomMusic()
            _randomMusicS.value = repository.getRandomMusic()
            _randomMusicT.value = repository.getRandomMusic()
        }
    }

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying
    // 切换播放状态的方法
    fun togglePlaying() {
        viewModelScope.launch {
            _isPlaying.value = !_isPlaying.value
        }
    }
    // 更改播放状态为播放的方法
    fun changePlayingOn() {
        viewModelScope.launch {
            _isPlaying.value = true
        }
    }

    private val _searchResults = MutableStateFlow<List<Music>>(emptyList())
    val searchResults: StateFlow<List<Music>> = _searchResults
    // 搜索音乐的方法
    fun searchMusic(query: String) {
        viewModelScope.launch {
            _searchResults.value = repository.searchMusic(query)
        }
    }
}