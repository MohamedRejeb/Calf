package com.mohamedrejeb.calf.ui.timepicker

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AdaptiveTimePickerStateMaterialTest {

    private fun state(is24Hour: Boolean = true) =
        AdaptiveTimePickerState(initialHour = 0, initialMinute = 0, is24Hour = is24Hour)

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

    @Test
    fun `hour and minute setters reach the material state`() {
        val state = state()

        state.hour = 9
        state.minute = 30

        assertEquals(9, state.hour)
        assertEquals(30, state.minute)
        assertEquals(9, state.timePickerState.hour)
        assertEquals(30, state.timePickerState.minute)
    }

    @Test
    fun `rejects a programmatic hour outside 0 to 23`() {
        val state = state()

        val error = assertFailsWith<IllegalArgumentException> { state.hour = 24 }

        assertTrue(error.message.orEmpty().contains("hour"), error.message)
        assertEquals(0, state.hour)
    }

    @Test
    fun `rejects a programmatic minute outside 0 to 59`() {
        val state = state()

        val error = assertFailsWith<IllegalArgumentException> { state.minute = -1 }

        assertTrue(error.message.orEmpty().contains("minute"), error.message)
        assertEquals(0, state.minute)
    }

    @Test
    fun `the format can be switched after creation`() {
        val state = state(is24Hour = true)

        state.is24hour = false

        assertFalse(state.is24hour)
        assertFalse(state.timePickerState.is24hour)
    }

    @Test
    fun `isPm is true from noon on`() {
        val state = state()

        assertFalse(state.isPm)
        state.hour = 11
        assertFalse(state.isPm)
        state.hour = 12
        assertTrue(state.isPm)
        state.hour = 23
        assertTrue(state.isPm)
    }

    @Test
    fun `saver round-trips the time and the format`() {
        @Suppress("UNCHECKED_CAST")
        val saver = AdaptiveTimePickerState.Saver() as Saver<AdaptiveTimePickerState, Any>
        val original = state(is24Hour = false)
        original.hour = 14
        original.minute = 45
        original.is24hour = true

        val saved = requireNotNull(with(saver) { SaverScope { true }.save(original) })
        val restored = requireNotNull(saver.restore(saved))

        assertEquals(14, restored.hour)
        assertEquals(45, restored.minute)
        assertTrue(restored.is24hour)
    }
}
