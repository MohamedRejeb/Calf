@file:OptIn(BetaInteropApi::class)

package com.mohamedrejeb.calf.ui.navigation

import com.mohamedrejeb.calf.ui.utils.toUIImage
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.UIKit.UIBarButtonItem
import platform.UIKit.UIBarButtonItemStyle
import platform.UIKit.UIBarButtonSystemItem
import platform.UIKit.UINavigationBar
import platform.UIKit.UINavigationBarAppearance
import platform.UIKit.UINavigationBarDelegateProtocol
import platform.UIKit.UINavigationItem
import platform.UIKit.UIBarPositionTopAttached
import platform.UIKit.UIBarPositioningProtocol
import platform.darwin.NSObject
import platform.objc.sel_registerName

/**
 * A helper class that manages a native iOS UINavigationBar, handling item setup and actions.
 *
 * @param navBar The native UINavigationBar instance to manage.
 */
@OptIn(ExperimentalForeignApi::class)
internal class TopBarManager(
    private val navBar: UINavigationBar,
) {
    private val delegate = NavigationBarDelegate()

    private var currentTitle: String = ""
    private var currentLeadingItems: List<UIKitUIBarButtonItem> = emptyList()
    private var currentTrailingItems: List<UIKitUIBarButtonItem> = emptyList()
    private var currentConfiguration: UIKitNavigationBarConfiguration = UIKitNavigationBarConfiguration()

    // Hold strong references to action handlers to prevent GC
    private var leadingActionHandlers: List<BarButtonActionHandler> = emptyList()
    private var trailingActionHandlers: List<BarButtonActionHandler> = emptyList()

    init {
        navBar.delegate = delegate
    }

    fun update(
        title: String,
        leadingItems: List<UIKitUIBarButtonItem>,
        trailingItems: List<UIKitUIBarButtonItem>,
        configuration: UIKitNavigationBarConfiguration,
        isLiquidGlassEnabled: Boolean,
    ) {
        val needsItemUpdate = title != currentTitle ||
            leadingItems != currentLeadingItems ||
            trailingItems != currentTrailingItems

        if (needsItemUpdate) {
            val navItem = UINavigationItem(title = title)

            // Create leading (left) bar button items
            leadingActionHandlers = leadingItems.map { item ->
                BarButtonActionHandler(item.onClick)
            }
            navItem.leftBarButtonItems = leadingItems.mapIndexed { index, item ->
                item.toUIBarButtonItem(leadingActionHandlers[index])
            }

            // Create trailing (right) bar button items
            trailingActionHandlers = trailingItems.map { item ->
                BarButtonActionHandler(item.onClick)
            }
            navItem.rightBarButtonItems = trailingItems.mapIndexed { index, item ->
                item.toUIBarButtonItem(trailingActionHandlers[index])
            }

            navBar.setItems(listOf(navItem), animated = false)

            currentTitle = title
            currentLeadingItems = leadingItems
            currentTrailingItems = trailingItems
        }

        if (configuration != currentConfiguration) {
            navBar.prefersLargeTitles = configuration.prefersLargeTitles
            navBar.translucent = configuration.isTranslucent

            if (!isLiquidGlassEnabled) {
                val appearance = UINavigationBarAppearance().apply {
                    configureWithDefaultBackground()
                }
                navBar.standardAppearance = appearance
                navBar.scrollEdgeAppearance = appearance
                navBar.compactAppearance = appearance
            }

            currentConfiguration = configuration
        }
    }
}

/**
 * Action handler for UIBarButtonItem that invokes a callback when the item is clicked.
 */
@OptIn(ExperimentalForeignApi::class)
internal class BarButtonActionHandler(
    private val onClick: () -> Unit,
) : NSObject() {
    @Suppress("unused")
    @ObjCAction
    fun handleAction() {
        onClick()
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun UIKitUIBarButtonItem.toUIBarButtonItem(
    actionHandler: BarButtonActionHandler,
): UIBarButtonItem {
    val selector = sel_registerName("handleAction")

    // If systemItem is specified, use system item constructor
    if (systemItem != null) {
        return UIBarButtonItem(
            barButtonSystemItem = systemItem.toUIBarButtonSystemItem(),
            target = actionHandler,
            action = selector,
        )
    }

    // If image is specified, use image constructor
    val uiImage = image?.toUIImage()
    if (uiImage != null) {
        return UIBarButtonItem(
            image = uiImage,
            style = UIBarButtonItemStyle.UIBarButtonItemStylePlain,
            target = actionHandler,
            action = selector,
        )
    }

    // Otherwise, use title constructor
    return UIBarButtonItem(
        title = title ?: "",
        style = UIBarButtonItemStyle.UIBarButtonItemStylePlain,
        target = actionHandler,
        action = selector,
    )
}

private fun UIKitUIBarButtonSystemItem.toUIBarButtonSystemItem(): UIBarButtonSystemItem =
    when (this) {
        UIKitUIBarButtonSystemItem.Done -> UIBarButtonSystemItem.UIBarButtonSystemItemDone
        UIKitUIBarButtonSystemItem.Cancel -> UIBarButtonSystemItem.UIBarButtonSystemItemCancel
        UIKitUIBarButtonSystemItem.Edit -> UIBarButtonSystemItem.UIBarButtonSystemItemEdit
        UIKitUIBarButtonSystemItem.Save -> UIBarButtonSystemItem.UIBarButtonSystemItemSave
        UIKitUIBarButtonSystemItem.Add -> UIBarButtonSystemItem.UIBarButtonSystemItemAdd
        UIKitUIBarButtonSystemItem.Compose -> UIBarButtonSystemItem.UIBarButtonSystemItemCompose
        UIKitUIBarButtonSystemItem.Reply -> UIBarButtonSystemItem.UIBarButtonSystemItemReply
        UIKitUIBarButtonSystemItem.Action -> UIBarButtonSystemItem.UIBarButtonSystemItemAction
        UIKitUIBarButtonSystemItem.Organize -> UIBarButtonSystemItem.UIBarButtonSystemItemOrganize
        UIKitUIBarButtonSystemItem.Trash -> UIBarButtonSystemItem.UIBarButtonSystemItemTrash
        UIKitUIBarButtonSystemItem.Search -> UIBarButtonSystemItem.UIBarButtonSystemItemSearch
        UIKitUIBarButtonSystemItem.Close -> UIBarButtonSystemItem.UIBarButtonSystemItemClose
    }

/**
 * Delegate for UINavigationBar that positions the bar at the top.
 */
internal class NavigationBarDelegate : NSObject(), UINavigationBarDelegateProtocol {
    override fun positionForBar(bar: UIBarPositioningProtocol): Long {
        return UIBarPositionTopAttached
    }
}
