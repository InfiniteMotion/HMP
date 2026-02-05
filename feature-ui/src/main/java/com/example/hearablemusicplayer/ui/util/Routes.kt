package com.example.hearablemusicplayer.ui.util

import kotlinx.serialization.Serializable

/**
 * 应用路由常量类
 * 使用object定义所有路由，确保全局唯一性和类型安全
 */
object Routes {
    /** 主页路由 */
    @Serializable object Home
    
    /** 画廊页路由 */
    @Serializable object Gallery
    
    /** 播放器页路由 */
    @Serializable object Player
    
    /** 列表页路由 */
    @Serializable object List
    
    /** 用户页路由 */
    @Serializable object User
    
    /** 设置页路由 */
    @Serializable object Setting
    
    /** 搜索页路由 */
    @Serializable object Search
    
    /** 播放列表页路由 */
    @Serializable data class Playlist(val name: String)
    
    /** 艺术家页路由 */
    @Serializable data class Artist(val name: String)
    
    /** 音频效果页路由 */
    @Serializable object AudioEffects
    
    /** AI页路由 */
    @Serializable object AI
    
    /** 自定义页路由 */
    @Serializable object Custom

    /** 歌曲详情页路由 */
    @Serializable data class SongDetail(val musicId: Long)
}