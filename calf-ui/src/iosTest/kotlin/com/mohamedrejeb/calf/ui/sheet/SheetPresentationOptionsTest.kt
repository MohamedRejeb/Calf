package com.mohamedrejeb.calf.ui.sheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalMaterial3Api::class)
class SheetPresentationOptionsTest {

    @Test
    fun `a default sheet shows its grabber and can be swiped away`() {
        assertEquals(
            SheetPresentationOptions(hasPartiallyExpandedState = true, showsGrabber = true, preventsDismissal = false),
            sheetPresentationOptions(state(), hasDragHandle = true, sheetGesturesEnabled = true),
        )
    }

    @Test
    fun `no gestures means no grabber and no swipe down`() {
        assertEquals(
            SheetPresentationOptions(hasPartiallyExpandedState = true, showsGrabber = false, preventsDismissal = true),
            sheetPresentationOptions(state(), hasDragHandle = true, sheetGesturesEnabled = false),
        )
    }

    @Test
    fun `a sheet without a hidden state cannot be swiped away`() {
        assertEquals(
            SheetPresentationOptions(hasPartiallyExpandedState = false, showsGrabber = true, preventsDismissal = true),
            sheetPresentationOptions(
                state(skipPartiallyExpanded = true, skipHiddenState = true, initialValue = SheetValue.Expanded),
                hasDragHandle = true,
                sheetGesturesEnabled = true,
            ),
        )
    }

    @Test
    fun `no drag handle means no grabber`() {
        assertEquals(
            false,
            sheetPresentationOptions(state(), hasDragHandle = false, sheetGesturesEnabled = true).showsGrabber,
        )
    }

    private fun state(
        skipPartiallyExpanded: Boolean = false,
        skipHiddenState: Boolean = false,
        initialValue: SheetValue = SheetValue.Hidden,
    ) = AdaptiveSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded,
        confirmValueChange = { true },
        initialValue = initialValue,
        skipHiddenState = skipHiddenState,
        positionalThreshold = { 56f },
        velocityThreshold = { 125f },
    )
}
