package com.example.hearablemusicplayer.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.hearablemusicplayer.repository.MusicRepository
import com.example.hearablemusicplayer.repository.SettingsRepository

class MusicViewModelFactory(
    private val MusicRepo: MusicRepository,
    private val SettingsRepo: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MusicViewModel(MusicRepo,SettingsRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class PlayControlViewModelFactory(
    private val application: Application,
    private val MusicRepo: MusicRepository,
    private val SettingsRepo: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlayControlViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlayControlViewModel(application, MusicRepo,SettingsRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}