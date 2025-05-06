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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hearablemusicplayer.R
import com.example.hearablemusicplayer.ui.components.Avatar
import com.example.hearablemusicplayer.ui.components.ListeningChart
import com.example.hearablemusicplayer.ui.components.SquareCard
import com.example.hearablemusicplayer.viewmodel.MusicViewModel

@Composable
fun UserScreen(
    viewModel: MusicViewModel
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut()
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
                    Avatar(120, viewModel)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "曦",
                        style = MaterialTheme.typography.displayLarge
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // 自定义柱状图组件
                ListeningChart(
                    text = "近期收听时长",
                    data = listOf(3, 4, 2, 1, 5, 3, 2),
                    days = listOf("03.05", "03.06", "03.07", "03.08", "03.09", "03.10", "Today")
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
                        SquareCard("主题定制", R.drawable.identify_song)
                        Spacer(modifier = Modifier.width(30.dp))
                        SquareCard("音效效果", R.drawable.slider_vertical_3)
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                    // 第二行
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SquareCard("云服务", R.drawable.icloud)
                        Spacer(modifier = Modifier.width(30.dp))
                        SquareCard("设置", R.drawable.gearshape)
                    }
                }
            }
        }
    }
}