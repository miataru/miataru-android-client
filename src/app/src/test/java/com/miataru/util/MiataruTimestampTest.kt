package com.miataru.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MiataruTimestampTest {

    @Test
    fun `toMiataruTimestampString converts milliseconds to epoch seconds`() {
        assertEquals("1735689600", toMiataruTimestampString(1_735_689_600_123L))
    }

    @Test
    fun `parseMiataruTimestampToEpochMillis parses epoch seconds`() {
        assertEquals(1_735_689_600_000L, parseMiataruTimestampToEpochMillis("1735689600"))
    }

    @Test
    fun `parseMiataruTimestampToEpochMillis parses epoch milliseconds`() {
        assertEquals(1_735_689_600_123L, parseMiataruTimestampToEpochMillis("1735689600123"))
    }

    @Test
    fun `parseMiataruTimestampToEpochMillis parses iso timestamps`() {
        assertEquals(1_735_689_600_000L, parseMiataruTimestampToEpochMillis("2025-01-01T00:00:00Z"))
    }

    @Test
    fun `parseMiataruTimestampToEpochMillis returns null for invalid input`() {
        assertNull(parseMiataruTimestampToEpochMillis("not-a-time"))
    }
}
