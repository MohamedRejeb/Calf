package com.mohamedrejeb.calf.ui.sheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import platform.UIKit.UISheetPresentationControllerDetentIdentifierLarge
import platform.UIKit.UISheetPresentationControllerDetentIdentifierMedium
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalMaterial3Api::class)
class SheetDetentsTest {

    @Test
    fun `expanded is the large detent and partially expanded the medium one`() {
        assertEquals(UISheetPresentationControllerDetentIdentifierLarge, SheetValue.Expanded.toDetentIdentifier())
        assertEquals(UISheetPresentationControllerDetentIdentifierMedium, SheetValue.PartiallyExpanded.toDetentIdentifier())
        assertNull(SheetValue.Hidden.toDetentIdentifier())
    }

    @Test
    fun `a detent identifier maps back to its sheet value`() {
        assertEquals(SheetValue.PartiallyExpanded, sheetValueOfDetent(UISheetPresentationControllerDetentIdentifierMedium))
        assertEquals(SheetValue.Expanded, sheetValueOfDetent(UISheetPresentationControllerDetentIdentifierLarge))
    }

    @Test
    fun `no selected detent means the sheet rests at its first detent`() {
        // UIKit leaves selectedDetentIdentifier nil until the user or the app picks one; the sheet
        // then sits at the first (smallest) detent of the list it was given.
        assertEquals(SheetValue.PartiallyExpanded, sheetValueOfDetent(null, hasPartiallyExpandedState = true))
        assertEquals(SheetValue.Expanded, sheetValueOfDetent(null, hasPartiallyExpandedState = false))
    }

    @Test
    fun `the detents are listed smallest first and only when defined`() {
        assertEquals(listOf(SheetValue.PartiallyExpanded, SheetValue.Expanded), sheetDetentValues(hasPartiallyExpandedState = true))
        assertEquals(listOf(SheetValue.Expanded), sheetDetentValues(hasPartiallyExpandedState = false))
    }
}
