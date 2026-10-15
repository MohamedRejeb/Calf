package com.mohamedrejeb.calf.ui.sheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.ui.sheet.AdaptiveSheetState.Companion.Saver
import kotlinx.coroutines.CancellationException

/**
 * Creates and remembers an [AdaptiveSheetState] for an [AdaptiveBottomSheet].
 *
 * The state survives configuration changes and process death through [rememberSaveable]: the
 * sheet's value is saved, and the flags given here are applied again on restore.
 *
 * @param skipPartiallyExpanded Whether the sheet has no partially expanded state: Material drops
 *   the half-height anchor, iOS offers the large detent only.
 * @param confirmValueChange Optional callback invoked to confirm or veto a pending value change.
 * @param initialValue The value the sheet starts in. An [AdaptiveBottomSheet] entering composition
 *   with a visible value shows itself at that height.
 * @param skipHiddenState Whether the sheet cannot be hidden: Material refuses to drag it down,
 *   iOS presents it with `isModalInPresentation` so it cannot be swiped away. [AdaptiveSheetState.hide]
 *   throws on such a state; close the sheet by leaving composition instead.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberAdaptiveSheetState(
    skipPartiallyExpanded: Boolean = false,
    confirmValueChange: (SheetValue) -> Boolean = { true },
    initialValue: SheetValue = SheetValue.Hidden,
    skipHiddenState: Boolean = false,
): AdaptiveSheetState {
    val density = LocalDensity.current

    val positionalThresholdToPx = { with(density) { PositionalThreshold.toPx() } }
    val velocityThresholdToPx = { with(density) { VelocityThreshold.toPx() } }
    return rememberSaveable(
        skipPartiallyExpanded,
        confirmValueChange,
        initialValue,
        skipHiddenState,
        saver =
            Saver(
                skipPartiallyExpanded = skipPartiallyExpanded,
                positionalThreshold = positionalThresholdToPx,
                velocityThreshold = velocityThresholdToPx,
                confirmValueChange = confirmValueChange,
                skipHiddenState = skipHiddenState,
            ),
    ) {
        AdaptiveSheetState(
            skipPartiallyExpanded = skipPartiallyExpanded,
            confirmValueChange = confirmValueChange,
            initialValue = initialValue,
            skipHiddenState = skipHiddenState,
            positionalThreshold = positionalThresholdToPx,
            velocityThreshold = velocityThresholdToPx,
        )
    }
}

/**
 * State of an [AdaptiveBottomSheet]: Material's `SheetState` on Android, Desktop and Web, the
 * selected detent of a `UISheetPresentationController` on iOS.
 *
 * [SheetValue.Expanded] is the large detent on iOS and [SheetValue.PartiallyExpanded] the medium
 * one; [SheetValue.Hidden] is a sheet that is not presented.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Stable
expect class AdaptiveSheetState(
    skipPartiallyExpanded: Boolean = false,
    confirmValueChange: (SheetValue) -> Boolean = { true },
    initialValue: SheetValue = SheetValue.Hidden,
    skipHiddenState: Boolean = false,
    positionalThreshold: () -> Float,
    velocityThreshold: () -> Float,
) {
    /** The value the sheet currently rests in. */
    val currentValue: SheetValue

    /** The value the sheet is settling towards, [currentValue] when it is at rest. */
    val targetValue: SheetValue

    /** Whether the sheet is visible, that is not [SheetValue.Hidden]. */
    val isVisible: Boolean

    /**
     * Whether the sheet has an expanded state defined. Always true: Material always anchors
     * [SheetValue.Expanded], iOS always offers the large detent.
     */
    val hasExpandedState: Boolean

    /**
     * Whether the sheet has a partially expanded state defined, that is `skipPartiallyExpanded`
     * was false. On Material a sheet that is already laid out also answers false when its content
     * is too short for a half-height stop; on iOS it is whether the medium detent is offered.
     */
    val hasPartiallyExpandedState: Boolean

    /**
     * Expand the bottom sheet with animation and suspend until it is [SheetValue.PartiallyExpanded]
     * if defined else [SheetValue.Expanded]. On iOS the sheet is presented at the medium detent if
     * it has one, else at the large detent.
     * @throws [CancellationException] if the animation is interrupted
     */
    suspend fun show()

    /**
     * Hide the bottom sheet with animation and suspend until it is fully hidden or animation has
     * been cancelled. On iOS the sheet is dismissed.
     * @throws [CancellationException] if the animation is interrupted
     * @throws [IllegalStateException] if `skipHiddenState` is true
     */
    suspend fun hide()

    /**
     * Fully expand the bottom sheet with animation and suspend until it is [SheetValue.Expanded]
     * or the animation has been cancelled. On iOS the large detent is selected, and the sheet is
     * presented first if it is hidden.
     * @throws [CancellationException] if the animation is interrupted
     */
    suspend fun expand()

    /**
     * Animate the bottom sheet to [SheetValue.PartiallyExpanded] and suspend until it is there or
     * the animation has been cancelled. On iOS the medium detent is selected, and the sheet is
     * presented first if it is hidden.
     * @throws [CancellationException] if the animation is interrupted
     * @throws [IllegalStateException] if `skipPartiallyExpanded` is true
     */
    suspend fun partialExpand()

    companion object {
        /**
         * The default [Saver] implementation for [AdaptiveSheetState].
         */
        fun Saver(
            skipPartiallyExpanded: Boolean,
            positionalThreshold: () -> Float,
            velocityThreshold: () -> Float,
            confirmValueChange: (SheetValue) -> Boolean,
            skipHiddenState: Boolean,
        ): Saver<AdaptiveSheetState, SheetValue>
    }
}

internal val PositionalThreshold = 56.dp

internal val VelocityThreshold = 125.dp
