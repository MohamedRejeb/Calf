package com.mohamedrejeb.calf.navigation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import com.mohamedrejeb.calf.navigation.internal.NavEntryScrollTracker
import kotlin.test.Test
import kotlin.test.assertEquals

class NavEntryScrollTrackerTest {
    private val reported = mutableListOf<Float>()
    private val tracker = NavEntryScrollTracker { reported.add(it) }

    private fun scroll(consumedY: Float) =
        tracker.onPostScroll(Offset(0f, consumedY), Offset.Zero, NestedScrollSource.UserInput)

    @Test
    fun `consumed downward scroll accumulates and is reported`() {
        scroll(-30f)
        scroll(-20f)

        assertEquals(50f, tracker.offset)
        assertEquals(listOf(30f, 50f), reported)
    }

    @Test
    fun `scrolling back clamps at the top`() {
        scroll(-30f)
        scroll(+80f)

        assertEquals(0f, tracker.offset)
        assertEquals(listOf(30f, 0f), reported)
    }

    @Test
    fun `unconsumed deltas and unchanged offsets report nothing`() {
        tracker.onPostScroll(Offset.Zero, Offset(0f, -100f), NestedScrollSource.UserInput)
        scroll(+10f) // already at the top

        assertEquals(0f, tracker.offset)
        assertEquals(emptyList(), reported)
    }

    @Test
    fun `nothing is consumed from the child`() {
        assertEquals(Offset.Zero, scroll(-30f))
    }
}
