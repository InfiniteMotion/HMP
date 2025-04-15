package com.example.hearablemusicplayer
import android.app.Application
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.example.hearablemusicplayer.database.AppDatabase
import com.example.hearablemusicplayer.database.AppPreferences
import com.example.hearablemusicplayer.repository.MusicRepository

class MusicApplication : Application() {
    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "music-database"
        ).build()
    }

    val repository by lazy {
        MusicRepository(database.musicDao(), applicationContext)
    }

    companion object {
        lateinit var instance: MusicApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        AppPreferences.init(this)
    }
}