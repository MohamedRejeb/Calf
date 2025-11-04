package com.mohamedrejeb.calf.ui.sheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlin.concurrent.Volatile

@OptIn(ExperimentalMaterial3Api::class)
@Stable
actual class AdaptiveSheetState @OptIn(ExperimentalMaterial3Api::class)
actual constructor(
    internal val skipPartiallyExpanded: Boolean,
    internal val confirmValueChange: (SheetValue) -> Boolean,
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

    internal var iosCurrentValue by mutableStateOf(initialValue)
    internal var iosTargetValue by mutableStateOf(initialValue)

    actual val currentValue: SheetValue get() = iosCurrentValue
    actual val targetValue: SheetValue get() = iosTargetValue
    actual val isVisible get() = currentValue != SheetValue.Hidden

    internal var iosSheetManager: BottomSheetManager? = null

    internal var showDragHandle: Boolean = true

    @Volatile
    private var deferredUntilShown = CompletableDeferred<Unit>()

    @Volatile
    private var deferredUntilHidden = CompletableDeferred<Unit>()

    /**
     * Expand the bottom sheet with animation and suspend until it is [PartiallyExpanded] if defined
     * else [Expanded].
     * @throws [CancellationException] if the animation is interrupted
     */
    actual suspend fun show() {
        val isShowingSheet = iosSheetManager?.show(
            skipPartiallyExpanded = skipPartiallyExpanded,
            showDragHandle = showDragHandle,
            completion = {
                deferredUntilShown.complete(Unit)
            }
        ) ?: false

        if (!isShowingSheet) return

        iosTargetValue = SheetValue.Expanded

        deferredUntilShown.await()
        deferredUntilShown = CompletableDeferred()

        iosCurrentValue = SheetValue.Expanded
    }

    /**
     * Hide the bottom sheet with animation and suspend until it is fully hidden or animation has
     * been cancelled.
     * @throws [CancellationException] if the animation is interrupted
     */
    actual suspend fun hide() {
        val isHidingSheet = iosSheetManager?.hide(
            completion = {
                deferredUntilHidden.complete(Unit)
            }
        ) ?: false

        if (!isHidingSheet) return

        iosTargetValue = SheetValue.Hidden

        deferredUntilHidden.await()
        deferredUntilHidden = CompletableDeferred()

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