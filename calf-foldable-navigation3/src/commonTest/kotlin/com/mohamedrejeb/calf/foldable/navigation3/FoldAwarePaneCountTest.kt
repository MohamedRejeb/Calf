package com.mohamedrejeb.calf.foldable.navigation3

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The pane-count rule both Calf paths share: calf-navigation's Material list-detail and the plain
 * Navigation 3 strategy. Upstream decides from the window width alone (two from 840dp); Calf adds
 * the open book-style foldable from the medium width, one pane on each side of the fold.
 */
class FoldAwarePaneCountTest {

    @Test
    fun upstreamTwoPartitions_areKept() {
        assertEquals(2, foldAwareHorizontalPartitions(upstreamPartitions = 2, minWidthDp = 840, hasVerticalHinge = false))
    }

    @Test
    fun compactWidth_isOnePane_evenWithAHinge() {
        assertEquals(1, foldAwareHorizontalPartitions(upstreamPartitions = 1, minWidthDp = 0, hasVerticalHinge = true))
    }

    @Test
    fun mediumWidth_withoutAHinge_staysOnePane() {
        assertEquals(1, foldAwareHorizontalPartitions(upstreamPartitions = 1, minWidthDp = 600, hasVerticalHinge = false))
    }

    @Test
    fun mediumWidth_withAVerticalHinge_getsTwoPanes() {
        assertEquals(2, foldAwareHorizontalPartitions(upstreamPartitions = 1, minWidthDp = 600, hasVerticalHinge = true))
    }

    @Test
    fun mediumWidth_startsAt600dp() {
        assertEquals(1, foldAwareHorizontalPartitions(upstreamPartitions = 1, minWidthDp = 599, hasVerticalHinge = true))
        assertEquals(2, foldAwareHorizontalPartitions(upstreamPartitions = 1, minWidthDp = 600, hasVerticalHinge = true))
    }

    @Test
    fun threeUpstreamPartitions_becomeTwo_thereIsNoThirdPane() {
        assertEquals(2, foldAwareHorizontalPartitions(upstreamPartitions = 3, minWidthDp = 1200, hasVerticalHinge = false))
    }
}
