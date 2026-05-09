package com.hmp.data.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hmp.domain.backup.BackupFileRepository
import com.hmp.domain.backup.UserBackupSnapshot
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupFileRepositoryImpl(
    private val context: Context,
    private val gson: Gson
) : BackupFileRepository {

    private val backupDir by lazy {
        File(context.filesDir, "backups").apply {
            if (!exists()) mkdirs()
        }
    }

    override suspend fun saveBackup(snapshot: UserBackupSnapshot): Result<String> {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())
            val filename = "hearable-backup-v${snapshot.version}-$timestamp.json"
            val file = File(backupDir, filename)

            FileWriter(file).use { writer ->
                gson.toJson(snapshot, writer)
            }
            Log.i("BackupFileRepository", "Backup saved to ${file.absolutePath}")
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Log.e("BackupFileRepository", "Failed to save backup", e)
            Result.failure(e)
        }
    }

    override suspend fun loadBackup(filePath: String): Result<UserBackupSnapshot> {
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                return Result.failure(Exception("Backup file not found"))
            }
            FileReader(file).use { reader ->
                val type = object : TypeToken<UserBackupSnapshot>() {}.type
                val snapshot = gson.fromJson<UserBackupSnapshot>(reader, type)
                Result.success(snapshot)
            }
        } catch (e: Exception) {
            Log.e("BackupFileRepository", "Failed to load backup", e)
            Result.failure(e)
        }
    }

    override suspend fun getBackups(): Result<List<String>> {
        return try {
            val files = backupDir.listFiles { file ->
                file.name.startsWith("hearable-backup-") && file.name.endsWith(".json")
            }?.sortedByDescending { it.lastModified() }?.map { it.absolutePath } ?: emptyList()
            Result.success(files)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteBackup(filePath: String): Result<Unit> {
         return try {
            val file = File(filePath)
            if (file.exists() && file.delete()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete file"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}