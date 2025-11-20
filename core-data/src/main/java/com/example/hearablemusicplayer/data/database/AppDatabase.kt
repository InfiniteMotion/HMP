package com.example.hearablemusicplayer.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.hearablemusicplayer.data.database.myenum.LabelConverters

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
    version = 2,
    exportSchema = false  // 禁用schema导出以避免序列化版本冲突
)
@TypeConverters(LabelConverters::class)
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

    companion object {
        /**
         * 数据库迁移:版本 1 -> 2
         * 预留迁移策略,当前保持兼容
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 当前版本升级不涉及表结构变化
                // 未来版本升级时在此处添加 ALTER TABLE 等 SQL 语句
            }
        }
    }
}
