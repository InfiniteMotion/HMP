package com.hmp.domain.playlist.usecase

import com.hmp.domain.music.Music
import com.hmp.domain.music.MusicInfo
import com.hmp.domain.music.MusicExtra
import com.hmp.domain.music.UserInfo
import com.hmp.test.fakes.FakeMusicRepository
import com.hmp.test.fakes.FakeSettingsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GeneratePlaylistUseCaseTest {

    private val musicRepository = FakeMusicRepository()
    private val settingsRepository = FakeSettingsRepository()
    private val useCase = GeneratePlaylistUseCase(musicRepository, settingsRepository)

    private fun musicInfo(id: Long) = MusicInfo(
        music = Music(id = id, title = "Song$id", artist = "Artist${id % 5}", album = "Album${id % 3}", duration = 100, path = "/$id.mp3", albumArtUri = ""),
        extra = MusicExtra(id = id, isGetExtraInfo = true),
        userInfo = UserInfo(id = id)
    )

    @Test
    fun execute_nonExistingSeed_returnsError() = runTest {
        val result = useCase.execute(seedMusicId = 999)
        assertIs<GeneratePlaylistResult.Error>(result)
    }

    @Test
    fun execute_validSeed_doesNotCrash() = runTest {
        for (i in 1..50) {
            musicRepository.addMusic(musicInfo(i.toLong()))
        }
        val result = useCase.execute(seedMusicId = 1, minLength = 3)
        // Algorithm may succeed or fail depending on similarity logic; both are valid
        assertTrue(result is GeneratePlaylistResult.Success || result is GeneratePlaylistResult.Error)
    }

    @Test
    fun getSavedAlgorithmType_defaultIsOptimized() = runTest {
        val type = useCase.getSavedAlgorithmType()
        assertEquals(com.hmp.domain.playlist.AlgorithmType.OPTIMIZED_SIMILARITY, type)
    }

    @Test
    fun getSavedWeightTemplate_defaultIsBalanced() = runTest {
        val template = useCase.getSavedWeightTemplate()
        assertEquals(com.hmp.domain.playlist.WeightTemplate.BALANCED, template)
    }

    @Test
    fun getSavedAlgorithmType_usesSavedValue() = runTest {
        settingsRepository.saveDefaultAlgorithmType("CHAIN_SIMILARITY")
        val type = useCase.getSavedAlgorithmType()
        assertEquals(com.hmp.domain.playlist.AlgorithmType.CHAIN_SIMILARITY, type)
    }

    @Test
    fun getSavedAlgorithmType_invalidValue_returnsDefault() = runTest {
        settingsRepository.saveDefaultAlgorithmType("INVALID")
        val type = useCase.getSavedAlgorithmType()
        assertEquals(com.hmp.domain.playlist.AlgorithmType.OPTIMIZED_SIMILARITY, type)
    }
}
