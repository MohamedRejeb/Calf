package com.mohamedrejeb.calf.ui.sheet

/**
 * What the native sheet is presented with, from the sheet's state and the composable's parameters.
 *
 * @param hasPartiallyExpandedState Whether the medium detent is offered next to the large one.
 * @param showsGrabber Whether UIKit draws its grabber (`prefersGrabberVisible`).
 * @param preventsDismissal Whether the sheet refuses the swipe down (`isModalInPresentation`); it
 *   is then closed by [AdaptiveSheetState.hide] or by leaving composition.
 */
internal data class SheetPresentationOptions(
    val hasPartiallyExpandedState: Boolean = true,
    val showsGrabber: Boolean = true,
    val preventsDismissal: Boolean = false,
)

/**
 * The options for a sheet with [state], a drag handle or not, and gestures enabled or not.
 *
 * Without gestures the grabber goes too: it invites a drag the sheet refuses. A sheet that cannot
 * be hidden (`skipHiddenState`) refuses the swipe down the same way.
 */
internal fun sheetPresentationOptions(
    state: AdaptiveSheetState,
    hasDragHandle: Boolean,
    sheetGesturesEnabled: Boolean,
): SheetPresentationOptions = SheetPresentationOptions(
    hasPartiallyExpandedState = state.hasPartiallyExpandedState,
    showsGrabber = hasDragHandle && sheetGesturesEnabled,
    preventsDismissal = !sheetGesturesEnabled || state.skipHiddenState,
)
