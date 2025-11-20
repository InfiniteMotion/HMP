package com.example.hearablemusicplayer.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.hearablemusicplayer.database.AppDatabase
import com.example.hearablemusicplayer.database.ListeningDurationDao
import com.example.hearablemusicplayer.database.MusicAllDao
import com.example.hearablemusicplayer.database.MusicDao
import com.example.hearablemusicplayer.database.MusicExtraDao
import com.example.hearablemusicplayer.database.MusicLabelDao
import com.example.hearablemusicplayer.database.PlaybackHistoryDao
import com.example.hearablemusicplayer.database.PlaylistDao
import com.example.hearablemusicplayer.database.PlaylistItemDao
import com.example.hearablemusicplayer.database.UserInfoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * 数据库迁移：版本 1 -> 2
     */
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 当前版本升级不涉及表结构变化
            // 未来版本升级时在此处添加 ALTER TABLE 等 SQL 语句
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "music_database"
        )
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration() // 迁移失败时重建数据库
            .build()
    }

    @Provides
    @Singleton
    fun provideMusicDao(database: AppDatabase): MusicDao {
        return database.musicDao()
    }

    @Provides
    @Singleton
    fun provideMusicExtraDao(database: AppDatabase): MusicExtraDao {
        return database.musicExtraDao()
    }

    @Provides
    @Singleton
    fun provideUserInfoDao(database: AppDatabase): UserInfoDao {
        return database.userInfoDao()
    }

    @Provides
    @Singleton
    fun provideMusicAllDao(database: AppDatabase): MusicAllDao {
        return database.musicAllDao()
    }

    @Provides
    @Singleton
    fun provideMusicLabelDao(database: AppDatabase): MusicLabelDao {
        return database.musicLabelDao()
    }

    @Provides
    @Singleton
    fun providePlaylistDao(database: AppDatabase): PlaylistDao {
        return database.playlistDao()
    }

    @Provides
    @Singleton
    fun providePlaylistItemDao(database: AppDatabase): PlaylistItemDao {
        return database.playlistItemDao()
    }

    @Provides
    @Singleton
    fun providePlaybackHistoryDao(database: AppDatabase): PlaybackHistoryDao {
        return database.playbackHistoryDao()
    }

    @Provides
    @Singleton
    fun provideListeningDurationDao(database: AppDatabase): ListeningDurationDao {
        return database.listeningDurationDao()
    }
}
