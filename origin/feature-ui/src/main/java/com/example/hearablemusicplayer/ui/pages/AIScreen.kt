package com.example.hearablemusicplayer.ui.pages

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hearablemusicplayer.ui.template.components.TitleWidget
import com.example.hearablemusicplayer.ui.template.pages.SubScreen
import com.example.hearablemusicplayer.ui.viewmodel.MusicViewModel
import kotlinx.coroutines.launch

@Composable
fun AIScreen(
    viewModel: MusicViewModel,
    navController: NavController
) {
    SubScreen(
        navController = navController,
        title = "AI"
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            val musicWithExtraCount by viewModel.musicWithExtraCount.collectAsState(initial = 0)

            SetDeepSeekApi(
                checkApiAccess = viewModel::checkApiAccess,
                updateDeepSeekApiKey = viewModel::saveDeepSeekApiKey
            )

            LoadMusicExtraInfo(
                musicWithExtraCount = musicWithExtraCount,
                startAutoProcessExtraInfo = viewModel::startAutoProcessExtraInfo
            )
        }
    }
}

@Composable
fun SetDeepSeekApi(
    checkApiAccess: suspend (String) -> Unit,
    updateDeepSeekApiKey: (String) -> Unit
){
    TitleWidget(
        title = "LLM API Key",
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "使用用户自己 DeepSeek API-Key",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))
            val context = LocalContext.current
            var keyValue by rememberSaveable { mutableStateOf("") }
            var isAccess by rememberSaveable { mutableStateOf(false) }

            TextField(
                value = keyValue,
                onValueChange = {
                    keyValue = it
                },
                label = { Text("请输入您的密钥,形如 Bearer sk-xxx", color = MaterialTheme.colorScheme.onBackground) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Default
                ),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Transparent, // 聚焦时下划线颜色
                    unfocusedIndicatorColor = Transparent, // 未聚焦时下划线颜色
                    disabledIndicatorColor = Transparent // 禁用时下划线颜色
                ),
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                val scope = rememberCoroutineScope()
                Button(
                    modifier = Modifier.width(120.dp),
                    onClick = {
                        scope.launch {
                            checkApiAccess(keyValue)
                        }
                    }
                ) {
                    Text("测试", color = MaterialTheme.colorScheme.onPrimary)
                }
                if (isAccess){
                    Toast.makeText(context, "可以访问到 DeepSeek", Toast.LENGTH_SHORT).show()
                }
                Spacer(modifier = Modifier.width(32.dp))
                Button(
                    modifier = Modifier.width(120.dp),
                    onClick = {
                        updateDeepSeekApiKey(keyValue)
                    }
                ) {
                    Text("使用", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}


@Composable
fun LoadMusicExtraInfo(
    musicWithExtraCount: Int,
    startAutoProcessExtraInfo: () -> Unit
) {
    TitleWidget(
        title = "音乐信息补全",
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "已获取额外信息的音乐数量：${musicWithExtraCount} 首",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
            Button(
                modifier = Modifier.width(300.dp),
                onClick = {
                    startAutoProcessExtraInfo()
                }
            ) {
                Text(text = "批量加载", color = MaterialTheme.colorScheme.onPrimary)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}