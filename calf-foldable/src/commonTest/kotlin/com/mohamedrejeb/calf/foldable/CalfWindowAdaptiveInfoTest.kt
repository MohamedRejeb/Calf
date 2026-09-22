package com.mohamedrejeb.calf.foldable

import androidx.compose.material3.adaptive.HingeInfo
import androidx.compose.material3.adaptive.Posture
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.ui.geometry.Rect
import androidx.window.core.layout.WindowSizeClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

/**
 * How Calf's fold reaches Material3 Adaptive's window info. Upstream leaves the posture empty on
 * every platform but Android, where it reads the same `FoldingFeature` calf-foldable does.
 */
class CalfWindowAdaptiveInfoTest {

    private val mediumWidth = WindowSizeClass(minWidthDp = 600, minHeightDp = 480)

    private val duoHinge = HingeInfo(
        bounds = Rect(475.5f, 0f, 475.5f, 669f),
        isFlat = true,
        isVertical = true,
        isSeparating = false,
        isOccluding = false,
    )

    private val upstreamWithoutPosture = WindowAdaptiveInfo(mediumWidth, Posture())

    @Test
    fun `platform that reports its own posture is passed through untouched`() {
        val upstream = WindowAdaptiveInfo(mediumWidth, Posture(hingeList = listOf(duoHinge)))
        val calfPosture = Posture(isTabletop = true, hingeList = listOf(duoHinge))

        val merged = mergeWindowAdaptiveInfo(upstream, calfPosture, upstreamReportsPosture = true)

        assertSame(upstream, merged)
    }

    @Test
    fun `platform without a posture is passed through when calf has no fold either`() {
        val merged = mergeWindowAdaptiveInfo(upstreamWithoutPosture, Posture(), upstreamReportsPosture = false)

        assertSame(upstreamWithoutPosture, merged)
    }

    @Test
    fun `calf posture fills the empty one and keeps the size class`() {
        val calfPosture = Posture(hingeList = listOf(duoHinge))

        val merged = mergeWindowAdaptiveInfo(upstreamWithoutPosture, calfPosture, upstreamReportsPosture = false)

        assertEquals(WindowAdaptiveInfo(mediumWidth, calfPosture), merged)
        assertSame(mediumWidth, merged.windowSizeClass)
    }

    @Test
    fun `tabletop without a hinge list still counts as a posture`() {
        val calfPosture = Posture(isTabletop = true)

        val merged = mergeWindowAdaptiveInfo(upstreamWithoutPosture, calfPosture, upstreamReportsPosture = false)

        assertEquals(calfPosture, merged.windowPosture)
    }

    @Test
    fun `an android platform does not double feed even when calf reports a fold`() {
        val calfPosture = Posture(hingeList = listOf(duoHinge))

        val merged = mergeWindowAdaptiveInfo(upstreamWithoutPosture, calfPosture, upstreamReportsPosture = true)

        assertSame(upstreamWithoutPosture, merged)
    }
}
