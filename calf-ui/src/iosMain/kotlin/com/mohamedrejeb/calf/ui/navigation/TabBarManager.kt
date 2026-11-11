package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.unit.LayoutDirection
import com.mohamedrejeb.calf.core.InternalCalfApi
import com.mohamedrejeb.calf.ui.utils.toUIColor
import com.mohamedrejeb.calf.ui.utils.toUIFont
import com.mohamedrejeb.calf.ui.utils.toUIImage
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.NSFontAttributeName
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UITabBar
import platform.UIKit.UITabBarAppearance
import platform.UIKit.UITabBarDelegateProtocol
import platform.UIKit.UITabBarItem
import platform.UIKit.UITabBarItemAppearance
import platform.UIKit.UITabBarItemStateAppearance
import platform.darwin.NSObject

/**
 * A helper class that manages a native iOS UITabBar, handling item setup and selection.
 *
 * @param tabBar The native UITabBar instance to manage.
 * @param onItemSelected Callback invoked when a tab bar item is selected, with the item index.
 */
@OptIn(ExperimentalForeignApi::class)
internal class TabBarManager(
    private val tabBar: UITabBar,
    onItemSelected: (Int) -> Unit,
) {
    private val uiTabBarDelegate = UITabBarDelegate(
        onItemSelected = onItemSelected,
    )

    // Capture the original tint colors and appearances so we can restore iOS defaults when the
    // user switches back to Color.Unspecified.
    private val defaultTintColor: UIColor = tabBar.tintColor
    private val defaultUnselectedItemTintColor: UIColor? = tabBar.unselectedItemTintColor
    private val defaultStandardAppearance: UITabBarAppearance = tabBar.standardAppearance
    private val defaultScrollEdgeAppearance: UITabBarAppearance? = tabBar.scrollEdgeAppearance

    private var currentItems = emptyList<UIKitUITabBarItem>()
    private var currentConfiguration = UIKitTabBarConfiguration()

    init {
        tabBar.delegate = uiTabBarDelegate
    }

    /**
     * Shows [items] and selects [selectedIndex].
     *
     * When only the content of the items changed (titles, images) and their count did not, the
     * existing native items are updated in place, so the tab bar does not rebuild and animate
     * its items, e.g. when an asynchronously loaded image arrives.
     */
    fun setItems(
        items: List<UIKitUITabBarItem>,
        selectedIndex: Int,
        density: Float,
        layoutDirection: LayoutDirection = LayoutDirection.Ltr,
    ) {
        if (items.isEmpty()) return

        val existingItems = tabBar.items?.filterIsInstance<UITabBarItem>().orEmpty()
        val uiKitItems =
            when {
                items == currentItems && existingItems.size == items.size -> existingItems
                existingItems.size == items.size -> {
                    items.forEachIndexed { index, item ->
                        if (item != currentItems.getOrNull(index)) {
                            existingItems[index].update(item, density, layoutDirection)
                        }
                    }
                    existingItems
                }
                else -> {
                    val newItems = items.mapIndexed { index, item ->
                        item.toUITabBarItem(index.toLong(), density, layoutDirection)
                    }
                    tabBar.setItems(newItems)
                    newItems
                }
            }
        currentItems = items
        val safeSelectedIndex = selectedIndex.coerceIn(uiKitItems.indices)
        tabBar.selectedItem = uiKitItems[safeSelectedIndex]
    }

    fun updateConfiguration(
        configuration: UIKitTabBarConfiguration,
    ) {
        if (configuration == currentConfiguration) return
        tabBar.applyTabBarConfiguration(configuration, defaultTintColor, defaultUnselectedItemTintColor)

        if (configuration.usesCustomAppearance) {
            val appearance = configuration.toUITabBarAppearance()
            tabBar.standardAppearance = appearance
            tabBar.scrollEdgeAppearance = appearance
        } else {
            tabBar.standardAppearance = defaultStandardAppearance
            tabBar.scrollEdgeAppearance = defaultScrollEdgeAppearance
        }

        currentConfiguration = configuration
    }
}

/**
 * Applies [configuration] to a native tab bar. Unspecified colors restore the
 * captured defaults so switching back to `Color.Unspecified` returns to the
 * system look. Shared with `calf-navigation`'s `UITabBarController` host.
 *
 * Liquid Glass (iOS 26+) ignores `unselectedItemTintColor`.
 */
@InternalCalfApi
public fun UITabBar.applyTabBarConfiguration(
    configuration: UIKitTabBarConfiguration,
    defaultTintColor: UIColor,
    defaultUnselectedItemTintColor: UIColor?,
) {
    translucent = configuration.isTranslucent
    tintColor =
        if (configuration.selectedItemColor.isSpecified) configuration.selectedItemColor.toUIColor()
        else defaultTintColor
    unselectedItemTintColor =
        if (configuration.unselectedItemColor.isSpecified) configuration.unselectedItemColor.toUIColor()
        else defaultUnselectedItemTintColor
}

@OptIn(ExperimentalForeignApi::class)
private fun UIKitTabBarConfiguration.toUITabBarAppearance(): UITabBarAppearance {
    val appearance = UITabBarAppearance()
    if (isTranslucent) appearance.configureWithDefaultBackground()
    else appearance.configureWithOpaqueBackground()
    if (backgroundColor.isSpecified) appearance.backgroundColor = backgroundColor.toUIColor()

    val font = titleFont?.toUIFont()
    listOf(
        appearance.stackedLayoutAppearance,
        appearance.inlineLayoutAppearance,
        appearance.compactInlineLayoutAppearance,
    ).forEach { itemAppearance: UITabBarItemAppearance ->
        itemAppearance.normal.apply(
            iconColor = unselectedIconColor,
            titleColor = unselectedTitleColor,
            font = font,
        )
        itemAppearance.selected.apply(
            iconColor = selectedIconColor,
            titleColor = selectedTitleColor,
            font = font,
        )
    }
    return appearance
}

@OptIn(ExperimentalForeignApi::class)
private fun UITabBarItemStateAppearance.apply(iconColor: Color, titleColor: Color, font: UIFont?) {
    if (iconColor.isSpecified) this.iconColor = iconColor.toUIColor()

    val attributes = titleTextAttributes.toMutableMap()
    if (titleColor.isSpecified) attributes[NSForegroundColorAttributeName] = titleColor.toUIColor()
    if (font != null) attributes[NSFontAttributeName] = font
    titleTextAttributes = attributes
}

/**
 * Converts a Calf tab bar item to a native one; [tag] identifies the tab index and
 * [layoutDirection] is the direction painter images are drawn in.
 */
@InternalCalfApi
public fun UIKitUITabBarItem.toUITabBarItem(
    tag: Long,
    density: Float,
    layoutDirection: LayoutDirection = LayoutDirection.Ltr,
): UITabBarItem {
    val item = UITabBarItem(
        title = title,
        image = image?.toUIImage(density, layoutDirection),
        tag = tag,
    )
    item.selectedImage = selectedImage?.toUIImage(density, layoutDirection)
    return item
}

@OptIn(ExperimentalForeignApi::class)
private fun UITabBarItem.update(
    item: UIKitUITabBarItem,
    density: Float,
    layoutDirection: LayoutDirection,
) {
    title = item.title
    image = item.image?.toUIImage(density, layoutDirection)
    selectedImage = item.selectedImage?.toUIImage(density, layoutDirection)
}

internal class UITabBarDelegate(
    private val onItemSelected: (Int) -> Unit,
) : NSObject(), UITabBarDelegateProtocol {
    override fun tabBar(tabBar: UITabBar, didSelectItem: UITabBarItem) {
        tabBar.selectedItem = didSelectItem
        onItemSelected(didSelectItem.tag.toInt())
    }
}
