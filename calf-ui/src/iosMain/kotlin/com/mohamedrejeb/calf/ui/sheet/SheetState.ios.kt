package com.mohamedrejeb.calf.ui.sheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlin.concurrent.Volatile

@OptIn(ExperimentalMaterial3Api::class)
@Stable
actual class AdaptiveSheetState @OptIn(ExperimentalMaterial3Api::class)
actual constructor(
    internal val skipPartiallyExpanded: Boolean,
    density: Density,
    initialValue: SheetValue,
    internal val confirmValueChange: (SheetValue) -> Boolean,
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

    var sheetValue by mutableStateOf(initialValue)
    actual val currentValue: SheetValue get() = sheetValue
    actual val isVisible get() = currentValue != SheetValue.Hidden

    @Volatile
    internal var deferredUntilHidden = CompletableDeferred<Unit>()

    /**
     * Expand the bottom sheet with animation and suspend until it is [PartiallyExpanded] if defined
     * else [Expanded].
     * @throws [CancellationException] if the animation is interrupted
     */
    actual suspend fun show() {
        sheetValue = SheetValue.Expanded
    }

    /**
     * Hide the bottom sheet with animation and suspend until it is fully hidden or animation has
     * been cancelled.
     * @throws [CancellationException] if the animation is interrupted
     */
    actual suspend fun hide() {
        sheetValue = SheetValue.Hidden
        deferredUntilHidden.await()
        deferredUntilHidden = CompletableDeferred()
    }

    actual companion object {
        /**
         * The default [Saver] implementation for [AdaptiveSheetState].
         */
        actual fun Saver(
            skipPartiallyExpanded: Boolean,
            confirmValueChange: (SheetValue) -> Boolean,
            density: Density
        ) = Saver<AdaptiveSheetState, SheetValue>(
            save = { it.currentValue },
            restore = { savedValue ->
                AdaptiveSheetState(skipPartiallyExpanded, density, savedValue, confirmValueChange, false)
            }
        )
    }
}