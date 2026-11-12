package com.mohamedrejeb.calf.ui.sheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTestApi::class)
class AdaptiveSheetStateMaterialTest {

    @Test
    fun `enabled values follow the two skip flags`() {
        assertEquals(
            setOf(SheetValue.Hidden, SheetValue.PartiallyExpanded, SheetValue.Expanded),
            enabledSheetValues(skipPartiallyExpanded = false, skipHiddenState = false),
        )
        assertEquals(
            setOf(SheetValue.Hidden, SheetValue.Expanded),
            enabledSheetValues(skipPartiallyExpanded = true, skipHiddenState = false),
        )
        assertEquals(
            setOf(SheetValue.PartiallyExpanded, SheetValue.Expanded),
            enabledSheetValues(skipPartiallyExpanded = false, skipHiddenState = true),
        )
        assertEquals(
            setOf(SheetValue.Expanded),
            enabledSheetValues(skipPartiallyExpanded = true, skipHiddenState = true),
        )
    }

    @Test
    fun `every sheet has an expanded state`() {
        for (skipPartiallyExpanded in listOf(false, true)) {
            for (skipHiddenState in listOf(false, true)) {
                val state = sheetState(skipPartiallyExpanded, skipHiddenState)
                assertTrue(state.hasExpandedState, "skipPartiallyExpanded=$skipPartiallyExpanded skipHiddenState=$skipHiddenState")
            }
        }
    }

    @Test
    fun `the partially expanded state is defined unless skipped`() {
        for (skipHiddenState in listOf(false, true)) {
            assertTrue(sheetState(skipPartiallyExpanded = false, skipHiddenState = skipHiddenState).hasPartiallyExpandedState)
            assertFalse(sheetState(skipPartiallyExpanded = true, skipHiddenState = skipHiddenState).hasPartiallyExpandedState)
        }
    }

    @Test
    fun `the initial value is the current and target value`() {
        val state = sheetState(skipPartiallyExpanded = false, skipHiddenState = false, initialValue = SheetValue.PartiallyExpanded)

        assertEquals(SheetValue.PartiallyExpanded, state.currentValue)
        assertEquals(SheetValue.PartiallyExpanded, state.targetValue)
        assertTrue(state.isVisible)
    }

    @Test
    fun `a partially expanded initial value needs the partially expanded state`() {
        assertFailsWith<IllegalArgumentException> {
            sheetState(skipPartiallyExpanded = true, skipHiddenState = false, initialValue = SheetValue.PartiallyExpanded)
        }
    }

    @Test
    fun `a hidden initial value needs the hidden state`() {
        assertFailsWith<IllegalArgumentException> {
            sheetState(skipPartiallyExpanded = false, skipHiddenState = true, initialValue = SheetValue.Hidden)
        }
    }

    @Test
    fun `remember starts hidden by default`() = runComposeUiTest {
        lateinit var state: AdaptiveSheetState
        setContent { state = rememberAdaptiveSheetState() }

        assertEquals(SheetValue.Hidden, state.currentValue)
        assertFalse(state.isVisible)
        assertTrue(state.hasPartiallyExpandedState)
        assertTrue(state.hasExpandedState)
    }

    @Test
    fun `remember honours the initial value and the skip flags`() = runComposeUiTest {
        lateinit var state: AdaptiveSheetState
        setContent {
            state = rememberAdaptiveSheetState(
                skipPartiallyExpanded = true,
                initialValue = SheetValue.Expanded,
                skipHiddenState = true,
            )
        }

        assertEquals(SheetValue.Expanded, state.currentValue)
        assertTrue(state.isVisible)
        assertFalse(state.hasPartiallyExpandedState)
        assertTrue(state.hasExpandedState)
    }

    private fun sheetState(
        skipPartiallyExpanded: Boolean,
        skipHiddenState: Boolean,
        initialValue: SheetValue = if (skipHiddenState) SheetValue.Expanded else SheetValue.Hidden,
    ) = AdaptiveSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded,
        confirmValueChange = { true },
        initialValue = initialValue,
        skipHiddenState = skipHiddenState,
        positionalThreshold = { 56f },
        velocityThreshold = { 125f },
    )
}
