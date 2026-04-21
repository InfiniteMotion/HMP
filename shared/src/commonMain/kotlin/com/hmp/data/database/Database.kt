package com.hmp.data.database

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface AppDatabase {
    val musicDao: MusicDao
    val playlistDao: PlaylistDao
    val playbackHistoryDao: PlaybackHistoryDao
    val dailyMusicInfoDao: DailyMusicInfoDao
    val listeningDurationDao: ListeningDurationDao

    interface Builder {
        fun addMigrations(vararg migrations: DatabaseMigration): Builder
        fun build(): AppDatabase
    }
}

interface DatabaseMigration {
    val version: Int
    fun migrate(database: Any)
}

expect fun provideDatabaseBuilder(context: Any): AppDatabase.Builder

class DatabaseManager private constructor(
    private val database: AppDatabase
) {
    companion object {
        private var instance: DatabaseManager? = null

        fun getInstance(context: Any): DatabaseManager {
            if (instance == null) {
                val builder = provideDatabaseBuilder(context)
                val database = builder.build()
                instance = DatabaseManager(database)
            }
            return instance!!
        }
    }

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        initializeDatabase()
    }

    private fun initializeDatabase() {
        _isInitialized.value = true
    }

    fun getMusicDao(): MusicDao = database.musicDao
    fun getPlaylistDao(): PlaylistDao = database.playlistDao
    fun getPlaybackHistoryDao(): PlaybackHistoryDao = database.playbackHistoryDao
    fun getDailyMusicInfoDao(): DailyMusicInfoDao = database.dailyMusicInfoDao
    fun getListeningDurationDao(): ListeningDurationDao = database.listeningDurationDao
}
