package com.hmp.domain.backup.usecase

import com.hmp.domain.backup.UserBackupSnapshot
import com.hmp.test.fakes.FakeBackupFileRepository
import com.hmp.test.fakes.FakeMusicRepository
import com.hmp.test.fakes.FakePlaylistRepository
import com.hmp.test.fakes.FakeSettingsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackupUseCaseTest {

    private val settingsRepository = FakeSettingsRepository()
    private val musicRepository = FakeMusicRepository()
    private val playlistRepository = FakePlaylistRepository()
    private val backupFileRepository = FakeBackupFileRepository()

    private val exportUseCase = ExportUserDataBackupUseCase(settingsRepository, musicRepository, playlistRepository, backupFileRepository)
    private val importUseCase = ImportUserDataBackupUseCase(settingsRepository, musicRepository, playlistRepository, backupFileRepository)
    private val deleteUseCase = DeleteBackupUseCase(backupFileRepository)
    private val getBackupsUseCase = GetBackupsUseCase(backupFileRepository)

    @Test
    fun export_returnsPath() = runTest {
        val result = exportUseCase()
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isNotEmpty())
    }

    @Test
    fun exportAndImport_roundTrip() = runTest {
        val path = exportUseCase().getOrNull()!!
        val importResult = importUseCase(path)
        assertTrue(importResult.isSuccess)
    }

    @Test
    fun getBackups_returnsSavedPaths() = runTest {
        exportUseCase()
        exportUseCase()
        val result = getBackupsUseCase()
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()!!.size)
    }

    @Test
    fun deleteBackup_removesFile() = runTest {
        val path = exportUseCase().getOrNull()!!
        assertEquals(1, getBackupsUseCase().getOrNull()!!.size)
        assertTrue(deleteUseCase(path).isSuccess)
        assertEquals(0, getBackupsUseCase().getOrNull()!!.size)
    }

    @Test
    fun deleteBackup_nonExisting_returnsFailure() = runTest {
        val result = deleteUseCase("/nonexistent.json")
        assertTrue(result.isFailure)
    }

    @Test
    fun importBackup_nonExisting_returnsFailure() = runTest {
        val result = importUseCase("/nonexistent.json")
        assertTrue(result.isFailure)
    }

    @Test
    fun getBackups_emptyRepository_returnsEmptyList() = runTest {
        val result = getBackupsUseCase()
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }
}
