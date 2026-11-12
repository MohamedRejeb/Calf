package com.mohamedrejeb.calf.foldable.navigation3

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.HingePolicy
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.mohamedrejeb.calf.foldable.ExperimentalCalfFoldableApi
import com.mohamedrejeb.calf.foldable.rememberCalfWindowAdaptiveInfo

/**
 * Material 3 Adaptive's [PaneScaffoldDirective] for [windowAdaptiveInfo], with two panes on an
 * open book-style foldable.
 *
 * Upstream's `calculatePaneScaffoldDirective` decides the pane count from the window width alone:
 * two panes from the expanded width (840dp). Most foldable inner displays are narrower, so an open
 * iPhone Duo or Pixel Fold would keep one pane. This gives two panes from the medium width (600dp)
 * when a vertical hinge runs through the window, and never a third pane. Everything else,
 * including how a separating hinge is kept clear ([verticalHingePolicy]), is upstream's. When the
 * rule changes nothing, the result is upstream's own object.
 *
 * Pass [rememberCalfWindowAdaptiveInfo] to see the fold on iOS, where Compose Multiplatform's own
 * window info has no posture.
 */
@ExperimentalCalfFoldableApi
@ExperimentalMaterial3AdaptiveApi
fun calfPaneScaffoldDirective(
    windowAdaptiveInfo: WindowAdaptiveInfo,
    verticalHingePolicy: HingePolicy = HingePolicy.AvoidSeparating,
): PaneScaffoldDirective {
    val upstream = calculatePaneScaffoldDirective(windowAdaptiveInfo, verticalHingePolicy)
    val partitions = foldAwareHorizontalPartitions(
        upstreamPartitions = upstream.maxHorizontalPartitions,
        minWidthDp = windowAdaptiveInfo.windowSizeClass.minWidthDp,
        hasVerticalHinge = windowAdaptiveInfo.windowPosture.hingeList.any { it.isVertical },
    )
    if (partitions == upstream.maxHorizontalPartitions) return upstream

    return upstream.copy(
        maxHorizontalPartitions = partitions,
        horizontalPartitionSpacerSize = if (partitions > 1) PANE_SPACER else 0.dp,
    )
}

/**
 * How many panes are laid out side by side. The rule both Calf paths share: calf-navigation's
 * Material list-detail and [rememberCalfListDetailSceneStrategy].
 *
 * @param upstreamPartitions `PaneScaffoldDirective.maxHorizontalPartitions` as Material computed it.
 * @param minWidthDp the lower bound of the window's width class, [WindowSizeClass.minWidthDp].
 * @param hasVerticalHinge whether a hinge runs top to bottom through the window.
 */
internal fun foldAwareHorizontalPartitions(
    upstreamPartitions: Int,
    minWidthDp: Int,
    hasVerticalHinge: Boolean,
): Int {
    val foldSplitsTheWindow = hasVerticalHinge && minWidthDp >= WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND
    val partitions = if (foldSplitsTheWindow) maxOf(upstreamPartitions, MAX_PANES) else upstreamPartitions
    return partitions.coerceIn(1, MAX_PANES)
}

/** Material's spacer between two horizontal partitions. */
private val PANE_SPACER = 24.dp
private const val MAX_PANES = 2
