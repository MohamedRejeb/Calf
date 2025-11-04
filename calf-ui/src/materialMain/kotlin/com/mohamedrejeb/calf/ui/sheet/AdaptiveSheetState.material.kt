package com.mohamedrejeb.calf.ui.sheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.Saver
import com.mohamedrejeb.calf.ui.sheet.AdaptiveSheetState.Companion.Saver
import kotlinx.coroutines.CancellationException

@OptIn(ExperimentalMaterial3Api::class)
@Stable
actual class AdaptiveSheetState actual constructor(
    skipPartiallyExpanded: Boolean,
    confirmValueChange: (SheetValue) -> Boolean,
    initialValue: SheetValue,
    skipHiddenState: Boolean,
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

    val materialSheetState = SheetState(
        skipPartiallyExpanded = skipPartiallyExpanded,
        positionalThreshold = positionalThreshold,
        velocityThreshold = velocityThreshold,
        initialValue = initialValue,
        confirmValueChange = confirmValueChange,
        skipHiddenState = skipHiddenState,
    )
    actual val currentValue: SheetValue get() = materialSheetState.currentValue
    actual val targetValue: SheetValue get() = materialSheetState.targetValue
    actual val isVisible get() = materialSheetState.isVisible

    /**
     * Expand the bottom sheet with animation and suspend until it is [PartiallyExpanded] if defined
     * else [Expanded].
     * @throws [CancellationException] if the animation is interrupted
     */
    actual suspend fun show() {
        materialSheetState.show()
    }

    /**
     * Hide the bottom sheet with animation and suspend until it is fully hidden or animation has
     * been cancelled.
     * @throws [CancellationException] if the animation is interrupted
     */
    actual suspend fun hide() {
        materialSheetState.hide()
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