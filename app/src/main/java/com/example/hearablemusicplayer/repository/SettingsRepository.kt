package com.example.hearablemusicplayer.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.hearablemusicplayer.database.DailyMusicInfo
import com.example.hearablemusicplayer.database.myenum.PlaybackMode
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
        val IS_LOAD_MUSIC = booleanPreferencesKey("is_load_music")
        val CURRENT_MUSIC_ID = longPreferencesKey("current_music_id")
        val PLAYBACK_MODE = stringPreferencesKey("playback_mode")
        val CURRENT_PLAYLIST_ID = longPreferencesKey("current_playlist_id")
        val LIKED_PLAYLIST_ID = longPreferencesKey("liked_playlist_id")
        val RECENT_PLAYLIST_ID = longPreferencesKey("recent_playlist_id")
//        val USER_NAME = stringPreferencesKey("user_name")
        val AVATAR_URI = intPreferencesKey("avatar_uri")

        val DAILY_MUSIC_ID = longPreferencesKey("daily_music_id")
        val DAILY_MUSIC_REWARDS = stringPreferencesKey("daily_music_rewards")
        val DAILY_MUSIC_LYRIC = stringPreferencesKey("daily_music_lyric")
        val DAILY_MUSIC_SINGER_INTRO = stringPreferencesKey("daily_music_singer_introduce")
        val DAILY_MUSIC_BACKGROUND_INTRO = stringPreferencesKey("daily_music_background_introduce")
        val DAILY_MUSIC_DESCRIPTION = stringPreferencesKey("daily_music_description")
        val DAILY_MUSIC_RELEVANT_MUSIC = stringPreferencesKey("daily_music_relevant_music")
    }

    // DataStore 访问实例
    private val dataStore = context.dataStore

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


    suspend fun saveIsLoadMusic(isLoadMusic: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.IS_LOAD_MUSIC]=isLoadMusic
        }
    }

//    // 保存用户名
//    suspend fun saveUserName(uri: String) {
//        dataStore.edit { prefs ->
//            prefs[PreferencesKeys.USER_NAME] = uri
//        }
//    }
//
//    // 获取用户名
//    suspend fun getUserName(): String? {
//        return context.dataStore.data.first()[PreferencesKeys.USER_NAME]
//    }

//    // 保存用户头像 URI
//    suspend fun saveAvatarUri(uri: Int) {
//        dataStore.edit { prefs ->
//            prefs[PreferencesKeys.AVATAR_URI] = uri
//        }
//    }

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

    // 获取最近播放列表 ID
    suspend fun getDaliyMusicInfoId(): Long? {
        return context.dataStore.data.first()[PreferencesKeys.DAILY_MUSIC_ID]
    }
    // 保存最近播放列表 ID
    suspend fun saveDaliyMusicInfoId(musicId: Long) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.DAILY_MUSIC_ID] = musicId
        }
    }

    // 获取最近播放列表 ID
    suspend fun getDaliyMusicInfo(): DailyMusicInfo? {
        return DailyMusicInfo(
            genre = emptyList(),
            mood = emptyList(),
            scenario = emptyList(),
            language = "",
            era = "",
            rewards = context.dataStore.data.first()[PreferencesKeys.DAILY_MUSIC_REWARDS]?:"",
            lyric = context.dataStore.data.first()[PreferencesKeys.DAILY_MUSIC_LYRIC]?:"",
            singerIntroduce = context.dataStore.data.first()[PreferencesKeys.DAILY_MUSIC_SINGER_INTRO]?:"",
            backgroundIntroduce = context.dataStore.data.first()[PreferencesKeys.DAILY_MUSIC_BACKGROUND_INTRO]?:"",
            description = context.dataStore.data.first()[PreferencesKeys.DAILY_MUSIC_DESCRIPTION]?:"",
            relevantMusic = context.dataStore.data.first()[PreferencesKeys.DAILY_MUSIC_RELEVANT_MUSIC]?:"",
            errorInfo = "None"
        )
    }
    // 保存最近播放列表 ID
    suspend fun saveDaliyMusicInfo(dailyMusicInfo: DailyMusicInfo) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.DAILY_MUSIC_REWARDS] = dailyMusicInfo.rewards
            prefs[PreferencesKeys.DAILY_MUSIC_LYRIC] = dailyMusicInfo.lyric
            prefs[PreferencesKeys.DAILY_MUSIC_SINGER_INTRO] = dailyMusicInfo.singerIntroduce
            prefs[PreferencesKeys.DAILY_MUSIC_BACKGROUND_INTRO] = dailyMusicInfo.backgroundIntroduce
            prefs[PreferencesKeys.DAILY_MUSIC_DESCRIPTION] = dailyMusicInfo.description
            prefs[PreferencesKeys.DAILY_MUSIC_RELEVANT_MUSIC] = dailyMusicInfo.relevantMusic
        }
    }
}
