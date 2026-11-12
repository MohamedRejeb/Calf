package com.mohamedrejeb.calf.navigation

import com.mohamedrejeb.calf.navigation.internal.PopGestureKind
import com.mohamedrejeb.calf.navigation.internal.canPopInteractively
import com.mohamedrejeb.calf.navigation.internal.managedPopGestures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PopGesturePolicyTest {
    private fun managedOn(iosMajorVersion: Int): List<PopGestureKind> =
        managedPopGestures { major -> iosMajorVersion >= major }

    @Test
    fun `iOS 26 manages the content swipe as well as the edge swipe`() {
        // Regression: only the edge recognizer was managed, so on iOS 26 an
        // entry with iosGestureEnabled = false still popped from a content swipe.
        assertEquals(listOf(PopGestureKind.Edge, PopGestureKind.Content), managedOn(iosMajorVersion = 26))
        assertEquals(PopGestureKind.entries, managedOn(iosMajorVersion = 27))
    }

    @Test
    fun `before iOS 26 only the edge swipe is managed`() {
        // The content recognizer's selector does not exist there; touching it crashes.
        assertEquals(listOf(PopGestureKind.Edge), managedOn(iosMajorVersion = 25))
        assertEquals(listOf(PopGestureKind.Edge), managedOn(iosMajorVersion = 18))
    }

    @Test
    fun `unknown iOS version falls back to the edge swipe only`() {
        assertEquals(listOf(PopGestureKind.Edge), managedOn(iosMajorVersion = 0))
    }

    @Test
    fun `root entry never pops interactively`() {
        assertFalse(canPopInteractively(stackSize = 1, topGestureEnabled = true))
        assertFalse(canPopInteractively(stackSize = 0, topGestureEnabled = true))
    }

    @Test
    fun `pushed entry pops interactively by default`() {
        assertTrue(canPopInteractively(stackSize = 2, topGestureEnabled = true))
    }

    @Test
    fun `entry with the gesture disabled does not pop interactively`() {
        assertFalse(canPopInteractively(stackSize = 2, topGestureEnabled = false))
        assertFalse(canPopInteractively(stackSize = 5, topGestureEnabled = false))
    }

    @Test
    fun `top controller that is not a nav entry keeps the gesture`() {
        assertTrue(canPopInteractively(stackSize = 2, topGestureEnabled = null))
    }
}
