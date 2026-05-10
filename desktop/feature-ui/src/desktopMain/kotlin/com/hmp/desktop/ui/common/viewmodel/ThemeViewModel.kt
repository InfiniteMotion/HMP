package com.hmp.desktop.ui.common.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hmp.domain.music.MusicInfo
import com.hmp.desktop.player.DesktopMusicController
import com.hmp.desktop.ui.common.util.PaletteExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PaletteColors(
    val dominantColor: Color = Color(0xFF121212),
    val primaryColor: Color = Color(0xFF1E1E1E),
    val vibrantColor: Color = Color(0xFF2A2A2A),
    val darkVibrantColor: Color = Color(0xFF0F0F0F),
    val lightVibrantColor: Color = Color(0xFF808080),
    val mutedColor: Color = Color(0xFF222222),
    val darkMutedColor: Color = Color(0xFF111111),
    val lightMutedColor: Color = Color(0xFF666666),
    val accentColor: Color = Color(0xFF444444)
)

class ThemeViewModel(
    private val musicController:DesktopMusicController
) : ViewModel() {

    private val paletteCache = mutableMapOf<String, PaletteColors>()
    private val _paletteColors = MutableStateFlow(PaletteColors())
    val paletteColors: StateFlow<PaletteColors> = _paletteColors.asStateFlow()

    val currentPlayingMusic: StateFlow<MusicInfo?> = musicController.currentPlayingMusic

    init {
        viewModelScope.launch {
            currentPlayingMusic
                .filterNotNull()
                .collectLatest { musicInfo ->
                    extractPaletteColors(musicInfo.music.albumArtUri)
                }
        }
    }

    private suspend fun extractPaletteColors(albumArtUri: String?) {
        if (albumArtUri == null) {
            _paletteColors.value = PaletteColors()
            return
        }

        paletteCache[albumArtUri]?.let {
            _paletteColors.value = it
            return
        }

        val colors = withContext(Dispatchers.IO) {
            PaletteExtractor.extract(albumArtUri)
        }
        paletteCache[albumArtUri] = colors
        _paletteColors.value = colors
    }
}
