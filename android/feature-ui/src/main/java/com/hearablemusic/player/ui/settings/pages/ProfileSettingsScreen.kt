package com.hearablemusic.player.ui.settings.pages

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import org.koin.androidx.compose.koinViewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import coil.compose.AsyncImage
import com.hearablemusic.player.ui.R
import com.hearablemusic.player.ui.common.components.Avatar
import com.hearablemusic.player.ui.player.components.MiniPlayerSafeSpacer
import com.hearablemusic.player.ui.common.components.base.TitleWidget
import com.hearablemusic.player.ui.common.dialogs.controller.DialogManager
import com.hearablemusic.player.ui.common.pages.base.SubScreen
import com.hearablemusic.player.ui.common.layout.LocalWindowSizeInfo
import com.hearablemusic.player.ui.common.design.dimens.LocalHMPDimens
import com.hearablemusic.player.ui.common.dialogs.viewmodel.DialogManagerViewModel
import com.hearablemusic.player.ui.settings.viewmodel.SettingsViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@Composable
fun ProfileSettingsScreen(
    navController: NavBackStack<NavKey>,
    settingsViewModel: SettingsViewModel = koinViewModel(),
    dialogManagerViewModel: DialogManagerViewModel = koinViewModel()
) {
    val dialogManager = dialogManagerViewModel.dialogManager
    val avatarUri by settingsViewModel.avatarUri.collectAsState("")
    val userName by settingsViewModel.userName.collectAsState("")

    SubScreen(
        onBackClick = { navController.removeLastOrNull() },
        title = stringResource(R.string.profile_settings)
    ) {
        val isLandscape = LocalWindowSizeInfo.current.isLandscape
        val dimens = LocalHMPDimens.current
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(dimens.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(dimens.spacing.lg)
        ) {
            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimens.spacing.lg)
                ) {
                    Box(Modifier.weight(1f)) {
                        UpdateAvatar(
                            avatarUri = avatarUri,
                            updateAvatar = settingsViewModel::saveAvatarUri,
                            dialogManager = dialogManager
                        )
                    }
                    Box(Modifier.weight(1f)) {
                        UpdateUserName(
                            userName = userName,
                            updateUserName = settingsViewModel::saveUserName
                        )
                    }
                }
            } else {
                UpdateAvatar(
                    avatarUri = avatarUri,
                    updateAvatar = settingsViewModel::saveAvatarUri,
                    dialogManager = dialogManager
                )
                UpdateUserName(
                    userName = userName,
                    updateUserName = settingsViewModel::saveUserName
                )
            }
            MiniPlayerSafeSpacer(height = 56.dp)
        }
    }
}

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
private fun UpdateAvatar(
    avatarUri: String,
    updateAvatar: (String) -> Unit,
    dialogManager: DialogManager
){
    val dimens = LocalHMPDimens.current
    TitleWidget(
        title = stringResource(R.string.avatar),
    ) {
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.spacing.md),
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
                Spacer(modifier = Modifier.height(dimens.spacing.md))
                // 如果 uriImg 有值（用户刚选了新图），优先显示预览
                // 否则显示当前的 avatarUri
                val displayUri = if (uriImg.value.isNotEmpty()) uriImg.value else avatarUri
                Avatar(dimens.component.md.value.toInt(), displayUri)
                Spacer(modifier = Modifier.height(dimens.spacing.md))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimens.spacing.md),
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
                        contentDescription = stringResource(R.string.user_avatar_desc),
                        modifier = Modifier
                            .size(dimens.component.xs)
                            .clip(RoundedCornerShape(dimens.corner.sm))
                    )
                    Spacer(modifier = Modifier.width(dimens.spacing.xl))
                    Button(
                        onClick = {
                            updateAvatar(uriImg.value)
                            dialogManager.showMessage(context.getString(R.string.avatar_changed))
                        }
                    ) {
                        Text(text = stringResource(R.string.change), color = MaterialTheme.colorScheme.onPrimary)
                    }
                    Button(
                        onClick = {
                            uriImg.value = ""
                            dialogManager.showMessage(context.getString(R.string.avatar_change_cancelled))
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
    val dimens = LocalHMPDimens.current
    TitleWidget(
        title = stringResource(R.string.user_name),
    ) {
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.spacing.md),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var name by rememberSaveable { mutableStateOf("") }
            Text(
                text = userName?:stringResource(R.string.user_name),
                style = MaterialTheme.typography.displayLarge,
                fontSize = dimens.type.xl,
                modifier = Modifier.padding(dimens.spacing.md),
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
                    shape = RoundedCornerShape(dimens.corner.lg),
                    modifier = Modifier.width(300.dp)
                        .padding(vertical = dimens.spacing.md)
                )
            }

            Spacer(modifier = Modifier.height(dimens.spacing.md))

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
