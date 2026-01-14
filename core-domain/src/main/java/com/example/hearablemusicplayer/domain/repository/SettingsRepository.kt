package com.example.hearablemusicplayer.domain.repository

import com.example.hearablemusicplayer.domain.model.AiProviderConfig
import com.example.hearablemusicplayer.domain.model.DailyRefreshConfig
import com.example.hearablemusicplayer.domain.model.enum.AiProviderType
import com.example.hearablemusicplayer.domain.model.enum.PlaybackMode
import kotlinx.coroutines.flow.Flow
import java.io.File

interface SettingsRepository {
    // Basic Settings
    val isFirstLaunch: Flow<Boolean>
    suspend fun saveIsFirstLaunch(isFirstLaunch: Boolean)
    
    val userName: Flow<String>
    suspend fun saveUserName(name: String)
    
    val themeMode: Flow<String>
    suspend fun saveThemeMode(themeMode: String)
    
    val isLoadMusic: Flow<Boolean>
    suspend fun saveIsLoadMusic(isLoadMusic: Boolean)
    
    suspend fun saveAvatarUri(uri: String)
    suspend fun getAvatarUri(): String?
    
    // Playback State
    val currentMusicId: Flow<Long?>
    suspend fun saveCurrentMusicId(id: Long)
    
    val playbackMode: Flow<PlaybackMode>
    suspend fun savePlaybackMode(mode: PlaybackMode)
    
    val currentPlaylistId: Flow<Long?>
    suspend fun saveCurrentPlaylistId(playlistId: Long)
    
    // Special Playlists
    val likedPlaylistId: Flow<Long?>
    suspend fun saveLikedPlaylistId(playlistId: Long)
    suspend fun getLikedPlaylistId(): Long?
    
    val recentPlaylistId: Flow<Long?>
    suspend fun saveRecentPlaylistId(playlistId: Long)
    suspend fun getRecentPlaylistId(): Long?
    
    suspend fun getCurrentPlaylistId(): Long?
    
    // AI Provider Config
    val currentAiProvider: Flow<AiProviderType>
    suspend fun getCurrentProvider(): AiProviderType
    suspend fun setCurrentProvider(provider: AiProviderType)
    
    suspend fun getProviderApiKey(provider: AiProviderType): String
    suspend fun setProviderApiKey(provider: AiProviderType, apiKey: String)
    
    suspend fun getProviderModel(provider: AiProviderType): String
    suspend fun setProviderModel(provider: AiProviderType, model: String)
    
    suspend fun getProviderConfig(provider: AiProviderType): AiProviderConfig
    suspend fun getCurrentProviderConfig(): AiProviderConfig
    suspend fun saveProviderConfig(config: AiProviderConfig)
    
    suspend fun isProviderConfigured(provider: AiProviderType): Boolean
    suspend fun getConfiguredProviders(): List<AiProviderType>
    
    // Audio Effects
    val equalizerPreset: Flow<Int>
    suspend fun saveEqualizerPreset(preset: Int)
    
    val bassBoostLevel: Flow<Int>
    suspend fun saveBassBoostLevel(level: Int)
    
    val isSurroundSoundEnabled: Flow<Boolean>
    suspend fun saveSurroundSoundEnabled(enabled: Boolean)
    
    val reverbPreset: Flow<Int>
    suspend fun saveReverbPreset(preset: Int)
    
    val customEqualizerLevels: Flow<FloatArray>
    suspend fun saveCustomEqualizerLevels(levels: FloatArray)
    
    // AI Batch Process
    val autoBatchProcess: Flow<Boolean>
    suspend fun saveAutoBatchProcess(enabled: Boolean)
    
    // Daily Refresh Strategy
    val dailyRefreshMode: Flow<String>
    suspend fun saveDailyRefreshMode(mode: String)
    
    val dailyRefreshHours: Flow<Int>
    suspend fun saveDailyRefreshHours(hours: Int)
    
    val dailyRefreshStartupCount: Flow<Int>
    suspend fun saveDailyRefreshStartupCount(count: Int)
    
    val lastDailyRefreshTimestamp: Flow<Long>
    suspend fun updateLastDailyRefreshTimestamp()
    
    val appLaunchCountSinceRefresh: Flow<Int>
    suspend fun incrementAppLaunchCount()
    
    suspend fun getDailyRefreshConfig(): DailyRefreshConfig
    
    suspend fun saveCurrentDailyMusicId(musicId: Long)
    suspend fun getCurrentDailyMusicId(): Long?
    
    // Backup / Restore
    suspend fun backupSettings(): kotlin.Result<File>
    suspend fun restoreSettings(backupFile: File): kotlin.Result<Unit>
    suspend fun cleanOldBackups(keepCount: Int = 3): kotlin.Result<Unit>
}
