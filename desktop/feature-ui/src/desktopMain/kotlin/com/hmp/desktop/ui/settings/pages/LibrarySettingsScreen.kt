package com.hmp.desktop.ui.settings.pages
import com.hmp.desktop.ui.common.navigation.NavController

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject


import com.hmp.desktop.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.painterResource
import com.hmp.desktop.ui.player.components.MiniPlayerSafeSpacer
import com.hmp.desktop.ui.common.components.base.TitleWidget
import com.hmp.desktop.ui.common.pages.base.SubScreen
import com.hmp.desktop.ui.library.viewmodel.FolderInfo
import com.hmp.desktop.ui.library.viewmodel.HiddenFolderInfo
import com.hmp.desktop.ui.library.viewmodel.LibraryViewModel
import com.hmp.desktop.ui.common.util.DesktopFilePicker
import com.hmp.domain.setting.model.ScanDirectoryConfig

@Composable
fun LibrarySettingsScreen(
    navController: NavController,
    libraryViewModel: LibraryViewModel = koinInject()
) {
    val musicCount by libraryViewModel.musicCount.collectAsState(initial = 0)
    val analyzedCount by libraryViewModel.musicWithExtraCount.collectAsState(initial = 0)
    val scannedFolders by libraryViewModel.scannedFolders.collectAsState()
    val hiddenFolders by libraryViewModel.hiddenFolders.collectAsState()
    val isScanning by libraryViewModel.isScanning.collectAsState(initial = false)
    val scanDirectoryConfig by libraryViewModel.scanDirectoryConfig.collectAsState()

    LaunchedEffect(Unit) {
        libraryViewModel.loadHiddenFolders()
    }

    SubScreen(
        onBackClick = { navController.popBackStack() },
        title = stringResource(Res.string.library_settings)
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. 音乐库统计
            LibraryStatsSection(
                musicCount = musicCount,
                analyzedCount = analyzedCount
            )

            // 2. 扫描目录
            ScanDirectoriesSection(
                scanDirectories = scanDirectoryConfig.scanDirectories,
                onAddDirectory = { libraryViewModel.addScanDirectory(it) },
                onRemoveDirectory = { libraryViewModel.removeScanDirectory(it) }
            )

            // 3. 已屏蔽目录
            BlockedDirectoriesSection(
                blockedDirectories = scanDirectoryConfig.blockedDirectories,
                onAddDirectory = { libraryViewModel.addBlockedDirectory(it) },
                onRemoveDirectory = { libraryViewModel.removeBlockedDirectory(it) }
            )

            // 4. 扫描选项
            ScanOptionsSection(
                isScanning = isScanning,
                onIncrementalScan = libraryViewModel::refreshMusicList,
                onFullRescan = libraryViewModel::fullRescan
            )

            // 5. 音乐库管理
            LibraryManagementSection(
                folders = scannedFolders,
                hiddenFolders = hiddenFolders,
                onHideFolder = libraryViewModel::hideFolder,
                onUnhideFolder = libraryViewModel::restoreToLibrary
            )
            MiniPlayerSafeSpacer(height = 56.dp)
        }
    }
}

@Composable
private fun ScanDirectoriesSection(
    scanDirectories: List<String>,
    onAddDirectory: (String) -> Unit,
    onRemoveDirectory: (String) -> Unit
) {
    TitleWidget(title = stringResource(Res.string.scan_directories)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (scanDirectories.isEmpty()) {
                Text(
                    text = stringResource(Res.string.no_scan_directories_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                scanDirectories.forEach { path ->
                    DirectoryItem(
                        path = path,
                        onRemoveClick = { onRemoveDirectory(path) }
                    )
                }
            }
            TextButton(
                onClick = {
                    DesktopFilePicker.pickDirectory()?.let { onAddDirectory(it) }
                }
            ) {
                Icon(
                    painter = painterResource(Res.drawable.plus),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(Res.string.add_directory))
            }
        }
    }
}

@Composable
private fun BlockedDirectoriesSection(
    blockedDirectories: List<String>,
    onAddDirectory: (String) -> Unit,
    onRemoveDirectory: (String) -> Unit
) {
    TitleWidget(title = stringResource(Res.string.blocked_directories)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (blockedDirectories.isEmpty()) {
                Text(
                    text = stringResource(Res.string.no_blocked_directories_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                blockedDirectories.forEach { path ->
                    DirectoryItem(
                        path = path,
                        onRemoveClick = { onRemoveDirectory(path) },
                        removeLabel = stringResource(Res.string.unblock_directory)
                    )
                }
            }
            TextButton(
                onClick = {
                    DesktopFilePicker.pickDirectory()?.let { onAddDirectory(it) }
                }
            ) {
                Icon(
                    painter = painterResource(Res.drawable.plus),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(Res.string.block_directory))
            }
        }
    }
}

@Composable
private fun DirectoryItem(
    path: String,
    onRemoveClick: () -> Unit,
    removeLabel: String = stringResource(Res.string.remove_directory)
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.rectangle_on_rectangle),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = path,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onRemoveClick) {
                Text(removeLabel, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun LibraryManagementSection(
    folders: List<FolderInfo>,
    hiddenFolders: List<HiddenFolderInfo>,
    onHideFolder: (String) -> Unit,
    onUnhideFolder: (List<Long>) -> Unit
) {
    var folderToHide by remember { mutableStateOf<String?>(null) }
    TitleWidget(title = stringResource(Res.string.library_management)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(Res.string.scanned_folders),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (folders.isEmpty()) {
                Text(
                    text = stringResource(Res.string.no_scanned_folders),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
                )
            } else {
                folders.forEach { folder ->
                    FolderItem(
                        folder = folder,
                        onHideClick = { folderToHide = folder.path }
                    )
                }
            }
            if (hiddenFolders.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.hidden_folders),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                hiddenFolders.forEach { hidden ->
                    HiddenFolderItem(
                        hidden = hidden,
                        onUnhideClick = { onUnhideFolder(hidden.musicIds) }
                    )
                }
            }
        }
    }
    if (folderToHide != null) {
        AlertDialog(
            onDismissRequest = { folderToHide = null },
            title = { Text(stringResource(Res.string.confirm_hide_folder)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        folderToHide?.let { onHideFolder(it) }
                        folderToHide = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(Res.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { folderToHide = null }) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun FolderItem(
    folder: FolderInfo,
    onHideClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.rectangle_on_rectangle),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folder.path,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(Res.string.folder_songs_count, folder.songCount),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onHideClick) {
                Text(stringResource(Res.string.hide_folder), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun HiddenFolderItem(
    hidden: HiddenFolderInfo,
    onUnhideClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.rectangle_on_rectangle),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = hidden.path,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(Res.string.folder_songs_count, hidden.songCount),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
            TextButton(onClick = onUnhideClick) {
                Text(stringResource(Res.string.unhide_folder), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
@Composable
private fun LibraryStatsSection(
    musicCount: Int,
    analyzedCount: Int
) {
    TitleWidget(title = stringResource(Res.string.library_stats)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatsCard(
                title = stringResource(Res.string.total_songs),
                value = musicCount.toString(),
                icon = Res.drawable.music_note_list,
                modifier = Modifier.weight(1f)
            )
            StatsCard(
                title = stringResource(Res.string.analyzed_songs),
                value = analyzedCount.toString(),
                icon = Res.drawable.media_center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatsCard(
    title: String,
    value: String,
    icon: DrawableResource,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ScanOptionsSection(
    isScanning: Boolean,
    onIncrementalScan: () -> Unit,
    onFullRescan: () -> Unit
) {
    var showFullRescanDialog by remember { mutableStateOf(false) }

    TitleWidget(title = stringResource(Res.string.scan_options)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 增量扫描选项
                ScanOptionCard(
                    title = stringResource(Res.string.incremental_load),
                    description = stringResource(Res.string.incremental_scan_desc),
                    icon = Res.drawable.magnifyingglass,
                    onClick = onIncrementalScan,
                    enabled = !isScanning,
                    modifier = Modifier.weight(1f)
                )
                
                // 全量重建选项
                ScanOptionCard(
                    title = stringResource(Res.string.full_rescan),
                    description = stringResource(Res.string.full_rescan_desc),
                    icon = Res.drawable.trash,
                    onClick = { showFullRescanDialog = true },
                    enabled = !isScanning,
                    isDestructive = true,
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (isScanning) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.scanning),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
    
    if (showFullRescanDialog) {
        AlertDialog(
            onDismissRequest = { showFullRescanDialog = false },
            title = { Text(stringResource(Res.string.confirm_full_rescan)) },
            text = { Text(stringResource(Res.string.full_rescan_warning)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onFullRescan()
                        showFullRescanDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(Res.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showFullRescanDialog = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun ScanOptionCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    icon: DrawableResource,
    onClick: () -> Unit,
    enabled: Boolean,
    isDestructive: Boolean = false
) {
    Card(
        modifier = modifier
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = when {
                    !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    isDestructive -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isDestructive && enabled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                minLines = 3,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
