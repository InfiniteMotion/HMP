package com.example.hearablemusicplayer.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.hearablemusicplayer.database.myenum.PlaybackMode
import com.example.hearablemusicplayer.tools.SecureStorageHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// 在 Context 中创建 DataStore 实例
private val Context.dataStore by preferencesDataStore(name = "player_preferences")

// 在 ViewModel 中通过 applicationContext 构造
class SettingsRepository(
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

    // 应用是否已加载音乐，如果未设置则为 0
    val isLoadMusic: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[PreferencesKeys.IS_LOAD_MUSIC] ?: false }

    // 当前正在播放的音乐 ID，如果未设置则为 null
    val currentMusicId: Flow<Long?> = dataStore.data
        .map { prefs -> prefs[PreferencesKeys.CURRENT_MUSIC_ID] }

    // 播放模式（SEQUENTIAL、REPEAT_ONE、SHUFFLE），存储为字符串
    val playbackMode: Flow<PlaybackMode> = dataStore.data
        .map { prefs ->
            prefs[PreferencesKeys.PLAYBACK_MODE]?.let {
                try { PlaybackMode.valueOf(it) } catch (e: IllegalArgumentException) { null }
            } ?: PlaybackMode.SEQUENTIAL  // 默认值
        }

    // 当前播放列表 ID，用于恢复列表上下文
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

}
