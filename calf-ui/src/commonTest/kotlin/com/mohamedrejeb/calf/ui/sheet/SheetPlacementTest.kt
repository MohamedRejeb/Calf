package com.mohamedrejeb.calf.ui.sheet

import kotlin.test.Test
import kotlin.test.assertEquals

class SheetPlacementTest {

    // Open iPhone Duo in landscape: the window is 951pt wide, the crease at 475.5.
    private val window = 951.0

    @Test
    fun `a full-width screen keeps the system placement`() {
        assertEquals(SheetPlacement.Automatic, sheetPlacement(0.0, 951.0, window, isRightToLeft = false))
    }

    @Test
    fun `a column that crosses the middle keeps the system placement`() {
        // A split view whose list column is narrow: the detail column spans the crease.
        assertEquals(SheetPlacement.Automatic, sheetPlacement(320.0, 867.0, window, isRightToLeft = false))
    }

    @Test
    fun `a column on the right gets the sheet on the trailing side`() {
        // Book pose: the detail column starts after the fold.
        assertEquals(SheetPlacement.Trailing, sheetPlacement(495.6, 867.0, window, isRightToLeft = false))
    }

    @Test
    fun `a column on the left gets the sheet on the leading side`() {
        assertEquals(SheetPlacement.Leading, sheetPlacement(0.0, 475.6, window, isRightToLeft = false))
    }

    @Test
    fun `leading and trailing swap in right-to-left`() {
        assertEquals(SheetPlacement.Leading, sheetPlacement(495.6, 867.0, window, isRightToLeft = true))
        assertEquals(SheetPlacement.Trailing, sheetPlacement(0.0, 475.6, window, isRightToLeft = true))
    }

    @Test
    fun `an unknown window keeps the system placement`() {
        assertEquals(SheetPlacement.Automatic, sheetPlacement(0.0, 0.0, 0.0, isRightToLeft = false))
    }
}
