package com.example.hearablemusicplayer.ui.util

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * 应用路由常量类
 * 使用object定义所有路由，确保全局唯一性和类型安全
 */
object Routes {
    /** Tabs 容器页路由（Home/Gallery/List/User 的统一承载） */
    @Serializable object Tabs : NavKey

    /** 主页路由 */
    @Serializable object Home : NavKey
    
    /** 画廊页路由 */
    @Serializable object Gallery : NavKey
    
    /** 播放器页路由 */
    @Serializable object Player : NavKey
    
    /** 列表页路由 */
    @Serializable object List : NavKey
    
    /** 用户页路由 */
    @Serializable object User : NavKey
    
    /** 设置页路由 */
    @Serializable object Setting : NavKey
    
    /** 个人资料设置路由 */
    @Serializable object ProfileSettings : NavKey
    
    /** 备份设置路由 */
    @Serializable object BackupSettings : NavKey
    
    /** 音乐库设置路由 */
    @Serializable object LibrarySettings : NavKey
    
    /** 搜索页路由 */
    @Serializable object Search : NavKey
    
    /** 播放列表页路由（按名称，用于标签列表与默认/红心/最近） */
    @Serializable data class Playlist(val name: String) : NavKey

    /** 用户自定义播放列表详情页路由（按 ID） */
    @Serializable data class CustomPlaylist(val playlistId: Long) : NavKey

    /** 用户歌单管理页路由 */
    @Serializable object UserPlaylistManage : NavKey
    
    /** 艺术家页路由 */
    @Serializable data class Artist(val name: String) : NavKey
    
    /** 音频效果页路由 */
    @Serializable object AudioEffects : NavKey
    
    /** AI页路由 */
    @Serializable object AI : NavKey
    
    /** 自定义页路由 */
    @Serializable object Custom : NavKey

    /** 歌曲详情页路由 */
    @Serializable data class SongDetail(val musicId: Long) : NavKey

    /** 歌词页路由 */
    @Serializable object Lyrics : NavKey

    /** 用户使用数据页路由 */
    @Serializable object UserUsageData : NavKey
}
