package com.example.hearablemusicplayer.ui.common.viewmodel

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.hearablemusicplayer.domain.music.MusicInfo
import com.example.hearablemusicplayer.player.controller.MusicController
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

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

@HiltViewModel
@UnstableApi
class ThemeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val musicController: MusicController
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

    private fun extractPaletteColors(albumArtUri: String?) {
        if (albumArtUri == null) {
            _paletteColors.value = PaletteColors()
            return
        }

        paletteCache[albumArtUri]?.let {
            _paletteColors.value = it
            return
        }

        viewModelScope.launch {
            try {
                val colors = withContext(Dispatchers.IO) {
                    val loader = ImageLoader(context)
                    val request = ImageRequest.Builder(context)
                        .data(albumArtUri)
                        .size(150, 150)
                        .allowHardware(false)
                        .build()

                    val result = (loader.execute(request) as? SuccessResult)?.drawable
                    val bitmap = (result as? BitmapDrawable)?.bitmap

                    bitmap?.let {
                        withContext(Dispatchers.Default) {
                            val palette = Palette.from(it)
                                .maximumColorCount(16)
                                .generate()

                            val dominant = palette.getDominantColor(0xFF121212.toInt())
                            val vibrant = palette.vibrantSwatch?.rgb
                            val darkVibrant = palette.darkVibrantSwatch?.rgb
                            val lightVibrant = palette.lightVibrantSwatch?.rgb
                            val muted = palette.mutedSwatch?.rgb
                            val darkMuted = palette.darkMutedSwatch?.rgb
                            val lightMuted = palette.lightMutedSwatch?.rgb

                            PaletteColors(
                                dominantColor = Color(dominant),
                                primaryColor = Color(vibrant ?: dominant),
                                vibrantColor = Color(vibrant ?: dominant),
                                darkVibrantColor = Color(darkVibrant ?: vibrant ?: dominant),
                                lightVibrantColor = Color(lightVibrant ?: vibrant ?: dominant),
                                mutedColor = Color(muted ?: dominant),
                                darkMutedColor = Color(darkMuted ?: muted ?: dominant),
                                lightMutedColor = Color(lightMuted ?: muted ?: dominant),
                                accentColor = Color(lightVibrant ?: vibrant ?: dominant)
                            )
                        }
                    } ?: PaletteColors()
                }

                if (paletteCache.size >= 50) {
                    paletteCache.remove(paletteCache.keys.first())
                }
                paletteCache[albumArtUri] = colors
                _paletteColors.value = colors
            } catch (e: Exception) {
                _paletteColors.value = PaletteColors()
            }
        }
    }
}
