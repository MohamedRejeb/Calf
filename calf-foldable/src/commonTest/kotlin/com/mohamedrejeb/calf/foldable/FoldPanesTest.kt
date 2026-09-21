package com.mohamedrejeb.calf.foldable

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FoldPanesTest {

    private val window = Size(951f, 669f)
    private val singlePane = FoldPanes(first = Rect(0f, 0f, 951f, 669f))

    // The Duo's crease: zero wide, in the middle of the inner display.
    private val crease = FoldInfo(
        bounds = Rect(475.5f, 0f, 475.5f, 669f),
        orientation = FoldOrientation.Vertical,
        isSeparating = false,
    )

    @Test
    fun `no fold gives one pane filling the window`() {
        val panes = calculateFoldPanes(window, FoldPosture.NoFold, fold = null)

        assertEquals(singlePane, panes)
        assertEquals(1, panes.count)
        assertNull(panes.second)
        assertNull(panes.gap)
    }

    @Test
    fun `flat vertical fold splits the window at the crease`() {
        val panes = calculateFoldPanes(window, FoldPosture.Flat, crease)

        assertEquals(2, panes.count)
        assertEquals(Rect(0f, 0f, 475.5f, 669f), panes.first)
        assertEquals(Rect(475.5f, 0f, 951f, 669f), panes.second)
        assertEquals(crease.bounds, panes.gap)
        assertEquals(FoldOrientation.Vertical, panes.orientation)
    }

    @Test
    fun `hinge with a width leaves a gap between the panes`() {
        val hinge = FoldInfo(Rect(440f, 0f, 510f, 669f), FoldOrientation.Vertical, isSeparating = true)

        val panes = calculateFoldPanes(window, FoldPosture.Flat, hinge)

        assertEquals(Rect(0f, 0f, 440f, 669f), panes.first)
        assertEquals(Rect(510f, 0f, 951f, 669f), panes.second)
    }

    @Test
    fun `horizontal fold stacks the panes`() {
        val tabletop = FoldInfo(Rect(0f, 330f, 951f, 340f), FoldOrientation.Horizontal, isSeparating = true)

        val panes = calculateFoldPanes(window, FoldPosture.HalfOpen, tabletop)

        assertEquals(Rect(0f, 0f, 951f, 330f), panes.first)
        assertEquals(Rect(0f, 340f, 951f, 669f), panes.second)
        assertEquals(FoldOrientation.Horizontal, panes.orientation)
    }

    @Test
    fun `margins widen the gap only while the fold separates`() {
        val margins = FoldMargins(left = 20f, right = 20f)

        val flat = calculateFoldPanes(window, FoldPosture.Flat, crease, margins)
        val halfOpen = calculateFoldPanes(window, FoldPosture.HalfOpen, crease.copy(isSeparating = true), margins)

        assertEquals(Rect(0f, 0f, 475.5f, 669f), flat.first)
        assertEquals(Rect(0f, 0f, 455.5f, 669f), halfOpen.first)
        assertEquals(Rect(495.5f, 0f, 951f, 669f), halfOpen.second)
        assertEquals(Rect(455.5f, 0f, 495.5f, 669f), halfOpen.gap)
    }

    @Test
    fun `closed device has one pane even when a fold is still reported`() {
        assertEquals(singlePane, calculateFoldPanes(window, FoldPosture.Closed, crease))
    }

    @Test
    fun `fold that does not cross the whole window does not split it`() {
        val partial = crease.copy(bounds = Rect(475.5f, 0f, 475.5f, 300f))

        assertEquals(singlePane, calculateFoldPanes(window, FoldPosture.Flat, partial))
    }

    @Test
    fun `fold outside the window does not split it`() {
        val outside = crease.copy(bounds = Rect(1200f, 0f, 1200f, 669f))
        val onTheEdge = crease.copy(bounds = Rect(0f, 0f, 0f, 669f))

        assertEquals(singlePane, calculateFoldPanes(window, FoldPosture.Flat, outside))
        assertEquals(singlePane, calculateFoldPanes(window, FoldPosture.Flat, onTheEdge))
    }

    @Test
    fun `pane narrower than the minimum falls back to one pane`() {
        val offCentre = crease.copy(bounds = Rect(200f, 0f, 200f, 669f))

        assertEquals(2, calculateFoldPanes(window, FoldPosture.Flat, offCentre, minPaneSize = 200f).count)
        assertEquals(singlePane, calculateFoldPanes(window, FoldPosture.Flat, offCentre, minPaneSize = 201f))
    }

    @Test
    fun `empty window gives one empty pane`() {
        assertEquals(FoldPanes(first = Rect.Zero), calculateFoldPanes(Size.Zero, FoldPosture.Flat, crease))
    }
}
