package com.example.hearablemusicplayer.ui.pages.settings

import android.annotation.SuppressLint
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.components.Avatar
import com.example.hearablemusicplayer.ui.components.TitleWidget
import com.example.hearablemusicplayer.ui.pages.base.SubScreen
import com.example.hearablemusicplayer.ui.viewmodel.SettingsViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@Composable
fun ProfileSettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val avatarUri by settingsViewModel.avatarUri.collectAsState("")
    val userName by settingsViewModel.userName.collectAsState("")

    SubScreen(
        onBackClick = { navController.popBackStack() },
        title = stringResource(R.string.profile_settings)
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            UpdateAvatar(
                avatarUri = avatarUri,
                updateAvatar = settingsViewModel::saveAvatarUri
            )
            UpdateUserName(
                userName = userName,
                updateUserName = settingsViewModel::saveUserName
            )
        }
    }
}

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
private fun UpdateAvatar(
    avatarUri: String,
    updateAvatar: (String) -> Unit
){
    TitleWidget(
        title = stringResource(R.string.avatar),
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
                // 如果 uriImg 有值（用户刚选了新图），优先显示预览
                // 否则显示当前的 avatarUri
                val displayUri = if (uriImg.value.isNotEmpty()) uriImg.value else avatarUri
                Avatar(128, displayUri)
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
                        Text(text = stringResource(R.string.change_avatar), color = MaterialTheme.colorScheme.onPrimary)
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
                            Toast.makeText(context, context.getString(R.string.avatar_changed), Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text(text = stringResource(R.string.change), color = MaterialTheme.colorScheme.onPrimary)
                    }
                    Button(
                        onClick = {
                            uriImg.value = ""
                            Toast.makeText(context, context.getString(R.string.avatar_change_cancelled), Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text(text = stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun UpdateUserName(
    userName: String?,
    updateUserName: (String) -> Unit,
){
    TitleWidget(
        title = stringResource(R.string.user_name),
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
                text = userName?:stringResource(R.string.user_name),
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
                    label = { Text(stringResource(R.string.enter_new_user_name)) },
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
                Text(text = stringResource(R.string.change_user_name), color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}
