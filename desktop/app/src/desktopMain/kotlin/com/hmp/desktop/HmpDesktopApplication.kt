package com.hmp.desktop

import com.hmp.desktop.player.di.desktopPlayerModule
import com.hmp.desktop.ui.di.uiModule
import com.hmp.di.initKoinDesktop

object HmpDesktopApplication {
    fun init() {
        val start = System.currentTimeMillis()
        initKoinDesktop(desktopPlayerModule, uiModule)
        println("[Startup] +${System.currentTimeMillis() - start}ms — initKoinDesktop (startKoin + module registration)")
    }
}
