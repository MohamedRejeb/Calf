package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import com.mohamedrejeb.calf.core.InternalCalfApi

/**
 * Nested-scroll connection that tracks how far a screen's content has
 * scrolled from its top, so a platform host can mirror it into a native
 * scroll view (iOS uses that to collapse large titles and switch the bar's
 * scroll-edge appearance).
 *
 * Only consumed deltas count, so overscroll does not move the value, and it
 * is clamped at zero so returning to the top always reports zero. Values are
 * in pixels, positive when scrolled down.
 */
@InternalCalfApi
public class LargeTitleScrollTracker(
    private val onOffsetChange: (Float) -> Unit,
) : NestedScrollConnection {
    public var offset: Float = 0f
        private set

    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
        val next = (offset - consumed.y).coerceAtLeast(0f)
        if (next != offset) {
            offset = next
            onOffsetChange(next)
        }
        return Offset.Zero
    }
}
