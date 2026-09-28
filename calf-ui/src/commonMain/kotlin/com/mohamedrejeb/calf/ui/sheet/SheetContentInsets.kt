package com.mohamedrejeb.calf.ui.sheet

import com.mohamedrejeb.calf.ui.navigation.IosBarEdgeInsets

/**
 * The part of a native iOS sheet's own safe area its Compose content is kept out of: the sides.
 *
 * UIKit places the sheet itself (centred, or on a half-open iPhone Duo in the leading pane) and
 * reports what of it is unsafe. The sides matter where the sheet reaches under something: the
 * iPhone Duo's vertical bar and camera in the closed poses, the Dynamic Island in iPhone landscape.
 * The top belongs to the sheet's grabber and the bottom to the content's own scrolling, as for
 * any UIKit sheet.
 *
 * @param sheetSafeArea the sheet view's `safeAreaInsets`. Compose's `WindowInsets` inside the sheet
 *   report the window's edges instead, so they cannot be used for this.
 */
internal fun sheetContentInsets(sheetSafeArea: IosBarEdgeInsets): IosBarEdgeInsets =
    IosBarEdgeInsets(left = sheetSafeArea.left, right = sheetSafeArea.right)
