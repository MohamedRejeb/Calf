package com.mohamedrejeb.calf.navigation.internal

/**
 * How many panes the Material list-detail scene lays out side by side.
 *
 * Material 3 adaptive decides from the window width alone: two panes from the expanded width
 * (840dp), and three on very wide windows. A hinge never changes its count. Calf changes two
 * things:
 *  - an open book-style foldable gets two panes from the medium width already, one on each side
 *    of the fold, the way the open iPhone Duo does. Most inner displays are narrower than 840dp;
 *  - there is no third pane, so more than two becomes two.
 *
 * @param upstreamPartitions `PaneScaffoldDirective.maxHorizontalPartitions` as Material computed it.
 * @param hasVerticalHinge whether a hinge runs top to bottom through the window.
 */
internal fun materialHorizontalPartitions(
    upstreamPartitions: Int,
    windowWidthDp: Float,
    hasVerticalHinge: Boolean,
): Int {
    val foldSplitsTheWindow = hasVerticalHinge && windowWidthDp >= MEDIUM_WIDTH_DP
    val partitions = if (foldSplitsTheWindow) maxOf(upstreamPartitions, MAX_PANES) else upstreamPartitions
    return partitions.coerceIn(1, MAX_PANES)
}

/** Lower bound of Material's medium window width class. */
private const val MEDIUM_WIDTH_DP = 600f
private const val MAX_PANES = 2
