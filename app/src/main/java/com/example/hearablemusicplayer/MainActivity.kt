package com.example.hearablemusicplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.hearablemusicplayer.ui.pages.CustomBottomNavBar
import com.example.hearablemusicplayer.ui.pages.GalleryScreen
import com.example.hearablemusicplayer.ui.pages.HomeScreen
import com.example.hearablemusicplayer.ui.pages.ListScreen
import com.example.hearablemusicplayer.ui.pages.PlayerScreen
import com.example.hearablemusicplayer.ui.pages.UserScreen
import com.example.hearablemusicplayer.ui.theme.HearableMusicPlayerTheme
import com.example.hearablemusicplayer.viewmodel.MusicViewModel
import com.example.hearablemusicplayer.viewmodel.MusicViewModelFactory

class MainActivity : ComponentActivity() {

    private val musicViewModel by viewModels<MusicViewModel> {
        MusicViewModelFactory(
            (application as MusicApplication).repository,
        )
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            musicViewModel.refreshMusicList()
            // 权限被授予，通知 ViewModel 加载音乐
        } else {
            // 处理权限被拒绝的情况
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            HearableMusicPlayerTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                // 定义一个函数来判断是否显示底栏
                val shouldShowBottomBar = currentRoute != "player"
                Scaffold(
                    bottomBar = {
                        if(shouldShowBottomBar) {
                            CustomBottomNavBar(
                                currentRoute = currentRoute,
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        launchSingleTop = true
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") { HomeScreen(musicViewModel,navController) }
                        composable("gallery") { GalleryScreen(musicViewModel,navController) }
                        composable("player") { PlayerScreen(musicViewModel,navController) }
                        composable("list") { ListScreen(musicViewModel) }
                        composable("user") { UserScreen() }
                    }
                }

                if (!hasReadStoragePermission()) {
                    // 如果没有权限，请求权限
                    LaunchedEffect(Unit) {
                        requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
                    }
                }
            }
        }
    }

    private fun hasReadStoragePermission(): Boolean {
        return checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

}