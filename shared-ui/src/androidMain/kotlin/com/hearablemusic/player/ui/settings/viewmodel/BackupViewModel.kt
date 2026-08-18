package com.hearablemusic.player.ui.settings.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hearablemusic.player.ui.R
import com.hmp.domain.backup.usecase.DeleteBackupUseCase
import com.hmp.domain.backup.usecase.ExportUserDataBackupUseCase
import com.hmp.domain.backup.usecase.GetBackupsUseCase
import com.hmp.domain.backup.usecase.ImportUserDataBackupUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 备份与恢复设置页的页面级 ViewModel。
 * 从 SettingsViewModel 中拆出，随 BackupSettings 页面生命周期创建/清理。
 */
class BackupViewModel(
    application: Application,
    private val exportUserDataBackupUseCase: ExportUserDataBackupUseCase,
    private val importUserDataBackupUseCase: ImportUserDataBackupUseCase,
    private val getBackupsUseCase: GetBackupsUseCase,
    private val deleteBackupUseCase: DeleteBackupUseCase
) : AndroidViewModel(application) {

    private val _backupResult = MutableStateFlow<String?>(null)
    val backupResult: StateFlow<String?> = _backupResult

    private val _localBackups = MutableStateFlow<List<String>>(emptyList())
    val localBackups: StateFlow<List<String>> = _localBackups

    init {
        loadLocalBackups()
    }

    fun loadLocalBackups() {
        viewModelScope.launch {
            getBackupsUseCase()
                .onSuccess { _localBackups.value = it }
        }
    }

    fun deleteLocalBackup(filePath: String) {
        viewModelScope.launch {
            deleteBackupUseCase(filePath)
                .onSuccess { loadLocalBackups() }
        }
    }

    fun clearBackupResult() {
        _backupResult.value = null
    }

    fun exportBackup(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            exportUserDataBackupUseCase()
                .onSuccess { filePath ->
                    _backupResult.value = getApplication<Application>().getString(R.string.backup_desc) + ": $filePath"
                    loadLocalBackups()
                    onSuccess(filePath)
                }
                .onFailure { e ->
                    _backupResult.value = getApplication<Application>().getString(R.string.backup_failed, e.message ?: "")
                    onError(e.message ?: getApplication<Application>().getString(R.string.unknown_error))
                }
        }
    }

    fun restoreBackup(filePath: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            importUserDataBackupUseCase(filePath)
                .onSuccess {
                    _backupResult.value = getApplication<Application>().getString(R.string.restore_success)
                    onSuccess()
                }
                .onFailure { e ->
                    _backupResult.value = getApplication<Application>().getString(R.string.restore_failed, e.message ?: "")
                    onError(e.message ?: getApplication<Application>().getString(R.string.unknown_error))
                }
        }
    }
}
