package com.example.hearablemusicplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import com.example.hearablemusicplayer.ui.pages.IntroScreen
import com.example.hearablemusicplayer.ui.pages.MainScreen
import com.example.hearablemusicplayer.ui.theme.HearableMusicPlayerTheme
import com.example.hearablemusicplayer.viewmodel.MusicViewModel
import com.example.hearablemusicplayer.viewmodel.MusicViewModelFactory
import com.example.hearablemusicplayer.viewmodel.PlayControlViewModel
import com.example.hearablemusicplayer.viewmodel.PlayControlViewModelFactory
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    val musicViewModel by viewModels<MusicViewModel> {
        MusicViewModelFactory(
            (application as MusicApplication).MusicRepo,
            (application as MusicApplication).SettingsRepo
        )
    }

    val playControlViewModel by viewModels<PlayControlViewModel> {
        PlayControlViewModelFactory(
            application = application,
            (application as MusicApplication).MusicRepo,
            (application as MusicApplication).SettingsRepo
        )
    }

    private val connection = object : ServiceConnection {
        @OptIn(UnstableApi::class)
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val service = (binder as? MusicPlayService.MusicPlayServiceBinder)?.getService()
            if (service != null) {
                playControlViewModel.bindPlayControl(service)
            }
        }

        @OptIn(UnstableApi::class)
        override fun onServiceDisconnected(name: ComponentName?) {
            playControlViewModel.bindPlayControl(null)
        }
    }

    @OptIn(UnstableApi::class)
    override fun onStart() {
        super.onStart()
        val intent = Intent(this, MusicPlayService::class.java)
        // 绑定服务
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HearableMusicPlayerTheme {
                val isMusicReadPermissionGiven = remember { mutableStateOf(false) }
                val isNotificationPermissionGiven = remember { mutableStateOf(false) }
                val isLoadMusic by musicViewModel.isLoadMusic.collectAsState(false)
                val isFirstLaunch by musicViewModel.isFirstLaunch.collectAsState(false)
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

                val shouldInitialize = remember { derivedStateOf { isLoadMusic && isMusicReadPermissionGiven.value } }
                LaunchedEffect(shouldInitialize.value) {
                    if (shouldInitialize.value) {
                        musicViewModel.getAvatarUri()
                        musicViewModel.getDailyMusicInfo()
                        delay(2000)
                        musicViewModel.startAutoProcessExtraInfo()
                    }
                }

                if(isFirstLaunch){
                    IntroScreen (
                        viewModel = musicViewModel,
                        onFinished = {
                            musicViewModel.saveIsFirstLaunchStatus(false)
                        }
                    )
                }else{
                    MainScreen(
                        musicViewModel = musicViewModel,
                        playControlViewModel = playControlViewModel
                    )
                }
            }
        }
    }
}