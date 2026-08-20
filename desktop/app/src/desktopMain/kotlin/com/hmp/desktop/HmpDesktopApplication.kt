package com.hmp.desktop

import com.hmp.desktop.player.di.desktopPlayerModule
import com.hmp.di.initKoinDesktop
import com.hearablemusic.player.ui.di.desktopUiModule

object HmpDesktopApplication {
    fun init() {
        val start = System.currentTimeMillis()
        initKoinDesktop(desktopPlayerModule, desktopUiModule)
        println("[Startup] +${System.currentTimeMillis() - start}ms — initKoinDesktop (startKoin + module registration)")
    }
}
