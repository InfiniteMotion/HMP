package com.example.hearablemusicplayer.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hearablemusicplayer.domain.model.ListeningDuration
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.components.Avatar
import com.example.hearablemusicplayer.ui.components.ListeningChart
import com.example.hearablemusicplayer.ui.components.SquareCard
import com.example.hearablemusicplayer.ui.template.pages.TabScreen
import com.example.hearablemusicplayer.ui.util.Routes
import com.example.hearablemusicplayer.ui.viewmodel.RecommendationViewModel
import com.example.hearablemusicplayer.ui.viewmodel.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun UserScreen(
    settingsViewModel: SettingsViewModel,
    recommendationViewModel: RecommendationViewModel,
    navController: NavController
) {

    val userName by settingsViewModel.userName.collectAsState("")
    val avatarUri by settingsViewModel.avatarUri.collectAsState("")
    val listeningData by recommendationViewModel.recentListeningDurations.collectAsState()

    LaunchedEffect(Unit) {
        settingsViewModel.getAvatarUri()
    }
    
    UserScreenContent(
        userName = userName,
        avatarUri = avatarUri,
        listeningData = listeningData,
        onNavigate = navController::navigate
    )
}

@Composable
fun UserScreenContent(
    userName: String?,
    avatarUri: String,
    listeningData: List<ListeningDuration>,
    onNavigate: (String) -> Unit
) {
    TabScreen {
        val sortedData = listeningData.sortedBy { it.date }.takeLast(35) // 取最近35天
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
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Avatar(
                    aSize = 100,
                    imageUri = avatarUri
                )
                Spacer(modifier = Modifier.height(16.dp))
                userName?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
                    .padding(horizontal = 24.dp), // 增加水平边距
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp) // 行间距
            ) {
                ListeningChart(
                    text = "近期听歌时长(Minutes)",
                    data = chartData,
                    days = chartDays
                )
                // 第一行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp) // 列间距
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        SquareCard(
                            "主题定制",
                            R.drawable.slider_vertical_3,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1.5f),
                            onClick = { onNavigate(Routes.CUSTOM) }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        SquareCard(
                            "音效效果",
                            R.drawable.identify_song,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1.5f),
                            onClick = { onNavigate(Routes.AUDIO_EFFECTS) }
                        )
                    }
                }
                
                // 第二行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp) // 列间距
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        SquareCard(
                            "AI服务",
                            R.drawable.icloud,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1.5f),
                            onClick = { onNavigate(Routes.AI) }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        SquareCard(
                            "设置",
                            R.drawable.gearshape,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1.5f),
                            onClick = { onNavigate(Routes.SETTING) }
                        )
                    }
                }
            }
        }
    }
}
