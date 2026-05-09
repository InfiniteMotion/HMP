package com.hmp.domain.setting

import com.hmp.domain.config.DailyRefreshConfig
import com.hmp.domain.config.DisplayMode
import com.hmp.domain.config.LyricsAlignment
import com.hmp.domain.enum.AiProviderType
import com.hmp.domain.setting.model.AiProviderConfig
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    // Basic Settings
    val isFirstLaunch: Flow<Boolean>
    suspend fun saveIsFirstLaunch(isFirstLaunch: Boolean)

    val userName: Flow<String>
    suspend fun saveUserName(name: String)

    val themeMode: Flow<String>
    suspend fun saveThemeMode(themeMode: String)

    val backgroundStyle: Flow<String>
    suspend fun saveBackgroundStyle(style: String)

    val hazeMode: Flow<String>
    suspend fun saveHazeMode(mode: String)

    val hazeMaterialPreset: Flow<String>
    suspend fun saveHazeMaterialPreset(preset: String)

    val hazeBlurRadius: Flow<Float>
    suspend fun saveHazeBlurRadius(radius: Float)

    val hazeNoiseFactor: Flow<Float>
    suspend fun saveHazeNoiseFactor(noiseFactor: Float)

    val hazeTintAlpha: Flow<Float>
    suspend fun saveHazeTintAlpha(alpha: Float)

    val hazeIntensity: Flow<Float>
    suspend fun saveHazeIntensity(intensity: Float)

    val isLoadMusic: Flow<Boolean>
    suspend fun saveIsLoadMusic(isLoadMusic: Boolean)

    suspend fun saveAvatarUri(uri: String)
    suspend fun getAvatarUri(): String?

    // Playback State
    val currentMusicId: Flow<Long?>
    suspend fun saveCurrentMusicId(id: Long)
    
    val currentPosition: Flow<Long>
    suspend fun saveCurrentPosition(position: Long)

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

    // Lyrics Configuration
    val lyricsOriginalTextSize: Flow<Int>
    suspend fun saveLyricsOriginalTextSize(size: Int)
    suspend fun getLyricsOriginalTextSize(): Int

    val lyricsTranslatedTextSize: Flow<Int>
    suspend fun saveLyricsTranslatedTextSize(size: Int)
    suspend fun getLyricsTranslatedTextSize(): Int

    val lyricsCurrentTimeTextSize: Flow<Int>
    suspend fun saveLyricsCurrentTimeTextSize(size: Int)
    suspend fun getLyricsCurrentTimeTextSize(): Int

    val lyricsLineSpacing: Flow<Int>
    suspend fun saveLyricsLineSpacing(spacing: Int)
    suspend fun getLyricsLineSpacing(): Int

    val lyricsDisplayMode: Flow<DisplayMode>
    suspend fun saveLyricsDisplayMode(mode: DisplayMode)
    suspend fun getLyricsDisplayMode(): DisplayMode

    val lyricsAlignment: Flow<LyricsAlignment>
    suspend fun saveLyricsAlignment(alignment: LyricsAlignment)
    suspend fun getLyricsAlignment(): LyricsAlignment

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

    // Gallery Sort
    val galleryOrderBy: Flow<String>
    suspend fun saveGalleryOrderBy(orderBy: String)

    val galleryOrderType: Flow<String>
    suspend fun saveGalleryOrderType(orderType: String)

    // Backup / Restore
    // Playlist Algorithm Configuration
    val defaultAlgorithmType: Flow<String>
    suspend fun saveDefaultAlgorithmType(type: String)
    suspend fun getDefaultAlgorithmType(): String
    
    val defaultWeightTemplate: Flow<String>
    suspend fun saveDefaultWeightTemplate(template: String)
    suspend fun getDefaultWeightTemplate(): String
    
    val defaultExtensionConfig: Flow<String>
    suspend fun saveDefaultExtensionConfig(configJson: String)
    suspend fun getDefaultExtensionConfig(): String
    
    // Snapshot Export/Import
    suspend fun exportAppSettingsSnapshot(): com.hmp.domain.backup.AppSettingsSnapshot
    suspend fun restoreFromSnapshot(snapshot: com.hmp.domain.backup.AppSettingsSnapshot)
    
    suspend fun exportDailyRecommendationSnapshot(): com.hmp.domain.backup.DailyRecommendationSnapshot?
    suspend fun restoreDailyRecommendationSnapshot(snapshot: com.hmp.domain.backup.DailyRecommendationSnapshot)

    suspend fun backupSettings(): Result<String>
    suspend fun restoreSettings(backupFilePath: String): Result<Unit>
    suspend fun cleanOldBackups(keepCount: Int = 3): Result<Unit>
}