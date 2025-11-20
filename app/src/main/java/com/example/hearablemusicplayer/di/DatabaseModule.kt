package com.example.hearablemusicplayer.di

import android.content.Context
import androidx.room.Room
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

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "music_database"
        ).build()
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
