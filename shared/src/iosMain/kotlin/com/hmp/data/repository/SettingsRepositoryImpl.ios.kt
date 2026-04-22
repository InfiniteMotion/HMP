package com.hmp.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hmp.data.util.SecureStorageHelper
import com.hmp.domain.config.DailyRefreshConfig
import com.hmp.domain.config.DisplayMode
import com.hmp.domain.config.LyricsAlignment
import com.hmp.domain.enum.AiProviderType
import com.hmp.domain.setting.SettingsRepository
import com.hmp.domain.setting.model.AiProviderConfig
import com.hmp.domain.backup.AppSettingsSnapshot
import com.hmp.domain.backup.DailyRecommendationSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {
    private companion object {
        const val HAZE_MODE_CUSTOM = "custom"
        const val HAZE_MODE_PRESET = "preset"
        const val HAZE_MATERIAL_PRESET_REGULAR = "regular"
        const val DEFAULT_HAZE_BLUR_RADIUS = 20f
        const val DEFAULT_HAZE_NOISE_FACTOR = 0.15f
        const val DEFAULT_HAZE_TINT_ALPHA = 0.22f
    }

    private object PreferencesKeys {
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val IS_LOAD_MUSIC = booleanPreferencesKey("is_load_music")
        val CURRENT_MUSIC_ID = longPreferencesKey("current_music_id")
        val CURRENT_POSITION = longPreferencesKey("current_position")
        val CURRENT_PLAYLIST_ID = longPreferencesKey("current_playlist_id")
        val LIKED_PLAYLIST_ID = longPreferencesKey("liked_playlist_id")
        val RECENT_PLAYLIST_ID = longPreferencesKey("recent_playlist_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val AVATAR_URI = stringPreferencesKey("avatar_uri")
        val DEEPSEEK_API_KEY = stringPreferencesKey("deepSeek_api_key")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val BACKGROUND_STYLE = stringPreferencesKey("background_style")
        val HAZE_MODE = stringPreferencesKey("haze_mode")
        val HAZE_MATERIAL_PRESET = stringPreferencesKey("haze_material_preset")
        val HAZE_BLUR_RADIUS = floatPreferencesKey("haze_blur_radius")
        val HAZE_NOISE_FACTOR = floatPreferencesKey("haze_noise_factor")
        val HAZE_TINT_ALPHA = floatPreferencesKey("haze_tint_alpha")
        val HAZE_INTENSITY = floatPreferencesKey("haze_intensity")
        val CURRENT_AI_PROVIDER = stringPreferencesKey("current_ai_provider")
        val DEEPSEEK_MODEL = stringPreferencesKey("deepseek_model")
        val OPENAI_API_KEY = stringPreferencesKey("openai_api_key")
        val OPENAI_MODEL = stringPreferencesKey("openai_model")
        val CLAUDE_API_KEY = stringPreferencesKey("claude_api_key")
        val CLAUDE_MODEL = stringPreferencesKey("claude_model")
        val QWEN_API_KEY = stringPreferencesKey("qwen_api_key")
        val QWEN_MODEL = stringPreferencesKey("qwen_model")
        val ERNIE_API_KEY = stringPreferencesKey("ernie_api_key")
        val ERNIE_MODEL = stringPreferencesKey("ernie_model")
        val EQUALIZER_PRESET = intPreferencesKey("equalizer_preset")
        val BASS_BOOST_LEVEL = intPreferencesKey("bass_boost_level")
        val IS_SURROUND_SOUND_ENABLED = booleanPreferencesKey("is_surround_sound_enabled")
        val REVERB_PRESET = intPreferencesKey("reverb_preset")
        val CUSTOM_EQUALIZER_LEVELS = stringPreferencesKey("custom_equalizer_levels")
        val AUTO_BATCH_PROCESS = booleanPreferencesKey("auto_batch_process")
        val DAILY_REFRESH_MODE = stringPreferencesKey("daily_refresh_mode")
        val DAILY_REFRESH_HOURS = intPreferencesKey("daily_refresh_hours")
        val DAILY_REFRESH_STARTUP_COUNT = intPreferencesKey("daily_refresh_startup_count")
        val LAST_DAILY_REFRESH_TIMESTAMP = longPreferencesKey("last_daily_refresh_timestamp")
        val APP_LAUNCH_COUNT_SINCE_REFRESH = intPreferencesKey("app_launch_count_since_refresh")
        val CURRENT_DAILY_MUSIC_ID = longPreferencesKey("current_daily_music_id")
        val LYRICS_ORIGINAL_TEXT_SIZE = intPreferencesKey("lyrics_original_text_size")
        val LYRICS_TRANSLATED_TEXT_SIZE = intPreferencesKey("lyrics_translated_text_size")
        val LYRICS_CURRENT_TIME_TEXT_SIZE = intPreferencesKey("lyrics_current_time_text_size")
        val LYRICS_LINE_SPACING = intPreferencesKey("lyrics_line_spacing")
        val LYRICS_DISPLAY_MODE = stringPreferencesKey("lyrics_display_mode")
        val LYRICS_ALIGNMENT = stringPreferencesKey("lyrics_alignment")
        val DEFAULT_ALGORITHM_TYPE = stringPreferencesKey("default_algorithm_type")
        val DEFAULT_WEIGHT_TEMPLATE = stringPreferencesKey("default_weight_template")
        val DEFAULT_EXTENSION_CONFIG = stringPreferencesKey("default_extension_config")
    }

    private fun normalizeHazeMode(mode: String): String {
        return if (mode == HAZE_MODE_PRESET) HAZE_MODE_PRESET else HAZE_MODE_CUSTOM
    }

    private fun normalizeHazeMaterialPreset(preset: String): String {
        return when (preset) {
            "ultra_thin", "thin", "regular", "thick", "ultra_thick" -> preset
            else -> HAZE_MATERIAL_PRESET_REGULAR
        }
    }

    override val isFirstLaunch: Flow<Boolean> = dataStore.data.map { prefs -> prefs[PreferencesKeys.IS_FIRST_LAUNCH] ?: true }
    override val userName: Flow<String> = dataStore.data.map { prefs -> prefs[PreferencesKeys.USER_NAME] ?: "User" }
    override val themeMode: Flow<String> = dataStore.data.map { prefs -> prefs[PreferencesKeys.THEME_MODE] ?: "default" }
    override val backgroundStyle: Flow<String> = dataStore.data.map { prefs -> prefs[PreferencesKeys.BACKGROUND_STYLE] ?: "FLUID" }
    override val hazeMode: Flow<String> = dataStore.data.map { prefs -> normalizeHazeMode(prefs[PreferencesKeys.HAZE_MODE] ?: HAZE_MODE_CUSTOM) }
    override val hazeMaterialPreset: Flow<String> = dataStore.data.map { prefs -> normalizeHazeMaterialPreset(prefs[PreferencesKeys.HAZE_MATERIAL_PRESET] ?: HAZE_MATERIAL_PRESET_REGULAR) }
    override val hazeBlurRadius: Flow<Float> = dataStore.data.map { prefs -> prefs[PreferencesKeys.HAZE_BLUR_RADIUS] ?: DEFAULT_HAZE_BLUR_RADIUS }
    override val hazeNoiseFactor: Flow<Float> = dataStore.data.map { prefs -> (prefs[PreferencesKeys.HAZE_NOISE_FACTOR] ?: DEFAULT_HAZE_NOISE_FACTOR).coerceIn(0f, 1f) }
    override val hazeTintAlpha: Flow<Float> = dataStore.data.map { prefs -> (prefs[PreferencesKeys.HAZE_TINT_ALPHA] ?: DEFAULT_HAZE_TINT_ALPHA).coerceIn(0f, 1f) }
    override val hazeIntensity: Flow<Float> = dataStore.data.map { prefs -> prefs[PreferencesKeys.HAZE_INTENSITY] ?: 0.6f }
    override val isLoadMusic: Flow<Boolean> = dataStore.data.map { prefs -> prefs[PreferencesKeys.IS_LOAD_MUSIC] ?: false }
    override val currentMusicId: Flow<Long?> = dataStore.data.map { prefs -> prefs[PreferencesKeys.CURRENT_MUSIC_ID] }
    override val currentPlaylistId: Flow<Long?> = dataStore.data.map { prefs -> prefs[PreferencesKeys.CURRENT_PLAYLIST_ID] }
    override val likedPlaylistId: Flow<Long?> = dataStore.data.map { prefs -> prefs[PreferencesKeys.LIKED_PLAYLIST_ID] }
    override val recentPlaylistId: Flow<Long?> = dataStore.data.map { prefs -> prefs[PreferencesKeys.RECENT_PLAYLIST_ID] }
    override val currentAiProvider: Flow<AiProviderType> = dataStore.data.map { prefs -> AiProviderType.fromName(prefs[PreferencesKeys.CURRENT_AI_PROVIDER] ?: "DEEPSEEK") }
    override val equalizerPreset: Flow<Int> = dataStore.data.map { prefs -> prefs[PreferencesKeys.EQUALIZER_PRESET] ?: 0 }
    override val bassBoostLevel: Flow<Int> = dataStore.data.map { prefs -> prefs[PreferencesKeys.BASS_BOOST_LEVEL] ?: 0 }
    override val isSurroundSoundEnabled: Flow<Boolean> = dataStore.data.map { prefs -> prefs[PreferencesKeys.IS_SURROUND_SOUND_ENABLED] ?: false }
    override val reverbPreset: Flow<Int> = dataStore.data.map { prefs -> prefs[PreferencesKeys.REVERB_PRESET] ?: 0 }
    override val customEqualizerLevels: Flow<FloatArray> = dataStore.data.map { prefs -> prefs[PreferencesKeys.CUSTOM_EQUALIZER_LEVELS]?.let { it.split(",").mapNotNull { s -> s.toFloatOrNull() }.toFloatArray() } ?: floatArrayOf() }
    override val autoBatchProcess: Flow<Boolean> = dataStore.data.map { prefs -> prefs[PreferencesKeys.AUTO_BATCH_PROCESS] ?: false }
    override val dailyRefreshMode: Flow<String> = dataStore.data.map { prefs -> prefs[PreferencesKeys.DAILY_REFRESH_MODE] ?: "time" }
    override val dailyRefreshHours: Flow<Int> = dataStore.data.map { prefs -> prefs[PreferencesKeys.DAILY_REFRESH_HOURS] ?: 24 }
    override val dailyRefreshStartupCount: Flow<Int> = dataStore.data.map { prefs -> prefs[PreferencesKeys.DAILY_REFRESH_STARTUP_COUNT] ?: 3 }
    override val lastDailyRefreshTimestamp: Flow<Long> = dataStore.data.map { prefs -> prefs[PreferencesKeys.LAST_DAILY_REFRESH_TIMESTAMP] ?: 0L }
    override val appLaunchCountSinceRefresh: Flow<Int> = dataStore.data.map { prefs -> prefs[PreferencesKeys.APP_LAUNCH_COUNT_SINCE_REFRESH] ?: 0 }
    override val currentPosition: Flow<Long> = dataStore.data.map { prefs -> prefs[PreferencesKeys.CURRENT_POSITION] ?: 0L }
    override val lyricsOriginalTextSize: Flow<Int> = dataStore.data.map { prefs -> prefs[PreferencesKeys.LYRICS_ORIGINAL_TEXT_SIZE] ?: 14 }
    override val lyricsTranslatedTextSize: Flow<Int> = dataStore.data.map { prefs -> prefs[PreferencesKeys.LYRICS_TRANSLATED_TEXT_SIZE] ?: 14 }
    override val lyricsCurrentTimeTextSize: Flow<Int> = dataStore.data.map { prefs -> prefs[PreferencesKeys.LYRICS_CURRENT_TIME_TEXT_SIZE] ?: 16 }
    override val lyricsLineSpacing: Flow<Int> = dataStore.data.map { prefs -> prefs[PreferencesKeys.LYRICS_LINE_SPACING] ?: 6 }
    override val lyricsDisplayMode: Flow<DisplayMode> = dataStore.data.map {
        val modeStr = it[PreferencesKeys.LYRICS_DISPLAY_MODE] ?: "DUAL"
        try { DisplayMode.valueOf(modeStr) } catch (e: IllegalArgumentException) { DisplayMode.DUAL }
    }
    override val lyricsAlignment: Flow<LyricsAlignment> = dataStore.data.map {
        val alignmentStr = it[PreferencesKeys.LYRICS_ALIGNMENT] ?: "CENTER"
        try { LyricsAlignment.valueOf(alignmentStr) } catch (e: IllegalArgumentException) { LyricsAlignment.CENTER }
    }
    override val defaultAlgorithmType: Flow<String> = dataStore.data.map { prefs -> prefs[PreferencesKeys.DEFAULT_ALGORITHM_TYPE] ?: "OPTIMIZED_SIMILARITY" }
    override val defaultWeightTemplate: Flow<String> = dataStore.data.map { prefs -> prefs[PreferencesKeys.DEFAULT_WEIGHT_TEMPLATE] ?: "BALANCED" }
    override val defaultExtensionConfig: Flow<String> = dataStore.data.map { prefs -> prefs[PreferencesKeys.DEFAULT_EXTENSION_CONFIG] ?: "{}" }

    override suspend fun saveIsFirstLaunch(isFirstLaunch: Boolean) { dataStore.edit { prefs -> prefs[PreferencesKeys.IS_FIRST_LAUNCH] = isFirstLaunch } }
    override suspend fun saveIsLoadMusic(isLoadMusic: Boolean) { dataStore.edit { prefs -> prefs[PreferencesKeys.IS_LOAD_MUSIC] = isLoadMusic } }
    override suspend fun saveThemeMode(themeMode: String) { dataStore.edit { prefs -> prefs[PreferencesKeys.THEME_MODE] = themeMode } }
    override suspend fun saveBackgroundStyle(style: String) { dataStore.edit { prefs -> prefs[PreferencesKeys.BACKGROUND_STYLE] = style } }
    override suspend fun saveHazeMode(mode: String) { dataStore.edit { prefs -> prefs[PreferencesKeys.HAZE_MODE] = normalizeHazeMode(mode) } }
    override suspend fun saveHazeMaterialPreset(preset: String) { dataStore.edit { prefs -> prefs[PreferencesKeys.HAZE_MATERIAL_PRESET] = normalizeHazeMaterialPreset(preset) } }
    override suspend fun saveHazeBlurRadius(radius: Float) { dataStore.edit { prefs -> prefs[PreferencesKeys.HAZE_BLUR_RADIUS] = radius.coerceAtLeast(0f) } }
    override suspend fun saveHazeNoiseFactor(noiseFactor: Float) { dataStore.edit { prefs -> prefs[PreferencesKeys.HAZE_NOISE_FACTOR] = noiseFactor.coerceIn(0f, 1f) } }
    override suspend fun saveHazeTintAlpha(alpha: Float) { dataStore.edit { prefs -> prefs[PreferencesKeys.HAZE_TINT_ALPHA] = alpha.coerceIn(0f, 1f) } }
    override suspend fun saveHazeIntensity(intensity: Float) { dataStore.edit { prefs -> prefs[PreferencesKeys.HAZE_INTENSITY] = intensity.coerceIn(0f, 1f) } }
    override suspend fun saveUserName(name: String) { dataStore.edit { prefs -> prefs[PreferencesKeys.USER_NAME] = name } }
    override suspend fun saveAvatarUri(uri: String) { dataStore.edit { prefs -> prefs[PreferencesKeys.AVATAR_URI] = uri } }
    override suspend fun getAvatarUri(): String? = dataStore.data.first()[PreferencesKeys.AVATAR_URI]
    override suspend fun saveCurrentMusicId(id: Long) { dataStore.edit { prefs -> prefs[PreferencesKeys.CURRENT_MUSIC_ID] = id } }
    override suspend fun saveCurrentPosition(position: Long) { dataStore.edit { prefs -> prefs[PreferencesKeys.CURRENT_POSITION] = position } }
    override suspend fun getCurrentPlaylistId(): Long? = dataStore.data.first()[PreferencesKeys.CURRENT_PLAYLIST_ID]
    override suspend fun saveCurrentPlaylistId(playlistId: Long) { dataStore.edit { prefs -> prefs[PreferencesKeys.CURRENT_PLAYLIST_ID] = playlistId } }
    override suspend fun getLikedPlaylistId(): Long? = dataStore.data.first()[PreferencesKeys.LIKED_PLAYLIST_ID]
    override suspend fun saveLikedPlaylistId(playlistId: Long) { dataStore.edit { prefs -> prefs[PreferencesKeys.LIKED_PLAYLIST_ID] = playlistId } }
    override suspend fun getRecentPlaylistId(): Long? = dataStore.data.first()[PreferencesKeys.RECENT_PLAYLIST_ID]
    override suspend fun saveRecentPlaylistId(playlistId: Long) { dataStore.edit { prefs -> prefs[PreferencesKeys.RECENT_PLAYLIST_ID] = playlistId } }
    override suspend fun saveAutoBatchProcess(enabled: Boolean) { dataStore.edit { prefs -> prefs[PreferencesKeys.AUTO_BATCH_PROCESS] = enabled } }
    override suspend fun saveDailyRefreshMode(mode: String) { dataStore.edit { prefs -> prefs[PreferencesKeys.DAILY_REFRESH_MODE] = mode } }
    override suspend fun saveDailyRefreshHours(hours: Int) { dataStore.edit { prefs -> prefs[PreferencesKeys.DAILY_REFRESH_HOURS] = hours } }
    override suspend fun saveDailyRefreshStartupCount(count: Int) { dataStore.edit { prefs -> prefs[PreferencesKeys.DAILY_REFRESH_STARTUP_COUNT] = count } }
    override suspend fun updateLastDailyRefreshTimestamp() { dataStore.edit { prefs -> prefs[PreferencesKeys.LAST_DAILY_REFRESH_TIMESTAMP] = System.currentTimeMillis(); prefs[PreferencesKeys.APP_LAUNCH_COUNT_SINCE_REFRESH] = 0 } }
    override suspend fun saveCurrentDailyMusicId(musicId: Long) { dataStore.edit { prefs -> prefs[PreferencesKeys.CURRENT_DAILY_MUSIC_ID] = musicId } }
    override suspend fun getCurrentDailyMusicId(): Long? = dataStore.data.first()[PreferencesKeys.CURRENT_DAILY_MUSIC_ID]
    override suspend fun incrementAppLaunchCount() { dataStore.edit { prefs -> prefs[PreferencesKeys.APP_LAUNCH_COUNT_SINCE_REFRESH] = (prefs[PreferencesKeys.APP_LAUNCH_COUNT_SINCE_REFRESH] ?: 0) + 1 } }
    override suspend fun getDailyRefreshConfig(): DailyRefreshConfig {
        val prefs = dataStore.data.first()
        return DailyRefreshConfig(
            mode = prefs[PreferencesKeys.DAILY_REFRESH_MODE] ?: "time",
            refreshHours = prefs[PreferencesKeys.DAILY_REFRESH_HOURS] ?: 24,
            startupCount = prefs[PreferencesKeys.DAILY_REFRESH_STARTUP_COUNT] ?: 3,
            lastRefreshTimestamp = prefs[PreferencesKeys.LAST_DAILY_REFRESH_TIMESTAMP] ?: 0L,
            launchCountSinceRefresh = prefs[PreferencesKeys.APP_LAUNCH_COUNT_SINCE_REFRESH] ?: 0
        )
    }
    override suspend fun getCurrentProvider(): AiProviderType = AiProviderType.fromName(dataStore.data.first()[PreferencesKeys.CURRENT_AI_PROVIDER] ?: "DEEPSEEK")
    override suspend fun setCurrentProvider(provider: AiProviderType) { dataStore.edit { prefs -> prefs[PreferencesKeys.CURRENT_AI_PROVIDER] = provider.name } }
    override suspend fun getProviderApiKey(provider: AiProviderType): String {
        val key = when (provider) {
            AiProviderType.DEEPSEEK -> PreferencesKeys.DEEPSEEK_API_KEY
            AiProviderType.OPENAI -> PreferencesKeys.OPENAI_API_KEY
            AiProviderType.CLAUDE -> PreferencesKeys.CLAUDE_API_KEY
            AiProviderType.QWEN -> PreferencesKeys.QWEN_API_KEY
            AiProviderType.ERNIE -> PreferencesKeys.ERNIE_API_KEY
        }
        val encryptedKey = dataStore.data.first()[key]
        return encryptedKey?.let { SecureStorageHelper.decrypt(it) } ?: ""
    }
    override suspend fun setProviderApiKey(provider: AiProviderType, apiKey: String) {
        val key = when (provider) {
            AiProviderType.DEEPSEEK -> PreferencesKeys.DEEPSEEK_API_KEY
            AiProviderType.OPENAI -> PreferencesKeys.OPENAI_API_KEY
            AiProviderType.CLAUDE -> PreferencesKeys.CLAUDE_API_KEY
            AiProviderType.QWEN -> PreferencesKeys.QWEN_API_KEY
            AiProviderType.ERNIE -> PreferencesKeys.ERNIE_API_KEY
        }
        dataStore.edit { prefs -> prefs[key] = SecureStorageHelper.encrypt(apiKey) }
    }
    override suspend fun getProviderModel(provider: AiProviderType): String {
        val key = when (provider) {
            AiProviderType.DEEPSEEK -> PreferencesKeys.DEEPSEEK_MODEL
            AiProviderType.OPENAI -> PreferencesKeys.OPENAI_MODEL
            AiProviderType.CLAUDE -> PreferencesKeys.CLAUDE_MODEL
            AiProviderType.QWEN -> PreferencesKeys.QWEN_MODEL
            AiProviderType.ERNIE -> PreferencesKeys.ERNIE_MODEL
        }
        return dataStore.data.first()[key] ?: provider.defaultModel
    }
    override suspend fun setProviderModel(provider: AiProviderType, model: String) {
        val key = when (provider) {
            AiProviderType.DEEPSEEK -> PreferencesKeys.DEEPSEEK_MODEL
            AiProviderType.OPENAI -> PreferencesKeys.OPENAI_MODEL
            AiProviderType.CLAUDE -> PreferencesKeys.CLAUDE_MODEL
            AiProviderType.QWEN -> PreferencesKeys.QWEN_MODEL
            AiProviderType.ERNIE -> PreferencesKeys.ERNIE_MODEL
        }
        dataStore.edit { prefs -> prefs[key] = model }
    }
    override suspend fun getProviderConfig(provider: AiProviderType): AiProviderConfig {
        val apiKey = getProviderApiKey(provider)
        val model = getProviderModel(provider)
        return AiProviderConfig(type = provider, apiKey = apiKey, model = model, isConfigured = apiKey.isNotBlank())
    }
    override suspend fun getCurrentProviderConfig(): AiProviderConfig = getProviderConfig(getCurrentProvider())
    override suspend fun saveProviderConfig(config: AiProviderConfig) {
        setProviderApiKey(config.type, config.apiKey)
        if (config.model.isNotBlank()) setProviderModel(config.type, config.model)
    }
    override suspend fun isProviderConfigured(provider: AiProviderType): Boolean = getProviderApiKey(provider).isNotBlank()
    override suspend fun getConfiguredProviders(): List<AiProviderType> = AiProviderType.entries.filter { isProviderConfigured(it) }
    override suspend fun saveEqualizerPreset(preset: Int) { dataStore.edit { prefs -> prefs[PreferencesKeys.EQUALIZER_PRESET] = preset } }
    override suspend fun saveBassBoostLevel(level: Int) { dataStore.edit { prefs -> prefs[PreferencesKeys.BASS_BOOST_LEVEL] = level } }
    override suspend fun saveSurroundSoundEnabled(enabled: Boolean) { dataStore.edit { prefs -> prefs[PreferencesKeys.IS_SURROUND_SOUND_ENABLED] = enabled } }
    override suspend fun saveReverbPreset(preset: Int) { dataStore.edit { prefs -> prefs[PreferencesKeys.REVERB_PRESET] = preset } }
    override suspend fun saveCustomEqualizerLevels(levels: FloatArray) { dataStore.edit { prefs -> prefs[PreferencesKeys.CUSTOM_EQUALIZER_LEVELS] = levels.joinToString(",") } }
    override suspend fun backupSettings(): Result<String> = Result.failure(NotImplementedError("Backup not implemented on iOS yet"))
    override suspend fun restoreSettings(backupFilePath: String): Result<Unit> = Result.failure(NotImplementedError("Restore not implemented on iOS yet"))
    override suspend fun saveLyricsOriginalTextSize(size: Int) { dataStore.edit { prefs -> prefs[PreferencesKeys.LYRICS_ORIGINAL_TEXT_SIZE] = size } }
    override suspend fun saveLyricsTranslatedTextSize(size: Int) { dataStore.edit { prefs -> prefs[PreferencesKeys.LYRICS_TRANSLATED_TEXT_SIZE] = size } }
    override suspend fun saveLyricsCurrentTimeTextSize(size: Int) { dataStore.edit { prefs -> prefs[PreferencesKeys.LYRICS_CURRENT_TIME_TEXT_SIZE] = size } }
    override suspend fun saveLyricsLineSpacing(spacing: Int) { dataStore.edit { prefs -> prefs[PreferencesKeys.LYRICS_LINE_SPACING] = spacing } }
    override suspend fun saveLyricsDisplayMode(mode: DisplayMode) { dataStore.edit { prefs -> prefs[PreferencesKeys.LYRICS_DISPLAY_MODE] = mode.name } }
    override suspend fun getLyricsDisplayMode(): DisplayMode {
        val modeStr = dataStore.data.first()[PreferencesKeys.LYRICS_DISPLAY_MODE] ?: "DUAL"
        return try { DisplayMode.valueOf(modeStr) } catch (e: IllegalArgumentException) { DisplayMode.DUAL }
    }
    override suspend fun saveLyricsAlignment(alignment: LyricsAlignment) { dataStore.edit { prefs -> prefs[PreferencesKeys.LYRICS_ALIGNMENT] = alignment.name } }
    override suspend fun getLyricsAlignment(): LyricsAlignment {
        val alignmentStr = dataStore.data.first()[PreferencesKeys.LYRICS_ALIGNMENT] ?: "CENTER"
        return try { LyricsAlignment.valueOf(alignmentStr) } catch (e: IllegalArgumentException) { LyricsAlignment.CENTER }
    }
    override suspend fun getLyricsOriginalTextSize(): Int = dataStore.data.first()[PreferencesKeys.LYRICS_ORIGINAL_TEXT_SIZE] ?: 14
    override suspend fun getLyricsTranslatedTextSize(): Int = dataStore.data.first()[PreferencesKeys.LYRICS_TRANSLATED_TEXT_SIZE] ?: 14
    override suspend fun getLyricsCurrentTimeTextSize(): Int = dataStore.data.first()[PreferencesKeys.LYRICS_CURRENT_TIME_TEXT_SIZE] ?: 16
    override suspend fun getLyricsLineSpacing(): Int = dataStore.data.first()[PreferencesKeys.LYRICS_LINE_SPACING] ?: 6
    override suspend fun cleanOldBackups(keepCount: Int): Result<Unit> = Result.success(Unit)
    override suspend fun saveDefaultAlgorithmType(type: String) { dataStore.edit { prefs -> prefs[PreferencesKeys.DEFAULT_ALGORITHM_TYPE] = type } }
    override suspend fun getDefaultAlgorithmType(): String = dataStore.data.first()[PreferencesKeys.DEFAULT_ALGORITHM_TYPE] ?: "OPTIMIZED_SIMILARITY"
    override suspend fun saveDefaultWeightTemplate(template: String) { dataStore.edit { prefs -> prefs[PreferencesKeys.DEFAULT_WEIGHT_TEMPLATE] = template } }
    override suspend fun getDefaultWeightTemplate(): String = dataStore.data.first()[PreferencesKeys.DEFAULT_WEIGHT_TEMPLATE] ?: "BALANCED"
    override suspend fun saveDefaultExtensionConfig(configJson: String) { dataStore.edit { prefs -> prefs[PreferencesKeys.DEFAULT_EXTENSION_CONFIG] = configJson } }
    override suspend fun getDefaultExtensionConfig(): String = dataStore.data.first()[PreferencesKeys.DEFAULT_EXTENSION_CONFIG] ?: "{}"

    override suspend fun exportAppSettingsSnapshot(): AppSettingsSnapshot {
        val prefs = dataStore.data.first()
        val currentAiProvider = AiProviderType.fromName(prefs[PreferencesKeys.CURRENT_AI_PROVIDER] ?: "DEEPSEEK")
        val aiProviderConfigs = AiProviderType.entries.associateWith { getProviderConfig(it) }
        return AppSettingsSnapshot(
            userName = prefs[PreferencesKeys.USER_NAME],
            avatarUri = prefs[PreferencesKeys.AVATAR_URI],
            themeMode = prefs[PreferencesKeys.THEME_MODE] ?: "default",
            backgroundStyle = prefs[PreferencesKeys.BACKGROUND_STYLE] ?: "FLUID",
            hazeMode = normalizeHazeMode(prefs[PreferencesKeys.HAZE_MODE] ?: HAZE_MODE_CUSTOM),
            hazeMaterialPreset = normalizeHazeMaterialPreset(prefs[PreferencesKeys.HAZE_MATERIAL_PRESET] ?: HAZE_MATERIAL_PRESET_REGULAR),
            hazeBlurRadius = prefs[PreferencesKeys.HAZE_BLUR_RADIUS] ?: DEFAULT_HAZE_BLUR_RADIUS,
            hazeNoiseFactor = (prefs[PreferencesKeys.HAZE_NOISE_FACTOR] ?: DEFAULT_HAZE_NOISE_FACTOR).coerceIn(0f, 1f),
            hazeTintAlpha = (prefs[PreferencesKeys.HAZE_TINT_ALPHA] ?: DEFAULT_HAZE_TINT_ALPHA).coerceIn(0f, 1f),
            hazeIntensity = prefs[PreferencesKeys.HAZE_INTENSITY] ?: 0.6f,
            autoBatchProcess = prefs[PreferencesKeys.AUTO_BATCH_PROCESS] ?: false,
            dailyRefreshMode = prefs[PreferencesKeys.DAILY_REFRESH_MODE] ?: "time",
            dailyRefreshHours = prefs[PreferencesKeys.DAILY_REFRESH_HOURS] ?: 24,
            dailyRefreshStartupCount = prefs[PreferencesKeys.DAILY_REFRESH_STARTUP_COUNT] ?: 3,
            currentAiProvider = currentAiProvider,
            aiProviderConfigs = aiProviderConfigs
        )
    }

    override suspend fun restoreFromSnapshot(snapshot: AppSettingsSnapshot) {
        dataStore.edit {
            snapshot.userName?.let { it[PreferencesKeys.USER_NAME] = it }
            snapshot.avatarUri?.let { it[PreferencesKeys.AVATAR_URI] = it }
            it[PreferencesKeys.THEME_MODE] = snapshot.themeMode
            it[PreferencesKeys.BACKGROUND_STYLE] = snapshot.backgroundStyle
            it[PreferencesKeys.HAZE_MODE] = normalizeHazeMode(snapshot.hazeMode)
            it[PreferencesKeys.HAZE_MATERIAL_PRESET] = normalizeHazeMaterialPreset(snapshot.hazeMaterialPreset)
            it[PreferencesKeys.HAZE_BLUR_RADIUS] = snapshot.hazeBlurRadius.coerceAtLeast(0f)
            it[PreferencesKeys.HAZE_NOISE_FACTOR] = snapshot.hazeNoiseFactor.coerceIn(0f, 1f)
            it[PreferencesKeys.HAZE_TINT_ALPHA] = snapshot.hazeTintAlpha.coerceIn(0f, 1f)
            it[PreferencesKeys.HAZE_INTENSITY] = snapshot.hazeIntensity.coerceIn(0f, 1f)
            it[PreferencesKeys.AUTO_BATCH_PROCESS] = snapshot.autoBatchProcess
            it[PreferencesKeys.DAILY_REFRESH_MODE] = snapshot.dailyRefreshMode
            it[PreferencesKeys.DAILY_REFRESH_HOURS] = snapshot.dailyRefreshHours
            it[PreferencesKeys.DAILY_REFRESH_STARTUP_COUNT] = snapshot.dailyRefreshStartupCount
            it[PreferencesKeys.CURRENT_AI_PROVIDER] = snapshot.currentAiProvider.name
        }
        snapshot.aiProviderConfigs.forEach { (type, config) -> saveProviderConfig(config) }
    }

    override suspend fun exportDailyRecommendationSnapshot(): DailyRecommendationSnapshot? {
        val prefs = dataStore.data.first()
        return DailyRecommendationSnapshot(
            currentDailyMusicId = prefs[PreferencesKeys.CURRENT_DAILY_MUSIC_ID],
            lastRefreshTimestamp = prefs[PreferencesKeys.LAST_DAILY_REFRESH_TIMESTAMP] ?: 0L,
            mode = prefs[PreferencesKeys.DAILY_REFRESH_MODE] ?: "time",
            refreshHours = prefs[PreferencesKeys.DAILY_REFRESH_HOURS] ?: 24,
            startupCount = prefs[PreferencesKeys.DAILY_REFRESH_STARTUP_COUNT] ?: 3,
            launchCountSinceRefresh = prefs[PreferencesKeys.APP_LAUNCH_COUNT_SINCE_REFRESH] ?: 0
        )
    }

    override suspend fun restoreDailyRecommendationSnapshot(snapshot: DailyRecommendationSnapshot) {
        dataStore.edit {
            snapshot.currentDailyMusicId?.let { it[PreferencesKeys.CURRENT_DAILY_MUSIC_ID] = it }
            it[PreferencesKeys.LAST_DAILY_REFRESH_TIMESTAMP] = snapshot.lastRefreshTimestamp
            it[PreferencesKeys.DAILY_REFRESH_MODE] = snapshot.mode
            it[PreferencesKeys.DAILY_REFRESH_HOURS] = snapshot.refreshHours
            it[PreferencesKeys.DAILY_REFRESH_STARTUP_COUNT] = snapshot.startupCount
            it[PreferencesKeys.APP_LAUNCH_COUNT_SINCE_REFRESH] = snapshot.launchCountSinceRefresh
        }
    }
}