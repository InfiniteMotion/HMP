package com.example.hearablemusicplayer.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.hearablemusicplayer.database.myenum.LabelConverters

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
    version = 1,
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
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "music_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
