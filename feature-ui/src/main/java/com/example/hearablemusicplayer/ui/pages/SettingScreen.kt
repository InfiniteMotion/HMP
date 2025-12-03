package com.example.hearablemusicplayer.ui.pages

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hearablemusicplayer.ui.components.Avatar
import com.example.hearablemusicplayer.ui.template.components.TitleWidget
import com.example.hearablemusicplayer.ui.template.pages.SubScreen
import com.example.hearablemusicplayer.ui.viewmodel.MusicViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@Composable
fun SettingScreen(
    viewModel: MusicViewModel,
    navController: NavController
) {
    // 使用SubScreen模板
    SubScreen(
        navController = navController,
        title = "设置"
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            val avatarUri by viewModel.avatarUri.collectAsState("")
            val userName by viewModel.userName.collectAsState("")
            val musicCount by viewModel.musicCount.collectAsState(initial = 0)


            UpdateAvatar(
                avatarUri = avatarUri,
                updateAvatar = viewModel::saveAvatarUri
            )

            UpdateUserName(
                userName = userName,
                updateUserName = viewModel::saveUserName
            )

            ReloadMusic(
                musicCount = musicCount,
                refreshMusicList = viewModel::refreshMusicList
            )
        }
    }
}

@Composable
fun UpdateAvatar(
    avatarUri: String,
    updateAvatar: (String) -> Unit
){
    TitleWidget(
        title = "头像",
    ) {
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
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

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Avatar(128, avatarUri)
                Spacer(modifier = Modifier.height(16.dp))
            }

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
                        Text(text = "更改头像", color = MaterialTheme.colorScheme.onPrimary)
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
                            updateAvatar(uriImg.value)
                            Toast.makeText(context, "头像已更改!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text(text = "更改", color = MaterialTheme.colorScheme.onPrimary)
                    }
                    Button(
                        onClick = {
                            uriImg.value = ""
                            Toast.makeText(context, "放弃更改头像！", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text(text = "取消", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun UpdateUserName(
    userName: String?,
    updateUserName: (String) -> Unit,
){
    TitleWidget(
        title = "用户名",
    ) {
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var name by rememberSaveable { mutableStateOf("") }
            Text(
                text = userName?:"用户名",
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
            ){
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
                        focusedIndicatorColor = Transparent, // 聚焦时下划线颜色
                        unfocusedIndicatorColor = Transparent, // 未聚焦时下划线颜色
                        disabledIndicatorColor = Transparent // 禁用时下划线颜色
                    ),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.width(300.dp)
                        .padding(vertical = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                modifier = Modifier.width(200.dp),
                onClick = {
                    updateUserName(name)
                }
            ) {
                Text(text = "更改用户名", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
fun ReloadMusic(
    musicCount: Int,
    refreshMusicList: () -> Unit,
) {
    TitleWidget(
        title = "音乐扫描",
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var isLoading by remember { mutableStateOf(false) }
            Text(
                text = "当前音乐数量：${musicCount} 首",
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "可选择方式从设备中读取音乐信息",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(8.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "增量加载只会增加音乐信息，重载会删除所有相关信息（建议仅在应用首次启动时使用）",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 32.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    modifier = Modifier.width(120.dp),
                    onClick = {
                        isLoading = true
                        refreshMusicList()
                    },
                    enabled = !isLoading
                ) {
                    Text("增量加载", color = MaterialTheme.colorScheme.onPrimary)
                }
                Spacer(modifier = Modifier.width(32.dp))
                Button(
                    modifier = Modifier.width(120.dp),
                    onClick = {
                        isLoading = true
                        refreshMusicList()
                    },
                    enabled = !isLoading
                ) {
                    Text("重新加载", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}
