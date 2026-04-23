package com.hmp.data.repository

import com.hmp.domain.backup.BackupFileRepository
import com.hmp.domain.backup.UserBackupSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

class BackupFileRepositoryImpl : BackupFileRepository {
    private val backupDir: String
        get() {
            val url = NSFileManager.defaultManager.URLForDirectory(
                NSDocumentDirectory,
                NSUserDomainMask,
                null,
                true,
                null
            )
            return requireNotNull(url?.path) + "/backups"
        }

    override suspend fun saveBackup(snapshot: UserBackupSnapshot): Result<String> = withContext(Dispatchers.Default) {
        runCatching {
            val fileManager = NSFileManager.defaultManager
            val dir = backupDir
            // Create backup directory if not exists
            fileManager.createDirectoryAtPath(dir, true, null, null)
            // TODO: Serialize snapshot to JSON and write to file
            val filePath = "$dir/backup_${System.currentTimeMillis()}.json"
            // val json = serialize(snapshot)
            // (json as NSString).writeToFile(filePath, true, NSUTF8StringEncoding, null)
            filePath
        }
    }

    override suspend fun loadBackup(filePath: String): Result<UserBackupSnapshot> = withContext(Dispatchers.Default) {
        runCatching {
            // TODO: Read and deserialize JSON to UserBackupSnapshot
            throw NotImplementedError("Backup deserialization not implemented yet")
        }
    }

    override suspend fun getBackups(): Result<List<String>> = withContext(Dispatchers.Default) {
        runCatching {
            val fileManager = NSFileManager.defaultManager
            val dir = backupDir
            val contents = fileManager.contentsOfDirectoryAtPath(dir, null) ?: emptyList()
            contents.mapNotNull { it as? String }
                .filter { it.endsWith(".json") }
                .map { "$dir/$it" }
                .sortedDescending()
        }
    }

    override suspend fun deleteBackup(filePath: String): Result<Unit> = withContext(Dispatchers.Default) {
        runCatching {
            val fileManager = NSFileManager.defaultManager
            fileManager.removeItemAtPath(filePath, null)
        }
    }
}
