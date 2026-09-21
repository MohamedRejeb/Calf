package com.mohamedrejeb.calf.foldable

import androidx.compose.ui.geometry.Rect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class IosFoldReadingTest {

    private val division = FoldRegion(
        bounds = Rect(455.5f, 0f, 495.5f, 669f),
        margins = FoldMargins(left = 20f, right = 20f),
        isActive = false,
    )
    private val camera = FoldRegion(bounds = Rect(677f, 21f, 735f, 58f))
    private val capabilities = IosFoldCapabilities(isHingeApiAvailable = true, isRegionsApiAvailable = true, hasHinge = true)

    private val innerDisplay = IosFoldReading(
        divisionRegions = listOf(division),
        occlusionRegions = listOf(camera),
        horizontalSizeClass = IosSizeClass.Regular,
        verticalSizeClass = IosSizeClass.Regular,
    )

    @Test
    fun `reading and posture combine into a snapshot`() {
        val snapshot = iosFoldSnapshotOf(FoldPosture.Flat, innerDisplay, capabilities)

        assertEquals(FoldPosture.Flat, snapshot.posture)
        assertEquals(foldInfoOfIosRegion(division, FoldPosture.Flat), snapshot.fold)
        assertEquals(division, snapshot.iosFoldRegion)
        assertEquals(listOf(camera), snapshot.iosOcclusionRegions)
        assertEquals(IosSizeClass.Regular, snapshot.iosHorizontalSizeClass)
        assertEquals(capabilities, snapshot.iosCapabilities)
    }

    @Test
    fun `new posture with the previous reading separates at once`() {
        // The hinge reports half open about a second before UIKit lays the regions out again.
        val before = iosFoldSnapshotOf(FoldPosture.Flat, innerDisplay, capabilities)
        val after = iosFoldSnapshotOf(FoldPosture.HalfOpen, innerDisplay, capabilities)

        assertEquals(false, before.isSeparating)
        assertEquals(true, after.isSeparating)
        assertEquals(before.iosFoldRegion, after.iosFoldRegion)
    }

    @Test
    fun `reading taken before any layout pass has no fold`() {
        val snapshot = iosFoldSnapshotOf(FoldPosture.Closed, IosFoldReading.Empty, capabilities)

        assertNull(snapshot.fold)
        assertNull(snapshot.iosFoldRegion)
        assertEquals(IosSizeClass.Unspecified, snapshot.iosVerticalSizeClass)
        assertEquals(FoldPosture.Closed, snapshot.posture)
    }

    @Test
    fun `first division region is the fold`() {
        val second = division.copy(bounds = Rect(0f, 300f, 951f, 340f))
        val reading = innerDisplay.copy(divisionRegions = listOf(division, second))

        assertEquals(division, iosFoldSnapshotOf(FoldPosture.Flat, reading, capabilities).iosFoldRegion)
    }
}
