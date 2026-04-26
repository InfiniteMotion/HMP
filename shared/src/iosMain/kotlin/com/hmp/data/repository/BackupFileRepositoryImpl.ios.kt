package com.hmp.data.repository

import com.hmp.data.database.currentTimeMillis
import com.hmp.domain.backup.BackupFileRepository
import com.hmp.domain.backup.UserBackupSnapshot
import kotlinx.coroutines.Dispatchers

class BackupFileRepositoryImpl : BackupFileRepository {

    override suspend fun saveBackup(snapshot: UserBackupSnapshot): Result<String> = Result.success("")
    override suspend fun loadBackup(filePath: String): Result<UserBackupSnapshot> = Result.failure(NotImplementedError())
    override suspend fun getBackups(): Result<List<String>> = Result.success(emptyList())
    override suspend fun deleteBackup(filePath: String): Result<Unit> = Result.success(Unit)
}
