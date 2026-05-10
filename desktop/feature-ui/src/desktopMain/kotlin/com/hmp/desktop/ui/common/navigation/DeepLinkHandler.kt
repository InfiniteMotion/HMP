package com.hmp.desktop.ui.common.navigation

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * 深层链接类型
 * 表示应用支持的深层链接格式
 */
@Serializable
sealed class DeepLink {
    /** 歌曲详情深层链接 */
    @Serializable
    data class Song(val musicId: Long) : DeepLink()
    
    /** 播放列表深层链接（按名称） */
    @Serializable
    data class Playlist(val name: String) : DeepLink()
    
    /** 自定义播放列表深层链接（按ID） */
    @Serializable
    data class CustomPlaylist(val playlistId: Long) : DeepLink()
    
    /** 艺术家深层链接 */
    @Serializable
    data class Artist(val name: String) : DeepLink()
    
    /** 专辑深层链接 */
    @Serializable
    data class Album(val name: String) : DeepLink()
    
    /** 搜索深层链接 */
    @Serializable
    data class Search(val query: String? = null) : DeepLink()
    
    /** 设置深层链接 */
    object Settings : DeepLink()
    
    /** 音频效果深层链接 */
    object AudioEffects : DeepLink()
    
    /** AI页面深层链接 */
    object AI : DeepLink()
    
    /** 用户使用数据深层链接 */
    object UserUsageData : DeepLink()
}

/**
 * 深层链接处理器
 * 负责解析深层链接 URI 并将其转换为导航路由
 *
 * 支持两种格式的深层链接：
 * 1. URI 格式：`hearablemusicplayer://song/{musicId}`，用于应用内和外部链接
 * 2. JSON 格式：通过 `fromJson` 和 `toJson` 方法，用于推送通知等场景
 *
 * 处理流程：URI → parseUri → DeepLink 对象 → convertToRoute → NavKey → NavController.navigate
 */
class DeepLinkHandler(private val navController: NavController) {
    /**
     * 处理深层链接 URI
     *
     * 该方法会解析 URI，将其转换为对应的 DeepLink 对象，再转换为 NavKey 路由，
     * 最后通过 NavController 导航到目标页面。如果解析或导航过程中发生异常，会返回 false。
     *
     * @param uri 深层链接 URI，支持 `hearablemusicplayer://` 和 `https://` scheme
     * @return 是否成功处理（解析成功且导航执行）
     * @throws IllegalArgumentException 当 URI 格式不支持时抛出
     */
    fun handleDeepLink(uriString: String): Boolean {
        return try {
            val deepLink = parseUri(uriString)
            val route = convertToRoute(deepLink)
            navController.navigate(route)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 解析 URI 字符串为 DeepLink 对象
     *
     * 根据 URI 的 host 和 path 解析出对应的 DeepLink 子类。
     * 桌面端使用简单的字符串解析替代 Android Uri 类。
     *
     * @param uriString 深层链接 URI 字符串
     * @return 解析后的 DeepLink 对象
     * @throws IllegalArgumentException 当 URI 格式不支持时抛出
     */
    private fun parseUri(uriString: String): DeepLink {
        // 解析 scheme://host/path?query 格式
        val schemeEnd = uriString.indexOf("://")
        if (schemeEnd == -1) throw IllegalArgumentException("Invalid URI: $uriString")
        val scheme = uriString.substring(0, schemeEnd)
        val rest = uriString.substring(schemeEnd + 3)

        val queryStart = rest.indexOf('?')
        val pathPart = if (queryStart >= 0) rest.substring(0, queryStart) else rest
        val queryPart = if (queryStart >= 0) rest.substring(queryStart + 1) else null

        val pathSegments = pathPart.split("/").filter { it.isNotEmpty() }
        val host = pathSegments.firstOrNull() ?: throw IllegalArgumentException("Missing host in URI")
        val path = pathSegments.drop(1)

        // 解析查询参数
        val queryParams = queryPart?.split("&")?.associate {
            val parts = it.split("=", limit = 2)
            parts[0] to (parts.getOrNull(1) ?: "")
        } ?: emptyMap()

        // 验证 scheme
        if (scheme != "hearablemusicplayer" && scheme != "https") {
            throw IllegalArgumentException("Unsupported scheme: $scheme")
        }

        // 根据 host 和 path 解析
        return when (host) {
            "song" -> {
                if (path.size >= 1) {
                    val musicId = path[0].toLongOrNull()
                        ?: throw IllegalArgumentException("Invalid music ID: ${path[0]}")
                    DeepLink.Song(musicId)
                } else {
                    throw IllegalArgumentException("Missing music ID in song deep link")
                }
            }
            "playlist" -> {
                if (path.size >= 1) {
                    val name = path[0]
                    DeepLink.Playlist(name)
                } else {
                    throw IllegalArgumentException("Missing playlist name in playlist deep link")
                }
            }
            "customPlaylist" -> {
                if (path.size >= 1) {
                    val playlistId = path[0].toLongOrNull()
                        ?: throw IllegalArgumentException("Invalid playlist ID: ${path[0]}")
                    DeepLink.CustomPlaylist(playlistId)
                } else {
                    throw IllegalArgumentException("Missing playlist ID in custom playlist deep link")
                }
            }
            "artist" -> {
                if (path.size >= 1) {
                    val name = path[0]
                    DeepLink.Artist(name)
                } else {
                    throw IllegalArgumentException("Missing artist name in artist deep link")
                }
            }
            "album" -> {
                if (path.size >= 1) {
                    val name = path[0]
                    DeepLink.Album(name)
                } else {
                    throw IllegalArgumentException("Missing album name in album deep link")
                }
            }
            "search" -> {
                val query = queryParams["q"]
                DeepLink.Search(query)
            }
            "settings" -> DeepLink.Settings
            "audioEffects" -> DeepLink.AudioEffects
            "ai" -> DeepLink.AI
            "userUsageData" -> DeepLink.UserUsageData
            else -> throw IllegalArgumentException("Unsupported deep link host: $host")
        }
    }
    
    /**
     * 将 DeepLink 转换为 NavKey 路由
     *
     * 将 DeepLink 子类映射到对应的 Routes 定义，确保深层链接最终导航到正确的页面。
     * 这种映射关系是静态的，新增 DeepLink 类型时需要在此处添加对应的转换逻辑。
     *
     * @param deepLink 深层链接对象
     * @return 对应的 NavKey 路由
     */
    private fun convertToRoute(deepLink: DeepLink): NavKey {
        return when (deepLink) {
            is DeepLink.Song -> Routes.Library.SongDetail(deepLink.musicId)
            is DeepLink.Playlist -> Routes.Playlist.Playlist(deepLink.name)
            is DeepLink.CustomPlaylist -> Routes.Playlist.CustomPlaylist(deepLink.playlistId)
            is DeepLink.Artist -> Routes.Library.Artist(deepLink.name)
            is DeepLink.Album -> Routes.Library.Album(deepLink.name)
            is DeepLink.Search -> Routes.Library.Search
            DeepLink.Settings -> Routes.Settings.Setting
            DeepLink.AudioEffects -> Routes.Player.AudioEffects
            DeepLink.AI -> Routes.AI.AI
            DeepLink.UserUsageData -> Routes.UserData.UserUsageData
        }
    }
    
    companion object {
        /**
         * 从 JSON 字符串解析深层链接
         *
         * 用于从推送通知、跨进程通信等场景传递复杂参数。JSON 格式提供了比 URI 更灵活的参数结构。
         * 如果 JSON 格式无效或解析失败，返回 null。
         *
         * @param json JSON 格式的深层链接字符串
         * @return 解析后的 DeepLink 对象，解析失败时返回 null
         */
        fun fromJson(json: String): DeepLink? {
            return try {
                Json.decodeFromString<DeepLink>(json)
            } catch (e: Exception) {
                null
            }
        }

        /**
         * 将深层链接转换为 JSON 字符串
         *
         * 用于将 DeepLink 对象序列化为 JSON 字符串，便于存储或传输。
         * 序列化使用 kotlinx.serialization 库，确保与 fromJson 对称。
         *
         * @param deepLink 深层链接对象
         * @return JSON 格式的字符串表示
         */
        fun toJson(deepLink: DeepLink): String {
            return Json.encodeToString(deepLink)
        }
    }
}