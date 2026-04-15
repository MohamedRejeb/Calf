@file:OptIn(BetaInteropApi::class)

package com.mohamedrejeb.calf.ui.toolbar

import androidx.compose.ui.unit.LayoutDirection
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonItem
import com.mohamedrejeb.calf.ui.navigation.BarButtonActionHandler
import com.mohamedrejeb.calf.ui.navigation.toUIBarButtonItem
import com.mohamedrejeb.calf.ui.utils.applyLayoutDirection
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIToolbar
import platform.UIKit.UIView
import platform.UIKit.NSLayoutConstraint

/**
 * A helper class that manages a native iOS UIToolbar, handling item setup and layout.
 *
 * This follows the same pattern as [TopBarManager] for UINavigationBar.
 */
@OptIn(ExperimentalForeignApi::class)
internal class ToolbarManager {

    private val toolbar = UIToolbar().apply {
        translatesAutoresizingMaskIntoConstraints = false
    }

    private var currentItems: List<UIKitUIBarButtonItem> = emptyList()

    // Hold strong references to action handlers to prevent GC
    private var actionHandlers: List<BarButtonActionHandler> = emptyList()

    private var isAttached = false

    fun attachTo(parentView: UIView) {
        if (isAttached) return

        parentView.addSubview(toolbar)

        NSLayoutConstraint.activateConstraints(
            listOf(
                toolbar.leadingAnchor.constraintEqualToAnchor(parentView.safeAreaLayoutGuide.leadingAnchor),
                toolbar.trailingAnchor.constraintEqualToAnchor(parentView.safeAreaLayoutGuide.trailingAnchor),
                toolbar.bottomAnchor.constraintEqualToAnchor(parentView.safeAreaLayoutGuide.bottomAnchor),
            )
        )

        isAttached = true
    }

    fun detach() {
        if (!isAttached) return
        toolbar.removeFromSuperview()
        isAttached = false
    }

    fun applyLayoutDirection(layoutDirection: LayoutDirection) {
        toolbar.applyLayoutDirection(layoutDirection)
    }

    fun update(items: List<UIKitUIBarButtonItem>, density: Float) {
        if (items == currentItems) return

        // Create action handlers for each item (onClick is ignored when a menu is present)
        actionHandlers = items.map { item ->
            BarButtonActionHandler(if (item.hasMenu) ({}) else item.onClick)
        }

        // Convert to UIBarButtonItems — no automatic spacing added
        val barItems = items.mapIndexed { index, item ->
            item.toUIBarButtonItem(actionHandlers[index], density)
        }

        toolbar.setItems(barItems, animated = currentItems.isNotEmpty())
        currentItems = items
    }
}
