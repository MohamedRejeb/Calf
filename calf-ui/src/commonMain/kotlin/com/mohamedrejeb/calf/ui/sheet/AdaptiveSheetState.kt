package com.mohamedrejeb.calf.ui.sheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CancellationException

private class SheetValueHolder @OptIn(ExperimentalMaterial3Api::class) constructor(
    var value: SheetValue
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberAdaptiveSheetState(
    skipPartiallyExpanded: Boolean = false,
    confirmValueChange: (SheetValue) -> Boolean = { true },
): AdaptiveSheetState {
    val sheetValueHolder = remember {
        SheetValueHolder(SheetValue.Hidden)
    }

    val state = rememberSaveable(
        skipPartiallyExpanded,
        confirmValueChange,
        saver = AdaptiveSheetState.Saver(
            skipPartiallyExpanded = skipPartiallyExpanded,
            confirmValueChange = confirmValueChange
        )
    ) {
        if (skipPartiallyExpanded && sheetValueHolder.value == SheetValue.PartiallyExpanded)
            sheetValueHolder.value = SheetValue.Expanded

        AdaptiveSheetState(skipPartiallyExpanded, sheetValueHolder.value, confirmValueChange)
    }


    LaunchedEffect(Unit) {
        snapshotFlow { state.currentValue }
            .collect { sheetValueHolder.value = it }
    }

    return state
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