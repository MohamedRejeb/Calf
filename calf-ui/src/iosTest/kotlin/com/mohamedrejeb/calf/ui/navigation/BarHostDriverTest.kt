@file:OptIn(ExperimentalForeignApi::class)

package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UINavigationController
import platform.UIKit.UIViewController
import platform.UIKit.navigationItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The driver against a real `UINavigationController`: what the slot's navigation item shows after
 * the host's state changed. Runs on the iOS simulator.
 */
class BarHostDriverTest {
    private val navigationController = UINavigationController(nibName = null, bundle = null)
    private val slot = UIViewController(nibName = null, bundle = null)
    private val recalibrations = mutableListOf<Any?>()
    private val driver = BarHostDriver(onTopBarLayoutChange = { recalibrations += it })

    private val screenA = Any()
    private val screenB = Any()
    private val screenC = Any()
    private val fullWidth = HostedSpan(left = 0.dp, right = 390.dp, hostWidth = 390.dp)

    private fun apply(state: BarHostState) = driver.apply(state, navigationController, slot, density = 1f)

    private fun topBar(title: String, vararg trailing: UIKitUIBarButtonItem) =
        HostedBars(topBar = HostedTopBar(title = title, trailingItems = trailing.toList()))

    @Test
    fun `a screen title and its items reach the navigation item`() {
        val state = BarHostState()
            .register(screenA, null)
            .withBars(screenA) { topBar("A", UIKitUIBarButtonItem(title = "Share")) }

        apply(state)

        assertEquals("A", slot.navigationItem.title)
        assertEquals(1, slot.navigationItem.rightBarButtonItems?.size)
        assertEquals(false, navigationController.navigationBarHidden)
    }

    @Test
    fun `a bar coming back after a screen without one does not keep the earlier screen items`() {
        // A has items, B has a scaffold with no top bar, C has a top bar with no items.
        val withA = BarHostState()
            .register(screenA, null)
            .withBars(screenA) { topBar("A", UIKitUIBarButtonItem(title = "Share")) }
        apply(withA)
        // A scaffold without bars only owns them once it is placed: B spans the whole host.
        val withB = withA.nextGeneration().register(screenB, null).withBars(screenB) { HostedBars() }
            .withSpan(screenB, fullWidth)
        apply(withB)
        assertEquals(true, navigationController.navigationBarHidden)

        val withC = withB.nextGeneration().register(screenC, null).withBars(screenC) { topBar("C") }
        apply(withC)

        assertEquals("C", slot.navigationItem.title)
        assertTrue(slot.navigationItem.rightBarButtonItems.isNullOrEmpty(), "A's items survived on C's bar")
        assertEquals(false, navigationController.navigationBarHidden)
    }

    @Test
    fun `recalibration names the screen whose bar is shown and no screen once none is left`() {
        val withA = BarHostState()
            .register(screenA, null)
            .withBars(screenA) { topBar("A") }
        apply(withA)
        val withB = withA.nextGeneration().register(screenB, null).withBars(screenB) { topBar("B") }
        apply(withB)
        apply(withB.unregister(screenB).unregister(screenA))

        assertEquals(listOf<Any?>(screenA, screenB, null), recalibrations)
        assertNull(slot.navigationItem.title)
    }
}
