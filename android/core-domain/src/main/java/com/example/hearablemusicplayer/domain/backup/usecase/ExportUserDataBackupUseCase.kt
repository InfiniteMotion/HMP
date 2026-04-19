package com.example.hearablemusicplayer.domain.backup.usecase

import com.example.hearablemusicplayer.domain.backup.BackupFileRepository
import com.example.hearablemusicplayer.domain.backup.UserBackupSnapshot
import com.example.hearablemusicplayer.domain.music.MusicRepository
import com.example.hearablemusicplayer.domain.playlist.PlaylistRepository
import com.example.hearablemusicplayer.domain.setting.SettingsRepository
import java.io.File
import javax.inject.Inject

class ExportUserDataBackupUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val musicRepository: MusicRepository,
    private val playlistRepository: PlaylistRepository,
    private val backupFileRepository: BackupFileRepository
) {
    suspend operator fun invoke(): Result<File> {
        return try {
            val appSettings = settingsRepository.exportAppSettingsSnapshot()
            val musicUserState = musicRepository.exportMusicUserStateSnapshot()
            val listeningStats = musicRepository.exportListeningStatsSnapshot()
            val playlists = playlistRepository.exportPlaylistsSnapshot()
            val dailyRecommendation = settingsRepository.exportDailyRecommendationSnapshot()

            val snapshot = UserBackupSnapshot(
                createdAt = System.currentTimeMillis(),
                appSettings = appSettings,
                musicUserState = musicUserState,
                listeningStats = listeningStats,
                playlists = playlists,
                dailyRecommendation = dailyRecommendation
            )

            backupFileRepository.saveBackup(snapshot)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
