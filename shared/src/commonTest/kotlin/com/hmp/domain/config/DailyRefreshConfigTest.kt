package com.hmp.domain.config

import kotlin.test.Test
import kotlin.test.assertEquals

class DailyRefreshConfigTest {

    @Test
    fun construction_timeMode() {
        val config = DailyRefreshConfig(
            mode = "time",
            refreshHours = 8,
            startupCount = 5,
            lastRefreshTimestamp = 1700000000L,
            launchCountSinceRefresh = 3
        )
        assertEquals("time", config.mode)
        assertEquals(8, config.refreshHours)
        assertEquals(5, config.startupCount)
        assertEquals(1700000000L, config.lastRefreshTimestamp)
        assertEquals(3, config.launchCountSinceRefresh)
    }

    @Test
    fun construction_startupMode() {
        val config = DailyRefreshConfig(
            mode = "startup",
            refreshHours = 0,
            startupCount = 10,
            lastRefreshTimestamp = 0L,
            launchCountSinceRefresh = 10
        )
        assertEquals("startup", config.mode)
        assertEquals(10, config.startupCount)
        assertEquals(10, config.launchCountSinceRefresh)
    }

    @Test
    fun construction_smartMode() {
        val config = DailyRefreshConfig(
            mode = "smart",
            refreshHours = 24,
            startupCount = 0,
            lastRefreshTimestamp = 1700000000L,
            launchCountSinceRefresh = 0
        )
        assertEquals("smart", config.mode)
        assertEquals(24, config.refreshHours)
    }

    @Test
    fun construction_offMode() {
        val config = DailyRefreshConfig(
            mode = "off",
            refreshHours = 8,
            startupCount = 5,
            lastRefreshTimestamp = 0L,
            launchCountSinceRefresh = 0
        )
        assertEquals("off", config.mode)
    }
}