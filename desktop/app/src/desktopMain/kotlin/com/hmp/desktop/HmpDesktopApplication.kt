package com.hmp.desktop

import com.hmp.desktop.player.di.desktopPlayerModule
import com.hmp.di.initKoinDesktop
import com.hearablemusic.player.ui.di.desktopUiModule

object HmpDesktopApplication {
    fun init() {
        val start = System.currentTimeMillis()
        // 第 5c 步：uiModule（旧 feature-ui）退役，切换 shared-ui 的 desktopUiModule
        // （PlaybackController 适配器/平台服务/全部 commonMain VM）
        initKoinDesktop(desktopPlayerModule, desktopUiModule)
        println("[Startup] +${System.currentTimeMillis() - start}ms — initKoinDesktop (startKoin + module registration)")
    }
}
