package com.mohamedrejeb.calf.foldable

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size

/**
 * Where content fits around the fold.
 *
 * @property first the whole window when there is one pane, otherwise the pane to the left of (or
 *   above) the fold.
 * @property second the pane to the right of (or below) the fold, `null` when there is one pane.
 * @property gap the strip between the two panes that content should stay out of. It can be zero
 *   wide: the panes then touch at the split line.
 * @property orientation the direction of the split, `null` when there is one pane.
 */
@ExperimentalCalfFoldableApi
@Immutable
data class FoldPanes(
    val first: Rect,
    val second: Rect? = null,
    val gap: Rect? = null,
    val orientation: FoldOrientation? = null,
) {
    /** How many panes fit: 1 or 2. */
    val count: Int
        get() = if (second == null) 1 else 2
}

/**
 * Works out how many panes fit in a window of [windowSize] and where they go.
 *
 * There are two panes when the device is open ([FoldPosture.isOpen]), the [fold] crosses the whole
 * window, and both sides are larger than [minPaneSize] along the split axis. Otherwise there is
 * one pane filling the window.
 *
 * All values are in pixels, in the window's coordinate space.
 *
 * @param foldMargins extra room to keep clear on each side of the fold. It is only applied while
 *   the fold [separates][FoldInfo.isSeparating] the content. A flat, seamless fold lets the panes
 *   touch.
 */
@ExperimentalCalfFoldableApi
fun calculateFoldPanes(
    windowSize: Size,
    posture: FoldPosture,
    fold: FoldInfo?,
    foldMargins: FoldMargins = FoldMargins.Zero,
    minPaneSize: Float = 0f,
): FoldPanes {
    val window = Rect(0f, 0f, windowSize.width, windowSize.height)
    val singlePane = FoldPanes(first = window)
    if (fold == null || !posture.isOpen || window.isEmpty) return singlePane

    val gap = if (fold.isSeparating) fold.bounds.expandedBy(foldMargins) else fold.bounds
    val panes = when (fold.orientation) {
        FoldOrientation.Vertical -> splitAtVerticalGap(window, gap)
        FoldOrientation.Horizontal -> splitAtHorizontalGap(window, gap)
    } ?: return singlePane

    val smallestPane = when (fold.orientation) {
        FoldOrientation.Vertical -> minOf(panes.first.width, panes.second.width)
        FoldOrientation.Horizontal -> minOf(panes.first.height, panes.second.height)
    }
    if (smallestPane <= 0f || smallestPane < minPaneSize) return singlePane

    return FoldPanes(
        first = panes.first,
        second = panes.second,
        gap = gap,
        orientation = fold.orientation,
    )
}

private fun splitAtVerticalGap(window: Rect, gap: Rect): Pair<Rect, Rect>? {
    val crossesWindow = gap.top <= window.top && gap.bottom >= window.bottom
    if (!crossesWindow) return null
    return Pair(
        Rect(window.left, window.top, gap.left, window.bottom),
        Rect(gap.right, window.top, window.right, window.bottom),
    )
}

private fun splitAtHorizontalGap(window: Rect, gap: Rect): Pair<Rect, Rect>? {
    val crossesWindow = gap.left <= window.left && gap.right >= window.right
    if (!crossesWindow) return null
    return Pair(
        Rect(window.left, window.top, window.right, gap.top),
        Rect(window.left, gap.bottom, window.right, window.bottom),
    )
}

@OptIn(ExperimentalCalfFoldableApi::class)
private fun Rect.expandedBy(margins: FoldMargins): Rect =
    Rect(
        left = left - margins.left,
        top = top - margins.top,
        right = right + margins.right,
        bottom = bottom + margins.bottom,
    )
