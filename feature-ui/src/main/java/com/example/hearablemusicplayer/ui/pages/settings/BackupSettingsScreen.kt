package com.example.hearablemusicplayer.ui.pages.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.widget.Toast
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.hearablemusicplayer.ui.R
import com.example.hearablemusicplayer.ui.components.TitleWidget
import com.example.hearablemusicplayer.ui.pages.base.SubScreen
import com.example.hearablemusicplayer.ui.viewmodel.SettingsViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackupSettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val localBackups by settingsViewModel.localBackups.collectAsState()

    SubScreen(
        onBackClick = { navController.popBackStack() },
        title = stringResource(R.string.backup_settings)
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
                onExportBackup = settingsViewModel::exportBackup
            )
            
            // 2. 导入备份
            ImportBackupSection(
                onRestoreBackup = settingsViewModel::restoreBackup
            )
            
            // 3. 备份管理
            ManageBackupsSection(
                localBackups = localBackups,
                onRestoreBackup = settingsViewModel::restoreBackup,
                onDeleteBackup = settingsViewModel::deleteLocalBackup,
                onRefreshBackups = settingsViewModel::loadLocalBackups
            )
        }
    }
}

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
private fun ExportBackupSection(
    onExportBackup: ((File) -> Unit, (String) -> Unit) -> Unit
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
                    onExportBackup({ file ->
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
                        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_backup)))
                    }, { error ->
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
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
    onRestoreBackup: (File, () -> Unit, (String) -> Unit) -> Unit
) {
    val context = LocalContext.current
    var showRestoreDialog by remember { mutableStateOf(false) }
    var selectedBackupFile by remember { mutableStateOf<File?>(null) }
    
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
                selectedBackupFile = tempFile
                showRestoreDialog = true
            } catch (e: Exception) {
                Toast.makeText(context, "Error reading file: ${e.message}", Toast.LENGTH_SHORT).show()
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
    
    if (showRestoreDialog && selectedBackupFile != null) {
        RestoreConfirmDialog(
            onConfirm = {
                onRestoreBackup(selectedBackupFile!!, {
                    Toast.makeText(context, "Restore Successful", Toast.LENGTH_SHORT).show()
                }, { error ->
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                })
                showRestoreDialog = false
            },
            onDismiss = { showRestoreDialog = false }
        )
    }
}

@Composable
private fun ManageBackupsSection(
    localBackups: List<File>,
    onRestoreBackup: (File, () -> Unit, (String) -> Unit) -> Unit,
    onDeleteBackup: (File) -> Unit,
    onRefreshBackups: () -> Unit
) {
    val context = LocalContext.current
    var showRestoreDialog by remember { mutableStateOf(false) }
    var selectedBackupFile by remember { mutableStateOf<File?>(null) }

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
                        contentDescription = "Refresh",
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
                localBackups.forEach { file ->
                    BackupItem(
                        file = file,
                        onRestore = {
                            selectedBackupFile = file
                            showRestoreDialog = true
                        },
                        onDelete = { onDeleteBackup(file) }
                    )
                }
            }
        }
    }
    
    if (showRestoreDialog && selectedBackupFile != null) {
        RestoreConfirmDialog(
            onConfirm = {
                onRestoreBackup(selectedBackupFile!!, {
                    Toast.makeText(context, "Restore Successful", Toast.LENGTH_SHORT).show()
                }, { error ->
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                })
                showRestoreDialog = false
            },
            onDismiss = { showRestoreDialog = false }
        )
    }
}

@Composable
private fun BackupItem(
    file: File,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
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
                    contentDescription = "Restore",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    painter = painterResource(R.drawable.trash),
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
