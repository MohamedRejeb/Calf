package com.mohamedrejeb.calf.ui.sheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.CancellationException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberAdaptiveSheetState(
    skipPartiallyExpanded: Boolean = false,
    confirmValueChange: (SheetValue) -> Boolean = { true },
): AdaptiveSheetState {
    return rememberSaveable(
        skipPartiallyExpanded, confirmValueChange,
        saver = AdaptiveSheetState.Saver(
            skipPartiallyExpanded = skipPartiallyExpanded,
            confirmValueChange = confirmValueChange
        )
    ) {
        AdaptiveSheetState(skipPartiallyExpanded, SheetValue.Hidden, confirmValueChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Stable
expect class AdaptiveSheetState(
    skipPartiallyExpanded: Boolean,
    initialValue: SheetValue = SheetValue.Hidden,
    confirmValueChange: (SheetValue) -> Boolean = { true },
    skipHiddenState: Boolean = false,
) {
    val currentValue: SheetValue
    val isVisible: Boolean

    /**
     * Expand the bottom sheet with animation and suspend until it is [PartiallyExpanded] if defined
     * else [Expanded].
     * @throws [CancellationException] if the animation is interrupted
     */
    suspend fun show()

    /**
     * Hide the bottom sheet with animation and suspend until it is fully hidden or animation has
     * been cancelled.
     * @throws [CancellationException] if the animation is interrupted
     */
    suspend fun hide()

    companion object {
        /**
         * The default [Saver] implementation for [AdaptiveSheetState].
         */
        fun Saver(
            skipPartiallyExpanded: Boolean,
            confirmValueChange: (SheetValue) -> Boolean
        ): Saver<AdaptiveSheetState, SheetValue>
    }
}