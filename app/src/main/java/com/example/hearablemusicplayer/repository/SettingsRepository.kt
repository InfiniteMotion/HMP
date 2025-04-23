package com.example.hearablemusicplayer.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.hearablemusicplayer.database.myClass.PlaybackMode
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
        val CURRENT_MUSIC_ID = longPreferencesKey("current_music_id")
        val PLAYBACK_MODE = stringPreferencesKey("playback_mode")
        val CURRENT_PLAYLIST_ID = longPreferencesKey("current_playlist_id")
        val LIKED_PLAYLIST_ID = longPreferencesKey("liked_playlist_id")
        val RECENT_PLAYLIST_ID = longPreferencesKey("recent_playlist_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val AVATAR_URI = intPreferencesKey("avatar_uri")
    }

    // DataStore 访问实例
    private val dataStore = context.dataStore

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

    // 用户名
    val userName: Flow<String?> = dataStore.data
        .map { prefs -> prefs[PreferencesKeys.USER_NAME] }

    // 用户头像 URI
    val avatarUri: Flow<Int?> = dataStore.data
        .map { prefs -> prefs[PreferencesKeys.AVATAR_URI] }

    // 保存用户头像 URI
    suspend fun saveAvatarUri(uri: Int) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.AVATAR_URI] = uri
        }
    }

    // 获取用户头像 URI
    suspend fun getAvatarUri(): Int? {
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

    // 获取喜爱播放列表 ID
    suspend fun getLikedPlaylistId(): Long? {
        return context.dataStore.data.first()[PreferencesKeys.LIKED_PLAYLIST_ID]
    }

    // 获取最近播放列表 ID
    suspend fun getRecentPlaylistId(): Long? {
        return context.dataStore.data.first()[PreferencesKeys.RECENT_PLAYLIST_ID]
    }

    // 保存当前播放列表 ID
    suspend fun saveCurrentPlaylistId(playlistId: Long) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.CURRENT_PLAYLIST_ID] = playlistId
        }
    }

    // 保存喜爱播放列表 ID
    suspend fun saveLikedPlaylistId(playlistId: Long) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.LIKED_PLAYLIST_ID] = playlistId
        }
    }

    // 保存最近播放列表 ID
    suspend fun saveRecentPlaylistId(playlistId: Long) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.RECENT_PLAYLIST_ID] = playlistId
        }
    }
}
