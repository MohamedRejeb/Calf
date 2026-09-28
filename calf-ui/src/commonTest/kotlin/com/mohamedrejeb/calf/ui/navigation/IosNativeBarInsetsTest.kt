package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class IosNativeBarInsetsTest {

    private val topBar = Any()
    private val tabBar = Any()
    private val toolbar = Any()

    // region contributions

    @Test
    fun `no bars means no insets`() {
        assertEquals(IosBarEdgeInsets.Zero, IosNativeBarInsets().total)
    }

    @Test
    fun `bars on different edges each claim their own edge`() {
        val insets = IosNativeBarInsets()
            .with(topBar, IosBarEdgeInsets(top = 116.dp))
            .with(tabBar, IosBarEdgeInsets(bottom = 83.dp))

        assertEquals(IosBarEdgeInsets(top = 116.dp, bottom = 83.dp), insets.total)
    }

    @Test
    fun `a bar can claim a side edge`() {
        val insets = IosNativeBarInsets()
            .with(tabBar, IosBarEdgeInsets(right = 84.dp))

        assertEquals(IosBarEdgeInsets(right = 84.dp), insets.total)
    }

    @Test
    fun `two bars on one edge combine to the one reaching furthest in`() {
        // Contributions are distances from the host view's edge, safe-area inset included,
        // so stacked bars must not be added up.
        val insets = IosNativeBarInsets()
            .with(tabBar, IosBarEdgeInsets(bottom = 83.dp))
            .with(toolbar, IosBarEdgeInsets(bottom = 127.dp))

        assertEquals(IosBarEdgeInsets(bottom = 127.dp), insets.total)
    }

    @Test
    fun `a bar that publishes again replaces only its own contribution`() {
        val insets = IosNativeBarInsets()
            .with(topBar, IosBarEdgeInsets(top = 101.dp))
            .with(tabBar, IosBarEdgeInsets(bottom = 83.dp))
            .with(topBar, IosBarEdgeInsets(top = 78.dp))

        assertEquals(IosBarEdgeInsets(top = 78.dp, bottom = 83.dp), insets.total)
    }

    @Test
    fun `a bar that leaves removes only its own contribution`() {
        val insets = IosNativeBarInsets()
            .with(tabBar, IosBarEdgeInsets(bottom = 83.dp))
            .with(toolbar, IosBarEdgeInsets(bottom = 127.dp))
            .without(toolbar)

        assertEquals(IosBarEdgeInsets(bottom = 83.dp), insets.total)
    }

    @Test
    fun `a replaced bar that leaves after its replacement arrived does not wipe it`() {
        // Compose disposes the old bar after the new one composed.
        val oldTopBar = Any()
        val insets = IosNativeBarInsets()
            .with(oldTopBar, IosBarEdgeInsets(top = 116.dp))
            .with(topBar, IosBarEdgeInsets(top = 168.dp))
            .without(oldTopBar)

        assertEquals(IosBarEdgeInsets(top = 168.dp), insets.total)
    }

    @Test
    fun `removing an unknown bar changes nothing`() {
        val insets = IosNativeBarInsets().with(topBar, IosBarEdgeInsets(top = 116.dp))

        assertSame(insets, insets.without(tabBar))
    }

    @Test
    fun `publishing an unchanged contribution changes nothing`() {
        val insets = IosNativeBarInsets().with(topBar, IosBarEdgeInsets(top = 116.dp))

        assertSame(insets, insets.with(topBar, IosBarEdgeInsets(top = 116.dp)))
    }

    @Test
    fun `holders with the same contributions are equal`() {
        assertEquals(
            IosNativeBarInsets().with(topBar, IosBarEdgeInsets(top = 116.dp)),
            IosNativeBarInsets().with(topBar, IosBarEdgeInsets(top = 116.dp)),
        )
    }

    // endregion

    // region merging into the scaffold's padding

    @Test
    fun `native bar insets replace a smaller scaffold padding on every edge`() {
        val merged = PaddingValues(start = 0.dp, top = 62.dp, end = 0.dp, bottom = 34.dp)
            .withNativeBarInsets(
                IosBarEdgeInsets(left = 69.dp, top = 116.dp, right = 84.dp, bottom = 83.dp),
                LayoutDirection.Ltr,
            )

        assertEquals(PaddingValues.Absolute(left = 69.dp, top = 116.dp, right = 84.dp, bottom = 83.dp), merged)
    }

    @Test
    fun `a larger scaffold padding wins over the native bar insets`() {
        // The Duo cover display: the window's own safe area reserves 84 on one side only.
        val merged = PaddingValues(start = 0.dp, top = 82.dp, end = 84.dp, bottom = 83.dp)
            .withNativeBarInsets(IosBarEdgeInsets(top = 82.dp, bottom = 65.dp), LayoutDirection.Ltr)

        assertEquals(PaddingValues.Absolute(left = 0.dp, top = 82.dp, right = 84.dp, bottom = 83.dp), merged)
    }

    @Test
    fun `native insets are absolute and do not flip in right-to-left`() {
        val merged = PaddingValues(start = 16.dp, end = 0.dp)
            .withNativeBarInsets(IosBarEdgeInsets(right = 84.dp), LayoutDirection.Rtl)

        // start is the right edge in RTL: max(16, 84) on the right, nothing on the left.
        assertEquals(PaddingValues.Absolute(left = 0.dp, top = 0.dp, right = 84.dp, bottom = 0.dp), merged)
    }

    @Test
    fun `without native bars the scaffold padding is kept`() {
        val merged = PaddingValues(start = 47.dp, top = 24.dp, end = 12.dp, bottom = 21.dp)
            .withNativeBarInsets(IosBarEdgeInsets.Zero, LayoutDirection.Ltr)

        assertEquals(PaddingValues.Absolute(left = 47.dp, top = 24.dp, right = 12.dp, bottom = 21.dp), merged)
    }

    // endregion

    // region reach

    @Test
    fun `a box filling the host gets every inset`() {
        val insets = IosBarEdgeInsets(left = 0.dp, top = 82.dp, right = 84.dp, bottom = 34.dp)

        assertEquals(insets, insets.reaching(IosBarEdgeInsets.Zero))
    }

    @Test
    fun `a box already moved below the bar gets no top inset`() {
        // An inner scaffold inside an outer one that applied its padding.
        val insets = IosBarEdgeInsets(top = 168.dp, bottom = 83.dp)

        assertEquals(
            IosBarEdgeInsets(top = 0.dp, bottom = 0.dp),
            insets.reaching(IosBarEdgeInsets(top = 168.dp, bottom = 83.dp)),
        )
    }

    @Test
    fun `a box partly under a bar gets the part it overlaps`() {
        val insets = IosBarEdgeInsets(top = 116.dp)

        assertEquals(IosBarEdgeInsets(top = 54.dp), insets.reaching(IosBarEdgeInsets(top = 62.dp)))
    }

    @Test
    fun `a left pane does not get the strip on the right`() {
        // Open iPhone Duo, two Compose panes: the list ends at the fold, far from the strip.
        val insets = IosBarEdgeInsets(top = 82.dp, right = 84.dp, bottom = 34.dp)
        val leftPane = IosBarEdgeInsets(right = 475.dp)

        assertEquals(IosBarEdgeInsets(top = 82.dp, bottom = 34.dp), insets.reaching(leftPane))
    }

    // endregion
}
