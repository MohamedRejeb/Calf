package com.mohamedrejeb.calf.navigation

import com.mohamedrejeb.calf.navigation.internal.entriesKeptAfterNativePop
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Which entries survive when UIKit reports what it shows after a transition. On iOS the items are
 * `EntryHostingController`s; strings stand in for them here.
 */
class NativePopMirrorTest {

    @Test
    fun nothingPopped_keepsEverything() {
        val entries = listOf("home", "detail")

        assertEquals(entries, entriesKeptAfterNativePop(entries, shown = setOf("home", "detail"), acknowledged = setOf("home", "detail")))
    }

    @Test
    fun backButton_dropsTheTopEntry() {
        val kept = entriesKeptAfterNativePop(
            entries = listOf("home", "detail"),
            shown = setOf("home"),
            acknowledged = setOf("home", "detail"),
        )

        assertEquals(listOf("home"), kept)
    }

    @Test
    fun popToRoot_dropsEverythingAboveTheRoot() {
        val kept = entriesKeptAfterNativePop(
            entries = listOf("home", "a", "b"),
            shown = setOf("home"),
            acknowledged = setOf("home", "a", "b"),
        )

        assertEquals(listOf("home"), kept)
    }

    @Test
    fun pushUIKitHasNotTakenUpYet_isNotAPop() {
        // A tab selected and pushed onto in the same turn: the navigation controller was never on
        // screen, defers the change, and first reports didShow for its old root.
        val kept = entriesKeptAfterNativePop(
            entries = listOf("home", "detail"),
            shown = setOf("home"),
            acknowledged = setOf("home"),
        )

        assertEquals(listOf("home", "detail"), kept)
    }

    @Test
    fun entryThatUIKitDroppedBelowAShownOne_cutsTheStackThere() {
        // A pop in a split view's list column: the list is gone, the detail column still shows its
        // entries, and they go with the list they belonged to.
        val kept = entriesKeptAfterNativePop(
            entries = listOf("home", "list", "detail"),
            shown = setOf("home", "detail"),
            acknowledged = setOf("home", "list", "detail"),
        )

        assertEquals(listOf("home"), kept)
    }

    @Test
    fun firstReport_acknowledgesNothingYet_keepsEverything() {
        val entries = listOf("home", "detail")

        assertEquals(entries, entriesKeptAfterNativePop(entries, shown = emptySet(), acknowledged = emptySet()))
    }
}
