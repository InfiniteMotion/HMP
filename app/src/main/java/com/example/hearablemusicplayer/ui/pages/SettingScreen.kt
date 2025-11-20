package com.example.hearablemusicplayer.ui.pages

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hearablemusicplayer.ui.components.BackButton
import com.example.hearablemusicplayer.ui.dialogs.MusicScanDialog
import com.example.hearablemusicplayer.viewmodel.MusicViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@Composable
fun SettingScreen(
    viewModel: MusicViewModel,
    navController: NavController
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
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                verticalArrangement = Arrangement.Top,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 32.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier.align(Alignment.CenterStart)
                    ){
                        BackButton(navController)
                    }
                    Text(
                        "设置",
                        style = MaterialTheme.typography.displayMedium,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))

                UpdateUserName(viewModel)

                Spacer(modifier = Modifier.height(16.dp))

                UpdateAvatar(viewModel)

                Spacer(modifier = Modifier.height(16.dp))

                ReloadMusic(viewModel)

                Spacer(modifier = Modifier.height(16.dp))

                SetDeepSeekApi(viewModel)

                Spacer(modifier = Modifier.height(16.dp))

                LoadMusicExtraInfo(viewModel)
            }
        }
    }
}


@Composable
fun UpdateUserName(
    viewModel: MusicViewModel
){
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
    ) {
        val userName by viewModel.userName.collectAsState("")
        var name by rememberSaveable { mutableStateOf("") }
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "当前用户名：$userName",
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.padding(16.dp)
            )
            TextField(
                value = name,
                onValueChange = {
                    name = it
                },
                label = { Text("请输入新的用户名") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Default
                ),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent, // 聚焦时下划线颜色
                    unfocusedIndicatorColor = Color.Transparent, // 未聚焦时下划线颜色
                    disabledIndicatorColor = Color.Transparent // 禁用时下划线颜色
                ),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.width(300.dp)
                    .padding(vertical = 16.dp)
            )
            Button(
                modifier = Modifier.width(300.dp),
                onClick = {
                    viewModel.saveUserName(name)
                }
            ) {
                Text(text = "更改用户名")
            }
        }
    }
}

@Composable
fun UpdateAvatar(
    viewModel: MusicViewModel
){
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
    ) {
        val context = LocalContext.current
        val uriImg = remember { mutableStateOf("") }
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
            onResult = { uri: Uri? ->
                uri?.let {
                    try {
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val file = File(context.filesDir, "user_avatar.jpg")

                        val outputStream = FileOutputStream(file)
                        inputStream?.copyTo(outputStream)

                        // 保存头像路径到 SharedPreferences
                        uriImg.value = file.absolutePath
                        inputStream?.close()
                        outputStream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (uriImg.value == "") {
                Button(
                    modifier = Modifier.width(300.dp),
                    onClick = {
                        launcher.launch("image/*")  // 打开图片选择器
                    }
                ) {
                    Text(text = "更改头像")
                }
            } else {
                AsyncImage(
                    model = uriImg.value,
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                Spacer(modifier = Modifier.width(32.dp))
                Button(
                    onClick = {
                        viewModel.saveAvatarUri(uriImg.value)
                        Toast.makeText(context, "头像已更改!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(text = "更改")
                }
                Button(
                    onClick = {
                        uriImg.value=""
                        Toast.makeText(context, "放弃更改头像！", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(text = "取消")
                }
            }
        }
    }
}

@Composable
fun ReloadMusic(
    viewModel: MusicViewModel
) {
    var isLoading by remember { mutableStateOf(false) }
    val musicCount by viewModel.musicCount.collectAsState(initial = 0)
    val isScanning by viewModel.isScanning.collectAsState(initial = false)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "当前音乐数量：${musicCount} 首",
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.padding(16.dp)
            )
            Text(
                text = "可选择方式从设备中读取音乐信息",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(8.dp)
            )
            Text(
                text = "增量加载只会增加音乐信息，重载会删除所有相关信息（建议仅在应用首次启动时使用）" ,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 32.dp)
            )
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    modifier = Modifier.width(120.dp),
                    onClick = {
                        isLoading = true
                        viewModel.refreshMusicList()
                    },
                    enabled = !isLoading
                ) {
                    Text("增量加载")
                }
                Spacer(modifier = Modifier.width(32.dp))
                Button(
                    modifier = Modifier.width(120.dp),
                    onClick = {
                        isLoading = true
                        viewModel.refreshMusicList()
                    },
                    enabled = !isLoading
                ) {
                    Text("重新加载")
                }
            }

        }

        if (isLoading) {
            MusicScanDialog(
                viewModel = viewModel,
                onDismiss = { isLoading = false }
            )
        }
    }
}

@Composable
fun SetDeepSeekApi(
    viewModel: MusicViewModel
){
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "使用用户自己 DeepSeek API-Key",
                style = MaterialTheme.typography.titleLarge,
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
                label = { Text("请输入您的密钥,形如 Bearer sk-xxx") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Default
                ),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent, // 聚焦时下划线颜色
                    unfocusedIndicatorColor = Color.Transparent, // 未聚焦时下划线颜色
                    disabledIndicatorColor = Color.Transparent // 禁用时下划线颜色
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
                            isAccess = viewModel.checkApiAccess(keyValue)
                        }
                    }
                ) {
                    Text("测试")
                }
                if (isAccess){
                    Toast.makeText(context, "可以访问到 DeepSeek", Toast.LENGTH_SHORT).show()
                }
                Spacer(modifier = Modifier.width(32.dp))
                Button(
                    modifier = Modifier.width(120.dp),
                    onClick = {
                        viewModel.saveDeepSeekApiKey(keyValue)
                    }
                ) {
                    Text("使用")
                }
            }
        }
    }
}


@Composable
fun LoadMusicExtraInfo(
    viewModel: MusicViewModel
) {
    val musicWithExtraCount by viewModel.musicWithExtraCount.collectAsState(initial = 0)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "已获取额外信息的音乐数量：${musicWithExtraCount} 首",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
            Button(
                modifier = Modifier.width(300.dp),
                onClick = {

                }
            ) {
                Text(text = "批量加载")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}