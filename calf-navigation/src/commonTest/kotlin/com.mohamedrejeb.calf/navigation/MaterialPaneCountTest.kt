package com.mohamedrejeb.calf.navigation

import com.mohamedrejeb.calf.navigation.internal.materialHorizontalPartitions
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * How many panes the Material list-detail scene gets. Upstream decides from the window width alone
 * (two from 840dp); Calf adds the open book-style foldable, whose inner display is often narrower.
 */
class MaterialPaneCountTest {

    @Test
    fun upstreamTwoPartitions_areKept() {
        assertEquals(2, materialHorizontalPartitions(upstreamPartitions = 2, windowWidthDp = 851f, hasVerticalHinge = false))
    }

    @Test
    fun compactWidth_isOnePane_evenWithAHinge() {
        assertEquals(1, materialHorizontalPartitions(upstreamPartitions = 1, windowWidthDp = 443f, hasVerticalHinge = true))
    }

    @Test
    fun mediumWidth_withoutAHinge_staysOnePane() {
        assertEquals(1, materialHorizontalPartitions(upstreamPartitions = 1, windowWidthDp = 700f, hasVerticalHinge = false))
    }

    @Test
    fun mediumWidth_withAVerticalHinge_getsTwoPanes() {
        assertEquals(2, materialHorizontalPartitions(upstreamPartitions = 1, windowWidthDp = 700f, hasVerticalHinge = true))
    }

    @Test
    fun mediumWidth_startsAt600dp() {
        assertEquals(1, materialHorizontalPartitions(upstreamPartitions = 1, windowWidthDp = 599f, hasVerticalHinge = true))
        assertEquals(2, materialHorizontalPartitions(upstreamPartitions = 1, windowWidthDp = 600f, hasVerticalHinge = true))
    }

    @Test
    fun threeUpstreamPartitions_becomeTwo_thereIsNoThirdPane() {
        assertEquals(2, materialHorizontalPartitions(upstreamPartitions = 3, windowWidthDp = 1400f, hasVerticalHinge = false))
    }
}
