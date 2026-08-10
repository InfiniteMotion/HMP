package com.hmp.domain.setting.usecase

import com.hmp.domain.config.DisplayMode
import com.hmp.domain.config.LyricsAlignment
import com.hmp.domain.lyrics.LyricsComponent
import com.hmp.domain.lyrics.LyricsComponentConfig
import com.hmp.test.fakes.FakeSettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LyricsSettingsUseCaseTest {

    private val settingsRepository = FakeSettingsRepository()
    private val useCase = LyricsSettingsUseCase(settingsRepository)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    // ===== getComponentConfig =====

    @Test
    fun getComponentConfig_player_defaultIsEmptyJson_returnsDefault() = runTest {
        val config = useCase.getComponentConfig(LyricsComponent.PLAYER).first()
        assertEquals(LyricsComponentConfig.DEFAULT, config)
    }

    @Test
    fun getComponentConfig_savedConfig_returnsParsedConfig() = runTest {
        val custom = LyricsComponentConfig(
            originalTextSize = 20,
            translatedTextSize = 18,
            currentTimeTextSize = 22,
            lineSpacing = 10,
            displayMode = DisplayMode.LANG1,
            alignment = LyricsAlignment.LEFT
        )
        val jsonStr = json.encodeToString(LyricsComponentConfig.serializer(), custom)
        settingsRepository.saveLyricsPlayerConfig(jsonStr)

        val config = useCase.getComponentConfig(LyricsComponent.PLAYER).first()
        assertEquals(20, config.originalTextSize)
        assertEquals(18, config.translatedTextSize)
        assertEquals(22, config.currentTimeTextSize)
        assertEquals(10, config.lineSpacing)
        assertEquals(DisplayMode.LANG1, config.displayMode)
        assertEquals(LyricsAlignment.LEFT, config.alignment)
    }

    @Test
    fun getComponentConfig_fullscreen_savedConfig() = runTest {
        val custom = LyricsComponentConfig(originalTextSize = 24, displayMode = DisplayMode.DUAL)
        settingsRepository.saveLyricsFullscreenConfig(json.encodeToString(LyricsComponentConfig.serializer(), custom))

        val config = useCase.getComponentConfig(LyricsComponent.FULLSCREEN).first()
        assertEquals(24, config.originalTextSize)
        assertEquals(DisplayMode.DUAL, config.displayMode)
    }

    @Test
    fun getComponentConfig_floating_savedConfig() = runTest {
        val custom = LyricsComponentConfig(originalTextSize = 12, alignment = LyricsAlignment.RIGHT)
        settingsRepository.saveLyricsFloatingConfig(json.encodeToString(LyricsComponentConfig.serializer(), custom))

        val config = useCase.getComponentConfig(LyricsComponent.FLOATING).first()
        assertEquals(12, config.originalTextSize)
        assertEquals(LyricsAlignment.RIGHT, config.alignment)
    }

    @Test
    fun getComponentConfig_invalidJson_returnsDefault() = runTest {
        settingsRepository.saveLyricsPlayerConfig("not valid json!!!")

        val config = useCase.getComponentConfig(LyricsComponent.PLAYER).first()
        assertEquals(LyricsComponentConfig.DEFAULT, config)
    }

    // ===== saveComponentConfig =====

    @Test
    fun saveComponentConfig_player_persistsToRepository() = runTest {
        val config = LyricsComponentConfig(originalTextSize = 30, lineSpacing = 8)
        useCase.saveComponentConfig(LyricsComponent.PLAYER, config)

        val saved = settingsRepository.getLyricsPlayerConfig()
        val parsed = json.decodeFromString(LyricsComponentConfig.serializer(), saved)
        assertEquals(30, parsed.originalTextSize)
        assertEquals(8, parsed.lineSpacing)
    }

    @Test
    fun saveComponentConfig_fullscreen_persistsToRepository() = runTest {
        val config = LyricsComponentConfig(displayMode = DisplayMode.LANG1)
        useCase.saveComponentConfig(LyricsComponent.FULLSCREEN, config)

        val parsed = json.decodeFromString(LyricsComponentConfig.serializer(), settingsRepository.getLyricsFullscreenConfig())
        assertEquals(DisplayMode.LANG1, parsed.displayMode)
    }

    @Test
    fun saveComponentConfig_floating_persistsToRepository() = runTest {
        val config = LyricsComponentConfig(alignment = LyricsAlignment.LEFT)
        useCase.saveComponentConfig(LyricsComponent.FLOATING, config)

        val parsed = json.decodeFromString(LyricsComponentConfig.serializer(), settingsRepository.getLyricsFloatingConfig())
        assertEquals(LyricsAlignment.LEFT, parsed.alignment)
    }

    // ===== getAllComponentConfigs =====

    @Test
    fun getAllComponentConfigs_default_returnsDefaultsForAll() = runTest {
        val allConfigs = useCase.getAllComponentConfigs()
        assertEquals(3, allConfigs.size)
        LyricsComponent.entries.forEach { component ->
            assertEquals(LyricsComponentConfig.DEFAULT, allConfigs[component])
        }
    }

    @Test
    fun getAllComponentConfigs_withSavedValues_returnsCorrectly() = runTest {
        val playerConfig = LyricsComponentConfig(originalTextSize = 20)
        val fullscreenConfig = LyricsComponentConfig(originalTextSize = 24)
        settingsRepository.saveLyricsPlayerConfig(json.encodeToString(LyricsComponentConfig.serializer(), playerConfig))
        settingsRepository.saveLyricsFullscreenConfig(json.encodeToString(LyricsComponentConfig.serializer(), fullscreenConfig))

        val allConfigs = useCase.getAllComponentConfigs()
        assertEquals(20, allConfigs[LyricsComponent.PLAYER]?.originalTextSize)
        assertEquals(24, allConfigs[LyricsComponent.FULLSCREEN]?.originalTextSize)
        assertEquals(LyricsComponentConfig.DEFAULT, allConfigs[LyricsComponent.FLOATING])
    }

    // ===== resolveConfig =====

    @Test
    fun resolveConfig_noLinkedTo_returnsOwnConfig() = runTest {
        val config = LyricsComponentConfig(originalTextSize = 20)
        settingsRepository.saveLyricsPlayerConfig(json.encodeToString(LyricsComponentConfig.serializer(), config))

        val resolved = useCase.resolveConfig(LyricsComponent.PLAYER)
        assertEquals(20, resolved.originalTextSize)
    }

    @Test
    fun resolveConfig_withLinkedTo_followsLink() = runTest {
        val playerConfig = LyricsComponentConfig(originalTextSize = 20)
        val fullscreenConfig = LyricsComponentConfig(linkedTo = "player")
        settingsRepository.saveLyricsPlayerConfig(json.encodeToString(LyricsComponentConfig.serializer(), playerConfig))
        settingsRepository.saveLyricsFullscreenConfig(json.encodeToString(LyricsComponentConfig.serializer(), fullscreenConfig))

        val resolved = useCase.resolveConfig(LyricsComponent.FULLSCREEN)
        assertEquals(20, resolved.originalTextSize)
    }

    @Test
    fun resolveConfig_circularLink_returnsOwnConfig() = runTest {
        val playerConfig = LyricsComponentConfig(originalTextSize = 20, linkedTo = "fullscreen")
        val fullscreenConfig = LyricsComponentConfig(originalTextSize = 24, linkedTo = "floating")
        settingsRepository.saveLyricsPlayerConfig(json.encodeToString(LyricsComponentConfig.serializer(), playerConfig))
        settingsRepository.saveLyricsFullscreenConfig(json.encodeToString(LyricsComponentConfig.serializer(), fullscreenConfig))

        val resolved = useCase.resolveConfig(LyricsComponent.PLAYER)
        assertEquals(20, resolved.originalTextSize)
    }

    // ===== resetComponentToDefault =====

    @Test
    fun resetComponentToDefault_resetsSavedConfig() = runTest {
        val custom = LyricsComponentConfig(originalTextSize = 99)
        useCase.saveComponentConfig(LyricsComponent.PLAYER, custom)
        assertEquals(99, useCase.getComponentConfig(LyricsComponent.PLAYER).first().originalTextSize)

        useCase.resetComponentToDefault(LyricsComponent.PLAYER)
        val config = useCase.getComponentConfig(LyricsComponent.PLAYER).first()
        assertEquals(LyricsComponentConfig.DEFAULT, config)
    }

    // ===== floatingLyricsEnabled =====

    @Test
    fun floatingLyricsEnabled_defaultIsFalse() = runTest {
        val enabled = useCase.floatingLyricsEnabled.first()
        assertFalse(enabled)
    }

    @Test
    fun floatingLyricsEnabled_saveAndRetrieve() = runTest {
        useCase.saveFloatingLyricsEnabled(true)
        assertTrue(useCase.floatingLyricsEnabled.first())

        useCase.saveFloatingLyricsEnabled(false)
        assertFalse(useCase.floatingLyricsEnabled.first())
    }

    // ===== Legacy API =====

    @Test
    fun legacy_saveOriginalTextSize_persists() = runTest {
        useCase.saveOriginalTextSize(25)
        assertEquals(25, settingsRepository.getLyricsOriginalTextSize())
    }

    @Test
    fun legacy_saveTranslatedTextSize_persists() = runTest {
        useCase.saveTranslatedTextSize(16)
        assertEquals(16, settingsRepository.getLyricsTranslatedTextSize())
    }

    @Test
    fun legacy_saveLineSpacing_persists() = runTest {
        useCase.saveLineSpacing(12)
        assertEquals(12, settingsRepository.getLyricsLineSpacing())
    }

    @Test
    fun legacy_saveDisplayMode_persists() = runTest {
        useCase.saveDisplayMode(DisplayMode.LANG1)
        assertEquals(DisplayMode.LANG1, settingsRepository.getLyricsDisplayMode())
    }

    @Test
    fun legacy_saveAlignment_persists() = runTest {
        useCase.saveAlignment(LyricsAlignment.LEFT)
        assertEquals(LyricsAlignment.LEFT, settingsRepository.getLyricsAlignment())
    }

    @Test
    fun legacy_getLyricsConfig_returnsAllFields() = runTest {
        useCase.saveOriginalTextSize(20)
        useCase.saveTranslatedTextSize(18)
        useCase.saveCurrentTimeTextSize(22)
        useCase.saveLineSpacing(10)
        useCase.saveDisplayMode(DisplayMode.DUAL)
        useCase.saveAlignment(LyricsAlignment.RIGHT)

        val config = useCase.getLyricsConfig()
        assertEquals(20, config.originalTextSize)
        assertEquals(18, config.translatedTextSize)
        assertEquals(22, config.currentTimeTextSize)
        assertEquals(10, config.lineSpacing)
        assertEquals(DisplayMode.DUAL, config.displayMode)
        assertEquals(LyricsAlignment.RIGHT, config.alignment)
    }

    @Test
    fun legacy_saveLyricsConfig_persistsAllFields() = runTest {
        val config = com.hmp.domain.config.LyricsConfig(
            originalTextSize = 16,
            translatedTextSize = 14,
            currentTimeTextSize = 18,
            lineSpacing = 8,
            displayMode = DisplayMode.LANG1,
            alignment = LyricsAlignment.LEFT
        )
        useCase.saveLyricsConfig(config)

        assertEquals(16, settingsRepository.getLyricsOriginalTextSize())
        assertEquals(14, settingsRepository.getLyricsTranslatedTextSize())
        assertEquals(18, settingsRepository.getLyricsCurrentTimeTextSize())
        assertEquals(8, settingsRepository.getLyricsLineSpacing())
        assertEquals(DisplayMode.LANG1, settingsRepository.getLyricsDisplayMode())
        assertEquals(LyricsAlignment.LEFT, settingsRepository.getLyricsAlignment())
    }

    @Test
    fun legacy_resetToDefault_setsDefaultValues() = runTest {
        useCase.saveOriginalTextSize(99)
        useCase.saveTranslatedTextSize(88)
        useCase.resetToDefault()

        val config = useCase.getLyricsConfig()
        val defaultConfig = com.hmp.domain.config.LyricsConfig()
        assertEquals(defaultConfig.originalTextSize, config.originalTextSize)
        assertEquals(defaultConfig.translatedTextSize, config.translatedTextSize)
        assertEquals(defaultConfig.currentTimeTextSize, config.currentTimeTextSize)
        assertEquals(defaultConfig.lineSpacing, config.lineSpacing)
        assertEquals(defaultConfig.displayMode, config.displayMode)
        assertEquals(defaultConfig.alignment, config.alignment)
    }
}
