package com.example.hearablemusicplayer.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.hearablemusicplayer.data.database.myenum.PlaybackMode
import com.example.hearablemusicplayer.data.util.SecureStorageHelper
import com.example.hearablemusicplayer.data.model.AiProviderType
import com.example.hearablemusicplayer.data.model.AiProviderConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 每日推荐刷新配置数据类
 */
data class DailyRefreshConfig(
    val mode: String, // "time", "startup", "smart"
    val refreshHours: Int, // 按小时刷新的间隔
    val startupCount: Int, // 按启动次数刷新
    val lastRefreshTimestamp: Long, // 上次刷新时间戳
    val launchCountSinceRefresh: Int // 自上次刷新后的启动次数
)

// 在 Context 中创建 DataStore 实例
private val Context.dataStore by preferencesDataStore(name = "player_preferences")

// 在 ViewModel 中通过 applicationContext 构造
@Singleton
class SettingsRepository @Inject constructor(
    private val context: Context
) {
    // 定义 DataStore 中的键
    private object PreferencesKeys {
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val IS_LOAD_MUSIC = booleanPreferencesKey("is_load_music")
        val CURRENT_MUSIC_ID = longPreferencesKey("current_music_id")
        val PLAYBACK_MODE = stringPreferencesKey("playback_mode")
        val CURRENT_PLAYLIST_ID = longPreferencesKey("current_playlist_id")
        val LIKED_PLAYLIST_ID = longPreferencesKey("liked_playlist_id")
        val RECENT_PLAYLIST_ID = longPreferencesKey("recent_playlist_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val AVATAR_URI = stringPreferencesKey("avatar_uri")
        val DEEPSEEK_API_KEY = stringPreferencesKey("deepSeek_api_key")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        
        // 多 AI 服务商配置键
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
        
        // 音效相关设置键
        val EQUALIZER_PRESET = intPreferencesKey("equalizer_preset")
        val BASS_BOOST_LEVEL = intPreferencesKey("bass_boost_level")
        val IS_SURROUND_SOUND_ENABLED = booleanPreferencesKey("is_surround_sound_enabled")
        val REVERB_PRESET = intPreferencesKey("reverb_preset")
        val CUSTOM_EQUALIZER_LEVELS = stringPreferencesKey("custom_equalizer_levels")
        
        // AI 自动处理设置
        val AUTO_BATCH_PROCESS = booleanPreferencesKey("auto_batch_process")
        
        // 每日推荐刷新策略设置
        val DAILY_REFRESH_MODE = stringPreferencesKey("daily_refresh_mode") // time, startup, smart
        val DAILY_REFRESH_HOURS = intPreferencesKey("daily_refresh_hours") // 按小时刷新的间隔
        val DAILY_REFRESH_STARTUP_COUNT = intPreferencesKey("daily_refresh_startup_count") // 按启动次数刷新
        val LAST_DAILY_REFRESH_TIMESTAMP = longPreferencesKey("last_daily_refresh_timestamp") // 上次刷新时间戳
        val APP_LAUNCH_COUNT_SINCE_REFRESH = intPreferencesKey("app_launch_count_since_refresh") // 自上次刷新后的启动次数
        val CURRENT_DAILY_MUSIC_ID = longPreferencesKey("current_daily_music_id") // 当前每日推荐的音乐ID
    }

    // DataStore 访问实例
    private val dataStore = context.dataStore

    val isFirstLaunch: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[PreferencesKeys.IS_FIRST_LAUNCH] ?: true }

    // 用户名
    val userName: Flow<String> = dataStore.data
        .map { prefs -> prefs[PreferencesKeys.USER_NAME] ?: "User" }

    // 主题模式
    val themeMode: Flow<String> = dataStore.data
        .map { prefs -> prefs[PreferencesKeys.THEME_MODE] ?: "default" }

    // 应用是否已加载音乐,如果未设置则为 0
    val isLoadMusic: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[PreferencesKeys.IS_LOAD_MUSIC] ?: false }

    // 当前正在播放的音乐 ID,如果未设置则为 null
    val currentMusicId: Flow<Long?> = dataStore.data
        .map { prefs -> prefs[PreferencesKeys.CURRENT_MUSIC_ID] }

    // 播放模式(SEQUENTIAL、REPEAT_ONE、SHUFFLE),存储为字符串
    val playbackMode: Flow<PlaybackMode> = dataStore.data
        .map { prefs ->
            prefs[PreferencesKeys.PLAYBACK_MODE]?.let {
                try { PlaybackMode.valueOf(it) } catch (e: IllegalArgumentException) { null }
            } ?: PlaybackMode.SEQUENTIAL  // 默认值
        }

    // 当前播放列表 ID,用于恢复列表上下文
    val currentPlaylistId: Flow<Long?> = dataStore.data
        .map { prefs -> prefs[PreferencesKeys.CURRENT_PLAYLIST_ID] }

    // 喜爱播放列表 ID
    val likedPlaylistId: Flow<Long?> = dataStore.data
        .map { prefs -> prefs[PreferencesKeys.LIKED_PLAYLIST_ID] }

    // 最近播放列表 ID
    val recentPlaylistId: Flow<Long?> = dataStore.data
        .map { prefs -> prefs[PreferencesKeys.RECENT_PLAYLIST_ID] }

    // DeepSeek API KEY
    val deepSeekApiKey: Flow<String?> = dataStore.data
        .map { prefs -> prefs[PreferencesKeys.DEEPSEEK_API_KEY] }
    
    // 当前 AI 服务商类型
    val currentAiProvider: Flow<AiProviderType> = dataStore.data
        .map { prefs -> 
            val providerName = prefs[PreferencesKeys.CURRENT_AI_PROVIDER] ?: "DEEPSEEK"
            AiProviderType.fromName(providerName)
        }
    
    // 音效相关设置 Flow
    val equalizerPreset: Flow<Int> = dataStore.data
        .map { prefs -> prefs[PreferencesKeys.EQUALIZER_PRESET] ?: 0 }
    
    val bassBoostLevel: Flow<Int> = dataStore.data
        .map { prefs -> prefs[PreferencesKeys.BASS_BOOST_LEVEL] ?: 0 }
    
    val isSurroundSoundEnabled: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[PreferencesKeys.IS_SURROUND_SOUND_ENABLED] ?: false }
    
    val reverbPreset: Flow<Int> = dataStore.data
        .map { prefs -> prefs[PreferencesKeys.REVERB_PRESET] ?: 0 }
    
    val customEqualizerLevels: Flow<FloatArray> = dataStore.data
        .map { prefs -> 
            prefs[PreferencesKeys.CUSTOM_EQUALIZER_LEVELS]?.let { levelsString ->
                levelsString.split(",").mapNotNull { it.toFloatOrNull() }.toFloatArray()
            } ?: floatArrayOf()
        }
    
    // AI 自动批量处理开关
    val autoBatchProcess: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[PreferencesKeys.AUTO_BATCH_PROCESS] ?: false }
    
    // 每日推荐刷新策略相关 Flow
    val dailyRefreshMode: Flow<String> = dataStore.data
        .map { prefs -> prefs[PreferencesKeys.DAILY_REFRESH_MODE] ?: "time" } // 默认按时间
    
    val dailyRefreshHours: Flow<Int> = dataStore.data
        .map { prefs -> prefs[PreferencesKeys.DAILY_REFRESH_HOURS] ?: 24 } // 默认24小时
    
    val dailyRefreshStartupCount: Flow<Int> = dataStore.data
        .map { prefs -> prefs[PreferencesKeys.DAILY_REFRESH_STARTUP_COUNT] ?: 3 } // 默认3次启动
    
    val lastDailyRefreshTimestamp: Flow<Long> = dataStore.data
        .map { prefs -> prefs[PreferencesKeys.LAST_DAILY_REFRESH_TIMESTAMP] ?: 0L }
    
    val appLaunchCountSinceRefresh: Flow<Int> = dataStore.data
        .map { prefs -> prefs[PreferencesKeys.APP_LAUNCH_COUNT_SINCE_REFRESH] ?: 0 }

    suspend fun saveIsFirstLaunch(isFirstLaunch: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.IS_FIRST_LAUNCH] = isFirstLaunch
        }
    }

    suspend fun saveIsLoadMusic(isLoadMusic: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.IS_LOAD_MUSIC] = isLoadMusic
        }
    }

    // 保存主题模式
    suspend fun saveThemeMode(themeMode: String) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.THEME_MODE] = themeMode
        }
    }

    // 保存用户名
    suspend fun saveUserName(uri: String) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.USER_NAME] = uri
        }
    }

    // 保存用户头像 URI
    suspend fun saveAvatarUri(uri: String) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.AVATAR_URI] = uri
        }
    }
    // 获取用户头像 URI
    suspend fun getAvatarUri(): String? {
        return context.dataStore.data.first()[PreferencesKeys.AVATAR_URI]
    }

    // 保存当前音乐 ID
    suspend fun saveCurrentMusicId(id: Long) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.CURRENT_MUSIC_ID] = id
        }
    }

    // 保存播放模式
    suspend fun savePlaybackMode(mode: PlaybackMode) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.PLAYBACK_MODE] = mode.name
        }
    }

    // 获取当前播放列表 ID
    suspend fun getCurrentPlaylistId(): Long? {
        return context.dataStore.data.first()[PreferencesKeys.CURRENT_PLAYLIST_ID]
    }
    // 保存当前播放列表 ID
    suspend fun saveCurrentPlaylistId(playlistId: Long) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.CURRENT_PLAYLIST_ID] = playlistId
        }
    }

    // 获取喜爱播放列表 ID
    suspend fun getLikedPlaylistId(): Long? {
        return context.dataStore.data.first()[PreferencesKeys.LIKED_PLAYLIST_ID]
    }
    // 保存喜爱播放列表 ID
    suspend fun saveLikedPlaylistId(playlistId: Long) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.LIKED_PLAYLIST_ID] = playlistId
        }
    }

    // 获取最近播放列表 ID
    suspend fun getRecentPlaylistId(): Long? {
        return context.dataStore.data.first()[PreferencesKeys.RECENT_PLAYLIST_ID]
    }
    // 保存最近播放列表 ID
    suspend fun saveRecentPlaylistId(playlistId: Long) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.RECENT_PLAYLIST_ID] = playlistId
        }
    }
    
    // 保存 AI 自动批量处理开关
    suspend fun saveAutoBatchProcess(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.AUTO_BATCH_PROCESS] = enabled
        }
    }
    
    // ==================== 每日推荐刷新策略方法 ====================
    
    /**
     * 保存每日推荐刷新模式
     * @param mode "time" (按时间), "startup" (按启动次数), "smart" (智能刷新)
     */
    suspend fun saveDailyRefreshMode(mode: String) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.DAILY_REFRESH_MODE] = mode
        }
    }
    
    /**
     * 保存按小时刷新的间隔
     */
    suspend fun saveDailyRefreshHours(hours: Int) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.DAILY_REFRESH_HOURS] = hours
        }
    }
    
    /**
     * 保存按启动次数刷新
     */
    suspend fun saveDailyRefreshStartupCount(count: Int) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.DAILY_REFRESH_STARTUP_COUNT] = count
        }
    }
    
    /**
     * 更新上次刷新时间戳
     */
    suspend fun updateLastDailyRefreshTimestamp() {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.LAST_DAILY_REFRESH_TIMESTAMP] = System.currentTimeMillis()
            prefs[PreferencesKeys.APP_LAUNCH_COUNT_SINCE_REFRESH] = 0 // 重置启动计数
        }
    }
    
    /**
     * 保存当前每日推荐的音乐ID
     */
    suspend fun saveCurrentDailyMusicId(musicId: Long) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.CURRENT_DAILY_MUSIC_ID] = musicId
        }
    }
    
    /**
     * 获取当前每日推荐的音乐ID
     */
    suspend fun getCurrentDailyMusicId(): Long? {
        return context.dataStore.data.first()[PreferencesKeys.CURRENT_DAILY_MUSIC_ID]
    }
    
    /**
     * 增加应用启动计数
     */
    suspend fun incrementAppLaunchCount() {
        dataStore.edit { prefs ->
            val currentCount = prefs[PreferencesKeys.APP_LAUNCH_COUNT_SINCE_REFRESH] ?: 0
            prefs[PreferencesKeys.APP_LAUNCH_COUNT_SINCE_REFRESH] = currentCount + 1
        }
    }
    
    /**
     * 获取所有刷新策略配置
     */
    suspend fun getDailyRefreshConfig(): DailyRefreshConfig {
        val prefs = context.dataStore.data.first()
        return DailyRefreshConfig(
            mode = prefs[PreferencesKeys.DAILY_REFRESH_MODE] ?: "time",
            refreshHours = prefs[PreferencesKeys.DAILY_REFRESH_HOURS] ?: 24,
            startupCount = prefs[PreferencesKeys.DAILY_REFRESH_STARTUP_COUNT] ?: 3,
            lastRefreshTimestamp = prefs[PreferencesKeys.LAST_DAILY_REFRESH_TIMESTAMP] ?: 0L,
            launchCountSinceRefresh = prefs[PreferencesKeys.APP_LAUNCH_COUNT_SINCE_REFRESH] ?: 0
        )
    }

    // ==================== 多 AI 服务商配置方法 ====================
    
    /**
     * 获取当前选中的 AI 服务商类型
     */
    suspend fun getCurrentProvider(): AiProviderType {
        val providerName = context.dataStore.data.first()[PreferencesKeys.CURRENT_AI_PROVIDER] ?: "DEEPSEEK"
        return AiProviderType.fromName(providerName)
    }
    
    /**
     * 设置当前 AI 服务商类型
     */
    suspend fun setCurrentProvider(provider: AiProviderType) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.CURRENT_AI_PROVIDER] = provider.name
        }
    }
    
    /**
     * 获取指定服务商的 API Key
     */
    suspend fun getProviderApiKey(provider: AiProviderType): String {
        val key = when (provider) {
            AiProviderType.DEEPSEEK -> PreferencesKeys.DEEPSEEK_API_KEY
            AiProviderType.OPENAI -> PreferencesKeys.OPENAI_API_KEY
            AiProviderType.CLAUDE -> PreferencesKeys.CLAUDE_API_KEY
            AiProviderType.QWEN -> PreferencesKeys.QWEN_API_KEY
            AiProviderType.ERNIE -> PreferencesKeys.ERNIE_API_KEY
        }
        val encryptedKey = context.dataStore.data.first()[key]
        return encryptedKey?.let { SecureStorageHelper.decrypt(it) } ?: ""
    }
    
    /**
     * 设置指定服务商的 API Key
     */
    suspend fun setProviderApiKey(provider: AiProviderType, apiKey: String) {
        val key = when (provider) {
            AiProviderType.DEEPSEEK -> PreferencesKeys.DEEPSEEK_API_KEY
            AiProviderType.OPENAI -> PreferencesKeys.OPENAI_API_KEY
            AiProviderType.CLAUDE -> PreferencesKeys.CLAUDE_API_KEY
            AiProviderType.QWEN -> PreferencesKeys.QWEN_API_KEY
            AiProviderType.ERNIE -> PreferencesKeys.ERNIE_API_KEY
        }
        dataStore.edit { prefs ->
            prefs[key] = SecureStorageHelper.encrypt(apiKey)
        }
    }
    
    /**
     * 获取指定服务商的模型名称
     */
    suspend fun getProviderModel(provider: AiProviderType): String {
        val key = when (provider) {
            AiProviderType.DEEPSEEK -> PreferencesKeys.DEEPSEEK_MODEL
            AiProviderType.OPENAI -> PreferencesKeys.OPENAI_MODEL
            AiProviderType.CLAUDE -> PreferencesKeys.CLAUDE_MODEL
            AiProviderType.QWEN -> PreferencesKeys.QWEN_MODEL
            AiProviderType.ERNIE -> PreferencesKeys.ERNIE_MODEL
        }
        return context.dataStore.data.first()[key] ?: provider.defaultModel
    }
    
    /**
     * 设置指定服务商的模型名称
     */
    suspend fun setProviderModel(provider: AiProviderType, model: String) {
        val key = when (provider) {
            AiProviderType.DEEPSEEK -> PreferencesKeys.DEEPSEEK_MODEL
            AiProviderType.OPENAI -> PreferencesKeys.OPENAI_MODEL
            AiProviderType.CLAUDE -> PreferencesKeys.CLAUDE_MODEL
            AiProviderType.QWEN -> PreferencesKeys.QWEN_MODEL
            AiProviderType.ERNIE -> PreferencesKeys.ERNIE_MODEL
        }
        dataStore.edit { prefs ->
            prefs[key] = model
        }
    }
    
    /**
     * 获取指定服务商的完整配置
     */
    suspend fun getProviderConfig(provider: AiProviderType): AiProviderConfig {
        val apiKey = getProviderApiKey(provider)
        val model = getProviderModel(provider)
        return AiProviderConfig(
            type = provider,
            apiKey = apiKey,
            model = model,
            isConfigured = apiKey.isNotBlank()
        )
    }
    
    /**
     * 获取当前服务商的完整配置
     */
    suspend fun getCurrentProviderConfig(): AiProviderConfig {
        val currentProvider = getCurrentProvider()
        return getProviderConfig(currentProvider)
    }
    
    /**
     * 保存服务商配置
     */
    suspend fun saveProviderConfig(config: AiProviderConfig) {
        setProviderApiKey(config.type, config.apiKey)
        if (config.model.isNotBlank()) {
            setProviderModel(config.type, config.model)
        }
    }
    
    /**
     * 检查指定服务商是否已配置
     */
    suspend fun isProviderConfigured(provider: AiProviderType): Boolean {
        return getProviderApiKey(provider).isNotBlank()
    }
    
    /**
     * 获取所有已配置的服务商列表
     */
    suspend fun getConfiguredProviders(): List<AiProviderType> {
        return AiProviderType.entries.filter { isProviderConfigured(it) }
    }
    
    // 音效相关设置保存方法
    suspend fun saveEqualizerPreset(preset: Int) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.EQUALIZER_PRESET] = preset
        }
    }
    
    suspend fun saveBassBoostLevel(level: Int) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.BASS_BOOST_LEVEL] = level
        }
    }
    
    suspend fun saveSurroundSoundEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.IS_SURROUND_SOUND_ENABLED] = enabled
        }
    }
    
    suspend fun saveReverbPreset(preset: Int) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.REVERB_PRESET] = preset
        }
    }
    
    suspend fun saveCustomEqualizerLevels(levels: FloatArray) {
        dataStore.edit { prefs ->
            val levelsString = levels.joinToString(",")
            prefs[PreferencesKeys.CUSTOM_EQUALIZER_LEVELS] = levelsString
        }
    }
    
    /**
     * 备份设置到文件
     * @return Result<File> 备份文件路径
     */
    suspend fun backupSettings(): kotlin.Result<File> {
        return try {
            val backupDir = File(context.filesDir, "backups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            
            val backupFile = File(backupDir, "settings_backup_${System.currentTimeMillis()}.json")
            val preferences = context.dataStore.data.first()
            
            // 将偏好设置转换为 JSON 格式
            val jsonContent = buildString {
                append("{")
                preferences.asMap().entries.forEachIndexed { index, entry ->
                    if (index > 0) append(",")
                    append("\"${entry.key.name}\":")
                    val value = entry.value
                    when (value) {
                        is String -> append("\"$value\"")
                        is Boolean -> append(value)
                        is Long -> append(value)
                        is Int -> append(value)
                        else -> append("null")
                    }
                }
                append("}")
            }
            
            backupFile.writeText(jsonContent)
            Log.i("SettingsRepository", "Settings backed up to: ${backupFile.absolutePath}")
            kotlin.Result.success(backupFile)
        } catch (e: IOException) {
            Log.e("SettingsRepository", "Failed to backup settings", e)
            kotlin.Result.failure(e)
        } catch (e: Exception) {
            Log.e("SettingsRepository", "Unexpected error during backup", e)
            kotlin.Result.failure(e)
        }
    }

    /**
     * 从文件恢复设置
     * @param backupFile 备份文件
     * @return Result<Unit> 恢复结果
     */
    suspend fun restoreSettings(backupFile: File): kotlin.Result<Unit> {
        return try {
            if (!backupFile.exists()) {
                return kotlin.Result.failure(IOException("Backup file does not exist"))
            }
            
            val jsonContent = backupFile.readText()
            // 简单的 JSON 解析(生产环境建议使用 Gson 或 Kotlin Serialization)
            val regex = """"([^"]+)":\s*([^,}]+)""".toRegex()
            val matches = regex.findAll(jsonContent)
            
            context.dataStore.edit { prefs ->
                prefs.clear()
                matches.forEach { match ->
                    val key = match.groupValues[1]
                    val value = match.groupValues[2].trim()
                    
                    when (key) {
                        PreferencesKeys.IS_FIRST_LAUNCH.name -> {
                            prefs[PreferencesKeys.IS_FIRST_LAUNCH] = value.toBoolean()
                        }
                        PreferencesKeys.IS_LOAD_MUSIC.name -> {
                            prefs[PreferencesKeys.IS_LOAD_MUSIC] = value.toBoolean()
                        }
                        PreferencesKeys.CURRENT_MUSIC_ID.name -> {
                            prefs[PreferencesKeys.CURRENT_MUSIC_ID] = value.toLong()
                        }
                        PreferencesKeys.PLAYBACK_MODE.name -> {
                            prefs[PreferencesKeys.PLAYBACK_MODE] = value.trim('"')
                        }
                        PreferencesKeys.CURRENT_PLAYLIST_ID.name -> {
                            prefs[PreferencesKeys.CURRENT_PLAYLIST_ID] = value.toLong()
                        }
                        PreferencesKeys.LIKED_PLAYLIST_ID.name -> {
                            prefs[PreferencesKeys.LIKED_PLAYLIST_ID] = value.toLong()
                        }
                        PreferencesKeys.RECENT_PLAYLIST_ID.name -> {
                            prefs[PreferencesKeys.RECENT_PLAYLIST_ID] = value.toLong()
                        }
                        PreferencesKeys.USER_NAME.name -> {
                            prefs[PreferencesKeys.USER_NAME] = value.trim('"')
                        }
                        PreferencesKeys.AVATAR_URI.name -> {
                            prefs[PreferencesKeys.AVATAR_URI] = value.trim('"')
                        }
                        PreferencesKeys.DEEPSEEK_API_KEY.name -> {
                            prefs[PreferencesKeys.DEEPSEEK_API_KEY] = value.trim('"')
                        }
                        // 音效相关设置恢复
                        PreferencesKeys.EQUALIZER_PRESET.name -> {
                            prefs[PreferencesKeys.EQUALIZER_PRESET] = value.toInt()
                        }
                        PreferencesKeys.BASS_BOOST_LEVEL.name -> {
                            prefs[PreferencesKeys.BASS_BOOST_LEVEL] = value.toInt()
                        }
                        PreferencesKeys.IS_SURROUND_SOUND_ENABLED.name -> {
                            prefs[PreferencesKeys.IS_SURROUND_SOUND_ENABLED] = value.toBoolean()
                        }
                        PreferencesKeys.REVERB_PRESET.name -> {
                            prefs[PreferencesKeys.REVERB_PRESET] = value.toInt()
                        }
                        PreferencesKeys.CUSTOM_EQUALIZER_LEVELS.name -> {
                            prefs[PreferencesKeys.CUSTOM_EQUALIZER_LEVELS] = value.trim('"')
                        }
                    }
                }
            }
            
            Log.i("SettingsRepository", "Settings restored from: ${backupFile.absolutePath}")
            kotlin.Result.success(Unit)
        } catch (e: IOException) {
            Log.e("SettingsRepository", "Failed to restore settings", e)
            kotlin.Result.failure(e)
        } catch (e: Exception) {
            Log.e("SettingsRepository", "Unexpected error during restore", e)
            kotlin.Result.failure(e)
        }
    }

    /**
     * 清理旧备份文件(保留最近的 3 个)
     */
    suspend fun cleanOldBackups(keepCount: Int = 3): kotlin.Result<Unit> {
        return try {
            val backupDir = File(context.filesDir, "backups")
            if (!backupDir.exists()) {
                return kotlin.Result.success(Unit)
            }
            
            val backupFiles = backupDir.listFiles { file ->
                file.name.startsWith("settings_backup_") && file.name.endsWith(".json")
            }?.sortedByDescending { it.lastModified() } ?: emptyList()
            
            if (backupFiles.size > keepCount) {
                backupFiles.drop(keepCount).forEach { file ->
                    file.delete()
                    Log.i("SettingsRepository", "Deleted old backup: ${file.name}")
                }
            }
            
            kotlin.Result.success(Unit)
        } catch (e: Exception) {
            Log.e("SettingsRepository", "Failed to clean old backups", e)
            kotlin.Result.failure(e)
        }
    }
}
