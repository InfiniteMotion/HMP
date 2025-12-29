package com.example.hearablemusicplayer.data

import com.example.hearablemusicplayer.data.database.*
import com.example.hearablemusicplayer.data.database.myenum.LabelCategory
import com.example.hearablemusicplayer.data.database.myenum.LabelName

/**
 * 测试数据工厂
 * 
 * 提供可复用的测试数据构建方法，用于创建各种实体的测试实例
 */
object TestDataFactory {
    
    /**
     * 创建测试用的Music实例
     */
    fun createMusic(
        id: Long = 1L,
        title: String = "测试歌曲",
        artist: String = "测试歌手",
        album: String = "测试专辑",
        duration: Long = 180000L, // 3分钟
        path: String = "/storage/emulated/0/Music/test.mp3",
        albumArtUri: String = ""
    ): Music {
        return Music(
            id = id,
            title = title,
            artist = artist,
            album = album,
            duration = duration,
            path = path,
            albumArtUri = albumArtUri
        )
    }
    
    /**
     * 创建测试用的MusicExtra实例
     */
    fun createMusicExtra(
        id: Long = 1L,
        lyrics: String? = null,
        bitRate: Int? = 320,
        sampleRate: Int? = 44100,
        fileSize: Long? = 3145728L, // 3MB
        format: String? = "mp3",
        language: String? = null,
        year: Int? = 2024,
        recommendationIds: String? = null,
        isGetExtraInfo: Boolean = false,
        rewards: String? = null,
        popLyric: String? = null,
        singerIntroduce: String? = null,
        backgroundIntroduce: String? = null,
        description: String? = null,
        relevantMusic: String? = null
    ): MusicExtra {
        return MusicExtra(
            id = id,
            lyrics = lyrics,
            bitRate = bitRate,
            sampleRate = sampleRate,
            fileSize = fileSize,
            format = format,
            language = language,
            year = year,
            recommendationIds = recommendationIds,
            isGetExtraInfo = isGetExtraInfo,
            rewards = rewards,
            popLyric = popLyric,
            singerIntroduce = singerIntroduce,
            backgroundIntroduce = backgroundIntroduce,
            description = description,
            relevantMusic = relevantMusic
        )
    }
    
    /**
     * 创建测试用的UserInfo实例
     */
    fun createUserInfo(
        id: Long = 1L,
        liked: Boolean = false,
        disLiked: Boolean = false,
        lastPlayed: Int? = null,
        playCount: Int? = null,
        skippedCount: Int? = null,
        userRating: Int? = null,
        inCustomPlaylistCount: Int? = null
    ): UserInfo {
        return UserInfo(
            id = id,
            liked = liked,
            disLiked = disLiked,
            lastPlayed = lastPlayed,
            playCount = playCount,
            skippedCount = skippedCount,
            userRating = userRating,
            inCustomPlaylistCount = inCustomPlaylistCount
        )
    }
    
    /**
     * 创建测试用的MusicInfo实例
     */
    fun createMusicInfo(
        music: Music = createMusic(),
        extra: MusicExtra? = null,
        userInfo: UserInfo? = null
    ): MusicInfo {
        return MusicInfo(
            music = music,
            extra = extra,
            userInfo = userInfo
        )
    }
    
    /**
     * 创建测试用的MusicLabel实例
     */
    fun createMusicLabel(
        musicId: Long = 1L,
        type: LabelCategory = LabelCategory.GENRE,
        label: LabelName = LabelName.ROCK
    ): MusicLabel {
        return MusicLabel(
            musicId = musicId,
            type = type,
            label = label
        )
    }
    
    /**
     * 创建测试用的Playlist实例
     */
    fun createPlaylist(
        id: Long = 1L,
        name: String = "测试播放列表"
    ): Playlist {
        return Playlist(
            id = id,
            name = name
        )
    }
    
    /**
     * 创建测试用的PlaylistItem实例
     */
    fun createPlaylistItem(
        songUrl: String = "/storage/emulated/0/Music/test.mp3",
        songId: Long = 1L,
        playlistId: Long = 1L
    ): PlaylistItem {
        return PlaylistItem(
            songUrl = songUrl,
            songId = songId,
            playlistId = playlistId
        )
    }
    
    /**
     * 创建测试用的PlaybackHistory实例
     */
    fun createPlaybackHistory(
        id: Long = 0L,
        musicId: Long = 1L,
        playedAt: Long = System.currentTimeMillis(),
        playDuration: Long = 180000L,
        isCompleted: Boolean = false,
        source: String? = null
    ): PlaybackHistory {
        return PlaybackHistory(
            id = id,
            musicId = musicId,
            playedAt = playedAt,
            playDuration = playDuration,
            isCompleted = isCompleted,
            source = source
        )
    }
    
    /**
     * 创建测试用的DailyMusicInfo实例
     */
    fun createDailyMusicInfo(
        genre: List<String> = listOf("流行"),
        mood: List<String> = listOf("快乐"),
        scenario: List<String> = listOf("运动"),
        language: String = "中文",
        era: String = "2024",
        rewards: String = "",
        lyric: String = "",
        singerIntroduce: String = "",
        backgroundIntroduce: String = "",
        description: String = "",
        relevantMusic: String = "",
        errorInfo: String = ""
    ): DailyMusicInfo {
        return DailyMusicInfo(
            genre = genre,
            mood = mood,
            scenario = scenario,
            language = language,
            era = era,
            rewards = rewards,
            lyric = lyric,
            singerIntroduce = singerIntroduce,
            backgroundIntroduce = backgroundIntroduce,
            description = description,
            relevantMusic = relevantMusic,
            errorInfo = errorInfo
        )
    }
    
    /**
     * 创建测试用的ListeningDuration实例
     */
    fun createListeningDuration(
        date: String = "2024-12-29",
        duration: Long = 3600000L, // 1小时
        updatedAt: Long = System.currentTimeMillis()
    ): ListeningDuration {
        return ListeningDuration(
            date = date,
            duration = duration,
            updatedAt = updatedAt
        )
    }
    
    /**
     * 批量创建Music列表
     */
    fun createMusicList(count: Int): List<Music> {
        return (1..count).map { index ->
            createMusic(
                id = index.toLong(),
                title = "歌曲 $index",
                artist = "歌手 ${index % 5 + 1}", // 5个不同的歌手
                album = "专辑 ${index % 3 + 1}", // 3个不同的专辑
                path = "/storage/emulated/0/Music/song_$index.mp3",
                albumArtUri = ""
            )
        }
    }
    
    /**
     * 批量创建MusicInfo列表
     */
    fun createMusicInfoList(count: Int): List<MusicInfo> {
        return (1..count).map { index ->
            createMusicInfo(
                music = createMusic(
                    id = index.toLong(),
                    title = "歌曲 $index",
                    artist = "歌手 ${index % 5 + 1}",
                    album = "专辑 ${index % 3 + 1}",
                    path = "/storage/emulated/0/Music/song_$index.mp3",
                    albumArtUri = ""
                ),
                extra = null,
                userInfo = null
            )
        }
    }
}
