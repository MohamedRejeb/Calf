@file:OptIn(BetaInteropApi::class)

package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.ui.graphics.isSpecified
import com.mohamedrejeb.calf.core.InternalCalfApi
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

    // Bindings retain the action handlers: UIKit holds bar-button targets weakly.
    private var leadingBinding: UIBarButtonItemsBinding = UIBarButtonItemsBinding.Empty
    private var trailingBinding: UIBarButtonItemsBinding = UIBarButtonItemsBinding.Empty

    init {
        navBar.delegate = delegate
    }

    fun update(
        title: String,
        leadingItems: List<UIKitUIBarButtonItem>,
        trailingItems: List<UIKitUIBarButtonItem>,
        configuration: UIKitNavigationBarConfiguration,
        isLiquidGlassEnabled: Boolean,
        density: Float,
    ) {
        val needsItemUpdate = title != currentTitle ||
            leadingItems != currentLeadingItems ||
            trailingItems != currentTrailingItems

        if (needsItemUpdate) {
            val navItem = UINavigationItem(title = title)

            leadingBinding = leadingItems.toUIBarButtonItems(density)
            trailingBinding = trailingItems.toUIBarButtonItems(density)
            navItem.leftBarButtonItems = leadingBinding.items
            navItem.rightBarButtonItems = trailingBinding.items

            navBar.setItems(listOf(navItem), animated = false)

            currentTitle = title
            currentLeadingItems = leadingItems
            currentTrailingItems = trailingItems
        }

        if (configuration != currentConfiguration) {
            navBar.prefersLargeTitles = configuration.prefersLargeTitles
            navBar.translucent = configuration.isTranslucent

            val appearance = configuration.toNavigationBarAppearance(isLiquidGlassEnabled)
            navBar.standardAppearance = appearance
            navBar.scrollEdgeAppearance = appearance
            navBar.compactAppearance = appearance

            currentConfiguration = configuration
        }
    }
}

/**
 * Builds a [UINavigationBarAppearance] from [UIKitNavigationBarConfiguration].
 * Only explicitly specified colors are applied; everything else keeps the
 * system default. Shared with `calf-navigation`, which applies the same
 * configuration to a `UINavigationController`'s bar.
 */
@InternalCalfApi
public fun UIKitNavigationBarConfiguration.toNavigationBarAppearance(
    isLiquidGlassEnabled: Boolean,
): UINavigationBarAppearance = UINavigationBarAppearance().apply {
    if (!isLiquidGlassEnabled) {
        configureWithDefaultBackground()
    }
    if (titleColor.isSpecified) {
        // "NSColor" is the raw key for NSForegroundColorAttributeName
        val textAttributes = mapOf<Any?, Any?>("NSColor" to titleColor.toUIColor())
        titleTextAttributes = textAttributes
        largeTitleTextAttributes = textAttributes
    }
}

/**
 * Native bar button items plus the action handlers they point at. UIKit holds
 * bar-button targets weakly, so the owner must keep this binding alive for as
 * long as the items are installed or clicks silently become no-ops.
 */
@InternalCalfApi
public class UIBarButtonItemsBinding internal constructor(
    public val items: List<UIBarButtonItem>,
    @Suppress("unused") private val handlers: List<BarButtonActionHandler>,
) {
    public companion object {
        public val Empty: UIBarButtonItemsBinding = UIBarButtonItemsBinding(emptyList(), emptyList())
    }
}

/**
 * Converts Calf bar button items to native ones, wiring each `onClick` to a
 * retained target/action handler. Items with a menu get no action: the menu
 * owns the interaction.
 */
@InternalCalfApi
public fun List<UIKitUIBarButtonItem>.toUIBarButtonItems(density: Float): UIBarButtonItemsBinding {
    val handlers = map { item -> BarButtonActionHandler(if (item.hasMenu) ({}) else item.onClick) }
    val items = mapIndexed { index, item -> item.toUIBarButtonItem(handlers[index], density) }
    return UIBarButtonItemsBinding(items, handlers)
}

/**
 * Action handler for UIBarButtonItem that invokes a callback when the item is clicked.
 */
@InternalCalfApi
@OptIn(ExperimentalForeignApi::class)
public class BarButtonActionHandler(
    private val onClick: () -> Unit,
) : NSObject() {
    @Suppress("unused")
    @ObjCAction
    fun handleAction() {
        onClick()
    }
}

@OptIn(ExperimentalForeignApi::class)
@InternalCalfApi
public fun UIKitUIBarButtonItem.toUIBarButtonItem(
    actionHandler: BarButtonActionHandler,
    density: Float,
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
            if (hasMenu) it.menu = buildUIMenu(menuItems, menuSections, density)
        }
    }

    // If image is specified, use image constructor
    val uiImage = image?.toUIImage(density)
    if (uiImage != null) {
        return UIBarButtonItem(
            image = uiImage,
            style = UIBarButtonItemStyle.UIBarButtonItemStylePlain,
            target = target,
            action = action,
        ).also {
            it.enabled = enabled
            it.selected = selected
            if (hasMenu) it.menu = buildUIMenu(menuItems, menuSections, density)
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
        if (hasMenu) it.menu = buildUIMenu(menuItems, menuSections, density)
    }
}

private fun buildUIMenu(
    items: List<AdaptiveDropDownItem>,
    sections: List<AdaptiveDropDownSection>,
    density: Float,
): UIMenu {
    val allActions: List<UIMenuElement> = items.map { it.toUIAction(density) }
    val sectionMenus: List<UIMenuElement> = sections.map { section ->
        UIMenu.menuWithTitle(
            title = section.title,
            image = null,
            identifier = null,
            options = UIMenuOptionsDisplayInline,
            children = section.items.map { it.toUIAction(density) },
        )
    }
    return UIMenu.menuWithTitle(
        title = "",
        children = allActions + sectionMenus,
    )
}

private fun AdaptiveDropDownItem.toUIAction(density: Float): UIAction {
    val image = iosIcon?.toUIImage(density)
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

@InternalCalfApi
public fun UIKitUIBarButtonSystemItem.toUIBarButtonSystemItem(): UIBarButtonSystemItem =
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
