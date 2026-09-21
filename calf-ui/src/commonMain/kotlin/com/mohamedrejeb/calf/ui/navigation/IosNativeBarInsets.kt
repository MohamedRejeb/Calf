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
