# Hearable Music Player 架构重构方案

## 一、问题分析

### 1.1 当前架构问题

通过代码审查，我们发现项目存在以下严重的架构问题：

#### 1.1.1 Domain 层依赖 Data 层实现

**核心问题**：`core-domain` 模块中的 UseCase 直接引用 `core-data` 中的具体类和数据库模型。

```kotlin
// GetAllMusicUseCase.kt - 违反依赖倒置原则
class GetAllMusicUseCase @Inject constructor(
    private val musicRepository: MusicRepository  // 具体实现类
) {
    suspend operator fun invoke(...): List<MusicInfo> { ... }  // 数据库模型
}
```

**影响范围**：
- 所有 UseCase（约 12 个）均存在此问题
- 涉及 `MusicRepository`、`MusicInfo`、`Music`、`MusicExtra`、`UserInfo` 等数据模型

#### 1.1.2 数据库模型定义位置错误

**当前状态**：所有数据模型定义在 `core-data/src/main/java/com/example/hearablemusicplayer/data/database/`

| 模型类 | 位置 | 问题 |
|--------|------|------|
| `Music` | data/database | 应在 domain 层定义 |
| `MusicExtra` | data/database | 应在 domain 层定义 |
| `UserInfo` | data/database | 应在 domain 层定义 |
| `MusicInfo` | data/database | 应在 domain 层定义 |
| `Playlist` | data/database | 应在 domain 层定义 |
| `PlaylistItem` | data/database | 应在 domain 层定义 |

#### 1.1.3 类型重复定义

```kotlin
// core-domain/domain/model/AiProviderType.kt
typealias AiProviderType = com.example.hearablemusicplayer.data.model.AiProviderType
```

这种 typealias 方式只是权宜之计，domain 层仍然强依赖 data 层的具体实现。

### 1.2 问题根因

1. **项目演进过程中**：先实现了数据层，后续添加 domain 层时为省事直接复用 data 层的模型
2. **职责边界不清**：未严格遵守 Clean Architecture 的模块边界
3. **时间压力**：快速迭代导致技术债务积累

### 1.3 架构目标

根据 Clean Architecture 原则：

```
┌─────────────────────────────────────────────────────────────┐
│                      Presentation Layer                      │
│                    (feature-ui, app modules)                 │
├─────────────────────────────────────────────────────────────┤
│                         Domain Layer                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │   Entities   │  │   Use Cases  │  │ Repository Interfaces│
│  └──────────────┘  └──────────────┘  └──────────────────┘   │
├─────────────────────────────────────────────────────────────┤
│                         Data Layer                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │   Models     │  │  Repositories │  │   Data Sources   │  │
│  │  (Database)  │  │ (Implementations)│  │(DAO, API, etc) │   │
│  └──────────────┘  └──────────────┘  └──────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

**核心原则**：
- **内层不依赖外层**：domain 层不依赖 data 层
- **外层依赖内层**：data 层实现 domain 层的接口
- **依赖方向**：代码依赖指向内层（稳定层）

---

## 二、重构方案

### 2.1 总体策略

采用**渐进式重构**，分三个阶段完成：

1. **阶段一**：创建 domain 实体和接口定义
2. **阶段二**：实现数据层的适配器（Mapper）
3. **阶段三**：更新 UseCase 依赖并移除直接引用

### 2.2 阶段一：定义 Domain 实体和接口

#### 2.2.1 创建 Domain 实体

**目标位置**：`core-domain/src/main/java/com/example/hearablemusicplayer/domain/model/`

**Music 实体**：

```kotlin
// core-domain/src/main/java/com/example/hearablemusicplayer/domain/model/Music.kt
package com.example.hearablemusicplayer.domain.model

/**
 * 领域实体：音乐
 * 代表音乐的核心属性，独立于任何数据存储方式
 */
data class Music(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val path: String,
    val albumArtUri: String,
)
```

**MusicExtra 实体**：

```kotlin
// core-domain/src/main/java/com/example/hearablemusicplayer/domain/model/MusicExtra.kt
package com.example.hearablemusicplayer.domain.model

/**
 * 领域实体：音乐额外信息
 * 包含歌词、比特率、采样率等扩展信息
 */
data class MusicExtra(
    val id: Long,
    val lyrics: String? = null,
    val bitRate: Int? = null,
    val sampleRate: Int? = null,
    val fileSize: Long? = null,
    val format: String? = null,
    val language: String? = null,
    val year: Int? = null,
    val recommendationIds: String? = null,
    val isGetExtraInfo: Boolean = false,
    val rewards: String? = null,
    val popLyric: String? = null,
    val singerIntroduce: String? = null,
    val backgroundIntroduce: String? = null,
    val description: String? = null,
    val relevantMusic: String? = null,
)
```

**UserInfo 实体**：

```kotlin
// core-domain/src/main/java/com/example/hearablemusicplayer/domain/model/UserInfo.kt
package com.example.hearablemusicplayer.domain.model

/**
 * 领域实体：用户音乐信息
 * 记录用户对特定音乐的交互状态
 */
data class UserInfo(
    val id: Long,
    val liked: Boolean = false,
    val disLiked: Boolean = false,
    val lastPlayed: Int? = null,
    val playCount: Int? = null,
    val skippedCount: Int? = null,
    val userRating: Int? = null,
    val inCustomPlaylistCount: Int? = null,
)
```

**MusicInfo 聚合实体**：

```kotlin
// core-domain/src/main/java/com/example/hearablemusicplayer/domain/model/MusicInfo.kt
package com.example.hearablemusicplayer.domain.model

/**
 * 领域实体：完整音乐信息
 * 聚合 Music、MusicExtra、UserInfo 的组合值对象
 */
data class MusicInfo(
    val music: Music,
    val extra: MusicExtra? = null,
    val userInfo: UserInfo? = null,
)
```

**Playlist 实体**：

```kotlin
// core-domain/src/main/java/com/example/hearablemusicplayer/domain/model/Playlist.kt
package com.example.hearablemusicplayer.domain.model

/**
 * 领域实体：播放列表
 */
data class Playlist(
    val id: Long = 0,
    val name: String,
)
```

**PlaylistItem 实体**：

```kotlin
// core-domain/src/main/java/com/example/hearablemusicplayer/domain/model/PlaylistItem.kt
package com.example.hearablemusicplayer.domain.model

/**
 * 领域实体：播放列表项
 */
data class PlaylistItem(
    val songUrl: String,
    val songId: Long,
    val playlistId: Long,
)
```

#### 2.2.2 定义 Repository 接口

**目标位置**：`core-domain/src/main/java/com/example/hearablemusicplayer/domain/repository/`

**IMusicRepository 接口**：

```kotlin
// core-domain/src/main/java/com/example/hearablemusicplayer/domain/repository/IMusicRepository.kt
package com.example.hearablemusicplayer.domain.repository

import com.example.hearablemusicplayer.domain.model.MusicInfo
import com.example.hearablemusicplayer.domain.model.ScanProgress
import kotlinx.coroutines.flow.Flow

/**
 * 音乐仓储接口 - 领域层定义
 * 定义音乐相关的数据操作契约，由数据层实现
 */
interface IMusicRepository {
    
    // 音乐扫描
    suspend fun loadMusicFromDevice(): Result<Unit>
    fun isScanning(): Flow<Boolean>
    fun getScanProgress(): Flow<ScanProgress>
    
    // 音乐查询
    suspend fun getAllMusic(orderBy: String = "title", orderType: String = "ASC"): List<MusicInfo>
    fun getMusicCount(): Flow<Int>
    fun getMusicWithExtraCount(): Flow<Int>
    fun getMusicWithMissingExtraCount(): Flow<Int>
    suspend fun getMusicById(id: Long): MusicInfo?
    suspend fun searchMusic(query: String): List<MusicInfo>
    suspend fun getMusicListByArtist(artistName: String): List<MusicInfo>
    
    // 用户交互
    suspend fun updateLikedStatus(id: Long, liked: Boolean)
    suspend fun getLikedStatus(id: Long): Boolean
    
    // 标签管理
    suspend fun getMusicLabels(musicId: Long): List<com.example.hearablemusicplayer.domain.model.MusicLabel>
    suspend fun addMusicLabel(label: com.example.hearablemusicplayer.domain.model.MusicLabel)
    
    // 歌词与额外信息
    suspend fun getMusicLyrics(musicId: Long): String?
    suspend fun getMusicExtraInfo(musicId: Long): com.example.hearablemusicplayer.domain.model.MusicExtra?
}
```

**IPlaylistRepository 接口**：

```kotlin
// core-domain/src/main/java/com/example/hearablemusicplayer/domain/repository/IPlaylistRepository.kt
package com.example.hearablemusicplayer.domain.repository

import com.example.hearablemusicplayer.domain.model.MusicInfo
import com.example.hearablemusicplayer.domain.model.Playlist
import kotlinx.coroutines.flow.Flow

/**
 * 播放列表仓储接口 - 领域层定义
 */
interface IPlaylistRepository {
    suspend fun createPlaylist(name: String): Long
    suspend fun removePlaylist(name: String)
    suspend fun getPlaylistById(playlistId: Long): List<MusicInfo>
    fun getMusicInfoInPlaylist(playlistId: Long): Flow<List<MusicInfo>>
    suspend fun addToPlaylist(playlistId: Long, musicId: Long, musicPath: String)
    suspend fun removeItemFromPlaylist(musicId: Long, playlistId: Long)
    suspend fun resetPlaylistItems(playlistId: Long, playlist: List<MusicInfo>)
}
```

**ISettingsRepository 接口**：

```kotlin
// core-domain/src/main/java/com/example/hearablemusicplayer/domain/repository/ISettingsRepository.kt
package com.example.hearablemusicplayer.domain.repository

import com.example.hearablemusicplayer.domain.model.AiProviderConfig

/**
 * 设置仓储接口 - 领域层定义
 */
interface ISettingsRepository {
    fun getAiProviderConfig(): AiProviderConfig
    fun saveAiProviderConfig(config: AiProviderConfig): Boolean
    fun clearAiProviderConfig(): Boolean
    // 其他设置项...
}
```

#### 2.2.3 定义领域相关类型

**ScanProgress**：

```kotlin
// core-domain/src/main/java/com/example/hearablemusicplayer/domain/model/ScanProgress.kt
package com.example.hearablemusicplayer.domain.model

/**
 * 扫描进度状态
 */
data class ScanProgress(
    val currentCount: Int = 0,
    val totalCount: Int = 0,
    val isScanning: Boolean = false,
    val error: String? = null,
)
```

**MusicLabel**（从 data 层迁移）：

```kotlin
// core-domain/src/main/java/com/example/hearablemusicplayer/domain/model/MusicLabel.kt
package com.example.hearablemusicplayer.domain.model

import com.example.hearablemusicplayer.data.database.myenum.LabelCategory
import com.example.hearablemusicplayer.data.database.myenum.LabelName

/**
 * 领域实体：音乐标签
 */
data class MusicLabel(
    val id: Long = 0,
    val musicId: Long,
    val label: LabelName,
    val labelType: LabelCategory,
)
```

**DailyMusicInfo**（从 data 层迁移）：

```kotlin
// core-domain/src/main/java/com/example/hearablemusicplayer/domain/model/DailyMusicInfo.kt
package com.example.hearablemusicplayer.domain.model

/**
 * 领域实体：每日音乐推荐信息
 * 包含 AI 分析生成的音乐元数据
 */
data class DailyMusicInfo(
    val genre: List<String>,
    val mood: List<String>,
    val scenario: List<String>,
    val language: String,
    val era: String,
    val rewards: String,
    val lyric: String,
    val singerIntroduce: String,
    val backgroundIntroduce: String,
    val description: String,
    val relevantMusic: String,
    val errorInfo: String,
)
```

---

### 2.3 阶段二：实现数据层适配器

#### 2.3.1 创建数据模型映射器

**目标位置**：`core-data/src/main/java/com/example/hearablemusicplayer/data/mapper/`

**MusicMapper**：

```kotlin
// core-data/src/main/java/com/example/hearablemusicplayer/data/mapper/MusicMapper.kt
package com.example.hearablemusicplayer.data.mapper

import com.example.hearablemusicplayer.data.database.Music as DataMusic
import com.example.hearablemusicplayer.data.database.MusicExtra as DataMusicExtra
import com.example.hearablemusicplayer.data.database.UserInfo as DataUserInfo
import com.example.hearablemusicplayer.data.database.MusicInfo as DataMusicInfo
import com.example.hearablemusicplayer.domain.model.Music
import com.example.hearablemusicplayer.domain.model.MusicExtra
import com.example.hearablemusicplayer.domain.model.UserInfo
import com.example.hearablemusicplayer.domain.model.MusicInfo

/**
 * 数据模型与领域模型映射器
 */
object MusicMapper {
    
    fun DataMusic.toDomain(): Music = Music(
        id = id,
        title = title,
        artist = artist,
        album = album,
        duration = duration,
        path = path,
        albumArtUri = albumArtUri,
    )
    
    fun Music.toData(): DataMusic = DataMusic(
        id = id,
        title = title,
        artist = artist,
        album = album,
        duration = duration,
        path = path,
        albumArtUri = albumArtUri,
    )
    
    fun DataMusicExtra.toDomain(): MusicExtra = MusicExtra(
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
        relevantMusic = relevantMusic,
    )
    
    fun MusicExtra.toData(): DataMusicExtra = DataMusicExtra(
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
        relevantMusic = relevantMusic,
    )
    
    fun DataUserInfo.toDomain(): UserInfo = UserInfo(
        id = id,
        liked = liked,
        disLiked = disLiked,
        lastPlayed = lastPlayed,
        playCount = playCount,
        skippedCount = skippedCount,
        userRating = userRating,
        inCustomPlaylistCount = inCustomPlaylistCount,
    )
    
    fun UserInfo.toData(): DataUserInfo = DataUserInfo(
        id = id,
        liked = liked,
        disLiked = disLiked,
        lastPlayed = lastPlayed,
        playCount = playCount,
        skippedCount = skippedCount,
        userRating = userRating,
        inCustomPlaylistCount = inCustomPlaylistCount,
    )
    
    fun DataMusicInfo.toDomain(): MusicInfo = MusicInfo(
        music = music.toDomain(),
        extra = extra?.toDomain(),
        userInfo = userInfo?.toDomain(),
    )
    
    fun List<DataMusicInfo>.toDomainList(): List<MusicInfo> = map { it.toDomain() }
}
```

**PlaylistMapper**：

```kotlin
// core-data/src/main/java/com/example/hearablemusicplayer/data/mapper/PlaylistMapper.kt
package com.example.hearablemusicplayer.data.mapper

import com.example.hearablemusicplayer.data.database.Playlist as DataPlaylist
import com.example.hearablemusicplayer.data.database.PlaylistItem as DataPlaylistItem
import com.example.hearablemusicplayer.domain.model.Playlist
import com.example.hearablemusicplayer.domain.model.PlaylistItem

object PlaylistMapper {
    
    fun DataPlaylist.toDomain(): Playlist = Playlist(
        id = id,
        name = name,
    )
    
    fun Playlist.toData(): DataPlaylist = DataPlaylist(
        id = id,
        name = name,
    )
    
    fun DataPlaylistItem.toDomain(): PlaylistItem = PlaylistItem(
        songUrl = songUrl,
        songId = songId,
        playlistId = playlistId,
    )
    
    fun PlaylistItem.toData(): DataPlaylistItem = DataPlaylistItem(
        songUrl = songUrl,
        songId = songId,
        playlistId = playlistId,
    )
}
```

#### 2.3.2 重构 Repository 实现

**更新 MusicRepository**：

```kotlin
// core-data/src/main/java/com/example/hearablemusicplayer/data/repository/MusicRepository.kt
package com.example.hearablemusicplayer.data.repository

import com.example.hearablemusicplayer.domain.model.MusicInfo
import com.example.hearablemusicplayer.domain.model.ScanProgress
import com.example.hearablemusicplayer.domain.repository.IMusicRepository
import com.example.hearablemusicplayer.data.mapper.MusicMapper.toData
import com.example.hearablemusicplayer.data.mapper.MusicMapper.toDomain
import com.example.hearablemusicplayer.data.mapper.MusicMapper.toDomainList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor(
    // 注入 DAO 等数据源...
) : IMusicRepository {

    private val _scanProgress = ScanProgress()
    
    override suspend fun loadMusicFromDevice(): Result<Unit> {
        // 实现逻辑，调用数据源
        return Result.Success(Unit)
    }
    
    override fun isScanning(): Flow<Boolean> = _isScanning.map { it }
    
    override fun getScanProgress(): Flow<ScanProgress> = _scanProgressFlow
    
    override suspend fun getAllMusic(orderBy: String, orderType: String): List<MusicInfo> {
        return musicAllDao.getAllMusicInfoAsList(...).toDomainList()
    }
    
    override fun getMusicCount(): Flow<Int> = musicDao.getMusicCount()
    
    override suspend fun getMusicById(id: Long): MusicInfo? {
        return musicAllDao.getMusicInfoById(id).toDomain()
    }
    
    override suspend fun searchMusic(query: String): List<MusicInfo> {
        return musicAllDao.searchMusic("%$query%").toDomainList()
    }
    
    // ... 其他方法实现
}
```

#### 2.3.3 更新 Hilt 模块

```kotlin
// core-data/src/main/java/com/example/hearablemusicplayer/data/di/RepositoryModule.kt
package com.example.hearablemusicplayer.data.di

import com.example.hearablemusicplayer.data.repository.MusicRepository
import com.example.hearablemusicplayer.data.repository.PlaylistRepository
import com.example.hearablemusicplayer.data.repository.SettingsRepository
import com.example.hearablemusicplayer.domain.repository.IMusicRepository
import com.example.hearablemusicplayer.domain.repository.IPlaylistRepository
import com.example.hearablemusicplayer.domain.repository.ISettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindIMusicRepository(
        musicRepository: MusicRepository
    ): IMusicRepository

    @Binds
    @Singleton
    abstract fun bindIPlaylistRepository(
        playlistRepository: PlaylistRepository
    ): IPlaylistRepository

    @Binds
    @Singleton
    abstract fun bindISettingsRepository(
        settingsRepository: SettingsRepository
    ): ISettingsRepository
}
```

---

### 2.4 阶段三：更新 UseCase 依赖

#### 2.4.1 更新 GetAllMusicUseCase

```kotlin
// core-domain/src/main/java/com/example/hearablemusicplayer/domain/usecase/music/GetAllMusicUseCase.kt
package com.example.hearablemusicplayer.domain.usecase.music

import com.example.hearablemusicplayer.domain.model.MusicInfo
import com.example.hearablemusicplayer.domain.repository.IMusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 获取所有音乐 - Use Case
 * 依赖接口而非具体实现
 */
class GetAllMusicUseCase @Inject constructor(
    private val musicRepository: IMusicRepository  // 接口依赖
) {
    suspend operator fun invoke(
        orderBy: String = "title", 
        orderType: String = "ASC"
    ): List<MusicInfo> {
        return musicRepository.getAllMusic(orderBy, orderType)
    }
    
    fun getMusicCount(): Flow<Int> = musicRepository.getMusicCount()
    
    fun getMusicWithExtraCount(): Flow<Int> = musicRepository.getMusicWithExtraCount()
    
    fun getMusicWithMissingExtraCount(): Flow<Int> = musicRepository.getMusicWithMissingExtraCount()
    
    suspend fun getMusicListByArtist(artistName: String): List<MusicInfo> {
        return musicRepository.getMusicListByArtist(artistName)
    }
    
    suspend fun getMusicById(musicId: Long): MusicInfo? {
        return musicRepository.getMusicById(musicId)
    }
}
```

#### 2.4.2 更新 ManagePlaylistUseCase

```kotlin
// core-domain/src/main/java/com/example/hearablemusicplayer/domain/usecase/playlist/ManagePlaylistUseCase.kt
package com.example.hearablemusicplayer.domain.usecase.playlist

import com.example.hearablemusicplayer.domain.model.MusicInfo
import com.example.hearablemusicplayer.domain.repository.IPlaylistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 播放列表管理 - Use Case
 */
class ManagePlaylistUseCase @Inject constructor(
    private val playlistRepository: IPlaylistRepository  // 接口依赖
) {
    suspend fun createPlaylist(name: String): Long {
        return playlistRepository.createPlaylist(name)
    }
    
    suspend fun removePlaylist(name: String) {
        playlistRepository.removePlaylist(name)
    }
    
    suspend fun getPlaylistById(playlistId: Long): List<MusicInfo> {
        return playlistRepository.getPlaylistById(playlistId)
    }
    
    fun getMusicInfoInPlaylist(playlistId: Long): Flow<List<MusicInfo>> {
        return playlistRepository.getMusicInfoInPlaylist(playlistId)
    }
    
    suspend fun addToPlaylist(playlistId: Long, musicId: Long, musicPath: String) {
        playlistRepository.addToPlaylist(playlistId, musicId, musicPath)
    }
    
    suspend fun removeItemFromPlaylist(musicId: Long, playlistId: Long) {
        playlistRepository.removeItemFromPlaylist(musicId, playlistId)
    }
    
    suspend fun resetPlaylistItems(playlistId: Long, playlist: List<MusicInfo>) {
        playlistRepository.resetPlaylistItems(playlistId, playlist)
    }
}
```

---

## 三、实施步骤

### 3.1 阶段一：创建领域层定义（约 4 小时）

| 步骤 | 任务 | 文件路径 | 预估时间 |
|------|------|----------|----------|
| 1.1 | 创建 Music 实体 | `domain/model/Music.kt` | 15min |
| 1.2 | 创建 MusicExtra 实体 | `domain/model/MusicExtra.kt` | 15min |
| 1.3 | 创建 UserInfo 实体 | `domain/model/UserInfo.kt` | 10min |
| 1.4 | 创建 MusicInfo 实体 | `domain/model/MusicInfo.kt` | 10min |
| 1.5 | 创建 Playlist 实体 | `domain/model/Playlist.kt` | 10min |
| 1.6 | 创建 PlaylistItem 实体 | `domain/model/PlaylistItem.kt` | 10min |
| 1.7 | 创建 ScanProgress | `domain/model/ScanProgress.kt` | 10min |
| 1.8 | 创建 MusicLabel | `domain/model/MusicLabel.kt` | 15min |
| 1.9 | 创建 DailyMusicInfo | `domain/model/DailyMusicInfo.kt` | 15min |
| 1.10 | 创建 IMusicRepository | `domain/repository/IMusicRepository.kt` | 30min |
| 1.11 | 创建 IPlaylistRepository | `domain/repository/IPlaylistRepository.kt` | 20min |
| 1.12 | 创建 ISettingsRepository | `domain/repository/ISettingsRepository.kt` | 20min |

### 3.2 阶段二：实现数据层适配器（约 4 小时）

| 步骤 | 任务 | 文件路径 | 预估时间 |
|------|------|----------|----------|
| 2.1 | 创建 MusicMapper | `data/mapper/MusicMapper.kt` | 30min |
| 2.2 | 创建 PlaylistMapper | `data/mapper/PlaylistMapper.kt` | 20min |
| 2.3 | 重构 MusicRepository | `data/repository/MusicRepository.kt` | 60min |
| 2.4 | 重构 PlaylistRepository | `data/repository/PlaylistRepository.kt` | 40min |
| 2.5 | 更新 RepositoryModule | `data/di/RepositoryModule.kt` | 20min |
| 2.6 | 移除 AiProviderType typealias | `domain/model/AiProviderType.kt` | 10min |

### 3.3 阶段三：更新 UseCase（约 2 小时）

| 步骤 | 任务 | 文件路径 | 预估时间 |
|------|------|----------|----------|
| 3.1 | 更新 GetAllMusicUseCase | `domain/usecase/music/GetAllMusicUseCase.kt` | 15min |
| 3.2 | 更新 ManagePlaylistUseCase | `domain/usecase/playlist/ManagePlaylistUseCase.kt` | 15min |
| 3.3 | 更新其他 UseCase（~10个） | 各 UseCase 文件 | 90min |

### 3.4 测试验证（约 2 小时）

| 步骤 | 任务 | 描述 |
|------|------|------|
| 4.1 | 编译检查 | `./gradlew build` |
| 4.2 | 单元测试 | 运行 domain 层测试 |
| 4.3 | 集成测试 | 验证数据层映射正确性 |
| 4.4 | UI 测试 | 验证功能完整性 |

---

## 四、验收标准

### 4.1 架构合规性

- [ ] domain 层不包含任何 data 层的 import 语句
- [ ] 所有 UseCase 依赖 Repository 接口而非实现
- [ ] data 层通过 Hilt 模块绑定接口与实现

### 4.2 代码质量

- [ ] 所有新增文件通过静态分析
- [ ] 代码注释覆盖率 > 80%
- [ ] 无重复代码（工具检测 < 3%）

### 4.3 功能完整性

- [ ] 现有功能测试用例 100% 通过
- [ ] 音乐扫描功能正常
- [ ] 播放列表 CRUD 正常
- [ ] 搜索功能正常
- [ ] 标签功能正常

### 4.4 可测试性

- [ ] 可以使用 Mock 替换 Repository 实现
- [ ] UseCase 单元测试无需启动 Android 框架
- [ ] Mapper 可独立测试

---

## 五、风险与应对

### 5.1 风险识别

| 风险 | 可能性 | 影响 | 应对措施 |
|------|--------|------|----------|
| 回归问题 | 高 | 中 | 完整的功能测试覆盖 |
| 性能下降 | 低 | 中 | 映射层开销极小，可优化 |
| 编译时间增加 | 低 | 低 | 合理的模块划分 |
| 开发周期延长 | 中 | 中 | 分阶段实施，保留回滚能力 |

### 5.2 回滚方案

1. 保留原 `MusicRepository` 类
2. 通过 typealias 保持临时兼容
3. 随时可以切换回原实现

---

## 六、收益预期

### 6.1 短期收益

- **代码可测试性**：UseCase 可独立测试，无需 Mock 数据库
- **模块解耦**：数据层实现可替换（如 Room → SQLDelight）
- **团队协作**：前后端可并行开发，基于接口约定

### 6.2 长期收益

- **技术债减少**：遵循 Clean Architecture 原则
- **扩展性提升**：新增数据源（如网络API）不影响业务逻辑
- **维护成本降低**：职责清晰，定位问题快

---

## 七、附录

### 7.1 文件变更清单

```
新增文件:
├── core-domain/src/main/java/com/example/hearablemusicplayer/domain/
│   ├── model/
│   │   ├── Music.kt
│   │   ├── MusicExtra.kt
│   │   ├── UserInfo.kt
│   │   ├── MusicInfo.kt
│   │   ├── Playlist.kt
│   │   ├── PlaylistItem.kt
│   │   ├── ScanProgress.kt
│   │   ├── MusicLabel.kt
│   │   └── DailyMusicInfo.kt
│   └── repository/
│       ├── IMusicRepository.kt
│       ├── IPlaylistRepository.kt
│       └── ISettingsRepository.kt
└── core-data/src/main/java/com/example/hearablemusicplayer/data/
    └── mapper/
        ├── MusicMapper.kt
        └── PlaylistMapper.kt

修改文件:
├── core-domain/src/main/java/.../domain/usecase/music/*.kt
├── core-domain/src/main/java/.../domain/usecase/playlist/*.kt
├── core-data/src/main/java/.../data/repository/*.kt
└── core-data/src/main/java/.../data/di/RepositoryModule.kt
```

### 7.2 依赖关系变更

**变更前**：
```
UseCase → MusicRepository → MusicDao → Music (data)
UseCase → MusicInfo (data)
```

**变更后**：
```
UseCase → IMusicRepository → MusicRepository → Mapper → Music (data)
                                       ↓
UseCase → MusicInfo (domain) ← Mapper ← MusicInfo (data)
```
