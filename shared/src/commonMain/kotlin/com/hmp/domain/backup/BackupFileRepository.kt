package com.hmp.domain.backup

interface BackupFileRepository {
    suspend fun saveBackup(snapshot: UserBackupSnapshot): Result<String>
    suspend fun loadBackup(filePath: String): Result<UserBackupSnapshot>
    suspend fun getBackups(): Result<List<String>>
    suspend fun deleteBackup(filePath: String): Result<Unit>
}
