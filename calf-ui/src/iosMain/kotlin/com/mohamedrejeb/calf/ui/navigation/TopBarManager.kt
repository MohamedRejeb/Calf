@file:OptIn(BetaInteropApi::class)

package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.ui.graphics.isSpecified
import com.mohamedrejeb.calf.ui.dropdown.AdaptiveDropDownItem
import com.mohamedrejeb.calf.ui.dropdown.AdaptiveDropDownSection
import com.mohamedrejeb.calf.ui.utils.toUIColor
import com.mohamedrejeb.calf.ui.utils.toUIImage
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.UIKit.UIAction
import platform.UIKit.UIBarButtonItem
import platform.UIKit.UIBarButtonItemStyle
import platform.UIKit.UIBarButtonSystemItem
import platform.UIKit.UIMenu
import platform.UIKit.UIMenuElement
import platform.UIKit.UIMenuElementAttributesDestructive
import platform.UIKit.UIMenuElementAttributesDisabled
import platform.UIKit.UIMenuOptionsDisplayInline
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

            // Create leading (left) bar button items (onClick is ignored when a menu is present)
            leadingActionHandlers = leadingItems.map { item ->
                BarButtonActionHandler(if (item.hasMenu) ({}) else item.onClick)
            }
            navItem.leftBarButtonItems = leadingItems.mapIndexed { index, item ->
                item.toUIBarButtonItem(leadingActionHandlers[index])
            }

            // Create trailing (right) bar button items (onClick is ignored when a menu is present)
            trailingActionHandlers = trailingItems.map { item ->
                BarButtonActionHandler(if (item.hasMenu) ({}) else item.onClick)
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

            val appearance = UINavigationBarAppearance().apply {
                if (!isLiquidGlassEnabled) {
                    configureWithDefaultBackground()
                }

                if (configuration.titleColor.isSpecified) {
                    val uiColor = configuration.titleColor.toUIColor()
                    // "NSColor" is the raw key for NSForegroundColorAttributeName
                    val textAttributes = mapOf<Any?, Any?>(
                        "NSColor" to uiColor,
                    )
                    titleTextAttributes = textAttributes
                    largeTitleTextAttributes = textAttributes
                }
            }
            navBar.standardAppearance = appearance
            navBar.scrollEdgeAppearance = appearance
            navBar.compactAppearance = appearance

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
internal fun UIKitUIBarButtonItem.toUIBarButtonItem(
    actionHandler: BarButtonActionHandler,
): UIBarButtonItem {
    val selector = sel_registerName("handleAction")

    // Handle spacing items — no target/action needed
    if (systemItem == UIKitUIBarButtonSystemItem.FlexibleSpace) {
        return UIBarButtonItem(
            barButtonSystemItem = UIBarButtonSystemItem.UIBarButtonSystemItemFlexibleSpace,
            target = null,
            action = null,
        )
    }
    if (systemItem == UIKitUIBarButtonSystemItem.FixedSpace) {
        return UIBarButtonItem(
            barButtonSystemItem = UIBarButtonSystemItem.UIBarButtonSystemItemFixedSpace,
            target = null,
            action = null,
        ).also {
            it.width = width ?: 16.0
        }
    }

    // When a menu is provided, ignore onClick — the menu handles interaction
    val target = if (hasMenu) null else actionHandler
    val action = if (hasMenu) null else selector

    // If systemItem is specified, use system item constructor
    if (systemItem != null) {
        return UIBarButtonItem(
            barButtonSystemItem = systemItem.toUIBarButtonSystemItem(),
            target = target,
            action = action,
        ).also {
            it.enabled = enabled
            it.selected = selected
            if (hasMenu) it.menu = buildUIMenu(menuItems, menuSections)
        }
    }

    // If image is specified, use image constructor
    val uiImage = image?.toUIImage()
    if (uiImage != null) {
        return UIBarButtonItem(
            image = uiImage,
            style = UIBarButtonItemStyle.UIBarButtonItemStylePlain,
            target = target,
            action = action,
        ).also {
            it.enabled = enabled
            it.selected = selected
            if (hasMenu) it.menu = buildUIMenu(menuItems, menuSections)
        }
    }

    // Otherwise, use title constructor
    return UIBarButtonItem(
        title = title ?: "",
        style = UIBarButtonItemStyle.UIBarButtonItemStylePlain,
        target = target,
        action = action,
    ).also {
        it.enabled = enabled
        it.selected = selected
        if (hasMenu) it.menu = buildUIMenu(menuItems, menuSections)
    }
}

private fun buildUIMenu(
    items: List<AdaptiveDropDownItem>,
    sections: List<AdaptiveDropDownSection>,
): UIMenu {
    val allActions: List<UIMenuElement> = items.map { it.toUIAction() }
    val sectionMenus: List<UIMenuElement> = sections.map { section ->
        UIMenu.menuWithTitle(
            title = section.title,
            image = null,
            identifier = null,
            options = UIMenuOptionsDisplayInline,
            children = section.items.map { it.toUIAction() },
        )
    }
    return UIMenu.menuWithTitle(
        title = "",
        children = allActions + sectionMenus,
    )
}

private fun AdaptiveDropDownItem.toUIAction(): UIAction {
    val image = iosIcon?.toUIImage()
    val action = UIAction.actionWithTitle(
        title = title,
        image = image,
        identifier = null,
    ) { _ ->
        onClick()
    }
    var attributes: ULong = 0u
    if (isDestructive) attributes = attributes or UIMenuElementAttributesDestructive
    if (isDisabled) attributes = attributes or UIMenuElementAttributesDisabled
    action.attributes = attributes
    return action
}

internal fun UIKitUIBarButtonSystemItem.toUIBarButtonSystemItem(): UIBarButtonSystemItem =
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
        UIKitUIBarButtonSystemItem.FlexibleSpace -> UIBarButtonSystemItem.UIBarButtonSystemItemFlexibleSpace
        UIKitUIBarButtonSystemItem.FixedSpace -> UIBarButtonSystemItem.UIBarButtonSystemItemFixedSpace
    }

/**
 * Delegate for UINavigationBar that positions the bar at the top.
 */
internal class NavigationBarDelegate : NSObject(), UINavigationBarDelegateProtocol {
    override fun positionForBar(bar: UIBarPositioningProtocol): Long {
        return UIBarPositionTopAttached
    }
}
