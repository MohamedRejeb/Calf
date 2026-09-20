package com.mohamedrejeb.calf.navigation

import com.mohamedrejeb.calf.navigation.internal.NavBarInsetCalibration
import com.mohamedrejeb.calf.navigation.internal.mirroredScrollPoints
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

    // region collapsed at the top

    @Test
    fun `a bar smaller than the expanded one while at the top has to be expanded`() {
        assertEquals(true, portrait.isCollapsedAtTop(atTop = true, currentTopInset = 101.0))
    }

    @Test
    fun `an expanded bar at the top is left alone`() {
        assertEquals(false, portrait.isCollapsedAtTop(atTop = true, currentTopInset = 153.0))
        assertEquals(false, portrait.isCollapsedAtTop(atTop = true, currentTopInset = 152.8))
    }

    @Test
    fun `a collapsed bar is expected while the content is scrolled`() {
        assertEquals(false, portrait.isCollapsedAtTop(atTop = false, currentTopInset = 101.0))
    }

    @Test
    fun `nothing is known to be collapsed before the expanded inset was sampled`() {
        assertEquals(false, NavBarInsetCalibration().isCollapsedAtTop(atTop = true, currentTopInset = 101.0))
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

    // region mirrored scroll distance

    @Test
    fun `a title that moves with the content mirrors the real scroll`() {
        assertEquals(0.0, mirroredScrollPoints(scrolledPoints = 0.0, titleMovesWithContent = true))
        assertEquals(12.5, mirroredScrollPoints(scrolledPoints = 12.5, titleMovesWithContent = true))
    }

    @Test
    fun `a title that only snaps is collapsed as soon as scrolling starts`() {
        assertEquals(100.0, mirroredScrollPoints(scrolledPoints = 0.5, titleMovesWithContent = false))
        assertEquals(100.0, mirroredScrollPoints(scrolledPoints = 99.0, titleMovesWithContent = false))
    }

    @Test
    fun `a snapping title stays expanded at the top`() {
        assertEquals(0.0, mirroredScrollPoints(scrolledPoints = 0.0, titleMovesWithContent = false))
    }

    @Test
    fun `past the collapse distance a snapping title mirrors the real scroll again`() {
        assertEquals(240.0, mirroredScrollPoints(scrolledPoints = 240.0, titleMovesWithContent = false))
    }

    // endregion
}
