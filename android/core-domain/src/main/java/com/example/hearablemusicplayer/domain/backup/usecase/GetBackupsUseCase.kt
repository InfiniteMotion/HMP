package com.example.hearablemusicplayer.domain.backup.usecase

import com.example.hearablemusicplayer.domain.backup.BackupFileRepository
import java.io.File
import javax.inject.Inject

class GetBackupsUseCase @Inject constructor(
    private val backupFileRepository: BackupFileRepository
) {
    suspend operator fun invoke(): Result<List<File>> {
        return backupFileRepository.getBackups()
    }
}
