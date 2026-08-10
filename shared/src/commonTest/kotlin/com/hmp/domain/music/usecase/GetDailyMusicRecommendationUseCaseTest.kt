package com.hmp.domain.music.usecase

import com.hmp.domain.music.Music
import com.hmp.domain.music.MusicExtra
import com.hmp.domain.music.MusicInfo
import com.hmp.domain.music.UserInfo
import com.hmp.domain.setting.model.DailyMusicInfo
import com.hmp.test.fakes.FakeMusicRepository
import com.hmp.test.fakes.FakePlaylistRepository
import com.hmp.test.fakes.FakeSettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GetDailyMusicRecommendationUseCaseTest {

    private val musicRepository = FakeMusicRepository()
    private val settingsRepository = FakeSettingsRepository()
    private val playlistRepository = FakePlaylistRepository()
    private val musicLabelUseCase = MusicLabelUseCase(musicRepository, playlistRepository)
    private val useCase = GetDailyMusicRecommendationUseCase(musicRepository, settingsRepository, musicLabelUseCase)

    private fun musicInfo(id: Long, hasExtra: Boolean = true) = MusicInfo(
        music = Music(id = id, title = "Song$id", artist = "Artist${id % 5}", album = "Album${id % 3}", duration = 100, path = "/$id.mp3", albumArtUri = ""),
        extra = MusicExtra(id = id, isGetExtraInfo = hasExtra),
        userInfo = UserInfo(id = id)
    )

    private fun dailyMusicInfo() = DailyMusicInfo(
        genre = listOf("Pop"), mood = listOf("Happy"), scenario = listOf("Workout"),
        language = "English", era = "2020s", rewards = "", lyric = "",
        singerIntroduce = "", backgroundIntroduce = "", description = "",
        relevantMusic = "", errorInfo = ""
    )

    private suspend fun addMusicWithExtra(id: Long) {
        musicRepository.addMusic(musicInfo(id))
        musicRepository.insertMusicExtra(id, dailyMusicInfo())
    }

    // ===== Processing state management =====

    @Test
    fun initialProcessingState_isNotPausedNotCancelled() {
        assertFalse(useCase.isPaused())
        assertFalse(useCase.isCancelled())
    }

    @Test
    fun pauseProcessing_setsPaused() {
        useCase.pauseProcessing()
        assertTrue(useCase.isPaused())
        assertFalse(useCase.isCancelled())
    }

    @Test
    fun resumeProcessing_clearsPaused() {
        useCase.pauseProcessing()
        useCase.resumeProcessing()
        assertFalse(useCase.isPaused())
    }

    @Test
    fun cancelProcessing_setsCancelledAndUnpaused() {
        useCase.pauseProcessing()
        useCase.cancelProcessing()
        assertFalse(useCase.isPaused())
        assertTrue(useCase.isCancelled())
    }

    @Test
    fun resetProcessingState_clearsAllFlags() {
        useCase.pauseProcessing()
        useCase.cancelProcessing()
        useCase.resetProcessingState()
        assertFalse(useCase.isPaused())
        assertFalse(useCase.isCancelled())
    }

    // ===== ProcessingResult =====

    @Test
    fun processingResult_isAllSuccess_allSuccess() {
        val result = GetDailyMusicRecommendationUseCase.ProcessingResult(
            totalProcessed = 5, successCount = 5, skippedCount = 0, failedCount = 0
        )
        assertTrue(result.isAllSuccess)
    }

    @Test
    fun processingResult_isAllSuccess_withFailures() {
        val result = GetDailyMusicRecommendationUseCase.ProcessingResult(
            totalProcessed = 5, successCount = 3, skippedCount = 0, failedCount = 2
        )
        assertFalse(result.isAllSuccess)
    }

    @Test
    fun processingResult_isAllSuccess_withSkipped() {
        val result = GetDailyMusicRecommendationUseCase.ProcessingResult(
            totalProcessed = 5, successCount = 3, skippedCount = 2, failedCount = 0
        )
        assertFalse(result.isAllSuccess)
    }

    @Test
    fun processingResult_isAllSuccess_zeroProcessed() {
        val result = GetDailyMusicRecommendationUseCase.ProcessingResult(
            totalProcessed = 0, successCount = 0
        )
        assertFalse(result.isAllSuccess)
    }

    @Test
    fun processingResult_defaults() {
        val result = GetDailyMusicRecommendationUseCase.ProcessingResult()
        assertEquals(0, result.totalProcessed)
        assertEquals(0, result.successCount)
        assertEquals(0, result.skippedCount)
        assertEquals(0, result.failedCount)
        assertTrue(result.errors.isEmpty())
        assertFalse(result.wasCancelled)
    }

    // ===== getRandomMusicWithExtra =====

    @Test
    fun getRandomMusicWithExtra_emptyRepository_returnsNullRecommendation() = runTest {
        val result = useCase.getRandomMusicWithExtra()
        assertNull(result.musicInfo)
        assertNull(result.dailyMusicInfo)
        assertTrue(result.labels.isEmpty())
    }

    @Test
    fun getRandomMusicWithExtra_withMusicAndExtra_returnsRecommendation() = runTest {
        addMusicWithExtra(1)

        val result = useCase.getRandomMusicWithExtra()
        assertNotNull(result.musicInfo)
        assertEquals(1L, result.musicInfo!!.music.id)
        assertNotNull(result.dailyMusicInfo)
    }

    @Test
    fun getRandomMusicWithExtra_onlyNoExtraMusic_returnsNull() = runTest {
        musicRepository.addMusic(musicInfo(1, hasExtra = false))
        musicRepository.addMusic(musicInfo(2, hasExtra = false))

        val result = useCase.getRandomMusicWithExtra()
        assertNull(result.musicInfo)
    }

    // ===== getMusicWithExtraById =====

    @Test
    fun getMusicWithExtraById_existing_returnsRecommendation() = runTest {
        addMusicWithExtra(42)

        val result = useCase.getMusicWithExtraById(42)
        assertNotNull(result)
        assertEquals(42L, result.musicInfo!!.music.id)
        assertNotNull(result.dailyMusicInfo)
    }

    @Test
    fun getMusicWithExtraById_nonExisting_returnsNull() = runTest {
        val result = useCase.getMusicWithExtraById(999)
        assertNull(result)
    }

    @Test
    fun getMusicWithExtraById_musicExistsButNoExtra_returnsNull() = runTest {
        musicRepository.addMusic(musicInfo(10, hasExtra = true))
        // No insertMusicExtra for id=10 -> fake throws -> caught -> returns null
        val result = useCase.getMusicWithExtraById(10)
        assertNull(result)
    }

    // ===== MusicRecommendation data class =====

    @Test
    fun musicRecommendation_labels_defaultEmpty() = runTest {
        val result = useCase.getRandomMusicWithExtra()
        assertTrue(result.labels.isEmpty())
    }

    // ===== validateProviderApiKey =====

    @Test
    fun validateProviderApiKey_fakeReturnsTrue() = runTest {
        val result = useCase.validateProviderApiKey()
        assertTrue(result)
    }

    // ===== getRecentListeningDurations =====

    @Test
    fun getRecentListeningDurations_empty_returnsEmpty() = runTest {
        val durations = useCase.getRecentListeningDurations().first()
        assertTrue(durations.isEmpty())
    }
}
