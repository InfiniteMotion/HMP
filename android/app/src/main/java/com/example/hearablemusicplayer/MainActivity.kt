package com.example.hearablemusicplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import com.example.hearablemusicplayer.player.controller.MusicController
import com.example.hearablemusicplayer.ui.common.pages.IntroScreen
import com.example.hearablemusicplayer.ui.common.pages.MainScreen
import com.example.hearablemusicplayer.ui.common.design.theme.HearableMusicPlayerTheme
import com.example.hearablemusicplayer.ui.library.viewmodel.LibraryViewModel
import com.example.hearablemusicplayer.ui.settings.viewmodel.RecommendationViewModel
import com.example.hearablemusicplayer.ui.settings.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay
import org.koin.android.ext.android.inject

@UnstableApi
class MainActivity : ComponentActivity() {

    private val musicController: MusicController by inject()

    private val settingsViewModel: SettingsViewModel by inject()
    private val libraryViewModel: LibraryViewModel by inject()
    private val recommendationViewModel: RecommendationViewModel by inject()


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
            HearableMusicPlayerTheme(darkTheme = darkTheme) {
                val isMusicReadPermissionGiven = remember { mutableStateOf(false) }
                val isNotificationPermissionGiven = remember { mutableStateOf(false) }
                // 初始值给 true：避免冷启动第一帧因默认 false 误判为非首次启动而跳过 Intro
                // DataStore 异步加载后会更新为真实值（老用户为 false，新用户为 true）
                val isFirstLaunch by settingsViewModel.isFirstLaunch.collectAsState(true)
                val autoBatchProcess by settingsViewModel.autoBatchProcess.collectAsState(false)
                val context = LocalContext.current

                LaunchedEffect(Unit) {
                    val statusOne = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_MEDIA_AUDIO
                    )
                    isMusicReadPermissionGiven.value = statusOne == PackageManager.PERMISSION_GRANTED
                    val statusTwo = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                    isNotificationPermissionGiven.value = statusTwo == PackageManager.PERMISSION_GRANTED
                }

                val shouldInitialize = remember { derivedStateOf { isMusicReadPermissionGiven.value } }
                LaunchedEffect(shouldInitialize.value, autoBatchProcess) {
                    if (shouldInitialize.value) {
                        settingsViewModel.getAvatarUri()

                        recommendationViewModel.getDailyMusicInfo()

                        if (autoBatchProcess) {
                            delay(2000)
                            recommendationViewModel.startAutoProcessWithCurrentProvider()
                        }
                    }
                }

                if (isFirstLaunch) {
                    IntroScreen(
                        settingsViewModel = settingsViewModel,
                        libraryViewModel = libraryViewModel,
                        recommendationViewModel = recommendationViewModel,
                        onFinished = {
                            settingsViewModel.saveIsFirstLaunchStatus(false)
                        }
                    )
                } else {
                    MainScreen()
                }
            }
        }
    }
}
