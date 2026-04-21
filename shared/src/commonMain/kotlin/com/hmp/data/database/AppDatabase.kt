package com.hmp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlin.native.concurrent.ConstructedBy

@Database(
    entities = [
        Music::class,
        MusicExtra::class,
        UserInfo::class,
        PlayList::class,
        PlaylistItem::class,
        ListeningDuration::class,
        MusicLabel::class,
        MusicLabelMapping::class,
        PlaybackHistory::class
    ],
    version = 1
)
@TypeConverters(
    com.hmp.data.database.myenum.LabelConverters::class
)
@ConstructedBy(AppDatabaseFactory::class)
expect abstract class AppDatabase : RoomDatabase {
    abstract fun musicDao(): MusicDao
    abstract fun musicExtraDao(): MusicExtraDao
    abstract fun userInfoDao(): UserInfoDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun listeningDurationDao(): ListeningDurationDao
    abstract fun musicLabelDao(): MusicLabelDao
    abstract fun musicLabelMappingDao(): MusicLabelMappingDao
    abstract fun playbackHistoryDao(): PlaybackHistoryDao

    companion object {
        fun getInstance(): AppDatabase
    }
}

class AppDatabaseFactory {
    fun create(): AppDatabase {
        return AppDatabase.getInstance()
    }
}
