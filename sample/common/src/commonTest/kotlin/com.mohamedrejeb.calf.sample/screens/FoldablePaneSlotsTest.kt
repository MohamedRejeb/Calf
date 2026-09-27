package com.mohamedrejeb.calf.sample.screens

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.mohamedrejeb.calf.foldable.ExperimentalCalfFoldableApi
import com.mohamedrejeb.calf.foldable.FoldOrientation
import com.mohamedrejeb.calf.foldable.FoldPanes
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCalfFoldableApi::class)
class FoldablePaneSlotsTest {

    private val screen = Rect(0f, 0f, 1000f, 800f)
    private val insets = EdgeInsets(left = 0f, top = 50f, right = 80f, bottom = 30f)

    @Test
    fun onePaneIsInsetByTheWholeSafeArea() {
        val slots = paneSlots(FoldPanes(first = screen), Offset.Zero, screen, insets)

        assertEquals(listOf(PaneSlot(screen, insets)), slots)
    }

    @Test
    fun sideBySidePanesOnlyTakeTheInsetsOfTheEdgesTheyTouch() {
        val panes = FoldPanes(
            first = Rect(0f, 0f, 490f, 800f),
            second = Rect(510f, 0f, 1000f, 800f),
            gap = Rect(490f, 0f, 510f, 800f),
            orientation = FoldOrientation.Vertical,
        )

        val (left, right) = paneSlots(panes, Offset.Zero, screen, insets)

        assertEquals(EdgeInsets(left = 0f, top = 50f, right = 0f, bottom = 30f), left.padding)
        assertEquals(EdgeInsets(left = 0f, top = 50f, right = 80f, bottom = 30f), right.padding)
    }

    @Test
    fun stackedPanesSplitTheTopAndBottomInsets() {
        val panes = FoldPanes(
            first = Rect(0f, 0f, 1000f, 390f),
            second = Rect(0f, 410f, 1000f, 800f),
            gap = Rect(0f, 390f, 1000f, 410f),
            orientation = FoldOrientation.Horizontal,
        )

        val (above, below) = paneSlots(panes, Offset.Zero, screen, insets)

        assertEquals(EdgeInsets(left = 0f, top = 50f, right = 80f, bottom = 0f), above.padding)
        assertEquals(EdgeInsets(left = 0f, top = 0f, right = 80f, bottom = 30f), below.padding)
    }

    @Test
    fun windowPanesAreMovedIntoTheScreenCoordinates() {
        val origin = Offset(100f, 0f)
        val window = FoldPanes(first = Rect(0f, 0f, 1100f, 800f))

        val slot = paneSlots(window, origin, screen, EdgeInsets.Zero).single()

        assertEquals(Rect(0f, 0f, 1000f, 800f), slot.bounds)
    }
}
