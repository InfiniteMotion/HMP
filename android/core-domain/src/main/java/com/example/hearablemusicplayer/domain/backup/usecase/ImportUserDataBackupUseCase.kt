package com.example.hearablemusicplayer.domain.backup.usecase

import com.example.hearablemusicplayer.domain.backup.BackupFileRepository
import com.example.hearablemusicplayer.domain.music.MusicRepository
import com.example.hearablemusicplayer.domain.playlist.PlaylistRepository
import com.example.hearablemusicplayer.domain.setting.SettingsRepository
import java.io.File
import javax.inject.Inject

class ImportUserDataBackupUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val musicRepository: MusicRepository,
    private val playlistRepository: PlaylistRepository,
    private val backupFileRepository: BackupFileRepository
) {
    suspend operator fun invoke(file: File): Result<Unit> {
        return try {
            val result = backupFileRepository.loadBackup(file)
            if (result.isFailure) {
                return Result.failure(result.exceptionOrNull() ?: Exception("Failed to load backup"))
            }
            val snapshot = result.getOrThrow()

            // Restore in order
            settingsRepository.restoreFromSnapshot(snapshot.appSettings)
            musicRepository.restoreMusicUserState(snapshot.musicUserState)
            musicRepository.restoreListeningStats(snapshot.listeningStats)
            playlistRepository.restoreFromSnapshot(snapshot.playlists)
            
            snapshot.dailyRecommendation?.let {
                settingsRepository.restoreDailyRecommendationSnapshot(it)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
