package com.hmp.desktop

import com.hmp.desktop.player.di.desktopPlayerModule
import com.hmp.di.initKoinDesktop

object HmpDesktopApplication {
    fun init() {
        initKoinDesktop(playerModule = desktopPlayerModule)
    }
}
