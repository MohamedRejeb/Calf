package com.mohamedrejeb.calf.navigation

import com.mohamedrejeb.calf.navigation.internal.NavBarInsetCalibration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class NavBarInsetCalibrationTest {

    private val portrait = NavBarInsetCalibration().forWidth(390.0).sampledAtTop(currentTopInset = 153.0)

    // region content inset

    @Test
    fun `content keeps the expanded inset while the large title collapses`() {
        assertEquals(153.0, portrait.contentTopInset(liveTopInset = 153.0))
        assertEquals(153.0, portrait.contentTopInset(liveTopInset = 130.0))
        assertEquals(153.0, portrait.contentTopInset(liveTopInset = 101.0))
    }

    @Test
    fun `content uses the live inset before anything was sampled`() {
        assertEquals(101.0, NavBarInsetCalibration().contentTopInset(liveTopInset = 101.0))
    }

    @Test
    fun `content follows an inset that grows past the sampled one`() {
        assertEquals(173.0, portrait.contentTopInset(liveTopInset = 173.0))
    }

    // endregion

    // region calibration

    @Test
    fun `a new width forgets the expanded inset`() {
        val landscape = portrait.forWidth(844.0)

        assertEquals(78.0, landscape.contentTopInset(liveTopInset = 78.0))
    }

    @Test
    fun `the same width keeps the calibration`() {
        assertSame(portrait, portrait.forWidth(390.0))
    }

    @Test
    fun `sampling at the top keeps the largest inset seen`() {
        val settling = portrait.sampledAtTop(currentTopInset = 120.0)

        assertEquals(153.0, settling.expandedTopInset)
    }

    // endregion

    // region mirrored offset

    @Test
    fun `at the top the mirrored offset rests on the current inset`() {
        assertEquals(-101.0, portrait.mirroredOffsetY(atTop = true, currentTopInset = 101.0, scrolledPoints = 0.0))
    }

    @Test
    fun `while scrolled the mirrored offset counts from the expanded inset`() {
        assertEquals(-123.0, portrait.mirroredOffsetY(atTop = false, currentTopInset = 123.0, scrolledPoints = 30.0))
        assertEquals(47.0, portrait.mirroredOffsetY(atTop = false, currentTopInset = 101.0, scrolledPoints = 200.0))
    }

    // endregion
}
