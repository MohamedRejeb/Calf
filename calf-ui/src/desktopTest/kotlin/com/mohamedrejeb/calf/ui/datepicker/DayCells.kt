package com.mohamedrejeb.calf.ui.datepicker

import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText

/** The day cell of a Material3 calendar, matched through its accessible date description. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalTestApi::class)
internal fun ComposeUiTest.dayCell(utcTimeMillis: Long): SemanticsNodeInteraction {
    val description = requireNotNull(
        DatePickerDefaults.dateFormatter()
            .formatDate(utcTimeMillis, getCalendarLocalDefault(), forContentDescription = true),
    )
    val describesDay = hasText(description, substring = true) or hasContentDescription(description, substring = true)
    return onNode(describesDay and hasClickAction())
}
