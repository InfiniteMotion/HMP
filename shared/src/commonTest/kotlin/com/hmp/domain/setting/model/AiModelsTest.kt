package com.hmp.domain.setting.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AiEndpointConfigTest {

    @Test
    fun defaultValues() {
        val config = AiEndpointConfig()
        assertEquals("", config.endpoint)
        assertEquals("", config.apiKey)
        assertEquals("", config.selectedModel)
        assertTrue(config.availableModels.isEmpty())
        assertFalse(config.isConfigured)
    }

    @Test
    fun customValues() {
        val config = AiEndpointConfig(
            endpoint = "https://api.example.com/v1",
            apiKey = "sk-test-key",
            selectedModel = "gpt-4o",
            availableModels = listOf("gpt-4o", "gpt-4o-mini"),
            isConfigured = true
        )
        assertEquals("https://api.example.com/v1", config.endpoint)
        assertEquals("sk-test-key", config.apiKey)
        assertEquals("gpt-4o", config.selectedModel)
        assertEquals(2, config.availableModels.size)
        assertTrue(config.isConfigured)
    }
}

class AiAccessModeTest {

    @Test
    fun allModes_exist() {
        val modes = AiAccessMode.entries
        assertEquals(3, modes.size)
        assertEquals(AiAccessMode.FREE, modes[0])
        assertEquals(AiAccessMode.CUSTOM, modes[1])
        assertEquals(AiAccessMode.PAID, modes[2])
    }
}

class DailyMusicInfoTest {

    @Test
    fun construction() {
        val info = DailyMusicInfo(
            genre = listOf("Rock", "Pop"),
            mood = listOf("Energetic"),
            scenario = listOf("Workout"),
            language = "English",
            era = "2020s",
            rewards = "Great song",
            lyric = "La la la",
            singerIntroduce = "A singer",
            backgroundIntroduce = "Background info",
            description = "A description",
            relevantMusic = "Similar songs",
            errorInfo = ""
        )
        assertEquals(2, info.genre.size)
        assertEquals(1, info.mood.size)
        assertEquals("English", info.language)
        assertEquals("2020s", info.era)
    }

    @Test
    fun errorInfo_isMutable() {
        val info = DailyMusicInfo(
            genre = emptyList(),
            mood = emptyList(),
            scenario = emptyList(),
            language = "",
            era = "",
            rewards = "",
            lyric = "",
            singerIntroduce = "",
            backgroundIntroduce = "",
            description = "",
            relevantMusic = "",
            errorInfo = ""
        )
        info.errorInfo = "Some error"
        assertEquals("Some error", info.errorInfo)
    }
}