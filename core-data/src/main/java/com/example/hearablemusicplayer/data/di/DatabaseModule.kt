package com.example.hearablemusicplayer.data.di

import android.content.Context
import androidx.room.Room
import com.example.hearablemusicplayer.data.database.AppDatabase
import com.example.hearablemusicplayer.data.database.ListeningDurationDao
import com.example.hearablemusicplayer.data.database.MusicAllDao
import com.example.hearablemusicplayer.data.database.MusicDao
import com.example.hearablemusicplayer.data.database.MusicExtraDao
import com.example.hearablemusicplayer.data.database.MusicLabelDao
import com.example.hearablemusicplayer.data.database.PlaybackHistoryDao
import com.example.hearablemusicplayer.data.database.PlaylistDao
import com.example.hearablemusicplayer.data.database.PlaylistItemDao
import com.example.hearablemusicplayer.data.database.UserInfoDao
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
        )
            .fallbackToDestructiveMigration()
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
