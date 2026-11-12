package com.mohamedrejeb.calf.navigation

import com.mohamedrejeb.calf.navigation.internal.reconcileNavStack
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Tests for the pure back-stack reconciliation that drives the iOS
 * `UINavigationController` bridge. Hosts are plain objects here; on iOS
 * they are `EntryHostingController`s.
 */
class NavStackReconcilerTest {

    private class Host(val key: String) {
        override fun toString() = "Host($key)"
    }

    private fun create(key: String) = Host(key)

    @Test
    fun initialMount_createsOneHostPerKey() {
        val result = reconcileNavStack(emptyList(), listOf("home", "detail"), ::create)

        assertEquals(listOf("home", "detail"), result.stack.map { it.first })
        assertTrue(result.dropped.isEmpty())
    }

    @Test
    fun push_reusesExistingHostsAndCreatesOnlyTheNewOne() {
        val first = reconcileNavStack(emptyList(), listOf("home"), ::create)
        val second = reconcileNavStack(first.stack, listOf("home", "detail"), ::create)

        assertSame(first.stack[0].second, second.stack[0].second, "home host should be reused")
        assertEquals("detail", second.stack[1].first)
        assertTrue(second.dropped.isEmpty())
    }

    @Test
    fun pop_dropsExactlyTheRemovedHost() {
        val mounted = reconcileNavStack(emptyList(), listOf("home", "detail"), ::create)
        val popped = reconcileNavStack(mounted.stack, listOf("home"), ::create)

        assertSame(mounted.stack[0].second, popped.stack[0].second)
        assertEquals(listOf(mounted.stack[1].second), popped.dropped)
    }

    @Test
    fun duplicateKeys_getDistinctHosts() {
        // home -> detail -> home: the same key twice must NOT share a host,
        // otherwise the same UIViewController instance would appear twice in
        // setViewControllers (UIKit throws).
        val result = reconcileNavStack(emptyList(), listOf("home", "detail", "home"), ::create)

        assertEquals(3, result.stack.size)
        assertNotSame(result.stack[0].second, result.stack[2].second)
    }

    @Test
    fun duplicateKeys_popDropsTheTopOccurrence() {
        val mounted = reconcileNavStack(emptyList(), listOf("home", "detail", "home"), ::create)
        val popped = reconcileNavStack(mounted.stack, listOf("home", "detail"), ::create)

        // The bottom "home" host keeps its identity (and its ViewModels);
        // the top occurrence is the one dropped.
        assertSame(mounted.stack[0].second, popped.stack[0].second)
        assertSame(mounted.stack[1].second, popped.stack[1].second)
        assertEquals(listOf(mounted.stack[2].second), popped.dropped)
    }

    @Test
    fun middleRemoval_keepsTopHostAlive() {
        val mounted = reconcileNavStack(emptyList(), listOf("a", "b", "c"), ::create)
        val removed = reconcileNavStack(mounted.stack, listOf("a", "c"), ::create)

        assertSame(mounted.stack[0].second, removed.stack[0].second)
        assertSame(mounted.stack[2].second, removed.stack[1].second, "top host must survive middle removal")
        assertEquals(listOf(mounted.stack[1].second), removed.dropped)
    }

    @Test
    fun clear_dropsEverything() {
        val mounted = reconcileNavStack(emptyList(), listOf("a", "b"), ::create)
        val cleared = reconcileNavStack(mounted.stack, emptyList(), ::create)

        assertTrue(cleared.stack.isEmpty())
        assertEquals(mounted.stack.map { it.second }.toSet(), cleared.dropped.toSet())
    }

    @Test
    fun replaceTop_dropsOldTopAndCreatesNew() {
        val mounted = reconcileNavStack(emptyList(), listOf("home", "detail"), ::create)
        val replaced = reconcileNavStack(mounted.stack, listOf("home", "settings"), ::create)

        assertSame(mounted.stack[0].second, replaced.stack[0].second)
        assertEquals("settings", replaced.stack[1].first)
        assertEquals(listOf(mounted.stack[1].second), replaced.dropped)
    }
}
