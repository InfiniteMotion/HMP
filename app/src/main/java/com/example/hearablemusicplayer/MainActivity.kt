package com.example.hearablemusicplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.hearablemusicplayer.ui.dialogs.MusicScanDialog
import com.example.hearablemusicplayer.ui.dialogs.PermissionRequest
import com.example.hearablemusicplayer.ui.pages.MainScreen
import com.example.hearablemusicplayer.ui.theme.HearableMusicPlayerTheme
import com.example.hearablemusicplayer.viewmodel.MusicViewModel
import com.example.hearablemusicplayer.viewmodel.MusicViewModelFactory
import com.example.hearablemusicplayer.viewmodel.PlayControlViewModel
import com.example.hearablemusicplayer.viewmodel.PlayControlViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val musicViewModel by viewModels<MusicViewModel> {
            MusicViewModelFactory(
                (application as MusicApplication).MusicRepo,
                (application as MusicApplication).SettingsRepo
            )
        }
        musicViewModel.initializeDefaultDailyMusic()

        val playControlViewModel by viewModels<PlayControlViewModel> {
            PlayControlViewModelFactory(
                application = application,
                (application as MusicApplication).MusicRepo,
                (application as MusicApplication).SettingsRepo
            )
        }
        playControlViewModel.initializeDefaultPlaylists()

        enableEdgeToEdge()

        setContent {
            HearableMusicPlayerTheme() {
                val isLoadMusic by musicViewModel.isLoadMusic.collectAsState(initial = false)
                val isPermissionGiven = remember { mutableStateOf(false) }
                isPermissionGiven.value = PermissionRequest()
                if(!isLoadMusic) {
                    if(isPermissionGiven.value){
                        musicViewModel.refreshMusicList()
                        val isScanning by musicViewModel.isScanning.collectAsState(initial = false)
                        MusicScanDialog(isLoading = isScanning, onDismiss = {})
                    }
                }
                if (isLoadMusic && isPermissionGiven.value){
                    MainScreen(
                        musicViewModel = musicViewModel,
                        playControlViewModel = playControlViewModel
                    )
                }
            }
        }
    }

}