package com.mohamedrejeb.calf.ui.sheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.Saver
import com.mohamedrejeb.calf.ui.sheet.AdaptiveSheetState.Companion.Saver

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

    private val enabledValues = enabledSheetValues(skipPartiallyExpanded, skipHiddenState)

    val materialSheetState = SheetState(
        enabledValues = enabledValues,
        positionalThreshold = positionalThreshold,
        velocityThreshold = velocityThreshold,
        initialValue = initialValue,
        confirmValueChange = confirmValueChange,
    )
    actual val currentValue: SheetValue get() = materialSheetState.currentValue
    actual val targetValue: SheetValue get() = materialSheetState.targetValue
    actual val isVisible get() = materialSheetState.isVisible

    actual val hasExpandedState: Boolean
        get() = SheetValue.Expanded in enabledValues

    /**
     * Configured, and once the sheet is laid out, actually anchored: Material drops the
     * half-height stop of a sheet whose content is too short for it. Before layout the sheet has
     * no anchors at all, so the configuration answers.
     */
    actual val hasPartiallyExpandedState: Boolean
        get() = SheetValue.PartiallyExpanded in enabledValues &&
            (!materialSheetState.hasExpandedState || materialSheetState.hasPartiallyExpandedState)

    actual suspend fun show() {
        materialSheetState.show()
    }

    actual suspend fun hide() {
        materialSheetState.hide()
    }

    actual suspend fun expand() {
        materialSheetState.expand()
    }

    actual suspend fun partialExpand() {
        materialSheetState.partialExpand()
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

/** The [SheetValue]s a sheet may settle in, from the two legacy skip flags. */
@OptIn(ExperimentalMaterial3Api::class)
internal fun enabledSheetValues(
    skipPartiallyExpanded: Boolean,
    skipHiddenState: Boolean,
): Set<SheetValue> = buildSet {
    add(SheetValue.Expanded)
    if (!skipPartiallyExpanded) add(SheetValue.PartiallyExpanded)
    if (!skipHiddenState) add(SheetValue.Hidden)
}
