package com.hmp.data.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.hmp.data.database.AppDatabase
import com.hmp.data.database.ListeningDurationDao
import com.hmp.data.database.MusicAllDao
import com.hmp.data.database.MusicDao
import com.hmp.data.database.MusicExtraDao
import com.hmp.data.database.MusicLabelDao
import com.hmp.data.database.PlaybackHistoryDao
import com.hmp.data.database.PlaylistDao
import com.hmp.data.database.PlaylistItemDao
import com.hmp.data.database.UserInfoDao
import com.hmp.data.network.MultiProviderApiAdapter
import com.hmp.data.network.createHttpClient
import com.hmp.data.repository.BackupFileRepositoryImpl
import com.hmp.data.repository.MusicRepositoryImpl
import com.hmp.data.repository.PlaylistRepositoryImpl
import com.hmp.data.repository.SettingsRepositoryImpl
import com.hmp.domain.backup.BackupFileRepository
import com.hmp.domain.music.MusicRepository
import com.hmp.domain.music.usecase.GetAllMusicUseCase
import com.hmp.domain.music.usecase.GetDailyMusicRecommendationUseCase
import com.hmp.domain.music.usecase.GetDeletedMusicIdsGroupedByFolderUseCase
import com.hmp.domain.music.usecase.LoadMusicFromDeviceUseCase
import com.hmp.domain.music.usecase.MusicLabelUseCase
import com.hmp.domain.music.usecase.RemoveFromLibraryUseCase
import com.hmp.domain.music.usecase.RestoreToLibraryUseCase
import com.hmp.domain.music.usecase.SearchMusicUseCase
import com.hmp.domain.music.usecase.SyncMusicFromDeviceIncrementalUseCase
import com.hmp.domain.playlist.PlaylistRepository
import com.hmp.domain.playlist.usecase.GeneratePlaylistUseCase
import com.hmp.domain.playlist.usecase.ManagePlaylistUseCase
import com.hmp.domain.setting.SettingsRepository
import com.hmp.domain.setting.usecase.CurrentPlaybackUseCase
import com.hmp.domain.setting.usecase.GetUserUsageDataUseCase
import com.hmp.domain.setting.usecase.LyricsSettingsUseCase
import com.hmp.domain.setting.usecase.PlaybackHistoryUseCase
import com.hmp.domain.setting.usecase.TimerUseCase
import com.hmp.domain.setting.usecase.UserSettingsUseCase
import com.hmp.domain.backup.usecase.DeleteBackupUseCase
import com.hmp.domain.backup.usecase.ExportUserDataBackupUseCase
import com.hmp.domain.backup.usecase.GetBackupsUseCase
import com.hmp.domain.backup.usecase.ImportUserDataBackupUseCase
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val sharedModule = module {
    single<Json> {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }
    }

    single { createHttpClient(get()) }

    single<Gson> {
        GsonBuilder()
            .setLenient()
            .create()
    }

    singleOf(::MultiProviderApiAdapter)

    singleOf(::MusicRepositoryImpl) bind MusicRepository::class
    singleOf(::PlaylistRepositoryImpl) bind PlaylistRepository::class
    singleOf(::SettingsRepositoryImpl) bind SettingsRepository::class
    singleOf(::BackupFileRepositoryImpl) bind BackupFileRepository::class

    single { GetAllMusicUseCase(get()) }
    single { SearchMusicUseCase(get()) }
    single { LoadMusicFromDeviceUseCase(get()) }
    single { SyncMusicFromDeviceIncrementalUseCase(get()) }
    single { RemoveFromLibraryUseCase(get()) }
    single { RestoreToLibraryUseCase(get()) }
    single { GetDeletedMusicIdsGroupedByFolderUseCase(get()) }
    single { MusicLabelUseCase(get(), get()) }
    single { GetDailyMusicRecommendationUseCase(get(), get(), get()) }
    single { ManagePlaylistUseCase(get(), get()) }
    single { GeneratePlaylistUseCase(get(), get()) }
    single { UserSettingsUseCase(get()) }
    single { LyricsSettingsUseCase(get()) }
    single { CurrentPlaybackUseCase(get(), get(), get()) }
    single { PlaybackHistoryUseCase(get()) }
    single { GetUserUsageDataUseCase(get()) }
    single { TimerUseCase(get()) }
    single { ExportUserDataBackupUseCase(get(), get(), get(), get()) }
    single { ImportUserDataBackupUseCase(get(), get(), get(), get()) }
    single { GetBackupsUseCase(get()) }
    single { DeleteBackupUseCase(get()) }
}

val androidPlatformModule = module {
    single<AppDatabase> {
        val context: Context = get()
        val dbFile = context.getDatabasePath("music_database.db")
        Room.databaseBuilder<AppDatabase>(
            context = context,
            name = dbFile.absolutePath
        ).fallbackToDestructiveMigration().build()
    }

    single<MusicDao> { get<AppDatabase>().musicDao() }
    single<MusicExtraDao> { get<AppDatabase>().musicExtraDao() }
    single<UserInfoDao> { get<AppDatabase>().userInfoDao() }
    single<MusicAllDao> { get<AppDatabase>().musicAllDao() }
    single<MusicLabelDao> { get<AppDatabase>().musicLabelDao() }
    single<PlaylistDao> { get<AppDatabase>().playlistDao() }
    single<PlaylistItemDao> { get<AppDatabase>().playlistItemDao() }
    single<PlaybackHistoryDao> { get<AppDatabase>().playbackHistoryDao() }
    single<ListeningDurationDao> { get<AppDatabase>().listeningDurationDao() }
}