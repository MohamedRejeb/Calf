package com.mohamedrejeb.calf.ui.dialog

/** A rectangle in points, in the presenting view's coordinates. */
internal data class AnchorRect(
    val left: Double,
    val top: Double,
    val right: Double,
    val bottom: Double,
)

/** A point in points, in the presenting view's coordinates. */
internal data class AnchorPoint(val x: Double, val y: Double)

/**
 * Where the action-sheet style of an iOS alert is centred when UIKit shows it as a popover
 * (regular width: iPad, an open iPhone Duo): the middle of the column that raised it, instead of
 * the middle of the window.
 *
 * @param presenter the presenting view's safe area, which is its column in a split view.
 * @param pane the horizontal extent of the pane that raised it, when one Compose surface holds
 *   several panes (plain Compose navigation in the bar host); `null` otherwise.
 */
internal fun actionSheetAnchor(presenter: AnchorRect, pane: ClosedFloatingPointRange<Double>?): AnchorPoint {
    val left = pane?.start?.coerceIn(presenter.left, presenter.right) ?: presenter.left
    val right = pane?.endInclusive?.coerceIn(left, presenter.right) ?: presenter.right
    return AnchorPoint(x = (left + right) / 2, y = (presenter.top + presenter.bottom) / 2)
}
