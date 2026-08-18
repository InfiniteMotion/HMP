package com.hearablemusic.player.ui.library.viewmodel

import android.app.Application
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hearablemusic.player.ui.R
import com.hmp.data.util.MusicTagEditor
import com.hmp.data.util.MusicTagParser
import com.hmp.domain.music.EditableMusicTags
import com.hmp.domain.music.usecase.EditMusicTagsUseCase
import com.hmp.domain.music.usecase.GetAllMusicUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

data class EditMusicTagsUiState(
    val isLoading: Boolean = true,
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val year: String = "",
    val genre: String = "",
    val track: String = "",
    val lyrics: String = "",
    val albumArtUri: String? = null,
    /** 新选择的封面字节；空数组表示移除封面，null 表示不修改 */
    val newAlbumArtBytes: ByteArray? = null,
    /** 是否有未保存的更改 */
    val hasChanges: Boolean = false,
    /** 音乐文件是否可直接写入；不可写时需通过 SAF 授权后保存 */
    val isFileWritable: Boolean = false,
    val error: String? = null
)

sealed interface SaveTagsResult {
    data object Success : SaveTagsResult
    data class Error(val message: String?) : SaveTagsResult
}

class EditMusicTagsViewModel(
    application: Application,
    private val getAllMusicUseCase: GetAllMusicUseCase,
    private val editMusicTagsUseCase: EditMusicTagsUseCase
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(EditMusicTagsUiState())
    val uiState: StateFlow<EditMusicTagsUiState> = _uiState.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveResult = MutableStateFlow<SaveTagsResult?>(null)
    val saveResult: StateFlow<SaveTagsResult?> = _saveResult.asStateFlow()

    private var currentMusicId: Long? = null
    private var currentMusicPath: String? = null
    private var originalTitle = ""
    private var originalArtist = ""
    private var originalAlbum = ""
    private var originalYear = ""
    private var originalGenre = ""
    private var originalTrack = ""
    private var originalLyrics = ""

    fun loadTags(musicId: Long) {
        currentMusicId = musicId
        _uiState.value = EditMusicTagsUiState()
        viewModelScope.launch {
            try {
                val musicInfo = getAllMusicUseCase.getMusicById(musicId)
                if (musicInfo == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = getApplication<Application>().getString(R.string.music_not_found)
                        )
                    }
                    return@launch
                }
                val music = musicInfo.music
                currentMusicPath = music.path
                val lyrics = musicInfo.extra?.lyrics.orEmpty()
                originalTitle = music.title
                originalArtist = music.artist
                originalAlbum = music.album
                originalLyrics = lyrics

                // 年份/流派/曲目号不在数据库，从文件标签解析（可能为空）
                var year = ""
                var genre = ""
                var track = ""
                var fileWritable = false
                withContext(Dispatchers.IO) {
                    fileWritable = File(music.path).canWrite()
                    MusicTagParser.parseMetadata(music.path)?.let { meta ->
                        year = meta.year.orEmpty()
                        genre = meta.genre.orEmpty()
                        track = meta.track.orEmpty()
                    }
                }
                originalYear = year
                originalGenre = genre
                originalTrack = track

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        title = music.title,
                        artist = music.artist,
                        album = music.album,
                        year = year,
                        genre = genre,
                        track = track,
                        lyrics = lyrics,
                        albumArtUri = music.albumArtUri,
                        hasChanges = false,
                        isFileWritable = fileWritable
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: getApplication<Application>()
                            .getString(R.string.unknown_error)
                    )
                }
            }
        }
    }

    fun onTitleChange(value: String) = updateField { it.copy(title = value) }
    fun onArtistChange(value: String) = updateField { it.copy(artist = value) }
    fun onAlbumChange(value: String) = updateField { it.copy(album = value) }
    fun onYearChange(value: String) {
        val filtered = value.filter { it.isDigit() }.take(MAX_YEAR_LENGTH)
        updateField { it.copy(year = filtered) }
    }
    fun onGenreChange(value: String) = updateField { it.copy(genre = value) }
    fun onTrackChange(value: String) {
        val filtered = value.filter { it.isDigit() }.take(MAX_TRACK_LENGTH)
        updateField { it.copy(track = filtered) }
    }
    fun onLyricsChange(value: String) = updateField { it.copy(lyrics = value) }

    private fun updateField(transform: (EditMusicTagsUiState) -> EditMusicTagsUiState) {
        _uiState.update { state ->
            val newState = transform(state)
            newState.copy(hasChanges = computeHasChanges(newState))
        }
    }

    fun onCoverSelected(uri: Uri) {
        viewModelScope.launch {
            val bytes = withContext(Dispatchers.IO) {
                readAndCompressCover(uri)
            }
            if (bytes != null) {
                _uiState.update { it.copy(newAlbumArtBytes = bytes) }
            }
        }
    }

    fun onRemoveCover() {
        updateField { it.copy(newAlbumArtBytes = ByteArray(0)) }
    }

    fun retry() {
        currentMusicId?.let { loadTags(it) }
    }

    /**
     * 解析当前音乐文件对应的 MediaStore URI，
     * 供 [android.provider.MediaStore.createWriteRequest] 请求修改该文件。
     */
    suspend fun resolveWriteUri(): Uri? = withContext(Dispatchers.IO) {
        val path = currentMusicPath ?: return@withContext null
        MusicTagEditor.queryMediaStoreUri(getApplication(), path)
    }

    fun save() {
        val musicId = currentMusicId ?: return
        if (_isSaving.value || _uiState.value.isLoading) return
        val tags = buildTags(_uiState.value) ?: return

        viewModelScope.launch {
            _isSaving.value = true
            editMusicTagsUseCase(musicId, tags)
                .onSuccess {
                    _saveResult.value = SaveTagsResult.Success
                }
                .onFailure { e ->
                    _saveResult.value = SaveTagsResult.Error(
                        e.message ?: getApplication<Application>().getString(R.string.unknown_error)
                    )
                }
            _isSaving.value = false
        }
    }

    /**
     * 通过 SAF 授权的内容 Uri 写入标签文件，随后仅刷新本地曲库记录。
     */
    fun saveWithUri(uri: Uri) {
        val musicId = currentMusicId ?: return
        if (_isSaving.value || _uiState.value.isLoading) return
        val tags = buildTags(_uiState.value) ?: return

        viewModelScope.launch {
            _isSaving.value = true
            MusicTagEditor.writeTags(uri, tags, scanPath = currentMusicPath)
                .fold(
                    onSuccess = {
                        editMusicTagsUseCase.refreshAfterFileWrite(musicId, tags)
                            .onSuccess {
                                _saveResult.value = SaveTagsResult.Success
                            }
                            .onFailure { e ->
                                _saveResult.value = SaveTagsResult.Error(
                                    e.message
                                        ?: getApplication<Application>()
                                            .getString(R.string.unknown_error)
                                )
                            }
                    },
                    onFailure = { e ->
                        _saveResult.value = SaveTagsResult.Error(
                            e.message
                                ?: getApplication<Application>().getString(R.string.unknown_error)
                        )
                    }
                )
            _isSaving.value = false
        }
    }

    fun clearSaveResult() {
        _saveResult.value = null
    }

    private fun buildTags(state: EditMusicTagsUiState): EditableMusicTags? {
        val tags = EditableMusicTags(
            title = state.title.trim().takeIf { it.isNotEmpty() && it != originalTitle },
            artist = state.artist.trim().takeIf { it.isNotEmpty() && it != originalArtist },
            album = state.album.trim().takeIf { it.isNotEmpty() && it != originalAlbum },
            year = state.year.trim().takeIf { it.isNotEmpty() && it != originalYear },
            genre = state.genre.trim().takeIf { it.isNotEmpty() && it != originalGenre },
            track = state.track.trim().takeIf { it.isNotEmpty() && it != originalTrack },
            lyrics = state.lyrics.takeIf { it.isNotEmpty() && it != originalLyrics },
            albumArt = state.newAlbumArtBytes
        )
        if (!tags.hasChanges) {
            _saveResult.value = SaveTagsResult.Error(
                getApplication<Application>().getString(R.string.no_changes)
            )
            return null
        }
        return tags
    }

    private fun computeHasChanges(state: EditMusicTagsUiState): Boolean {
        return (state.title.trim().isNotEmpty() && state.title.trim() != originalTitle) ||
            (state.artist.trim().isNotEmpty() && state.artist.trim() != originalArtist) ||
            (state.album.trim().isNotEmpty() && state.album.trim() != originalAlbum) ||
            (state.year.trim().isNotEmpty() && state.year.trim() != originalYear) ||
            (state.genre.trim().isNotEmpty() && state.genre.trim() != originalGenre) ||
            (state.track.trim().isNotEmpty() && state.track.trim() != originalTrack) ||
            (state.lyrics.isNotEmpty() && state.lyrics != originalLyrics) ||
            state.newAlbumArtBytes != null
    }

    private suspend fun readAndCompressCover(uri: Uri): ByteArray? {
        return try {
            val resolver = getApplication<Application>().contentResolver
            val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
            if (bytes.size <= MAX_COVER_BYTES) return bytes

            // 过大时压缩为 JPEG，避免写入超大封面
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            val sample = calculateInSampleSize(options, MAX_COVER_DIMENSION, MAX_COVER_DIMENSION)
            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sample }
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)
                ?: return bytes
            val output = ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, output)
            output.toByteArray()
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateInSampleSize(
        options: android.graphics.BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1
        var width = options.outWidth
        var height = options.outHeight
        while (width / (inSampleSize * 2) >= reqWidth || height / (inSampleSize * 2) >= reqHeight) {
            inSampleSize *= 2
        }
        return inSampleSize
    }

    private companion object {
        const val MAX_COVER_BYTES = 512 * 1024
        const val MAX_COVER_DIMENSION = 1024
        const val MAX_YEAR_LENGTH = 4
        const val MAX_TRACK_LENGTH = 3
    }
}
