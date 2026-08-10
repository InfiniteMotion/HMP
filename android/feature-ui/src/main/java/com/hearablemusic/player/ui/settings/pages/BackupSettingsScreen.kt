package com.hearablemusic.player.ui.settings.pages

import android.annotation.SuppressLint
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import org.koin.androidx.compose.koinViewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.hearablemusic.player.ui.R
import com.hearablemusic.player.ui.player.components.MiniPlayerSafeSpacer
import com.hearablemusic.player.ui.common.components.base.TitleWidget
import com.hearablemusic.player.ui.common.dialogs.controller.DialogManager
import com.hearablemusic.player.ui.common.pages.base.SubScreen
import com.hearablemusic.player.ui.common.layout.LocalWindowSizeInfo
import com.hearablemusic.player.ui.common.dialogs.viewmodel.DialogManagerViewModel
import com.hearablemusic.player.ui.settings.viewmodel.SettingsViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackupSettingsScreen(
    navController: NavBackStack<NavKey>,
    settingsViewModel: SettingsViewModel = koinViewModel(),
    dialogManagerViewModel: DialogManagerViewModel = koinViewModel()
) {
    val dialogManager = dialogManagerViewModel.dialogManager
    val localBackups by settingsViewModel.localBackups.collectAsState()

    SubScreen(
        onBackClick = { navController.removeLastOrNull() },
        title = stringResource(R.string.backup_settings)
    ) {
        val isLandscape = LocalWindowSizeInfo.current.isLandscape
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        ExportBackupSection(
                            onExportBackup = settingsViewModel::exportBackup,
                            dialogManager = dialogManager
                        )
                        ImportBackupSection(
                            onRestoreBackup = settingsViewModel::restoreBackup,
                            dialogManager = dialogManager
                        )
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        ManageBackupsSection(
                            localBackups = localBackups,
                            onRestoreBackup = settingsViewModel::restoreBackup,
                            onDeleteBackup = settingsViewModel::deleteLocalBackup,
                            onRefreshBackups = settingsViewModel::loadLocalBackups,
                            dialogManager = dialogManager
                        )
                    }
                }
            } else {
            // 1. 生成备份
            ExportBackupSection(
                onExportBackup = settingsViewModel::exportBackup,
                dialogManager = dialogManager
            )
            
            // 2. 导入备份
            ImportBackupSection(
                onRestoreBackup = settingsViewModel::restoreBackup,
                dialogManager = dialogManager
            )
            
            // 3. 备份管理
            ManageBackupsSection(
                localBackups = localBackups,
                onRestoreBackup = settingsViewModel::restoreBackup,
                onDeleteBackup = settingsViewModel::deleteLocalBackup,
                onRefreshBackups = settingsViewModel::loadLocalBackups,
                dialogManager = dialogManager
            )
            } // else
            MiniPlayerSafeSpacer(height = 56.dp)
        }
    }
}

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
private fun ExportBackupSection(
    onExportBackup: ((String) -> Unit, (String) -> Unit) -> Unit,
    dialogManager: DialogManager
) {
    val context = LocalContext.current
    
    TitleWidget(title = stringResource(R.string.export_backup)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.export_backup_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
            
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onExportBackup({ filePath ->
                        val file = File(filePath)
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, context.getString(R.string.export_backup)))
                    }, { error ->
                        dialogManager.showMessage(error)
                    })
                }
            ) {
                Icon(painter = painterResource(R.drawable.externaldrive), contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.export_backup))
            }
        }
    }
}

@Composable
private fun ImportBackupSection(
    onRestoreBackup: (String, () -> Unit, (String) -> Unit) -> Unit,
    dialogManager: DialogManager
) {
    val context = LocalContext.current
    var showRestoreDialog by remember { mutableStateOf(false) }
    var selectedBackupFilePath by remember { mutableStateOf<String?>(null) }
    
    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val tempFile = File(context.cacheDir, "restore_temp.json")
                val outputStream = FileOutputStream(tempFile)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                selectedBackupFilePath = tempFile.absolutePath
                showRestoreDialog = true
            } catch (e: Exception) {
                dialogManager.showMessage(context.getString(R.string.scan_error, e.message ?: ""))
            }
        }
    }

    TitleWidget(title = stringResource(R.string.restore_backup)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.restore_backup_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    restoreLauncher.launch(arrayOf("application/json"))
                }
            ) {
                Icon(painter = painterResource(R.drawable.ic_gallery_material_select_checkbox), contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.restore_backup))
            }
        }
    }
    
    if (showRestoreDialog && selectedBackupFilePath != null) {
        RestoreConfirmDialog(
            onConfirm = {
                onRestoreBackup(selectedBackupFilePath!!, {
                    dialogManager.showMessage(context.getString(R.string.restore_successful))
                }, { error ->
                    dialogManager.showMessage(error)
                })
                showRestoreDialog = false
            },
            onDismiss = { showRestoreDialog = false }
        )
    }
}

@Composable
private fun ManageBackupsSection(
    localBackups: List<String>,
    onRestoreBackup: (String, () -> Unit, (String) -> Unit) -> Unit,
    onDeleteBackup: (String) -> Unit,
    onRefreshBackups: () -> Unit,
    dialogManager: DialogManager
) {
    val context = LocalContext.current
    var showRestoreDialog by remember { mutableStateOf(false) }
    var selectedBackupFilePath by remember { mutableStateOf<String?>(null) }

    TitleWidget(title = stringResource(R.string.local_backup)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.local_backup_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = onRefreshBackups) {
                    Icon(
                        painter = painterResource(R.drawable.externaldrive),
                        contentDescription = stringResource(R.string.refresh),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (localBackups.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_local_backups),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
                )
            } else {
                localBackups.forEach { filePath ->
                    BackupItem(
                        filePath = filePath,
                        onRestore = {
                            selectedBackupFilePath = filePath
                            showRestoreDialog = true
                        },
                        onDelete = { onDeleteBackup(filePath) }
                    )
                }
            }
        }
    }
    
    if (showRestoreDialog && selectedBackupFilePath != null) {
        RestoreConfirmDialog(
            onConfirm = {
                onRestoreBackup(selectedBackupFilePath!!, {
                    dialogManager.showMessage(context.getString(R.string.restore_successful))
                }, { error ->
                    dialogManager.showMessage(error)
                })
                showRestoreDialog = false
            },
            onDismiss = { showRestoreDialog = false }
        )
    }
}

@Composable
private fun BackupItem(
    filePath: String,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    val file = File(filePath)
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(file.lastModified())),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onRestore) {
                Icon(
                    painter = painterResource(R.drawable.ic_gallery_material_select_checkbox),
                    contentDescription = stringResource(R.string.restore_action),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    painter = painterResource(R.drawable.trash),
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun RestoreConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.confirm_restore)) },
        text = { Text(stringResource(R.string.confirm_restore)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.restore_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_action))
            }
        }
    )
}
