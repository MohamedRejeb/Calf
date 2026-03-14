package com.mohamedrejeb.calf.ui.navigation

import com.mohamedrejeb.calf.ui.utils.toUIImage
import kotlinx.cinterop.ExperimentalForeignApi
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

    private var currentItems = emptyList<UIKitUITabBarItem>()

    init {
        tabBar.delegate = uiTabBarDelegate
    }

    fun setItems(
        items: List<UIKitUITabBarItem>,
        selectedIndex: Int,
    ) {
        if (items.isEmpty()) return

        if (items != currentItems) {
            val uiKitItems = items.mapIndexed { index, item ->
                item.toUITabBarItem(index.toLong())
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
}

private fun UIKitUITabBarItem.toUITabBarItem(tag: Long): UITabBarItem {
    val item = UITabBarItem(
        title = title,
        image = image?.toUIImage(),
        tag = tag,
    )
    selectedImage?.toUIImage()?.let { item.selectedImage = it }
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
