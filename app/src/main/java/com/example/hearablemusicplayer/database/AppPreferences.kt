package com.example.hearablemusicplayer.database
import android.content.Context
import android.content.SharedPreferences
import com.example.hearablemusicplayer.AppConstants

object AppPreferences {
    private var sharedPreferences: SharedPreferences? = null

    // 初始化 SharedPreferences
    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)
    }

    // 保存用户名
    fun saveUserName(name: String) {
        sharedPreferences?.edit()?.putString(AppConstants.USER_NAME, name)?.apply()
    }

    // 获取用户名
    fun getUserName(): String? {
        return sharedPreferences?.getString(AppConstants.USER_NAME, null)
    }

    // 保存用户头像地址
    fun saveAvatarUri(uri: String) {
        sharedPreferences?.edit()?.putString(AppConstants.AVATAR_URI, uri)?.apply()
    }

    // 获取用户头像地址
    fun getAvatarUri(): String? {
        return sharedPreferences?.getString(AppConstants.AVATAR_URI, null)
    }

    // 保存当前播放歌曲的 ID
    fun saveCurrentPlayingMusicId(musicId: String) {
        sharedPreferences?.edit()?.putString(AppConstants.CURRENT_PLAYING_MUSIC_ID, musicId)?.apply()
    }

    // 获取当前播放歌曲的 ID
    fun getCurrentPlayingMusicId(): String? {
        return sharedPreferences?.getString(AppConstants.CURRENT_PLAYING_MUSIC_ID, null)
    }

}