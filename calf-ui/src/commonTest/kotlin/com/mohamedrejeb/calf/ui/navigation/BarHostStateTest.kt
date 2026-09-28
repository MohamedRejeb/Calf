package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class BarHostStateTest {

    private val tabs = Any()
    private val home = Any()
    private val detail = Any()
    private val sheet = Any()

    private val homeBar = HostedTopBar(title = "Home")
    private val detailBar = HostedTopBar(title = "Detail")
    private val tabBar = HostedTabBar(items = listOf(UIKitUITabBarItem("A"), UIKitUITabBarItem("B")), selectedIndex = 0)

    private fun BarHostState.topBar(key: Any, bar: HostedTopBar?) =
        withBars(key) { it.copy(topBar = bar) }

    // region owner

    @Test
    fun `an empty host has no owner and shows no bars`() {
        val state = BarHostState()

        assertNull(state.owner)
        assertEquals(HostedBars(), state.shownBars)
    }

    @Test
    fun `the newest registered scaffold owns the bar`() {
        val state = BarHostState()
            .register(home, parent = null).topBar(home, homeBar)
            .register(detail, parent = null).topBar(detail, detailBar)

        assertSame(detail, state.owner)
        assertEquals(detailBar, state.shownBars.topBar)
    }

    @Test
    fun `a scaffold that leaves hands the bar back to the previous one`() {
        val state = BarHostState()
            .register(home, parent = null).topBar(home, homeBar)
            .register(detail, parent = null).topBar(detail, detailBar)
            .unregister(detail)

        assertSame(home, state.owner)
        assertEquals(homeBar, state.shownBars.topBar)
    }

    @Test
    fun `a replaced scaffold that leaves late does not take the bar with it`() {
        // Compose disposes the leaving screen after the entering one composed.
        val state = BarHostState()
            .register(home, parent = null).topBar(home, homeBar)
            .register(detail, parent = null).topBar(detail, detailBar)
            .unregister(home)

        assertSame(detail, state.owner)
        assertEquals(detailBar, state.shownBars.topBar)
    }

    @Test
    fun `registering twice keeps the first position`() {
        val state = BarHostState()
            .register(home, parent = null)
            .register(detail, parent = null)
            .register(home, parent = null)

        assertSame(detail, state.owner)
    }

    @Test
    fun `a scaffold without bars still owns and hides the bar`() {
        val state = BarHostState()
            .register(home, parent = null).topBar(home, homeBar)
            .register(detail, parent = null)

        assertSame(detail, state.owner)
        assertNull(state.shownBars.topBar)
    }

    @Test
    fun `bars of an unregistered scaffold are ignored`() {
        val state = BarHostState().topBar(home, homeBar)

        assertNull(state.owner)
        assertEquals(HostedBars(), state.shownBars)
    }

    // endregion

    // region nesting

    @Test
    fun `an inner scaffold fills its empty slots from the scaffolds around it`() {
        val state = BarHostState()
            .register(tabs, parent = null).withBars(tabs) { it.copy(tabBar = tabBar) }
            .register(home, parent = tabs).topBar(home, homeBar)

        assertSame(home, state.owner)
        assertEquals(HostedBars(topBar = homeBar, tabBar = tabBar), state.shownBars)
        assertSame(tabs, state.shownTabBarSource)
    }

    @Test
    fun `an inner scaffold bar wins over the same slot around it`() {
        val state = BarHostState()
            .register(tabs, parent = null).withBars(tabs) { it.copy(topBar = homeBar) }
            .register(detail, parent = tabs).topBar(detail, detailBar)

        assertEquals(detailBar, state.shownBars.topBar)
    }

    @Test
    fun `a scaffold outside the owner ancestry shows nothing`() {
        // A screen pushed over the tabs: the tab bar must hide as soon as it takes the bar.
        val state = BarHostState()
            .register(tabs, parent = null).withBars(tabs) { it.copy(topBar = homeBar, tabBar = tabBar) }
            .register(detail, parent = null).topBar(detail, detailBar)

        assertEquals(HostedBars(topBar = detailBar), state.shownBars)
        assertNull(state.shownTabBarSource)
    }

    @Test
    fun `an ancestor that already left is skipped`() {
        val state = BarHostState()
            .register(tabs, parent = null).withBars(tabs) { it.copy(tabBar = tabBar) }
            .register(home, parent = tabs).topBar(home, homeBar)
            .unregister(tabs)

        assertEquals(HostedBars(topBar = homeBar), state.shownBars)
    }

    // endregion

    // region back gesture

    @Test
    fun `a back gesture keeps the owner while the previous screen composes`() {
        val state = BarHostState()
            .register(detail, parent = null).topBar(detail, detailBar)
            .withBackGesture(inProgress = true)
            .register(home, parent = null).topBar(home, homeBar)

        assertSame(detail, state.owner)
        assertEquals(detailBar, state.shownBars.topBar)
    }

    @Test
    fun `a committed back gesture hands the bar over when the screen leaves`() {
        val state = BarHostState()
            .register(detail, parent = null).topBar(detail, detailBar)
            .withBackGesture(inProgress = true)
            .register(home, parent = null).topBar(home, homeBar)
            .withBackGesture(inProgress = false)

        assertSame(detail, state.owner)
        assertSame(home, state.unregister(detail).owner)
    }

    @Test
    fun `a cancelled back gesture never shows the previous screen bar`() {
        val cancelled = BarHostState()
            .register(detail, parent = null).topBar(detail, detailBar)
            .withBackGesture(inProgress = true)
            .register(home, parent = null).topBar(home, homeBar)
            .withBackGesture(inProgress = false)

        // Navigation 3 animates back for a while before the previous screen leaves.
        assertSame(detail, cancelled.owner)
        val settled = cancelled.unregister(home)
        assertSame(detail, settled.owner)

        // Once settled the freeze is gone: a new screen takes the bar as usual.
        assertSame(sheet, settled.register(sheet, parent = null).owner)
    }

    @Test
    fun `a screen that composes after the gesture ended takes the bar`() {
        val state = BarHostState()
            .register(detail, parent = null)
            .withBackGesture(inProgress = true)
            .register(home, parent = null)
            .withBackGesture(inProgress = false)
            .register(sheet, parent = null)

        assertSame(sheet, state.owner)
    }

    @Test
    fun `a gesture over an empty host freezes nothing`() {
        val state = BarHostState()
            .withBackGesture(inProgress = true)
            .register(home, parent = null)

        assertSame(home, state.owner)
    }

    // endregion

    // region content insets

    @Test
    fun `the owner gets the live insets`() {
        val live = IosBarEdgeInsets(top = 168.dp, bottom = 34.dp)
        val state = BarHostState().register(home, parent = null).withLiveInsets(live)

        assertEquals(live, state.contentInsetsOf(home, live))
    }

    @Test
    fun `a scaffold that lost the bar keeps its last insets`() {
        val inline = IosBarEdgeInsets(top = 116.dp)
        val large = IosBarEdgeInsets(top = 168.dp)
        val state = BarHostState()
            .register(detail, parent = null).withLiveInsets(inline)
            .register(home, parent = null).withLiveInsets(large)

        assertEquals(inline, state.contentInsetsOf(detail, large))
        assertEquals(large, state.contentInsetsOf(home, large))
    }

    @Test
    fun `a scaffold that never owned the bar gets the live insets`() {
        val live = IosBarEdgeInsets(top = 116.dp)
        val state = BarHostState()
            .register(detail, parent = null)
            .withBackGesture(inProgress = true)
            .register(home, parent = null)

        assertEquals(live, state.contentInsetsOf(home, live))
    }

    @Test
    fun `insets of a scaffold that left are forgotten`() {
        val state = BarHostState()
            .register(home, parent = null).withLiveInsets(IosBarEdgeInsets(top = 116.dp))
            .unregister(home)
        val live = IosBarEdgeInsets(top = 62.dp)

        assertEquals(live, state.contentInsetsOf(home, live))
    }

    // endregion
}
