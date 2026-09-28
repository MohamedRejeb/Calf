package com.mohamedrejeb.calf.ui.sheet

/** Where a native iOS sheet sits across the window (iOS 27 `UISheetPresentationController.preferredPlacement`). */
internal enum class SheetPlacement {
    /** The system decides: centred, or on an iPhone Duo in book pose, the leading pane. */
    Automatic,
    Leading,
    Trailing,
}

/**
 * The side of the window the sheet belongs to, from the horizontal extent of the column that
 * presents it ([columnLeft] to [columnRight], window points). A column in one half of the window
 * (a split view's detail column, a pane of an open iPhone Duo) gets the sheet on its side instead
 * of across both columns, or over the other one; anything that reaches across the middle keeps
 * the system placement.
 */
internal fun sheetPlacement(
    columnLeft: Double,
    columnRight: Double,
    windowWidth: Double,
    isRightToLeft: Boolean,
): SheetPlacement {
    if (windowWidth <= 0.0) return SheetPlacement.Automatic
    val middle = windowWidth / 2
    val isLeftHalf = columnRight <= middle + MIDDLE_TOLERANCE
    val isRightHalf = columnLeft >= middle - MIDDLE_TOLERANCE
    return when {
        isLeftHalf == isRightHalf -> SheetPlacement.Automatic
        isLeftHalf != isRightToLeft -> SheetPlacement.Leading
        else -> SheetPlacement.Trailing
    }
}

/**
 * Points a column may reach past the middle and still count as one half: a pane of a half-open
 * iPhone Duo ends inside the 40pt fold region, not exactly at the crease.
 */
private const val MIDDLE_TOLERANCE = 24.0
