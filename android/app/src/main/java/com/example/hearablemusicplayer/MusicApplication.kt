package com.example.hearablemusicplayer

import android.app.Application
import com.example.hearablemusicplayer.player.di.playerModule
import com.example.hearablemusicplayer.ui.di.uiModule
import com.hmp.data.di.androidPlatformModule
import com.hmp.data.di.sharedModule
import com.hmp.shared.resource.SharedIconLoader
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MusicApplication : Application() {

    companion object {
        lateinit var instance: MusicApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        SharedIconLoader.init(this)
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@MusicApplication)
            modules(sharedModule, androidPlatformModule, playerModule, uiModule)
        }
    }
}
