# P2: core-data 迁移 — Room 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 Room 数据库相关文件迁移到 shared 模块，配置 KMP 模式，确保跨平台兼容性。

**Architecture:** 使用 Room 2.8+ 的 KMP 模式，通过 `@ConstructedBy` + `expect/actual` 实现平台特定的数据库构建，使用 `BundledSQLiteDriver` 保证跨平台一致性。

**Tech Stack:** Kotlin Multiplatform, Room 2.8+, SQLite, Koin

---

## 文件结构

### 创建/修改的文件

**共享层文件:**
- `shared/src/commonMain/kotlin/com/hmp/data/database/AppDatabase.kt` — 主数据库定义（KMP 模式）
- `shared/src/commonMain/kotlin/com/hmp/data/database/Music.kt` — 音乐相关 Entity + DAO
- `shared/src/commonMain/kotlin/com/hmp/data/database/PlayList.kt` — 播放列表 Entity + DAO
- `shared/src/commonMain/kotlin/com/hmp/data/database/PlaylistItem.kt` — 播放列表项 Entity + DAO
- `shared/src/commonMain/kotlin/com/hmp/data/database/MusicLabel.kt` — 音乐标签 Entity + DAO
- `shared/src/commonMain/kotlin/com/hmp/data/database/ListeningDuration.kt` — 听歌时长 Entity + DAO
- `shared/src/commonMain/kotlin/com/hmp/data/database/PlaybackHistory.kt` — 播放历史 Entity + DAO
- `shared/src/commonMain/kotlin/com/hmp/data/database/DailyMusicInfo.kt` — 非 Entity 数据类
- `shared/src/commonMain/kotlin/com/hmp/data/database/myenum/Label.kt` — 标签枚举 + TypeConverter
- `shared/src/commonMain/kotlin/com/hmp/data/database/myenum/PlaybackMode.kt` — 播放模式枚举
- `shared/src/commonMain/kotlin/com/hmp/data/database/Database.kt` — 通用数据库构建逻辑

**Android 平台文件:**
- `shared/src/androidMain/kotlin/com/hmp/data/database/DatabaseBuilder.android.kt` — Android 平台数据库构建器

**iOS 平台文件:**
- `shared/src/iosMain/kotlin/com/hmp/data/database/DatabaseBuilder.ios.kt` — iOS 平台数据库构建器

---

## 任务分解

### Task 1: 准备共享层 database 目录

**Files:**
- Create: `shared/src/commonMain/kotlin/com/hmp/data/database/`
- Create: `shared/src/commonMain/kotlin/com/hmp/data/database/myenum/`
- Create: `shared/src/androidMain/kotlin/com/hmp/data/database/`
- Create: `shared/src/iosMain/kotlin/com/hmp/data/database/`

- [ ] **Step 1: 创建目录结构**

```bash
mkdir -p shared/src/commonMain/kotlin/com/hmp/data/database/myenum
mkdir -p shared/src/androidMain/kotlin/com/hmp/data/database
mkdir -p shared/src/iosMain/kotlin/com/hmp/data/database
```

- [ ] **Step 2: 验证目录创建**

```bash
ls -la shared/src/commonMain/kotlin/com/hmp/data/
ls -la shared/src/androidMain/kotlin/com/hmp/data/
ls -la shared/src/iosMain/kotlin/com/hmp/data/
```

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/com/hmp/data/
git add shared/src/androidMain/kotlin/com/hmp/data/
git add shared/src/iosMain/kotlin/com/hmp/data/
git commit -m "feat: create database directory structure for P2"
```

### Task 2: 迁移 Label 枚举和 TypeConverter

**Files:**
- Create: `shared/src/commonMain/kotlin/com/hmp/data/database/myenum/Label.kt`
- Modify: `android/core-data/src/main/java/com/example/hearablemusicplayer/data/database/myenum/Label.kt`

- [ ] **Step 1: 复制 Label.kt 到共享层**

```kotlin
// shared/src/commonMain/kotlin/com/hmp/data/database/myenum/Label.kt
package com.hmp.data.database.myenum

import androidx.room.TypeConverter

enum class LabelCategory {
    EMOTION,
    SCENE,
    GENRE,
    LANGUAGE,
    DECADE
}

enum class LabelName {
    // EMOTION
    HAPPY,
    SAD,
    CALM,
    ENERGETIC,
    ROMANTIC,
    MELANCHOLY,
    LONELY,
    HOPEFUL,
    MYSTERIOUS,
    UPLIFTING,
    ANGRY,
    RELAX,
    // SCENE
    MORNING,
    NIGHT,
    PARTY,
    WORKOUT,
    STUDY,
    FOCUS,
    DRIVING,
    DINNER,
    TRAVEL,
    MEDITATION,
    SLEEP,
    // GENRE
    POP,
    ROCK,
    HIPHOP,
    JAZZ,
    CLASSICAL,
    ELECTRONIC,
    RNB,
    COUNTRY,
    FOLK,
    BLUES,
    METAL,
    PUNK,
    REGGAE,
    SOUL,
    INDIE,
    FUNK,
    // LANGUAGE
    ENGLISH,
    CHINESE,
    JAPANESE,
    KOREAN,
    SPANISH,
    FRENCH,
    GERMAN,
    ITALIAN,
    RUSSIAN,
    ARABIC,
    HINDI,
    // DECADE
    SIXTIES,
    SEVENTIES,
    EIGHTIES,
    NINETIES,
    TWO_THOUSANDS,
    TWENTY_TENS,
    TWENTY_TWENTIES
}

class LabelConverters {
    @TypeConverter
    fun fromLabelCategory(category: LabelCategory?): String? {
        return category?.name
    }

    @TypeConverter
    fun toLabelCategory(name: String?): LabelCategory? {
        return name?.let { LabelCategory.valueOf(it) }
    }

    @TypeConverter
    fun fromLabelName(name: LabelName?): String? {
        return name?.name
    }

    @TypeConverter
    fun toLabelName(name: String?): LabelName? {
        return name?.let { LabelName.valueOf(it) }
    }
}
```

- [ ] **Step 2: 复制 PlaybackMode.kt 到共享层**

```kotlin
// shared/src/commonMain/kotlin/com/hmp/data/database/myenum/PlaybackMode.kt
package com.hmp.data.database.myenum

enum class PlaybackMode {
    ORDER,
    REPEAT_ALL,
    REPEAT_ONE,
    SHUFFLE
}
```

- [ ] **Step 3: 验证文件创建**

```bash
ls -la shared/src/commonMain/kotlin/com/hmp/data/database/myenum/
```

- [ ] **Step 4: Commit**

```bash
git add shared/src/commonMain/kotlin/com/hmp/data/database/myenum/
git commit -m "feat: migrate label enums and type converters to shared"
```

### Task 3: 迁移 Music 相关 Entity 和 DAO

**Files:**
- Create: `shared/src/commonMain/kotlin/com/hmp/data/database/Music.kt`
- Modify: `android/core-data/src/main/java/com/example/hearablemusicplayer/data/database/Music.kt`

- [ ] **Step 1: 复制并修改 Music.kt**

```kotlin
// shared/src/commonMain/kotlin/com/hmp/data/database/Music.kt
package com.hmp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "music")
data class Music(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val path: String,
    val size: Long,
    val dateAdded: Long,
    val dateModified: Long,
    val isDeleted: Boolean = false
)

@Entity(tableName = "music_extra")
data class MusicExtra(
    @PrimaryKey
    val musicId: Int,
    val bitrate: Int?,
    val sampleRate: Int?,
    val lyrics: String?,
    val coverArtPath: String?,
    val sortKey: String?
)

@Entity(tableName = "user_info")
data class UserInfo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val key: String,
    val value: String
)

@Dao
interface MusicDao {
    @Query("SELECT * FROM music WHERE isDeleted = 0 ORDER BY title")
    fun getAllMusic(): Flow<List<Music>>

    @Query("SELECT * FROM music WHERE id = :id")
    suspend fun getMusicById(id: Int): Music?

    @Insert
    suspend fun insertMusic(music: Music): Long

    @Update
    suspend fun updateMusic(music: Music)

    @Delete
    suspend fun deleteMusic(music: Music)

    @Query("UPDATE music SET isDeleted = 1 WHERE id = :id")
    suspend fun markAsDeleted(id: Int)

    @Query("UPDATE music SET isDeleted = 0 WHERE id = :id")
    suspend fun restoreFromDeleted(id: Int)
}

@Dao
interface MusicExtraDao {
    @Query("SELECT * FROM music_extra WHERE musicId = :musicId")
    suspend fun getMusicExtraById(musicId: Int): MusicExtra?

    @Insert
    suspend fun insertMusicExtra(musicExtra: MusicExtra)

    @Update
    suspend fun updateMusicExtra(musicExtra: MusicExtra)

    @Delete
    suspend fun deleteMusicExtra(musicExtra: MusicExtra)
}

@Dao
interface UserInfoDao {
    @Query("SELECT value FROM user_info WHERE key = :key")
    suspend fun getValue(key: String): String?

    @Insert
    suspend fun insertUserInfo(userInfo: UserInfo)

    @Update
    suspend fun updateUserInfo(userInfo: UserInfo)

    @Query("DELETE FROM user_info WHERE key = :key")
    suspend fun deleteUserInfo(key: String)
}

@Dao
interface MusicAllDao {
    @Query("""
        SELECT m.*, me.lyrics, me.coverArtPath 
        FROM music m 
        LEFT JOIN music_extra me ON m.id = me.musicId 
        WHERE m.isDeleted = 0 
        ORDER BY m.title
    """)
    fun getAllMusicInfo(): Flow<List<MusicWithExtra>>

    @Query("""
        SELECT m.*, me.lyrics, me.coverArtPath 
        FROM music m 
        LEFT JOIN music_extra me ON m.id = me.musicId 
        WHERE m.id = :id
    """)
    suspend fun getMusicInfoById(id: Int): MusicWithExtra?

    @Query("""
        SELECT m.*, me.lyrics, me.coverArtPath 
        FROM music m 
        LEFT JOIN music_extra me ON m.id = me.musicId 
        WHERE m.isDeleted = 0 AND m.title LIKE '%' || :keyword || '%' OR m.artist LIKE '%' || :keyword || '%' OR m.album LIKE '%' || :keyword || '%'
        ORDER BY m.title
    """)
    fun searchMusic(keyword: String): Flow<List<MusicWithExtra>>
}

data class MusicWithExtra(
    val id: Int,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val path: String,
    val size: Long,
    val dateAdded: Long,
    val dateModified: Long,
    val isDeleted: Boolean,
    val lyrics: String?,
    val coverArtPath: String?
)
```

- [ ] **Step 2: 验证文件创建**

```bash
ls -la shared/src/commonMain/kotlin/com/hmp/data/database/Music.kt
```

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/com/hmp/data/database/Music.kt
git commit -m "feat: migrate Music entity and DAOs to shared"
```

### Task 4: 迁移 PlayList 和 PlaylistItem

**Files:**
- Create: `shared/src/commonMain/kotlin/com/hmp/data/database/PlayList.kt`
- Create: `shared/src/commonMain/kotlin/com/hmp/data/database/PlaylistItem.kt`
- Modify: `android/core-data/src/main/java/com/example/hearablemusicplayer/data/database/PlayList.kt`
- Modify: `android/core-data/src/main/java/com/example/hearablemusicplayer/data/database/PlaylistItem.kt`

- [ ] **Step 1: 复制并修改 PlayList.kt**

```kotlin
// shared/src/commonMain/kotlin/com/hmp/data/database/PlayList.kt
package com.hmp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "playlist")
data class Playlist(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String?,
    val coverArtPath: String?,
    val createdAt: Long,
    val updatedAt: Long
)

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlist ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Query("SELECT * FROM playlist WHERE id = :id")
    suspend fun getPlaylistById(id: Int): Playlist?

    @Insert
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Update
    suspend fun updatePlaylist(playlist: Playlist)

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)
}
```

- [ ] **Step 2: 复制并修改 PlaylistItem.kt**

```kotlin
// shared/src/commonMain/kotlin/com/hmp/data/database/PlaylistItem.kt
package com.hmp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "playlist_item")
data class PlaylistItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val playlistId: Int,
    val musicId: Int,
    val position: Int,
    val addedAt: Long
)

@Dao
interface PlaylistItemDao {
    @Query("SELECT * FROM playlist_item WHERE playlistId = :playlistId ORDER BY position")
    fun getPlaylistItems(playlistId: Int): Flow<List<PlaylistItem>>

    @Query("SELECT * FROM playlist_item WHERE playlistId = :playlistId ORDER BY position")
    suspend fun getPlaylistItemsList(playlistId: Int): List<PlaylistItem>

    @Insert
    suspend fun insertPlaylistItem(playlistItem: PlaylistItem): Long

    @Delete
    suspend fun deletePlaylistItem(playlistItem: PlaylistItem)

    @Query("DELETE FROM playlist_item WHERE playlistId = :playlistId")
    suspend fun deleteAllPlaylistItems(playlistId: Int)

    @Query("UPDATE playlist_item SET position = position - 1 WHERE playlistId = :playlistId AND position > :position")
    suspend fun updatePositionsAfterDelete(playlistId: Int, position: Int)
}
```

- [ ] **Step 3: 验证文件创建**

```bash
ls -la shared/src/commonMain/kotlin/com/hmp/data/database/PlayList.kt
ls -la shared/src/commonMain/kotlin/com/hmp/data/database/PlaylistItem.kt
```

- [ ] **Step 4: Commit**

```bash
git add shared/src/commonMain/kotlin/com/hmp/data/database/PlayList.kt
git add shared/src/commonMain/kotlin/com/hmp/data/database/PlaylistItem.kt
git commit -m "feat: migrate Playlist and PlaylistItem to shared"
```

### Task 5: 迁移 MusicLabel

**Files:**
- Create: `shared/src/commonMain/kotlin/com/hmp/data/database/MusicLabel.kt`
- Modify: `android/core-data/src/main/java/com/example/hearablemusicplayer/data/database/MusicLabel.kt`

- [ ] **Step 1: 复制并修改 MusicLabel.kt**

```kotlin
// shared/src/commonMain/kotlin/com/hmp/data/database/MusicLabel.kt
package com.hmp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Delete
import com.hmp.data.database.myenum.LabelCategory
import com.hmp.data.database.myenum.LabelName
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "music_label", primaryKeys = ["musicId", "labelName"])
data class MusicLabel(
    val musicId: Int,
    val labelName: String,
    val labelCategory: String,
    val confidence: Float
)

@Dao
interface MusicLabelDao {
    @Query("SELECT * FROM music_label WHERE musicId = :musicId")
    fun getMusicLabels(musicId: Int): Flow<List<MusicLabel>>

    @Query("SELECT * FROM music_label WHERE musicId = :musicId")
    suspend fun getMusicLabelsList(musicId: Int): List<MusicLabel>

    @Insert
    suspend fun insertMusicLabel(musicLabel: MusicLabel)

    @Delete
    suspend fun deleteMusicLabel(musicLabel: MusicLabel)

    @Query("DELETE FROM music_label WHERE musicId = :musicId")
    suspend fun deleteAllMusicLabels(musicId: Int)

    @Query("SELECT DISTINCT labelName FROM music_label WHERE labelCategory = :category")
    suspend fun getLabelsByCategory(category: LabelCategory): List<LabelName>
}
```

- [ ] **Step 2: 验证文件创建**

```bash
ls -la shared/src/commonMain/kotlin/com/hmp/data/database/MusicLabel.kt
```

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/com/hmp/data/database/MusicLabel.kt
git commit -m "feat: migrate MusicLabel to shared"
```

### Task 6: 迁移 ListeningDuration 和 PlaybackHistory

**Files:**
- Create: `shared/src/commonMain/kotlin/com/hmp/data/database/ListeningDuration.kt`
- Create: `shared/src/commonMain/kotlin/com/hmp/data/database/PlaybackHistory.kt`
- Modify: `android/core-data/src/main/java/com/example/hearablemusicplayer/data/database/ListeningDuration.kt`
- Modify: `android/core-data/src/main/java/com/example/hearablemusicplayer/data/database/PlaybackHistory.kt`

- [ ] **Step 1: 复制并修改 ListeningDuration.kt**

```kotlin
// shared/src/commonMain/kotlin/com/hmp/data/database/ListeningDuration.kt
package com.hmp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "listening_duration")
data class ListeningDuration(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val musicId: Int,
    val date: String, // YYYY-MM-DD
    val duration: Long, // 毫秒
    val lastPlayed: Long
)

@Dao
interface ListeningDurationDao {
    @Query("SELECT * FROM listening_duration WHERE musicId = :musicId AND date = :date")
    suspend fun getListeningDuration(musicId: Int, date: String): ListeningDuration?

    @Insert
    suspend fun insertListeningDuration(listeningDuration: ListeningDuration)

    @Query("UPDATE listening_duration SET duration = duration + :duration, lastPlayed = :lastPlayed WHERE id = :id")
    suspend fun updateListeningDuration(id: Int, duration: Long, lastPlayed: Long)

    @Query("SELECT SUM(duration) FROM listening_duration WHERE date = :date")
    suspend fun getTotalListeningDuration(date: String): Long?

    @Query("SELECT * FROM listening_duration WHERE date BETWEEN :startDate AND :endDate ORDER BY duration DESC LIMIT :limit")
    suspend fun getTopListeningMusic(startDate: String, endDate: String, limit: Int): List<ListeningDuration>
}
```

- [ ] **Step 2: 复制并修改 PlaybackHistory.kt**

```kotlin
// shared/src/commonMain/kotlin/com/hmp/data/database/PlaybackHistory.kt
package com.hmp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "playback_history")
data class PlaybackHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val musicId: Int,
    val playedAt: Long,
    val durationPlayed: Long
)

@Dao
interface PlaybackHistoryDao {
    @Query("SELECT * FROM playback_history ORDER BY playedAt DESC LIMIT :limit")
    fun getRecentPlaybackHistory(limit: Int): Flow<List<PlaybackHistory>>

    @Insert
    suspend fun insertPlaybackHistory(playbackHistory: PlaybackHistory)

    @Query("DELETE FROM playback_history WHERE id NOT IN (SELECT id FROM playback_history ORDER BY playedAt DESC LIMIT :keepLimit)")
    suspend fun cleanupOldHistory(keepLimit: Int)
}
```

- [ ] **Step 3: 复制 DailyMusicInfo.kt**

```kotlin
// shared/src/commonMain/kotlin/com/hmp/data/database/DailyMusicInfo.kt
package com.hmp.data.database

data class DailyMusicInfo(
    val date: String,
    val totalDuration: Long,
    val totalSongs: Int,
    val topSong: MusicWithExtra?,
    val mostPlayedGenre: String?
)
```

- [ ] **Step 4: 验证文件创建**

```bash
ls -la shared/src/commonMain/kotlin/com/hmp/data/database/ListeningDuration.kt
ls -la shared/src/commonMain/kotlin/com/hmp/data/database/PlaybackHistory.kt
ls -la shared/src/commonMain/kotlin/com/hmp/data/database/DailyMusicInfo.kt
```

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/com/hmp/data/database/ListeningDuration.kt
git add shared/src/commonMain/kotlin/com/hmp/data/database/PlaybackHistory.kt
git add shared/src/commonMain/kotlin/com/hmp/data/database/DailyMusicInfo.kt
git commit -m "feat: migrate ListeningDuration, PlaybackHistory and DailyMusicInfo to shared"
```

### Task 7: 创建 KMP 模式的 AppDatabase

**Files:**
- Create: `shared/src/commonMain/kotlin/com/hmp/data/database/AppDatabase.kt`
- Modify: `android/core-data/src/main/java/com/example/hearablemusicplayer/data/database/AppDatabase.kt`

- [ ] **Step 1: 创建 KMP 模式的 AppDatabase.kt**

```kotlin
// shared/src/commonMain/kotlin/com/hmp/data/database/AppDatabase.kt
package com.hmp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.ConstructedBy
import com.hmp.data.database.myenum.LabelConverters

@Database(
    entities = [
        Music::class,
        MusicExtra::class,
        UserInfo::class,
        MusicLabel::class,
        Playlist::class,
        PlaylistItem::class,
        PlaybackHistory::class,
        ListeningDuration::class
    ],
    version = 5,
    exportSchema = true
)
@TypeConverters(LabelConverters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao
    abstract fun musicExtraDao(): MusicExtraDao
    abstract fun userInfoDao(): UserInfoDao
    abstract fun musicAllDao(): MusicAllDao
    abstract fun musicLabelDao(): MusicLabelDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playlistItemDao(): PlaylistItemDao
    abstract fun playbackHistoryDao(): PlaybackHistoryDao
    abstract fun listeningDurationDao(): ListeningDurationDao
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
```

- [ ] **Step 2: 验证文件创建**

```bash
ls -la shared/src/commonMain/kotlin/com/hmp/data/database/AppDatabase.kt
```

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/com/hmp/data/database/AppDatabase.kt
git commit -m "feat: create KMP AppDatabase"
```

### Task 8: 创建平台特定的 Database Builder

**Files:**
- Create: `shared/src/androidMain/kotlin/com/hmp/data/database/DatabaseBuilder.android.kt`
- Create: `shared/src/iosMain/kotlin/com/hmp/data/database/DatabaseBuilder.ios.kt`
- Create: `shared/src/commonMain/kotlin/com/hmp/data/database/Database.kt`

- [ ] **Step 1: 创建 Android 平台 Database Builder**

```kotlin
// shared/src/androidMain/kotlin/com/hmp/data/database/DatabaseBuilder.android.kt
package com.hmp.data.database

import android.content.Context
import androidx.room.RoomDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val dbFile = context.getDatabasePath("music_database.db")
    return Room.databaseBuilder<AppDatabase>(
        context = context,
        name = dbFile.absolutePath
    )
}

actual object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase {
        TODO("Not yet implemented")
    }
}
```

- [ ] **Step 2: 创建 iOS 平台 Database Builder**

```kotlin
// shared/src/iosMain/kotlin/com/hmp/data/database/DatabaseBuilder.ios.kt
package com.hmp.data.database

import androidx.room.RoomDatabase
import platform.Foundation.NSFileManager
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSUserDomainMask

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFilePath = documentDirectory() + "/music_database.db"
    return Room.databaseBuilder<AppDatabase>(name = dbFilePath)
}

private fun documentDirectory(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(documentDirectory?.path)
}

actual object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase {
        TODO("Not yet implemented")
    }
}
```

- [ ] **Step 3: 创建通用 Database 构建逻辑**

```kotlin
// shared/src/commonMain/kotlin/com/hmp/data/database/Database.kt
package com.hmp.data.database

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

fun getRoomDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
        .build()
}

// 迁移定义
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `music_extra` (`musicId` INTEGER NOT NULL, `bitrate` INTEGER, `sampleRate` INTEGER, `lyrics` TEXT, `coverArtPath` TEXT, `sortKey` TEXT, PRIMARY KEY (`musicId`))")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `music_label` (`musicId` INTEGER NOT NULL, `labelName` TEXT NOT NULL, `labelCategory` TEXT NOT NULL, `confidence` REAL NOT NULL, PRIMARY KEY (`musicId`, `labelName`))")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `listening_duration` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `musicId` INTEGER NOT NULL, `date` TEXT NOT NULL, `duration` INTEGER NOT NULL, `lastPlayed` INTEGER NOT NULL)")
        database.execSQL("CREATE TABLE IF NOT EXISTS `playback_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `musicId` INTEGER NOT NULL, `playedAt` INTEGER NOT NULL, `durationPlayed` INTEGER NOT NULL)")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `music` ADD COLUMN `isDeleted` INTEGER NOT NULL DEFAULT 0")
    }
}
```

- [ ] **Step 4: 验证文件创建**

```bash
ls -la shared/src/androidMain/kotlin/com/hmp/data/database/DatabaseBuilder.android.kt
ls -la shared/src/iosMain/kotlin/com/hmp/data/database/DatabaseBuilder.ios.kt
ls -la shared/src/commonMain/kotlin/com/hmp/data/database/Database.kt
```

- [ ] **Step 5: Commit**

```bash
git add shared/src/androidMain/kotlin/com/hmp/data/database/DatabaseBuilder.android.kt
git add shared/src/iosMain/kotlin/com/hmp/data/database/DatabaseBuilder.ios.kt
git add shared/src/commonMain/kotlin/com/hmp/data/database/Database.kt
git commit -m "feat: create platform-specific database builders"
```

### Task 9: 验证构建

**Files:**
- N/A

- [ ] **Step 1: 构建 shared 模块**

```bash
./gradlew :shared:build
```

- [ ] **Step 2: 检查构建结果**

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 验证 Android 应用构建**

```bash
./gradlew :android:app:assembleDebug
```

- [ ] **Step 4: 检查构建结果**

Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git commit -m "feat: verify P2 Room migration build"
```

---

## 总结

P2 阶段完成了 Room 数据库的 KMP 迁移，包括：

1. **文件迁移**：将所有 Room 相关文件从 Android 模块迁移到 shared 模块
2. **KMP 适配**：使用 `@ConstructedBy` + `expect/actual` 实现平台特定的数据库构建
3. **平台构建器**：为 Android 和 iOS 平台创建了专用的数据库构建逻辑
4. **迁移兼容性**：保留了现有的数据库迁移逻辑

**验证标准**：
- `./gradlew :shared:build` 编译通过
- `./gradlew :android:app:assembleDebug` 编译通过

P2 阶段完成后，共享层的数据库功能将跨平台可用，为后续的 P3（网络层迁移）和 P4（其他 data 层迁移）奠定基础。

---

**Plan complete and saved to `docs/superpowers/plans/2026-04-21-p2-room-migration.md`.**

**Two execution options:**

1. **Subagent-Driven (recommended)** - I dispatch a fresh subagent per task, review between tasks, fast iteration

2. **Inline Execution** - Execute tasks in this session using executing-plans, batch execution with checkpoints

**Which approach?**