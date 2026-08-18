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
import com.hearablemusic.player.ui.settings.viewmodel.SettingsViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

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
            val customMode by settingsViewModel.customMode.collectAsState("default")
            val darkTheme = when (customMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }
            // 第 1 步（C9）：入口切至新共享层空壳；旧 MainScreen/IntroScreen 不再被引用（冷死，androidMain 保留可对照）
            AppRoot(darkTheme = darkTheme)
        }
    }
}
