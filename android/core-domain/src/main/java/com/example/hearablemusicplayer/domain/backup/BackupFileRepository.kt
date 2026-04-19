package com.example.hearablemusicplayer.domain.backup

import java.io.File

interface BackupFileRepository {
    suspend fun saveBackup(snapshot: UserBackupSnapshot): Result<File>
    suspend fun loadBackup(file: File): Result<UserBackupSnapshot>
    suspend fun getBackups(): Result<List<File>>
    suspend fun deleteBackup(file: File): Result<Unit>
}
