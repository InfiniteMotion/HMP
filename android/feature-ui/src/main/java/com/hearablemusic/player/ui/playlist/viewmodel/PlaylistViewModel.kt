package com.hearablemusic.player.ui.playlist.viewmodel

import android.app.Application

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hmp.domain.music.MusicInfo
import com.hmp.domain.enum.LabelCategory
import com.hmp.domain.enum.LabelName
import com.hmp.domain.music.usecase.GetAllMusicUseCase
import com.hmp.domain.music.usecase.MusicLabelUseCase
import com.hmp.domain.playlist.Playlist
import com.hmp.domain.playlist.usecase.ManagePlaylistUseCase
import com.hmp.domain.setting.SettingsRepository
import com.hearablemusic.player.ui.common.util.UiState
import com.hearablemusic.player.ui.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import kotlinx.coroutines.Job

data class PlaylistScreenUiState(
    val playlistName: String = "",
    val playlist: UiState<List<MusicInfo>> = UiState.Idle,
    val playlistMeta: Playlist? = null,
    val selectedPlaylistId: Long? = null,
    val isCustomPlaylist: Boolean = false
)

class PlaylistViewModel(
    private val application: Application,
    private val managePlaylistUseCase: ManagePlaylistUseCase,
    private val musicLabelUseCase: MusicLabelUseCase,
    private val settingsRepository: SettingsRepository,
    private val getAllMusicUseCase: GetAllMusicUseCase
) : AndroidViewModel(application) {

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

    private val _selectedPlaylistName = MutableStateFlow("")
    val selectedPlaylistName: StateFlow<String> = _selectedPlaylistName

    private val _selectedPlaylistId = MutableStateFlow<Long?>(null)
    val selectedPlaylistId: StateFlow<Long?> = _selectedPlaylistId.asStateFlow()

    private val _selectedPlaylistMeta = MutableStateFlow<Playlist?>(null)
    val selectedPlaylistMeta: StateFlow<Playlist?> = _selectedPlaylistMeta.asStateFlow()

    private val _isCustomPlaylist = MutableStateFlow(false)
    val isCustomPlaylist: StateFlow<Boolean> = _isCustomPlaylist.asStateFlow()

    private val _selectedPlaylistState = MutableStateFlow<UiState<List<MusicInfo>>>(UiState.Idle)
    val selectedPlaylistState: StateFlow<UiState<List<MusicInfo>>> = _selectedPlaylistState

    val playlistUiState: StateFlow<PlaylistScreenUiState> = combine(
        _selectedPlaylistName,
        _selectedPlaylistState,
        _selectedPlaylistMeta,
        _selectedPlaylistId,
        _isCustomPlaylist
    ) { name, playlistState, meta, id, isCustom ->
        PlaylistScreenUiState(
            playlistName = name,
            playlist = playlistState,
            playlistMeta = meta,
            selectedPlaylistId = id,
            isCustomPlaylist = isCustom
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        PlaylistScreenUiState()
    )

    private val _userCustomPlaylistsState = MutableStateFlow<UiState<List<Playlist>>>(UiState.Idle)
    val userCustomPlaylistsState: StateFlow<UiState<List<Playlist>>> = _userCustomPlaylistsState

    private val _selectedArtistName = MutableStateFlow("")
    val selectedArtistName: StateFlow<String> = _selectedArtistName
    private val _selectedArtistMusicListState = MutableStateFlow<UiState<List<MusicInfo>>>(UiState.Idle)
    val selectedArtistMusicListState: StateFlow<UiState<List<MusicInfo>>> = _selectedArtistMusicListState

    private val _selectedAlbumName = MutableStateFlow("")
    val selectedAlbumName: StateFlow<String> = _selectedAlbumName
    private val _selectedAlbumMusicListState = MutableStateFlow<UiState<List<MusicInfo>>>(UiState.Idle)
    val selectedAlbumMusicListState: StateFlow<UiState<List<MusicInfo>>> = _selectedAlbumMusicListState

    private val _allMusicForAddPickerState = MutableStateFlow<UiState<List<MusicInfo>>>(UiState.Idle)
    val allMusicForAddPickerState: StateFlow<UiState<List<MusicInfo>>> = _allMusicForAddPickerState

    fun initializeDefaultPlaylists() {
        viewModelScope.launch {
            if (settingsRepository.getCurrentPlaylistId() == null) {
                managePlaylistUseCase.removePlaylist(name = getApplication<Application>().getString(R.string.default_playlist))
                val defaultId = managePlaylistUseCase.createPlaylist(name = getApplication<Application>().getString(R.string.default_playlist))
                settingsRepository.saveCurrentPlaylistId(defaultId)
            }

            if (settingsRepository.getLikedPlaylistId() == null) {
                managePlaylistUseCase.removePlaylist(name = getApplication<Application>().getString(R.string.heart))
                val likedId = managePlaylistUseCase.createPlaylist(name = getApplication<Application>().getString(R.string.heart))
                settingsRepository.saveLikedPlaylistId(likedId)
            }

            if (settingsRepository.getRecentPlaylistId() == null) {
                managePlaylistUseCase.removePlaylist(name = getApplication<Application>().getString(R.string.recently_played))
                val recentId = managePlaylistUseCase.createPlaylist(name = getApplication<Application>().getString(R.string.recently_played))
                settingsRepository.saveRecentPlaylistId(recentId)
            }
        }
    }

    init {
        initializeDefaultPlaylists()
        loadUserCustomPlaylists()
    }

    fun loadUserCustomPlaylists() {
        viewModelScope.launch {
            managePlaylistUseCase.getAllPlaylistsFlow()
                .catch {
                    _userCustomPlaylistsState.value = UiState.Error(it.message ?: "Failed to load playlists")
                }
                .collect { all ->
                    val playlists = all.filter { it.songCount > 0 }
                    _userCustomPlaylistsState.value = if (playlists.isEmpty()) {
                        UiState.Empty
                    } else {
                        UiState.Success(playlists)
                    }
                }
        }
    }

    private var currentPlaylistJob: Job? = null

    fun loadPlaylistById(playlistId: Long) {
        currentPlaylistJob?.cancel()
        _selectedPlaylistId.value = playlistId
        _selectedPlaylistState.value = UiState.Loading

        currentPlaylistJob = viewModelScope.launch {
            val meta = managePlaylistUseCase.getPlaylistMeta(playlistId)
            _selectedPlaylistMeta.value = meta
            _selectedPlaylistName.value = meta?.name ?: ""
            val currentId = settingsRepository.getCurrentPlaylistId()
            val likedId = settingsRepository.getLikedPlaylistId()
            val recentId = settingsRepository.getRecentPlaylistId()
            _isCustomPlaylist.value = playlistId != currentId && playlistId != likedId && playlistId != recentId

            managePlaylistUseCase.getMusicInfoInPlaylist(playlistId)
                .catch {
                    _selectedPlaylistState.value = UiState.Error(it.message ?: "Failed to load playlist")
                }
                .collect {
                    if (_selectedPlaylistId.value == playlistId) {
                        _selectedPlaylistState.value = if (it.isEmpty()) {
                            UiState.Empty
                        } else {
                            UiState.Success(it)
                        }
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
            _selectedPlaylistState.value = UiState.Idle
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

    fun addItemToPlaylist(playlistId: Long, musicId: Long, musicPath: String) {
        viewModelScope.launch {
            managePlaylistUseCase.addToPlaylist(playlistId, musicId, musicPath)
            if (playlistId == _selectedPlaylistId.value) refreshSelectedPlaylistMeta()
            loadUserCustomPlaylists()
        }
    }

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

    fun loadAllMusicForAddPicker(onLoaded: ((List<MusicInfo>) -> Unit)? = null) {
        viewModelScope.launch {
            _allMusicForAddPickerState.value = UiState.Loading
            try {
                val allMusic = getAllMusicUseCase("title", "ASC")
                _allMusicForAddPickerState.value = if (allMusic.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Success(allMusic)
                }
                onLoaded?.invoke(allMusic)
            } catch (e: Exception) {
                _allMusicForAddPickerState.value = UiState.Error(e.message ?: "Failed to load music")
            }
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

    fun getSelectedPlaylist(label: LabelName) {
        currentPlaylistJob?.cancel()
        _selectedPlaylistId.value = null
        _selectedPlaylistMeta.value = null
        _selectedPlaylistName.value = label.name
        _selectedPlaylistState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val musicList = musicLabelUseCase.getMusicListByLabel(label)
                _selectedPlaylistState.value = if (musicList.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Success(musicList)
                }
            } catch (e: Exception) {
                _selectedPlaylistState.value = UiState.Error(e.message ?: "Failed to load playlist")
            }
        }
    }

    fun getSelectedPlaylist(label: String) {
        currentPlaylistJob?.cancel()
        _selectedPlaylistName.value = label
        _selectedPlaylistState.value = UiState.Loading
        viewModelScope.launch {
            val id = when (label) {
                getApplication<Application>().getString(R.string.default_list) -> settingsRepository.getCurrentPlaylistId()
                getApplication<Application>().getString(R.string.heart_list) -> settingsRepository.getLikedPlaylistId()
                getApplication<Application>().getString(R.string.recently_played) -> settingsRepository.getRecentPlaylistId()
                else -> null
            }
            if (id != null) {
                _selectedPlaylistId.value = id
                val currentId = settingsRepository.getCurrentPlaylistId()
                val likedId = settingsRepository.getLikedPlaylistId()
                val recentId = settingsRepository.getRecentPlaylistId()
                _isCustomPlaylist.value = id != currentId && id != likedId && id != recentId
                _selectedPlaylistMeta.value = managePlaylistUseCase.getPlaylistMeta(id)

                managePlaylistUseCase.getMusicInfoInPlaylist(id)
                    .catch {
                        _selectedPlaylistState.value = UiState.Error(it.message ?: "Failed to load playlist")
                    }
                    .collect {
                        if (_selectedPlaylistId.value == id) {
                            _selectedPlaylistState.value = if (it.isEmpty()) {
                                UiState.Empty
                            } else {
                                UiState.Success(it)
                            }
                        }
                    }
                return@launch
            }
            _selectedPlaylistId.value = null
            _selectedPlaylistMeta.value = null
            _isCustomPlaylist.value = false
            val labelEnum = LabelName.match(label)
            if (labelEnum != null) {
                try {
                    val musicList = musicLabelUseCase.getMusicListByLabel(labelEnum)
                    _selectedPlaylistState.value = if (musicList.isEmpty()) {
                        UiState.Empty
                    } else {
                        UiState.Success(musicList)
                    }
                } catch (e: Exception) {
                    _selectedPlaylistState.value = UiState.Error(e.message ?: "Failed to load playlist")
                }
            } else {
                _selectedPlaylistState.value = UiState.Empty
            }
        }
    }

    fun getSelectedArtistMusicList(artistName: String) {
        currentPlaylistJob?.cancel()
        _selectedArtistName.value = artistName
        _selectedArtistMusicListState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val musicList = getAllMusicUseCase.getMusicListByArtist(artistName)
                _selectedArtistMusicListState.value = if (musicList.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Success(musicList)
                }
            } catch (e: Exception) {
                _selectedArtistMusicListState.value = UiState.Error(e.message ?: "Failed to load artist music")
            }
        }
    }

    fun getSelectedAlbumMusicList(albumName: String) {
        currentPlaylistJob?.cancel()
        _selectedAlbumName.value = albumName
        _selectedAlbumMusicListState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val musicList = getAllMusicUseCase.getMusicListByAlbum(albumName)
                _selectedAlbumMusicListState.value = if (musicList.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Success(musicList)
                }
            } catch (e: Exception) {
                _selectedAlbumMusicListState.value = UiState.Error(e.message ?: "Failed to load album music")
            }
        }
    }
}