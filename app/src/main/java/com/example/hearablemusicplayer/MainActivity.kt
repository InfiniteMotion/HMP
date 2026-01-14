package com.example.hearablemusicplayer

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import com.example.hearablemusicplayer.ui.pages.IntroScreen
import com.example.hearablemusicplayer.ui.pages.MainScreen
import com.example.hearablemusicplayer.ui.theme.HearableMusicPlayerTheme
import com.example.hearablemusicplayer.ui.viewmodel.LibraryViewModel
import com.example.hearablemusicplayer.ui.viewmodel.RecommendationViewModel
import com.example.hearablemusicplayer.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var musicController: MusicController

    private val settingsViewModel by viewModels<SettingsViewModel>()
    private val libraryViewModel by viewModels<LibraryViewModel>()
    private val recommendationViewModel by viewModels<RecommendationViewModel>()


    @OptIn(UnstableApi::class)
    override fun onStart() {
        super.onStart()
        musicController.setTargetActivityClass(MainActivity::class.java)
        musicController.bindService()
    }

    @OptIn(UnstableApi::class)
    override fun onDestroy() {
        super.onDestroy()
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
                val isFirstLaunch by settingsViewModel.isFirstLaunch.collectAsState(false)
                val autoBatchProcess by settingsViewModel.autoBatchProcess.collectAsState(false)
                val context = LocalContext.current
                
                LaunchedEffect(Unit) {
                    val statusOne = ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.READ_MEDIA_AUDIO
                    )
                    isMusicReadPermissionGiven.value = statusOne == PackageManager.PERMISSION_GRANTED
                    val statusTwo = ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    )
                    isNotificationPermissionGiven.value = statusTwo == PackageManager.PERMISSION_GRANTED
                }

                val shouldInitialize = remember { derivedStateOf { isMusicReadPermissionGiven.value } }
                LaunchedEffect(shouldInitialize.value, autoBatchProcess) {
                    if (shouldInitialize.value) {
                        settingsViewModel.getAvatarUri()
                        
                        // 获取每日推荐（内部会自动处理启动计数和刷新判断）
                        recommendationViewModel.getDailyMusicInfo()
                        
                        // 如果开启了自动后台补全，延迟 2 秒后自动开始
                        if (autoBatchProcess) {
                            delay(2000)
                            recommendationViewModel.startAutoProcessWithCurrentProvider()
                        }
                    }
                }

                if(isFirstLaunch){
                    IntroScreen (
                        settingsViewModel = settingsViewModel,
                        libraryViewModel = libraryViewModel,
                        onFinished = {
                            settingsViewModel.saveIsFirstLaunchStatus(false)
                        }
                    )
                }else{
                    MainScreen()
                }
            }
        }
    }
}