package com.example.hearablemusicplayer
import android.app.Application
import com.example.hearablemusicplayer.database.AppDatabase
import com.example.hearablemusicplayer.repository.MusicRepository
import com.example.hearablemusicplayer.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MusicApplication : Application() {

    val MusicRepo by lazy {
        val db = AppDatabase.getDatabase(this)
        MusicRepository(
            db.musicDao(),
            db.playlistDao(),
            db.playlistItemDao(),
            db.playbackHistoryDao(),
            applicationContext
        )
    }

    val SettingsRepo by lazy {
        SettingsRepository(
            applicationContext
        )
    }

    companion object {
        lateinit var instance: MusicApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}

