package com.example.hearablemusicplayer.data.database

import androidx.room.Database
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
    version = 5,
    exportSchema = false
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
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // lastPlayed 从 Int? 改为 Long? 在 SQLite 中仍为 INTEGER 类型
                // 且其他统计字段已存在于版本 1 中，因此此处无需执行 SQL 变更
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 为增量扫描引入软删除标记
                db.execSQL("ALTER TABLE music ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE musicExtra ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE userInfo ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE playlist ADD COLUMN coverUri TEXT")
                db.execSQL("ALTER TABLE playlist ADD COLUMN playCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE playlist ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE playlist ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE playlist ADD COLUMN lastPlayedAt INTEGER")
                db.execSQL("ALTER TABLE playlist ADD COLUMN description TEXT")
                db.execSQL("ALTER TABLE playlist ADD COLUMN songCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE playlist ADD COLUMN totalDurationMs INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE playlist ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS musicExtra_new (
                        id INTEGER PRIMARY KEY NOT NULL,
                        lyrics TEXT,
                        bitRate INTEGER,
                        sampleRate INTEGER,
                        fileSize INTEGER,
                        format TEXT,
                        language TEXT,
                        date INTEGER,
                        recommendationIds TEXT,
                        isGetExtraInfo INTEGER NOT NULL,
                        rewards TEXT,
                        popLyric TEXT,
                        singerIntroduce TEXT,
                        backgroundIntroduce TEXT,
                        description TEXT,
                        relevantMusic TEXT,
                        isDeleted INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO musicExtra_new (id, lyrics, bitRate, sampleRate, fileSize, format, language, date, recommendationIds, isGetExtraInfo, rewards, popLyric, singerIntroduce, backgroundIntroduce, description, relevantMusic, isDeleted)
                    SELECT id, lyrics, bitRate, sampleRate, fileSize, format, language, NULL, recommendationIds, isGetExtraInfo, rewards, popLyric, singerIntroduce, backgroundIntroduce, description, relevantMusic, isDeleted
                    FROM musicExtra
                """.trimIndent())
                db.execSQL("DROP TABLE musicExtra")
                db.execSQL("ALTER TABLE musicExtra_new RENAME TO musicExtra")
            }
        }
    }
}
