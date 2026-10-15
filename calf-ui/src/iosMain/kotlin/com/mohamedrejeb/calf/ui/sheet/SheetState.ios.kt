package com.mohamedrejeb.calf.ui.sheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CompletableDeferred

@OptIn(ExperimentalMaterial3Api::class)
@Stable
actual class AdaptiveSheetState @OptIn(ExperimentalMaterial3Api::class)
actual constructor(
    internal val skipPartiallyExpanded: Boolean,
    internal val confirmValueChange: (SheetValue) -> Boolean,
    initialValue: SheetValue,
    internal val skipHiddenState: Boolean,
    positionalThreshold: () -> Float,
    velocityThreshold: () -> Float,
) {
    init {
        if (skipPartiallyExpanded) {
            require(initialValue != SheetValue.PartiallyExpanded) {
                "The initial value must not be set to PartiallyExpanded if skipPartiallyExpanded " +
                        "is set to true."
            }
        }
        if (skipHiddenState) {
            require(initialValue != SheetValue.Hidden) {
                "The initial value must not be set to Hidden if skipHiddenState is set to true."
            }
        }
    }

    internal var iosCurrentValue by mutableStateOf(initialValue)
    internal var iosTargetValue by mutableStateOf(initialValue)

    actual val currentValue: SheetValue get() = iosCurrentValue
    actual val targetValue: SheetValue get() = iosTargetValue
    actual val isVisible get() = currentValue != SheetValue.Hidden

    /** The large detent is always offered. */
    actual val hasExpandedState: Boolean get() = true

    /** The medium detent is offered unless `skipPartiallyExpanded`. */
    actual val hasPartiallyExpandedState: Boolean get() = !skipPartiallyExpanded

    internal var iosSheetManager: BottomSheetManager? = null

    actual suspend fun show() {
        animateTo(if (hasPartiallyExpandedState) SheetValue.PartiallyExpanded else SheetValue.Expanded)
    }

    actual suspend fun hide() {
        check(!skipHiddenState) {
            "Attempted to animate to hidden when skipHiddenState was enabled. Set skipHiddenState" +
                " to false to use this function."
        }
        if (!confirmValueChange(SheetValue.Hidden)) return

        val hidden = CompletableDeferred<Unit>()
        val isHidingSheet = iosSheetManager?.hide(completion = { hidden.complete(Unit) }) ?: false
        if (!isHidingSheet) return

        iosTargetValue = SheetValue.Hidden
        hidden.await()
        iosCurrentValue = SheetValue.Hidden
    }

    actual suspend fun expand() {
        animateTo(SheetValue.Expanded)
    }

    actual suspend fun partialExpand() {
        check(hasPartiallyExpandedState) {
            "Attempted to animate to partial expanded when skipPartiallyExpanded was enabled. Set" +
                " skipPartiallyExpanded to false to use this function."
        }
        animateTo(SheetValue.PartiallyExpanded)
    }

    /**
     * Presents the sheet at [value]'s detent, or moves it there when it is already on screen, and
     * suspends until it is presented.
     */
    private suspend fun animateTo(value: SheetValue) {
        val manager = iosSheetManager ?: return
        if (!confirmValueChange(value)) return

        val presented = CompletableDeferred<Unit>()
        val isPresenting = manager.present(value, completion = { presented.complete(Unit) })
        if (!isPresenting) return

        iosTargetValue = value
        presented.await()
        iosCurrentValue = value
    }

    /**
     * Brings the sheet on screen when its [AdaptiveBottomSheet] enters composition: at the value it
     * already holds (an initial or restored value), else as [show] does.
     */
    internal suspend fun presentOnEnter() {
        when (currentValue) {
            SheetValue.Hidden -> show()
            SheetValue.PartiallyExpanded -> partialExpand()
            SheetValue.Expanded -> expand()
        }
    }

    /** The user dragged the sheet to another detent. */
    internal fun onDetentSelected(value: SheetValue) {
        iosTargetValue = value
        iosCurrentValue = value
    }

    /**
     * The sheet left the screen: the user swiped it away, or its [AdaptiveBottomSheet] left
     * composition. A sheet without a hidden state keeps its value, as Material does.
     */
    internal fun onSheetGone() {
        if (skipHiddenState) return
        iosTargetValue = SheetValue.Hidden
        iosCurrentValue = SheetValue.Hidden
    }

    actual companion object {
        /**
         * The default [Saver] implementation for [AdaptiveSheetState].
         */
        actual fun Saver(
            skipPartiallyExpanded: Boolean,
            positionalThreshold: () -> Float,
            velocityThreshold: () -> Float,
            confirmValueChange: (SheetValue) -> Boolean,
            skipHiddenState: Boolean,
        ) =
            Saver<AdaptiveSheetState, SheetValue>(
                save = { it.currentValue },
                restore = { savedValue ->
                    AdaptiveSheetState(
                        skipPartiallyExpanded,
                        confirmValueChange,
                        savedValue,
                        skipHiddenState,
                        positionalThreshold,
                        velocityThreshold,
                    )
                },
            )
    }
}
