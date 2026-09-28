package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The horizontal extent of a hosted scaffold in the bar host's Compose surface: from [left] to
 * [right], in a surface [hostWidth] wide. A pane of a list-detail layout spans part of the width;
 * a screen spans all of it.
 */
@Immutable
internal data class HostedSpan(
    val left: Dp,
    val right: Dp,
    val hostWidth: Dp,
) {
    val fillsWidth: Boolean
        get() = left <= SPAN_EPSILON && right >= hostWidth - SPAN_EPSILON

    /** Within the host: the part of a sliding pane that is off screen does not count. */
    fun clamped(): HostedSpan =
        copy(
            left = left.coerceIn(0.dp, hostWidth),
            right = right.coerceIn(0.dp, hostWidth),
        )

    /** Side by side with [other], not overlapping it: two panes, not a screen over another. */
    fun isBeside(other: HostedSpan): Boolean =
        right <= other.left + SPAN_EPSILON || other.right <= left + SPAN_EPSILON

    /**
     * Room the owner's bar leaves on each side so that it covers only this span: a detail pane's
     * title starts after the list, the way a split view's secondary column does it.
     */
    fun barColumnInsets(): IosBarEdgeInsets =
        if (fillsWidth)
            IosBarEdgeInsets.Zero
        else
            IosBarEdgeInsets(left = left, right = (hostWidth - right).coerceAtLeast(0.dp))
}

/** Rounding slack for spans measured from pixels. */
private val SPAN_EPSILON = 1.dp
