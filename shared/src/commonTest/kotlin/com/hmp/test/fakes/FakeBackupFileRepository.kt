package com.hmp.test.fakes

import com.hmp.domain.backup.BackupFileRepository
import com.hmp.domain.backup.UserBackupSnapshot

class FakeBackupFileRepository : BackupFileRepository {

    private val backups = mutableMapOf<String, UserBackupSnapshot>()
    private var nextId = 1

    override suspend fun saveBackup(snapshot: UserBackupSnapshot): Result<String> {
        val path = "/backup_${nextId++}.json"
        backups[path] = snapshot
        return Result.success(path)
    }

    override suspend fun loadBackup(filePath: String): Result<UserBackupSnapshot> {
        val snapshot = backups[filePath]
        return if (snapshot != null) {
            Result.success(snapshot)
        } else {
            Result.failure(IllegalArgumentException("Backup not found: $filePath"))
        }
    }

    override suspend fun getBackups(): Result<List<String>> {
        return Result.success(backups.keys.toList().sorted())
    }

    override suspend fun deleteBackup(filePath: String): Result<Unit> {
        return if (backups.remove(filePath) != null) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("Backup not found: $filePath"))
        }
    }
}
