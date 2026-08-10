package com.hearablemusic.player.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AudioEffectManagerTest {

    private lateinit var manager: AudioEffectManager

    @Before
    fun setup() {
        manager = AudioEffectManager()
    }

    @Test
    fun initialize_invalidSessionId_returnsFalse() {
        assertFalse(manager.initialize(0))
    }

    @Test
    fun initialize_negativeSessionId_returnsFalse() {
        assertFalse(manager.initialize(-1))
    }

    @Test
    fun initialize_validSessionId_doesNotThrow() {
        manager.initialize(1)
    }

    @Test
    fun equalizerBandCount_defaultIsFive() {
        assertEquals(5, manager.getEqualizerBandCount())
    }

    @Test
    fun equalizerBandLevelRange_defaultRange() {
        val range = manager.getEqualizerBandLevelRange()
        assertEquals(-1500, range.first.toInt())
        assertEquals(1500, range.second.toInt())
    }

    @Test
    fun currentEqualizerPreset_defaultIsZero() {
        assertEquals(0, manager.getCurrentEqualizerPreset())
    }

    @Test
    fun currentBassBoostStrength_defaultIsZero() {
        assertEquals(0.toShort(), manager.getCurrentBassBoostStrength())
    }

    @Test
    fun virtualizerNotEnabledByDefault() {
        assertFalse(manager.isVirtualizerCurrentlyEnabled())
    }

    @Test
    fun currentReverbPreset_defaultIsZero() {
        assertEquals(0.toShort(), manager.getCurrentReverbPreset())
    }

    @Test
    fun getCurrentEqualizerBandLevels_beforeInit_returnsEmpty() {
        assertTrue(manager.getCurrentEqualizerBandLevels().isEmpty())
    }

    @Test
    fun setEqualizerPreset_beforeInit_returnsFalse() {
        assertFalse(manager.setEqualizerPreset(1))
    }

    @Test
    fun setEqualizerBandLevel_beforeInit_returnsFalse() {
        assertFalse(manager.setEqualizerBandLevel(0, 100))
    }

    @Test
    fun setBassBoostStrength_beforeInit_returnsFalse() {
        assertFalse(manager.setBassBoostStrength(500))
    }

    @Test
    fun setVirtualizerEnabled_beforeInit_returnsFalse() {
        assertFalse(manager.setVirtualizerEnabled(true))
    }

    @Test
    fun setReverbPreset_beforeInit_returnsFalse() {
        assertFalse(manager.setReverbPreset(1))
    }

    @Test
    fun setReverbPreset_invalidPreset_afterInit_returnsFalse() {
        manager.initialize(1)
        assertFalse(manager.setReverbPreset(99))
    }

    @Test
    fun release_afterInit_clearsSupportedFlags() {
        manager.initialize(1)
        manager.release()
        assertFalse(manager.isEqualizerSupported())
        assertFalse(manager.isBassBoostSupported())
        assertFalse(manager.isVirtualizerSupported())
        assertFalse(manager.isReverbSupported())
    }

    @Test
    fun release_canBeCalledMultipleTimes() {
        manager.initialize(1)
        manager.release()
        manager.release()
    }

    @Test
    fun release_beforeInit_doesNotThrow() {
        manager.release()
    }

    @Test
    fun getBandFreqRange_beforeInit_returnsNull() {
        assertEquals(null, manager.getBandFreqRange(0))
    }

    @Test
    fun customPresetCount_isTen() {
        manager.initialize(1)
        manager.setEqualizerPreset(0)
        assertEquals(0, manager.getCurrentEqualizerPreset())
    }
}
