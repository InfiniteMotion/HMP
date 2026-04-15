package com.example.hearablemusicplayer.ui.viewmodel

import androidx.compose.runtime.Stable
import com.example.hearablemusicplayer.domain.config.DisplayMode
import com.example.hearablemusicplayer.domain.config.LyricsAlignment

@Stable
data class LyricsSettingsState(
    val lyricsOriginalTextSize: Int = 14,
    val lyricsTranslatedTextSize: Int = 14,
    val lyricsCurrentTimeTextSize: Int = 16,
    val lyricsLineSpacing: Int = 6,
    val lyricsDisplayMode: DisplayMode = DisplayMode.DUAL,
    val lyricsAlignment: LyricsAlignment = LyricsAlignment.CENTER
)
