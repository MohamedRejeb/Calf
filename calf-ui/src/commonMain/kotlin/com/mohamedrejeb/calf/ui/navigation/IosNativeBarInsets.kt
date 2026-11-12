package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/*
 * What the native iOS bars claim from the content of an AdaptiveScaffold. Pure rules, no UIKit,
 * so they can be unit tested; the UIKit side only feeds measurements in.
 */

/**
 * Room claimed on each edge of the host view, measured from that edge, so a value already
 * contains the safe-area inset the bar sits next to. Absolute like UIKit's `safeAreaInsets`:
 * left and right do not swap in right-to-left.
 */
@Immutable
internal data class IosBarEdgeInsets(
    val left: Dp = 0.dp,
    val top: Dp = 0.dp,
    val right: Dp = 0.dp,
    val bottom: Dp = 0.dp,
) {
    /**
     * Both claims at once. Each one is a distance from the same edge, so two bars on one edge
     * need the larger distance; adding them up would count the safe-area inset twice.
     */
    fun union(other: IosBarEdgeInsets): IosBarEdgeInsets =
        IosBarEdgeInsets(
            left = maxOf(left, other.left),
            top = maxOf(top, other.top),
            right = maxOf(right, other.right),
            bottom = maxOf(bottom, other.bottom),
        )

    /**
     * The part of these insets that reaches a box whose edges are [boxOffsets] away from the host's
     * edges. A box already moved clear of a bar (an inner scaffold inside a padded outer one, a
     * pane that ends before the far edge) needs no padding for it.
     *
     * A box past an edge (a pane halfway through a slide transition) counts as touching it: the
     * part outside the host is off screen, and padding it would squeeze the content for the whole
     * transition.
     */
    fun reaching(boxOffsets: IosBarEdgeInsets): IosBarEdgeInsets =
        IosBarEdgeInsets(
            left = (left - boxOffsets.left.coerceAtLeast(0.dp)).coerceAtLeast(0.dp),
            top = (top - boxOffsets.top.coerceAtLeast(0.dp)).coerceAtLeast(0.dp),
            right = (right - boxOffsets.right.coerceAtLeast(0.dp)).coerceAtLeast(0.dp),
            bottom = (bottom - boxOffsets.bottom.coerceAtLeast(0.dp)).coerceAtLeast(0.dp),
        )

    /** How far these insets reach beyond [other] on each edge; never negative. */
    fun beyond(other: IosBarEdgeInsets): IosBarEdgeInsets =
        IosBarEdgeInsets(
            left = (left - other.left).coerceAtLeast(0.dp),
            top = (top - other.top).coerceAtLeast(0.dp),
            right = (right - other.right).coerceAtLeast(0.dp),
            bottom = (bottom - other.bottom).coerceAtLeast(0.dp),
        )

    operator fun plus(other: IosBarEdgeInsets): IosBarEdgeInsets =
        IosBarEdgeInsets(
            left = left + other.left,
            top = top + other.top,
            right = right + other.right,
            bottom = bottom + other.bottom,
        )

    companion object {
        val Zero = IosBarEdgeInsets()
    }
}

/**
 * The contributions of all native bars of one scaffold, keyed by bar. A bar only ever replaces
 * or removes its own contribution, so the order in which bars come and go does not matter:
 * Compose disposes a replaced bar *after* its replacement composed, and that late removal must
 * not take the new bar's value with it.
 */
@Immutable
internal class IosNativeBarInsets private constructor(
    private val contributions: Map<Any, IosBarEdgeInsets>,
) {
    constructor() : this(emptyMap())

    /** Everything the bars claim together. */
    val total: IosBarEdgeInsets =
        contributions.values.fold(IosBarEdgeInsets.Zero, IosBarEdgeInsets::union)

    fun with(barKey: Any, insets: IosBarEdgeInsets): IosNativeBarInsets =
        if (contributions[barKey] == insets)
            this
        else
            IosNativeBarInsets(contributions + (barKey to insets))

    fun without(barKey: Any): IosNativeBarInsets =
        if (barKey in contributions)
            IosNativeBarInsets(contributions - barKey)
        else
            this

    override fun equals(other: Any?): Boolean =
        other is IosNativeBarInsets && other.contributions == contributions

    override fun hashCode(): Int =
        contributions.hashCode()

    override fun toString(): String =
        "IosNativeBarInsets(total=$total, bars=${contributions.size})"
}

/**
 * The scaffold's own padding with the native bars taken into account. The native bars are
 * UIKit subviews the Compose scaffold never measures, so on each edge the larger claim wins.
 *
 * The result is absolute because the native insets are physical edges. Start and end are
 * therefore resolved here, with [layoutDirection]: content that overrides
 * `LocalLayoutDirection` further down does not flip them again.
 */
internal fun PaddingValues.withNativeBarInsets(
    insets: IosBarEdgeInsets,
    layoutDirection: LayoutDirection,
): PaddingValues {
    val isLtr = layoutDirection == LayoutDirection.Ltr
    val start = calculateStartPadding(layoutDirection)
    val end = calculateEndPadding(layoutDirection)

    return PaddingValues.Absolute(
        left = maxOf(if (isLtr) start else end, insets.left),
        top = maxOf(calculateTopPadding(), insets.top),
        right = maxOf(if (isLtr) end else start, insets.right),
        bottom = maxOf(calculateBottomPadding(), insets.bottom),
    )
}
