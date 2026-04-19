package com.example.hearablemusicplayer.domain.backup.usecase

import com.example.hearablemusicplayer.domain.backup.BackupFileRepository
import java.io.File
import javax.inject.Inject

class DeleteBackupUseCase @Inject constructor(
    private val backupFileRepository: BackupFileRepository
) {
    suspend operator fun invoke(file: File): Result<Unit> {
        return backupFileRepository.deleteBackup(file)
    }
}
