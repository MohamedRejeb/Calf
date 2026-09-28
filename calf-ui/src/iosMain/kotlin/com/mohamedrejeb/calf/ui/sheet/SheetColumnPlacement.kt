@file:OptIn(ExperimentalForeignApi::class)

package com.mohamedrejeb.calf.ui.sheet

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.Foundation.NSNumber
import platform.Foundation.setValue
import platform.UIKit.UISheetPresentationController
import platform.UIKit.UIUserInterfaceLayoutDirection
import platform.UIKit.UIViewController
import platform.objc.sel_registerName

/**
 * Keeps the sheet on the side of the column that presents it (iOS 27+; a no-op before).
 *
 * UIKit places a sheet by the window: across both columns of a split view, and on a half-open
 * iPhone Duo in the leading pane, over the list, even when the detail column presented it. The
 * presenting controller's safe area is its column: a split view's secondary column spans the
 * window and gets the primary column's width as its leading inset.
 *
 * `preferredPlacement` is newer than the SDK Kotlin/Native's platform libraries are built from,
 * so it is set by key after checking that the running system has it.
 */
internal fun UISheetPresentationController.placeInColumnOf(viewController: UIViewController) {
    if (!respondsToSelector(sel_registerName("setPreferredPlacement:"))) return
    val view = viewController.view
    val windowWidth = view.window?.bounds?.useContents { size.width } ?: return
    val column = view.convertRect(view.safeAreaLayoutGuide.layoutFrame, toView = null)
    val placement = column.useContents {
        sheetPlacement(
            columnLeft = origin.x,
            columnRight = origin.x + size.width,
            windowWidth = windowWidth,
            isRightToLeft = view.effectiveUserInterfaceLayoutDirection ==
                UIUserInterfaceLayoutDirection.UIUserInterfaceLayoutDirectionRightToLeft,
        )
    }
    setValue(NSNumber(longLong = placement.rawValue), forKey = "preferredPlacement")
}

/** `UISheetPresentationControllerPlacement` raw values, from the iOS 27 SDK headers. */
private val SheetPlacement.rawValue: Long
    get() = when (this) {
        SheetPlacement.Automatic -> 0L
        SheetPlacement.Leading -> 1L
        SheetPlacement.Trailing -> 3L
    }
