package com.example.hearablemusicplayer.di

import android.content.Context
import com.example.hearablemusicplayer.database.ListeningDurationDao
import com.example.hearablemusicplayer.database.MusicAllDao
import com.example.hearablemusicplayer.database.MusicDao
import com.example.hearablemusicplayer.database.MusicExtraDao
import com.example.hearablemusicplayer.database.MusicLabelDao
import com.example.hearablemusicplayer.database.PlaybackHistoryDao
import com.example.hearablemusicplayer.database.PlaylistDao
import com.example.hearablemusicplayer.database.PlaylistItemDao
import com.example.hearablemusicplayer.database.UserInfoDao
import com.example.hearablemusicplayer.repository.MusicRepository
import com.example.hearablemusicplayer.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideMusicRepository(
        musicDao: MusicDao,
        musicExtraDao: MusicExtraDao,
        userInfoDao: UserInfoDao,
        musicAllDao: MusicAllDao,
        musicLabelDao: MusicLabelDao,
        playlistDao: PlaylistDao,
        playlistItemDao: PlaylistItemDao,
        playbackHistoryDao: PlaybackHistoryDao,
        listeningDurationDao: ListeningDurationDao,
        @ApplicationContext context: Context
    ): MusicRepository {
        return MusicRepository(
            musicDao,
            musicExtraDao,
            userInfoDao,
            musicAllDao,
            musicLabelDao,
            playlistDao,
            playlistItemDao,
            playbackHistoryDao,
            listeningDurationDao,
            context
        )
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context
    ): SettingsRepository {
        return SettingsRepository(context)
    }
}
