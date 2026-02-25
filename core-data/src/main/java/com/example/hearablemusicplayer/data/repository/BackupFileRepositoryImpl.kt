package com.example.hearablemusicplayer.data.repository

import android.content.Context
import android.util.Log
import com.example.hearablemusicplayer.domain.backup.BackupFileRepository
import com.example.hearablemusicplayer.domain.backup.UserBackupSnapshot
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class BackupFileRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) : BackupFileRepository {

    private val backupDir by lazy {
        File(context.filesDir, "backups").apply {
            if (!exists()) mkdirs()
        }
    }

    override suspend fun saveBackup(snapshot: UserBackupSnapshot): kotlin.Result<File> {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())
            val filename = "hearable-backup-v${snapshot.version}-$timestamp.json"
            val file = File(backupDir, filename)

            FileWriter(file).use { writer ->
                gson.toJson(snapshot, writer)
            }
            Log.i("BackupFileRepository", "Backup saved to ${file.absolutePath}")
            kotlin.Result.success(file)
        } catch (e: Exception) {
            Log.e("BackupFileRepository", "Failed to save backup", e)
            kotlin.Result.failure(e)
        }
    }

    override suspend fun loadBackup(file: File): kotlin.Result<UserBackupSnapshot> {
        return try {
            if (!file.exists()) {
                return kotlin.Result.failure(Exception("Backup file not found"))
            }
            FileReader(file).use { reader ->
                val type = object : TypeToken<UserBackupSnapshot>() {}.type
                val snapshot = gson.fromJson<UserBackupSnapshot>(reader, type)
                kotlin.Result.success(snapshot)
            }
        } catch (e: Exception) {
            Log.e("BackupFileRepository", "Failed to load backup", e)
            kotlin.Result.failure(e)
        }
    }

    override suspend fun getBackups(): kotlin.Result<List<File>> {
        return try {
            val files = backupDir.listFiles { file ->
                file.name.startsWith("hearable-backup-") && file.name.endsWith(".json")
            }?.sortedByDescending { it.lastModified() }?.toList() ?: emptyList()
            kotlin.Result.success(files)
        } catch (e: Exception) {
            kotlin.Result.failure(e)
        }
    }

    override suspend fun deleteBackup(file: File): kotlin.Result<Unit> {
         return try {
            if (file.exists() && file.delete()) {
                kotlin.Result.success(Unit)
            } else {
                kotlin.Result.failure(Exception("Failed to delete file"))
            }
        } catch (e: Exception) {
            kotlin.Result.failure(e)
        }
    }
}
