package com.hearablemusic.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.media3.common.util.UnstableApi
import com.hearablemusic.player.player.controller.MusicController
import com.hearablemusic.player.ui.AppRoot
import com.hearablemusic.player.ui.common.util.LocalAppViewModelStoreOwner
import com.hearablemusic.player.ui.platform.AndroidPlatformServices
import com.hearablemusic.player.ui.platform.PlatformServices
import com.hearablemusic.player.ui.settings.viewmodel.SettingsViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

@UnstableApi
class MainActivity : ComponentActivity() {

    private val musicController: MusicController by inject()

    // 仅保留主题偏好读取（customMode 决定深浅色）；其余旧 UI 依赖的 VM 已随入口切换摘除
    private val settingsViewModel: SettingsViewModel by viewModel()


    @OptIn(UnstableApi::class)
    override fun onStart() {
        super.onStart()
        musicController.setTargetActivityClass(MainActivity::class.java)
        musicController.bindService()
    }

    @OptIn(UnstableApi::class)
    override fun onDestroy() {
        super.onDestroy()
        musicController.release()
        musicController.unbindService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 平台服务注册（分享/文件选择/悬浮窗权限/标签编辑桥/触觉/悬浮歌词）。
        // 构造需宿主 Activity（launcher 挂其 registry），故在 Activity 侧注册而非 UiKoinModule。
        val platformServices = AndroidPlatformServices(applicationContext, this)
        loadKoinModules(
            module {
                single<PlatformServices> { platformServices }
            }
        )

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        setContent {
            androidx.compose.runtime.CompositionLocalProvider(
                LocalAppViewModelStoreOwner provides this
            ) {
                val customMode by settingsViewModel.customMode.collectAsState("default")
                val darkTheme = when (customMode) {
                    "light" -> false
                    "dark" -> true
                    else -> isSystemInDarkTheme()
                }
                AppRoot(darkTheme = darkTheme)
            }
        }
    }
}
