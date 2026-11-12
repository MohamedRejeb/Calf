package com.mohamedrejeb.calf.ui.sheet

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.ui.navigation.IosBarEdgeInsets

/**
 * The room a native iOS sheet's grabber takes at the top of the content when it is shown. UIKit
 * draws the grabber over the content without insetting it (a 5pt pill 5pt from the top), so
 * content that starts at the top sits under it. This is the iOS counterpart of the height of
 * Material's drag handle slot, sized for the smaller grabber.
 */
internal val SheetGrabberInset: Dp = 24.dp

/**
 * The part of a native iOS sheet its Compose content is kept out of: the unsafe sides, and the
 * grabber at the top when there is one.
 *
 * UIKit places the sheet itself (centred, or on a half-open iPhone Duo in the leading pane) and
 * reports what of it is unsafe. The sides matter where the sheet reaches under something: the
 * iPhone Duo's vertical bar and camera in the closed poses, the Dynamic Island in iPhone landscape.
 * The bottom belongs to the content's own scrolling, as for any UIKit sheet.
 *
 * @param sheetSafeArea the sheet view's `safeAreaInsets`. Compose's `WindowInsets` inside the sheet
 *   report the window's edges instead, so they cannot be used for this.
 * @param grabberShown whether UIKit draws its grabber at the top of the sheet.
 */
internal fun sheetContentInsets(sheetSafeArea: IosBarEdgeInsets, grabberShown: Boolean): IosBarEdgeInsets =
    IosBarEdgeInsets(
        left = sheetSafeArea.left,
        top = if (grabberShown) SheetGrabberInset else 0.dp,
        right = sheetSafeArea.right,
    )
