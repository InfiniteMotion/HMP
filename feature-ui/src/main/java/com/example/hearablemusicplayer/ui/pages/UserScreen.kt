package com.example.hearablemusicplayer.ui.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.components.Avatar
import com.example.hearablemusicplayer.ui.components.ListeningChart
import com.example.hearablemusicplayer.ui.components.SquareCard
import com.example.hearablemusicplayer.ui.viewmodel.MusicViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun UserScreen(
    viewModel: MusicViewModel,
    navController: NavController
) {
    var visible by remember { mutableStateOf(false) }
    val userName by viewModel.userName.collectAsState("")
    val listeningData by viewModel.recentListeningDurations.collectAsState()

    LaunchedEffect(Unit) {
        visible = true
        viewModel.getAvatarUri()
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500)),
        exit = fadeOut(animationSpec = tween(500))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                // 顶部头像 + 用户名
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Avatar(128, viewModel)
                    Spacer(modifier = Modifier.height(16.dp))
                    userName?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                val sortedData = listeningData.sortedBy { it.date }.takeLast(7)
                val chartData = sortedData.map { ((it.duration / (1000 * 60)).toInt()) }
                val chartDays = sortedData.map { ld ->
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("MM.dd", Locale.getDefault())
                    try {
                        dateFormat.parse(ld.date)?.let { date ->
                            outputFormat.format(date)
                        } ?: "--"
                    } catch (_: Exception) {
                        "--"
                    }
                }

                ListeningChart(
                    text = "近期听歌时长(Minutes)",
                    data = chartData,
                    days = chartDays
                )

                Spacer(modifier = Modifier.height(30.dp))

                // 功能按钮
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 第一行
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SquareCard(
                            "主题定制",
                            R.drawable.identify_song,
                            onClick = {}
                            )
                        Spacer(modifier = Modifier.width(30.dp))
                        SquareCard(
                            "音效效果",
                            R.drawable.slider_vertical_3,
                            onClick = { navController.navigate("audioEffects") }
                        )
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                    // 第二行
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SquareCard(
                            "云服务",
                            R.drawable.icloud,
                            onClick = {}
                        )
                        Spacer(modifier = Modifier.width(30.dp))
                        SquareCard(
                            "设置",
                            R.drawable.gearshape,
                            onClick = { navController.navigate("setting") }
                        )
                    }
                }
            }
        }
    }
}
