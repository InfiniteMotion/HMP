package com.hmp.desktop.ui.settings.pages
import com.hmp.desktop.ui.common.navigation.NavController

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
import com.hmp.desktop.ui.common.util.DesktopFilePicker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject


import com.hmp.desktop.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.painterResource
import com.hmp.desktop.ui.player.components.MiniPlayerSafeSpacer
import com.hmp.desktop.ui.common.components.base.TitleWidget
import com.hmp.desktop.ui.common.dialogs.controller.DialogManager
import com.hmp.desktop.ui.common.pages.base.SubScreen
import com.hmp.desktop.ui.common.dialogs.viewmodel.DialogManagerViewModel
import com.hmp.desktop.ui.settings.viewmodel.SettingsViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackupSettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = koinInject(),
    dialogManagerViewModel: DialogManagerViewModel = koinInject()
) {
    val dialogManager = dialogManagerViewModel.dialogManager
    val localBackups by settingsViewModel.localBackups.collectAsState()

    SubScreen(
        onBackClick = { navController.popBackStack() },
        title = stringResource(Res.string.backup_settings)
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
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
            MiniPlayerSafeSpacer(height = 56.dp)
        }
    }
}

@Composable
private fun ExportBackupSection(
    onExportBackup: ((String) -> Unit, (String) -> Unit) -> Unit,
    dialogManager: DialogManager
) {
    TitleWidget(title = stringResource(Res.string.export_backup)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.export_backup_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onExportBackup({ filePath ->
                        // Desktop: backup file is saved to disk; notify user of the path
                        dialogManager.showMessage("Backup exported to: $filePath")
                    }, { error ->
                        dialogManager.showMessage(error)
                    })
                }
            ) {
                Icon(painter = painterResource(Res.drawable.externaldrive), contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(Res.string.export_backup))
            }
        }
    }
}

@Composable
private fun ImportBackupSection(
    onRestoreBackup: (String, () -> Unit, (String) -> Unit) -> Unit,
    dialogManager: DialogManager
) {
    var showRestoreDialog by remember { mutableStateOf(false) }
    var selectedBackupFilePath by remember { mutableStateOf<String?>(null) }

    TitleWidget(title = stringResource(Res.string.restore_backup)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(Res.string.restore_backup_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val path = DesktopFilePicker.pickBackupFile()
                    if (path != null) {
                        selectedBackupFilePath = path
                        showRestoreDialog = true
                    }
                }
            ) {
                Icon(painter = painterResource(Res.drawable.ic_gallery_material_select_checkbox), contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(Res.string.restore_backup))
            }
        }
    }

    if (showRestoreDialog && selectedBackupFilePath != null) {
        RestoreConfirmDialog(
            onConfirm = {
                onRestoreBackup(selectedBackupFilePath!!, {
                    dialogManager.showMessage("Restore Successful")
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
    var showRestoreDialog by remember { mutableStateOf(false) }
    var selectedBackupFilePath by remember { mutableStateOf<String?>(null) }

    TitleWidget(title = stringResource(Res.string.local_backup)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(Res.string.local_backup_desc),
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
                        painter = painterResource(Res.drawable.externaldrive),
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (localBackups.isEmpty()) {
                Text(
                    text = stringResource(Res.string.no_local_backups),
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
                    dialogManager.showMessage("Restore Successful")
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
                    painter = painterResource(Res.drawable.ic_gallery_material_select_checkbox),
                    contentDescription = "Restore",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    painter = painterResource(Res.drawable.trash),
                    contentDescription = "Delete",
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
        title = { Text("Confirm Restore") },
        text = { Text("Restoring will overwrite current data. Are you sure?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Restore")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
