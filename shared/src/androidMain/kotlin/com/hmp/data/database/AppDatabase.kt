package com.hmp.data.database

import android.content.Context
import androidx.room.Room

actual abstract class AppDatabase : RoomDatabase() {
    actual abstract fun musicDao(): MusicDao
    actual abstract fun musicExtraDao(): MusicExtraDao
    actual abstract fun userInfoDao(): UserInfoDao
    actual abstract fun playlistDao(): PlaylistDao
    actual abstract fun listeningDurationDao(): ListeningDurationDao
    actual abstract fun musicLabelDao(): MusicLabelDao
    actual abstract fun musicLabelMappingDao(): MusicLabelMappingDao
    actual abstract fun playbackHistoryDao(): PlaybackHistoryDao

    actual companion object {
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "hmp_database"
                ).build().also {
                    instance = it
                }
            }
        }

        actual fun getInstance(): AppDatabase {
            TODO("Not yet implemented")
        }
    }
}
