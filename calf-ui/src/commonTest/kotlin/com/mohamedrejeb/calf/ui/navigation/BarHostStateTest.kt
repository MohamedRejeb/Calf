package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

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

    // Open iPhone Duo in landscape: 951pt wide, a 360pt list pane and a detail pane from 384.
    private val width = 951.dp

    private fun BarHostState.fullWidth(key: Any) = withSpan(key, HostedSpan(0.dp, width, width))

    private fun BarHostState.listPane(key: Any) = withSpan(key, HostedSpan(0.dp, 360.dp, width))

    private fun BarHostState.detailPane(key: Any) = withSpan(key, HostedSpan(384.dp, width, width))

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
            .register(home, parent = null).fullWidth(home)
            .register(detail, parent = null).fullWidth(detail)
            .register(home, parent = null)

        assertSame(detail, state.owner)
    }

    @Test
    fun `a scaffold without bars still owns and hides the bar`() {
        val state = BarHostState()
            .register(home, parent = null).topBar(home, homeBar)
            .nextGeneration()
            .register(detail, parent = null).fullWidth(detail)

        assertSame(detail, state.owner)
        assertNull(state.shownBars.topBar)
    }

    @Test
    fun `bars of an unregistered scaffold are ignored`() {
        val state = BarHostState().topBar(home, homeBar)

        assertNull(state.owner)
        assertEquals(HostedBars(), state.shownBars)
    }

    @Test
    fun `of scaffolds that appear together the one with bars owns them`() {
        // Two panes composed in the same frame: a bar-less list beside a detail with a bar.
        val state = BarHostState()
            .register(detail, parent = null).topBar(detail, detailBar)
            .register(home, parent = null)

        assertSame(detail, state.owner)
        assertEquals(detailBar, state.shownBars.topBar)
    }

    @Test
    fun `a bar-less scaffold that appears later still hides the bars`() {
        val state = BarHostState()
            .register(detail, parent = null).topBar(detail, detailBar)
            .nextGeneration()
            .register(home, parent = null).fullWidth(home)

        assertSame(home, state.owner)
        assertNull(state.shownBars.topBar)
    }

    @Test
    fun `of scaffolds that appear together with bars the newest owns them`() {
        val state = BarHostState()
            .register(home, parent = null).topBar(home, homeBar)
            .register(detail, parent = null).topBar(detail, detailBar)

        assertSame(detail, state.owner)
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
        assertSame(sheet, settled.nextGeneration().register(sheet, parent = null).fullWidth(sheet).owner)
    }

    @Test
    fun `a screen that composes after the gesture ended takes the bar`() {
        val state = BarHostState()
            .register(detail, parent = null).fullWidth(detail)
            .withBackGesture(inProgress = true)
            .register(home, parent = null).fullWidth(home)
            .withBackGesture(inProgress = false)
            .register(sheet, parent = null).fullWidth(sheet)

        assertSame(sheet, state.owner)
    }

    @Test
    fun `a gesture over an empty host freezes nothing`() {
        val state = BarHostState()
            .withBackGesture(inProgress = true)
            .register(home, parent = null).fullWidth(home)

        assertSame(home, state.owner)
    }

    // endregion

    // region side by side

    @Test
    fun `a bar-less scaffold is not a candidate before it is placed`() {
        // Its box decides whether it covers the owner or sits beside it.
        val state = BarHostState()
            .register(detail, parent = null).topBar(detail, detailBar)
            .nextGeneration()
            .register(home, parent = null)

        assertSame(detail, state.owner)
        assertSame(home, state.fullWidth(home).owner)
    }

    @Test
    fun `a pane that joins beside the owner does not take its bars`() {
        // A scene change composes the list pane again next to the open detail.
        val state = BarHostState()
            .register(detail, parent = null).topBar(detail, detailBar).detailPane(detail)
            .nextGeneration()
            .register(home, parent = null).listPane(home)

        assertSame(detail, state.owner)
        assertEquals(detailBar, state.shownBars.topBar)
    }

    @Test
    fun `a bar-less screen pushed over the panes takes the bars`() {
        val state = BarHostState()
            .register(home, parent = null).listPane(home)
            .register(detail, parent = null).topBar(detail, detailBar).detailPane(detail)
            .nextGeneration()
            .register(sheet, parent = null).fullWidth(sheet)

        assertSame(sheet, state.owner)
        assertNull(state.shownBars.topBar)
    }

    @Test
    fun `a bar-less detail beside a list with bars leaves them to the list`() {
        val state = BarHostState()
            .register(home, parent = null).topBar(home, homeBar).listPane(home)
            .nextGeneration()
            .register(detail, parent = null).detailPane(detail)

        assertSame(home, state.owner)
    }

    @Test
    fun `a bar-less scaffold nested in one with bars owns them through its ancestry`() {
        val state = BarHostState()
            .register(tabs, parent = null).withBars(tabs) { it.copy(tabBar = tabBar) }.fullWidth(tabs)
            .nextGeneration()
            .register(home, parent = tabs).fullWidth(home)

        assertSame(home, state.owner)
        assertEquals(tabBar, state.shownBars.tabBar)
    }

    @Test
    fun `a placed scaffold reports its span until it leaves`() {
        val state = BarHostState().register(detail, parent = null).detailPane(detail)

        assertEquals(HostedSpan(384.dp, width, width), state.spanOf(detail))
        assertNull(state.unregister(detail).spanOf(detail))
    }

    // endregion

    // region hand-over

    @Test
    fun `when the last hosted scaffold leaves the host is back to its empty state`() {
        val state = BarHostState()
            .register(tabs, parent = null).withBars(tabs) { it.copy(tabBar = tabBar) }
            .register(home, parent = tabs).topBar(home, homeBar)
            .withMeasuredInsets(bars = IosBarEdgeInsets(top = 116.dp), system = IosBarEdgeInsets(top = 62.dp))
            .unregister(home)
            .unregister(tabs)

        assertNull(state.owner)
        assertEquals(HostedBars(), state.shownBars)
        assertFalse(state.hasTabs)
        assertNull(state.ownerSpan)
        val system = IosBarEdgeInsets(top = 62.dp)
        assertEquals(system, state.contentInsetsOf(home, bars = IosBarEdgeInsets(top = 116.dp), system = system))
    }

    @Test
    fun `the host has tabs only while a registered scaffold publishes them`() {
        val withTabs = BarHostState()
            .register(tabs, parent = null).withBars(tabs) { it.copy(tabBar = tabBar) }
            .register(detail, parent = null).topBar(detail, detailBar)

        // Pushed over the tabs: they are hidden, but the tab screen is still composed.
        assertNull(withTabs.shownBars.tabBar)
        assertTrue(withTabs.hasTabs)
        assertFalse(withTabs.unregister(tabs).hasTabs)
    }

    @Test
    fun `a tab owner that withdraws its tab bar no longer counts`() {
        val state = BarHostState()
            .register(tabs, parent = null).withBars(tabs) { it.copy(tabBar = tabBar) }
            .withBars(tabs) { it.copy(tabBar = null) }

        assertFalse(state.hasTabs)
    }

    @Test
    fun `a whole tree swapped for another leaves nothing of the first one`() {
        val second = Any()
        val state = BarHostState()
            .register(tabs, parent = null).withBars(tabs) { it.copy(tabBar = tabBar) }.fullWidth(tabs)
            .register(home, parent = tabs).topBar(home, homeBar).fullWidth(home)
            .withMeasuredInsets(bars = IosBarEdgeInsets(top = 116.dp, bottom = 83.dp), system = IosBarEdgeInsets(top = 62.dp, bottom = 34.dp))
            .nextGeneration()
            // The other shell composes before the first one is disposed.
            .register(second, parent = null).fullWidth(second)
            .unregister(home)
            .unregister(tabs)

        assertSame(second, state.owner)
        assertEquals(HostedBars(), state.shownBars)
        assertFalse(state.hasTabs)
        val system = IosBarEdgeInsets(top = 62.dp, bottom = 34.dp)
        assertEquals(system, state.contentInsetsOf(second, bars = system, system = system))
    }

    @Test
    fun `going from two panes to one keeps the detail bars`() {
        val state = BarHostState()
            .register(home, parent = null).listPane(home)
            .register(detail, parent = null).topBar(detail, detailBar).detailPane(detail)
            .unregister(home)
            .fullWidth(detail)

        assertSame(detail, state.owner)
        assertEquals(detailBar, state.shownBars.topBar)
        assertNull(state.ownerSpan?.takeUnless { it.fillsWidth })
    }

    @Test
    fun `going from one pane to two keeps the detail bars`() {
        val state = BarHostState()
            .register(detail, parent = null).topBar(detail, detailBar).fullWidth(detail)
            .nextGeneration()
            .register(home, parent = null).listPane(home)
            .detailPane(detail)

        assertSame(detail, state.owner)
        assertEquals(HostedSpan(384.dp, width, width), state.ownerSpan)
    }

    // endregion

    // region bar column

    @Test
    fun `a full-width owner leaves no room beside its bar`() {
        assertEquals(IosBarEdgeInsets.Zero, HostedSpan(0.dp, width, width).barColumnInsets())
    }

    @Test
    fun `a detail pane moves its bar past the list`() {
        assertEquals(IosBarEdgeInsets(left = 384.dp), HostedSpan(384.dp, width, width).barColumnInsets())
    }

    @Test
    fun `a list pane keeps its bar off the detail`() {
        assertEquals(IosBarEdgeInsets(right = 591.dp), HostedSpan(0.dp, 360.dp, width).barColumnInsets())
    }

    @Test
    fun `a span that slides past the edges is clamped to the host`() {
        assertEquals(
            HostedSpan(0.dp, 200.dp, width),
            HostedSpan((-160).dp, 200.dp, width).clamped(),
        )
    }

    // endregion

    // region content insets

    private val system = IosBarEdgeInsets(top = 62.dp, bottom = 34.dp)
    private val withTopBar = IosBarEdgeInsets(top = 116.dp, bottom = 34.dp)

    @Test
    fun `scaffolds whose bars are shown get the insets with the bars`() {
        val state = BarHostState()
            .register(tabs, parent = null).withBars(tabs) { it.copy(tabBar = tabBar) }
            .register(home, parent = tabs).topBar(home, homeBar)

        assertEquals(withTopBar, state.contentInsetsOf(home, withTopBar, system))
        assertEquals(withTopBar, state.contentInsetsOf(tabs, withTopBar, system))
    }

    @Test
    fun `a leaving screen keeps its owner-time insets until it is disposed`() {
        // Switch (bar, 116) is popped to a bar-less Home: the bar hides under Switch while it slides out.
        val state = BarHostState()
            .register(detail, parent = null).topBar(detail, detailBar).fullWidth(detail)
            .withMeasuredInsets(withTopBar, system)
            .nextGeneration()
            .register(home, parent = null).fullWidth(home)
            .withMeasuredInsets(bars = system, system = system)

        assertSame(home, state.owner)
        assertEquals(withTopBar, state.contentInsetsOf(detail, bars = system, system = system))
        assertEquals(system, state.contentInsetsOf(home, bars = system, system = system))
    }

    @Test
    fun `a leaving screen follows the system insets but keeps its bar share`() {
        // Rotating while it leaves: the status bar changes, the bar it had does not come back.
        val state = BarHostState()
            .register(detail, parent = null).topBar(detail, detailBar).fullWidth(detail)
            .withMeasuredInsets(withTopBar, system)
            .nextGeneration()
            .register(home, parent = null).fullWidth(home)
        val rotated = IosBarEdgeInsets(left = 62.dp, right = 62.dp, bottom = 21.dp)

        assertEquals(
            IosBarEdgeInsets(left = 62.dp, top = 54.dp, right = 62.dp, bottom = 21.dp),
            state.contentInsetsOf(detail, bars = rotated, system = rotated),
        )
    }

    @Test
    fun `a pane beside the owner never gets the owner bar`() {
        // The list beside a detail with a bar: the bar only covers the detail column.
        val state = BarHostState()
            .register(home, parent = null).listPane(home)
            .withMeasuredInsets(bars = system, system = system)
            .register(detail, parent = null).topBar(detail, detailBar).detailPane(detail)
            .withMeasuredInsets(withTopBar, system)

        assertSame(detail, state.owner)
        assertEquals(system, state.contentInsetsOf(home, withTopBar, system))
    }

    @Test
    fun `a pane that never had the bars gets the system insets`() {
        val state = BarHostState()
            .register(detail, parent = null).topBar(detail, detailBar).detailPane(detail)
            .register(home, parent = null).listPane(home)
            .withMeasuredInsets(withTopBar, system)

        assertEquals(system, state.contentInsetsOf(home, withTopBar, system))
    }

    @Test
    fun `insets of a scaffold that left are forgotten`() {
        val state = BarHostState()
            .register(home, parent = null).topBar(home, homeBar)
            .withMeasuredInsets(withTopBar, system)
            .unregister(home)

        assertEquals(system, state.contentInsetsOf(home, withTopBar, system))
    }

    // endregion

    // region large title calibration

    @Test
    fun `the calibration starts over when a top bar appears or hides or changes title style`() {
        val large = HostedTopBar(configuration = UIKitNavigationBarConfiguration(prefersLargeTitles = true))

        assertTrue(topBarNeedsRecalibration(previous = null, next = homeBar, isNewLayout = false))
        assertTrue(topBarNeedsRecalibration(previous = homeBar, next = null, isNewLayout = false))
        assertTrue(topBarNeedsRecalibration(previous = homeBar, next = large, isNewLayout = false))
        assertTrue(topBarNeedsRecalibration(previous = homeBar, next = detailBar, isNewLayout = true))
    }

    @Test
    fun `the calibration is kept while the same bar only changes its content`() {
        assertFalse(topBarNeedsRecalibration(previous = homeBar, next = detailBar, isNewLayout = false))
        assertFalse(topBarNeedsRecalibration(previous = null, next = null, isNewLayout = true))
    }

    // endregion
}
