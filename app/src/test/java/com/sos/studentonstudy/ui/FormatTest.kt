package com.sos.studentonstudy.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test untuk fungsi murni di ui/Format.kt.
 * Semua fungsi di sini tidak menyentuh Android framework, jadi bisa dijalankan
 * sebagai JVM test (./gradlew testDebugUnitTest) tanpa emulator.
 */
class FormatTest {

    // --- formatIdr -------------------------------------------------------

    @Test fun `formatIdr adds a thousands separator`() {
        assertEquals("IDR 15,000", formatIdr(15_000L))
        assertEquals("IDR 250,000", formatIdr(250_000L))
    }

    @Test fun `formatIdr leaves values below one thousand unseparated`() {
        assertEquals("IDR 0", formatIdr(0L))
        assertEquals("IDR 999", formatIdr(999L))
    }

    @Test fun `formatIdr separates at the thousand boundary`() {
        assertEquals("IDR 1,000", formatIdr(1_000L))
    }

    // --- formatTimeAgo ---------------------------------------------------

    private val now = 1_700_000_000_000L // waktu acuan tetap supaya test tidak bergantung jam sistem

    @Test fun `formatTimeAgo shows Now for anything under a minute`() {
        assertEquals("Now", formatTimeAgo(now - 30_000L, now))
        assertEquals("Now", formatTimeAgo(now, now))
    }

    @Test fun `formatTimeAgo switches to minutes exactly at one minute`() {
        assertEquals("Now", formatTimeAgo(now - 59_999L, now))
        assertEquals("1m", formatTimeAgo(now - 60_000L, now))
        assertEquals("5m", formatTimeAgo(now - 5 * 60_000L, now))
    }

    @Test fun `formatTimeAgo switches to hours exactly at one hour`() {
        assertEquals("59m", formatTimeAgo(now - 59 * 60_000L, now))
        assertEquals("1h", formatTimeAgo(now - 3_600_000L, now))
        assertEquals("10h", formatTimeAgo(now - 10 * 3_600_000L, now))
    }

    @Test fun `formatTimeAgo switches to days exactly at one day`() {
        assertEquals("23h", formatTimeAgo(now - 23 * 3_600_000L, now))
        assertEquals("1d", formatTimeAgo(now - 86_400_000L, now))
        assertEquals("5d", formatTimeAgo(now - 5 * 86_400_000L, now))
    }

    @Test fun `formatTimeAgo switches to weeks exactly at seven days`() {
        assertEquals("6d", formatTimeAgo(now - 6 * 86_400_000L, now))
        assertEquals("1w", formatTimeAgo(now - 7 * 86_400_000L, now))
        assertEquals("2w", formatTimeAgo(now - 14 * 86_400_000L, now))
    }

    @Test fun `formatTimeAgo clamps a future timestamp to Now`() {
        assertEquals("Now", formatTimeAgo(now + 86_400_000L, now))
    }

    // --- isUrgent --------------------------------------------------------

    @Test fun `isUrgent is true for a deadline today`() {
        assertTrue(isUrgent(daysFromToday(0)))
    }

    @Test fun `isUrgent is true for a deadline tomorrow`() {
        assertTrue(isUrgent(daysFromToday(1)))
    }

    @Test fun `isUrgent is false from the day after tomorrow onwards`() {
        assertFalse(isUrgent(daysFromToday(2)))
        assertFalse(isUrgent(daysFromToday(7)))
    }

    @Test fun `isUrgent is true for a deadline that has already passed`() {
        assertTrue(isUrgent(daysFromToday(-1)))
    }
}
