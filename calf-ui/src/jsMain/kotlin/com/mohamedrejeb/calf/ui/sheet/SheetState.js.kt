package com.mohamedrejeb.calf.ui.sheet

import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.Saver
import kotlinx.coroutines.CancellationException

@Stable
actual class SheetState actual constructor(
    skipPartiallyExpanded: Boolean,
    initialValue: SheetValue,
    confirmValueChange: (SheetValue) -> Boolean,
    skipHiddenState: Boolean,
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

    actual val nativeSheetState: Any = ""
    actual val currentValue: SheetValue get() = SheetValue.Hidden
    actual val isVisible get() = false

    /**
     * Expand the bottom sheet with animation and suspend until it is [PartiallyExpanded] if defined
     * else [Expanded].
     * @throws [CancellationException] if the animation is interrupted
     */
    actual suspend fun show() {

    }

    /**
     * Hide the bottom sheet with animation and suspend until it is fully hidden or animation has
     * been cancelled.
     * @throws [CancellationException] if the animation is interrupted
     */
    actual suspend fun hide() {}

    actual companion object {
        /**
         * The default [Saver] implementation for [SheetState].
         */
        actual fun Saver(
            skipPartiallyExpanded: Boolean,
            confirmValueChange: (SheetValue) -> Boolean
        ) = Saver<SheetState, SheetValue>(
            save = { it.currentValue },
            restore = { savedValue ->
                SheetState(skipPartiallyExpanded, savedValue, confirmValueChange, false)
            }
        )
    }
}