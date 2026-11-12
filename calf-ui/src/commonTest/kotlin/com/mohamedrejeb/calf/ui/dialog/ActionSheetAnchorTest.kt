package com.mohamedrejeb.calf.ui.dialog

import kotlin.test.Test
import kotlin.test.assertEquals

class ActionSheetAnchorTest {

    // A calf-navigation detail column on an open iPhone Duo: its view spans the window, its safe
    // area is the column (the list's width on the left, the vertical bar on the right).
    private val detailColumn = AnchorRect(left = 320.0, top = 82.0, right = 867.0, bottom = 635.0)

    @Test
    fun `the action sheet is centred in the column that presents it`() {
        assertEquals(AnchorPoint(x = 593.5, y = 358.5), actionSheetAnchor(presenter = detailColumn, pane = null))
    }

    @Test
    fun `a pane of the presenting surface narrows the column to the pane`() {
        // Plain Compose navigation: one surface for both panes, the dialog raised from the detail pane.
        val window = AnchorRect(left = 0.0, top = 0.0, right = 867.0, bottom = 635.0)

        assertEquals(
            AnchorPoint(x = 625.5, y = 317.5),
            actionSheetAnchor(presenter = window, pane = 384.0..867.0),
        )
    }

    @Test
    fun `a pane reaching past the presenter is cut to it`() {
        // The pane runs under the vertical bar; the presenter's safe area ends before it.
        assertEquals(
            AnchorPoint(x = 625.5, y = 358.5),
            actionSheetAnchor(presenter = detailColumn.copy(left = 0.0), pane = 384.0..951.0),
        )
    }
}
