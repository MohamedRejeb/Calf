@file:OptIn(ExperimentalForeignApi::class)

package com.mohamedrejeb.calf.ui.datepicker

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UICalendarView
import platform.UIKit.UILayoutPriorityFittingSizeLevel
import platform.UIKit.UILayoutPriorityRequired
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Against real UIKit on the simulator. `UICalendarView` answers a fitting-size question from the
 * width it was last laid out at, not from the width asked for: a calendar laid out at its default
 * 280pt width keeps reporting that layout's height for any width until it is laid out again.
 * The cache has to lay the view out at the requested width before asking.
 */
class FittingHeightCacheTest {

    private val width = 362.0

    /** The height UIKit gives a calendar that was laid out at [width], the reference. */
    private fun referenceHeight(): Double {
        val calendar = UICalendarView()
        calendar.setFrame(CGRectMake(0.0, 0.0, width, 0.0))
        calendar.layoutIfNeeded()
        return calendar.rawFittingHeight()
    }

    /** A calendar laid out at its own default width, the state `sizeToFit()` plus a refresh leaves. */
    private fun calendarLaidOutAtDefaultWidth(): UICalendarView = UICalendarView().apply {
        sizeToFit()
        layoutIfNeeded()
    }

    @Test
    fun `a calendar laid out at another width reports a stale height`() {
        val stale = calendarLaidOutAtDefaultWidth().rawFittingHeight()

        assertNotEquals(referenceHeight(), stale, "UIKit no longer reports a stale height; drop this test")
    }

    @Test
    fun `the cache lays the view out at the requested width before measuring`() {
        val cache = FittingHeightCache(calendarLaidOutAtDefaultWidth())

        assertEquals(referenceHeight(), cache.heightPoints(width.toFloat()))
    }

    @Test
    fun `the cache measures again for a new width`() {
        val calendar = UICalendarView()
        val cache = FittingHeightCache(calendar)
        cache.heightPoints(width.toFloat())

        val widerHeight = cache.heightPoints(WIDER_WIDTH.toFloat())

        assertEquals(WIDER_WIDTH, calendar.frame.useContents { size.width })
        assertEquals(calendar.rawFittingHeight(), widerHeight)
    }

    private fun UICalendarView.rawFittingHeight(): Double = systemLayoutSizeFittingSize(
        CGSizeMake(width, 0.0),
        withHorizontalFittingPriority = UILayoutPriorityRequired,
        verticalFittingPriority = UILayoutPriorityFittingSizeLevel,
    ).useContents { height }
}

private const val WIDER_WIDTH = 511.0
