package com.hearablemusic.player.ui.common.util

import com.hmp.domain.enum.LabelName
import org.junit.Assert.assertEquals
import org.junit.Test

class LabelExtensionsTest {

    @Test
    fun iconName_returnsLowercaseName() {
        assertEquals("rock", LabelName.ROCK.iconName)
        assertEquals("pop", LabelName.POP.iconName)
        assertEquals("jazz", LabelName.JAZZ.iconName)
        assertEquals("happy", LabelName.HAPPY.iconName)
        assertEquals("sad", LabelName.SAD.iconName)
        assertEquals("workout", LabelName.WORKOUT.iconName)
        assertEquals("english", LabelName.ENGLISH.iconName)
        assertEquals("chinese", LabelName.CHINESE.iconName)
    }

    @Test
    fun iconName_unknown_returnsLowercase() {
        assertEquals("unknown", LabelName.UNKNOWN.iconName)
    }

    @Test
    fun iconName_allEntries_returnsLowercase() {
        for (label in LabelName.entries) {
            assertEquals(label.name.lowercase(), label.iconName)
        }
    }
}
