package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class IosBarLayoutPolicyTest {

    private val portraitNavBar = IosBarHeightKey(IosBarKind.NavigationBar, isCompactHeight = false)
    private val landscapeNavBar = IosBarHeightKey(IosBarKind.NavigationBar, isCompactHeight = true)

    // region height cache

    @Test
    fun `unmeasured bar falls back to the default height`() {
        val heights = IosBarHeights(isLiquidGlassEnabled = false)

        assertEquals(44.dp, heights.heightFor(portraitNavBar))
        assertEquals(32.dp, heights.heightFor(landscapeNavBar))
    }

    @Test
    fun `liquid glass defaults are taller than the classic ones`() {
        val heights = IosBarHeights(isLiquidGlassEnabled = true)

        assertEquals(54.dp, heights.heightFor(portraitNavBar))
        assertEquals(
            106.dp,
            heights.heightFor(IosBarHeightKey(IosBarKind.LargeTitleNavigationBar, isCompactHeight = false)),
        )
        assertEquals(83.dp, heights.heightFor(IosBarHeightKey(IosBarKind.TabBar, isCompactHeight = false)))
    }

    @Test
    fun `measured height is served for the same bar and size class`() {
        val heights = IosBarHeights(isLiquidGlassEnabled = false)
            .withMeasuredHeight(portraitNavBar, 50.dp)

        assertEquals(50.dp, heights.heightFor(portraitNavBar))
    }

    @Test
    fun `height measured in portrait is not served in landscape`() {
        val heights = IosBarHeights(isLiquidGlassEnabled = false)
            .withMeasuredHeight(portraitNavBar, 50.dp)

        assertEquals(32.dp, heights.heightFor(landscapeNavBar))
    }

    @Test
    fun `height measured for one bar kind is not served for another`() {
        val heights = IosBarHeights(isLiquidGlassEnabled = false)
            .withMeasuredHeight(portraitNavBar, 50.dp)

        assertEquals(
            96.dp,
            heights.heightFor(IosBarHeightKey(IosBarKind.LargeTitleNavigationBar, isCompactHeight = false)),
        )
    }

    @Test
    fun `zero height is not cached`() {
        val measured = IosBarHeights(isLiquidGlassEnabled = false)
            .withMeasuredHeight(portraitNavBar, 50.dp)

        assertSame(measured, measured.withMeasuredHeight(portraitNavBar, 0.dp))
    }

    @Test
    fun `negative height is not cached`() {
        val heights = IosBarHeights(isLiquidGlassEnabled = false)

        assertSame(heights, heights.withMeasuredHeight(portraitNavBar, (-1).dp))
    }

    @Test
    fun `measuring the same height again keeps the same instance`() {
        val measured = IosBarHeights(isLiquidGlassEnabled = false)
            .withMeasuredHeight(portraitNavBar, 50.dp)

        assertSame(measured, measured.withMeasuredHeight(portraitNavBar, 50.dp))
    }

    @Test
    fun `tab bar height measured in portrait is not served in landscape`() {
        val heights = IosBarHeights(isLiquidGlassEnabled = false)
            .withMeasuredHeight(IosBarHeightKey(IosBarKind.TabBar, isCompactHeight = false), 49.dp)
            .withMeasuredHeight(IosBarHeightKey(IosBarKind.TabBar, isCompactHeight = true), 30.dp)

        assertEquals(49.dp, heights.heightFor(IosBarHeightKey(IosBarKind.TabBar, isCompactHeight = false)))
        assertEquals(30.dp, heights.heightFor(IosBarHeightKey(IosBarKind.TabBar, isCompactHeight = true)))
    }

    @Test
    fun `caching returns a new instance and leaves the original untouched`() {
        val original = IosBarHeights(isLiquidGlassEnabled = false)

        original.withMeasuredHeight(portraitNavBar, 50.dp)

        assertEquals(44.dp, original.heightFor(portraitNavBar))
    }

    // endregion

    // region content padding

    @Test
    fun `bar below the safe area claims its height plus the inset`() {
        assertEquals(
            91.dp,
            iosBarContentPadding(barHeight = 44.dp, safeAreaInset = 47.dp, barCoversSafeArea = false),
        )
    }

    @Test
    fun `bar that covers the safe area claims only its own height`() {
        assertEquals(
            83.dp,
            iosBarContentPadding(barHeight = 83.dp, safeAreaInset = 34.dp, barCoversSafeArea = true),
        )
    }

    // endregion

    // region layout

    @Test
    fun `estimated layout gives the first frame its padding`() {
        val layout = IosBarLayout.estimated(
            cachedBarHeight = 44.dp,
            safeAreaInset = 47.dp,
            barCoversSafeArea = false,
        )

        assertEquals(91.dp, layout.contentPadding)
    }

    @Test
    fun `measurement of a bar that is not laid out yet keeps the estimate`() {
        val estimated = IosBarLayout.estimated(
            cachedBarHeight = 44.dp,
            safeAreaInset = 47.dp,
            barCoversSafeArea = false,
        )
        val notLaidOut = IosBarMeasurement(x = 0.dp, y = 0.dp, width = 0.dp, barHeight = 0.dp, safeAreaInset = 47.dp)

        assertSame(estimated, estimated.measured(notLaidOut, barCoversSafeArea = false))
    }

    @Test
    fun `rotating to landscape replaces the portrait padding`() {
        val portrait = IosBarLayout.estimated(44.dp, 47.dp, barCoversSafeArea = false).measured(
            IosBarMeasurement(x = 0.dp, y = 47.dp, width = 375.dp, barHeight = 44.dp, safeAreaInset = 47.dp),
            barCoversSafeArea = false,
        )

        val landscape = portrait.measured(
            IosBarMeasurement(x = 0.dp, y = 0.dp, width = 812.dp, barHeight = 32.dp, safeAreaInset = 0.dp),
            barCoversSafeArea = false,
        )

        assertEquals(91.dp, portrait.contentPadding)
        assertEquals(IosBarLayout(x = 0.dp, y = 0.dp, width = 812.dp, contentPadding = 32.dp), landscape)
    }

    @Test
    fun `rotating back to portrait restores the larger padding`() {
        val landscape = IosBarLayout(x = 0.dp, y = 0.dp, width = 812.dp, contentPadding = 32.dp)

        val portrait = landscape.measured(
            IosBarMeasurement(x = 0.dp, y = 47.dp, width = 375.dp, barHeight = 44.dp, safeAreaInset = 47.dp),
            barCoversSafeArea = false,
        )

        assertEquals(IosBarLayout(x = 0.dp, y = 47.dp, width = 375.dp, contentPadding = 91.dp), portrait)
    }

    // endregion
}
