package com.example.hearablemusicplayer.ui.template.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hearablemusicplayer.ui.components.BackButton

@Composable
fun SubScreen(
    navController: NavController,
    title: String? = null,
    content: @Composable () -> Unit
){
    var visible by rememberSaveable { mutableStateOf(false) }

    val currentContent by rememberUpdatedState(content)
    val currentTitle by rememberUpdatedState(title)

    // 启动动画
    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 300, easing = LinearEasing)),
        exit = fadeOut(animationSpec = tween(durationMillis = 200))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(top = 16.dp, bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
            ) {
                // 返回按钮
                BackButton(navController)
                
                // 标题（如果提供）
                currentTitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            
            // 主内容区域
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        currentContent()
                    }
                }
            }
        }
    }
}

