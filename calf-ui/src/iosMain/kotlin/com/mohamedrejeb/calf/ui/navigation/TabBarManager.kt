package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.ui.graphics.isSpecified
import com.mohamedrejeb.calf.core.InternalCalfApi
import com.mohamedrejeb.calf.ui.utils.toUIColor
import com.mohamedrejeb.calf.ui.utils.toUIImage
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIColor
import platform.UIKit.UITabBar
import platform.UIKit.UITabBarDelegateProtocol
import platform.UIKit.UITabBarItem
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

    // Capture the original tint colors so we can restore iOS defaults when the user
    // switches back to Color.Unspecified.
    private val defaultTintColor: UIColor = tabBar.tintColor
    private val defaultUnselectedItemTintColor: UIColor? = tabBar.unselectedItemTintColor

    private var currentItems = emptyList<UIKitUITabBarItem>()
    private var currentConfiguration = UIKitTabBarConfiguration()

    init {
        tabBar.delegate = uiTabBarDelegate
    }

    fun setItems(
        items: List<UIKitUITabBarItem>,
        selectedIndex: Int,
        density: Float,
    ) {
        if (items.isEmpty()) return

        if (items != currentItems) {
            val uiKitItems = items.mapIndexed { index, item ->
                item.toUITabBarItem(index.toLong(), density)
            }
            tabBar.setItems(uiKitItems)
            currentItems = items
            val safeSelectedIndex = selectedIndex.coerceIn(uiKitItems.indices)
            tabBar.selectedItem = uiKitItems[safeSelectedIndex]
        } else {
            val uiKitItems = tabBar.items?.filterIsInstance<UITabBarItem>() ?: return
            val safeSelectedIndex = selectedIndex.coerceIn(uiKitItems.indices)
            tabBar.selectedItem = uiKitItems[safeSelectedIndex]
        }
    }

    fun updateConfiguration(
        configuration: UIKitTabBarConfiguration,
    ) {
        if (configuration == currentConfiguration) return
        tabBar.applyTabBarConfiguration(configuration, defaultTintColor, defaultUnselectedItemTintColor)
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

/** Converts a Calf tab bar item to a native one; [tag] identifies the tab index. */
@InternalCalfApi
public fun UIKitUITabBarItem.toUITabBarItem(tag: Long, density: Float): UITabBarItem {
    val item = UITabBarItem(
        title = title,
        image = image?.toUIImage(density),
        tag = tag,
    )
    selectedImage?.toUIImage(density)?.let { item.selectedImage = it }
    return item
}

internal class UITabBarDelegate(
    private val onItemSelected: (Int) -> Unit,
) : NSObject(), UITabBarDelegateProtocol {
    override fun tabBar(tabBar: UITabBar, didSelectItem: UITabBarItem) {
        tabBar.selectedItem = didSelectItem
        onItemSelected(didSelectItem.tag.toInt())
    }
}
