package com.mohamedrejeb.calf.ui.sheet

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A modal bottom sheet: Material 3's `ModalBottomSheet` on Android, Desktop and Web, a native
 * `UISheetPresentationController` sheet with the medium and large detents on iOS.
 *
 * @param onDismissRequest Called when the user dismisses the sheet: a swipe down, a tap on the
 *   scrim, or the back button on Material.
 * @param adaptiveSheetState The state that drives and reports the sheet's value, from
 *   [rememberAdaptiveSheetState].
 * @param sheetGesturesEnabled Whether the user can drag the sheet. When false, Material ignores
 *   drags on the sheet and iOS presents it with `isModalInPresentation` and no grabber, so it
 *   cannot be swiped away; on iOS the user can still drag between the medium and large detents.
 *   Either way the sheet is still closed by [AdaptiveSheetState.hide] or by leaving composition.
 * @param dragHandle The drag handle shown at the top on Material; on iOS it only decides whether
 *   the system grabber is shown.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
expect fun AdaptiveBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    adaptiveSheetState: AdaptiveSheetState,
    sheetMaxWidth: Dp = BottomSheetDefaults.SheetMaxWidth,
    sheetGesturesEnabled: Boolean = true,
    shape: Shape = BottomSheetDefaults.ExpandedShape,
    containerColor: Color = BottomSheetDefaults.ContainerColor,
    contentColor: Color = contentColorFor(containerColor),
    tonalElevation: Dp = 0.dp,
    scrimColor: Color = BottomSheetDefaults.ScrimColor,
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    contentWindowInsets: @Composable () -> WindowInsets = { BottomSheetDefaults.modalWindowInsets },
    properties: ModalBottomSheetProperties = ModalBottomSheetDefaults.properties,
    content: @Composable ColumnScope.() -> Unit,
)