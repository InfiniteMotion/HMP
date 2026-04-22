package com.hmp.data.repository

import com.hmp.domain.backup.BackupFileRepository
import com.hmp.domain.backup.UserBackupSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class BackupFileRepositoryImpl : BackupFileRepository {
    override suspend fun createBackup(): Result<UserBackupSnapshot> {
        return Result.failure(NotImplementedError("Backup not implemented on iOS yet"))
    }

    override suspend fun restoreBackup(backupFilePath: String): Result<Unit> {
        return Result.failure(NotImplementedError("Restore not implemented on iOS yet"))
    }

    override suspend fun listBackups(): Result<List<File>> {
        return Result.failure(NotImplementedError("List backups not implemented on iOS yet"))
    }

    override suspend fun deleteBackup(backupFile: File): Result<Unit> {
        return Result.failure(NotImplementedError("Delete backup not implemented on iOS yet"))
    }

    override suspend fun getBackupDirectory(): File {
        throw NotImplementedError("Get backup directory not implemented on iOS yet")
    }
}