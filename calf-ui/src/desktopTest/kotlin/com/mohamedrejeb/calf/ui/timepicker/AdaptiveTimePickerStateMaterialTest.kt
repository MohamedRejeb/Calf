package com.mohamedrejeb.calf.ui.timepicker

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AdaptiveTimePickerStateMaterialTest {

    @Test
    fun `keeps a valid initial time`() {
        val state = AdaptiveTimePickerState(initialHour = 23, initialMinute = 59, is24Hour = true)

        assertEquals(23, state.hour)
        assertEquals(59, state.minute)
    }

    @Test
    fun `rejects an hour outside 0 to 23`() {
        val error = assertFailsWith<IllegalArgumentException> {
            AdaptiveTimePickerState(initialHour = 24, initialMinute = 0, is24Hour = true)
        }

        assertTrue(error.message.orEmpty().contains("initialHour"), error.message)
    }

    @Test
    fun `rejects a minute outside 0 to 59`() {
        val error = assertFailsWith<IllegalArgumentException> {
            AdaptiveTimePickerState(initialHour = 0, initialMinute = 60, is24Hour = true)
        }

        assertTrue(error.message.orEmpty().contains("initialMinute"), error.message)
    }
}
