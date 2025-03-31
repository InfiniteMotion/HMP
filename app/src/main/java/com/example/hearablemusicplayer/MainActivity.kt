package com.example.hearablemusicplayer

import MusicViewModel
import MusicViewModelFactory
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.hearablemusicplayer.repository.MusicRepository
import com.example.hearablemusicplayer.ui.pages.CustomBottomNavBar
import com.example.hearablemusicplayer.ui.pages.GalleryScreen
import com.example.hearablemusicplayer.ui.pages.HomeScreen
import com.example.hearablemusicplayer.ui.pages.ListScreen
import com.example.hearablemusicplayer.ui.pages.PlayerScreen
import com.example.hearablemusicplayer.ui.pages.UserScreen
import com.example.hearablemusicplayer.ui.theme.HearableMusicPlayerTheme

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 权限被授予，通知 ViewModel 加载音乐
            viewModel.loadMusic(this)
        } else {
            // 处理权限被拒绝的情况
        }
    }

    private val viewModel: MusicViewModel by viewModels {
        MusicViewModelFactory(MusicRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            HearableMusicPlayerTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                Scaffold(
                    bottomBar = {
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
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") { HomeScreen(viewModel) }
                        composable("gallery") { GalleryScreen() }
                        composable("player") { PlayerScreen() }
                        composable("list") { ListScreen() }
                        composable("user") { UserScreen() }
                    }
                }

                if (!hasReadStoragePermission()) {
                    // 如果没有权限，请求权限
                    LaunchedEffect(Unit) {
                        requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
                    }
                } else {
                    // 如果已经有权限，直接加载音乐
                    viewModel.loadMusic(LocalContext.current)
                }


            }
        }
    }

    private fun hasReadStoragePermission(): Boolean {
        return checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
    }
}