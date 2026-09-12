package com.mohamedrejeb.calf.ui.datepicker

import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals

private const val PLACEHOLDER = "Pick a date"
private const val CONFIRM = "Done"
private const val DISMISS = "Back"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTestApi::class)
class AdaptiveCompactDatePickerMaterialTest {

    private val formatter = DatePickerDefaults.dateFormatter()

    private fun state(selectedDateMillis: Long? = null) = AdaptiveDatePickerState(
        initialSelectedDateMillis = selectedDateMillis,
        initialDisplayedMonthMillis = selectedDateMillis,
        yearRange = DatePickerDefaults.YearRange,
        initialMaterialDisplayMode = DisplayMode.Picker,
        initialUIKitDisplayMode = UIKitDisplayMode.Picker,
    )

    private fun fieldLabel(utcTimeMillis: Long): String =
        requireNotNull(formatter.formatDate(utcTimeMillis, getCalendarLocalDefault()))

    @Test
    fun `shows the placeholder while nothing is selected`() = runComposeUiTest {
        setContent {
            AdaptiveCompactDatePicker(state = state(), materialPlaceholder = PLACEHOLDER)
        }

        onNodeWithText(PLACEHOLDER).assertIsDisplayed()
    }

    @Test
    fun `shows the formatted selected date`() = runComposeUiTest {
        setContent {
            AdaptiveCompactDatePicker(state = state(MAR_15_2026), materialPlaceholder = PLACEHOLDER)
        }

        onNodeWithText(fieldLabel(MAR_15_2026)).assertIsDisplayed()
    }

    @Test
    fun `tapping the field opens a dialog with confirm and dismiss buttons`() = runComposeUiTest {
        setContent {
            AdaptiveCompactDatePicker(
                state = state(),
                materialPlaceholder = PLACEHOLDER,
                materialConfirmText = CONFIRM,
                materialDismissText = DISMISS,
            )
        }

        onNodeWithText(PLACEHOLDER).performClick()

        onNodeWithText(CONFIRM).assertIsDisplayed()
        onNodeWithText(DISMISS).assertIsDisplayed()
    }

    @Test
    fun `a day picked in the dialog does not reach the state until confirmed`() = runComposeUiTest {
        val state = state(MAR_15_2026)
        setContent {
            AdaptiveCompactDatePicker(state = state, materialConfirmText = CONFIRM, materialDismissText = DISMISS)
        }

        onNodeWithText(fieldLabel(MAR_15_2026)).performClick()
        dayCell(MAR_16_2026).performClick()

        assertEquals(MAR_15_2026, state.selectedDateMillis)
    }

    @Test
    fun `confirming applies the day picked in the dialog`() = runComposeUiTest {
        val state = state(MAR_15_2026)
        setContent {
            AdaptiveCompactDatePicker(state = state, materialConfirmText = CONFIRM, materialDismissText = DISMISS)
        }

        onNodeWithText(fieldLabel(MAR_15_2026)).performClick()
        dayCell(MAR_16_2026).performClick()
        onNodeWithText(CONFIRM).performClick()

        assertEquals(MAR_16_2026, state.selectedDateMillis)
        onAllNodesWithText(CONFIRM).assertCountEquals(0)
    }

    @Test
    fun `dismissing discards the day picked in the dialog`() = runComposeUiTest {
        val state = state(MAR_15_2026)
        setContent {
            AdaptiveCompactDatePicker(state = state, materialConfirmText = CONFIRM, materialDismissText = DISMISS)
        }

        onNodeWithText(fieldLabel(MAR_15_2026)).performClick()
        dayCell(MAR_16_2026).performClick()
        onNodeWithText(DISMISS).performClick()

        assertEquals(MAR_15_2026, state.selectedDateMillis)
        onAllNodesWithText(DISMISS).assertCountEquals(0)
    }

    @Test
    fun `a disabled field cannot be opened`() = runComposeUiTest {
        setContent {
            AdaptiveCompactDatePicker(state = state(), enabled = false, materialPlaceholder = PLACEHOLDER)
        }

        onNodeWithText(PLACEHOLDER).assertIsNotEnabled()
    }
}
