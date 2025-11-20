package com.example.hearablemusicplayer.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.hearablemusicplayer.data.database.myenum.PlaybackMode
import com.example.hearablemusicplayer.data.util.SecureStorageHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

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
    }

    // DataStore 访问实例
    private val dataStore = context.dataStore

    val isFirstLaunch: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[PreferencesKeys.IS_FIRST_LAUNCH] ?: true }

    // 用户名
    val userName: Flow<String> = dataStore.data
        .map { prefs -> prefs[PreferencesKeys.USER_NAME] ?: "User" }

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

    // 保存 DeepSeek API KEY
    suspend fun saveDeepSeekApiKey(apiKey: String) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.DEEPSEEK_API_KEY] = SecureStorageHelper.encrypt(apiKey)
        }
    }
    // 获取 DeepSeek API KEY
    suspend fun getDeepSeekApiKey(): String {
        val apiKey = context.dataStore.data.first()[PreferencesKeys.DEEPSEEK_API_KEY]?.let {
            SecureStorageHelper.decrypt(
                it
            )
        }
        return apiKey?:"Bearer sk-6f67067abbd04e68baedf13c0aeb8c0a"
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
