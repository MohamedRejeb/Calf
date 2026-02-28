package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.ComposeUIViewController
import com.mohamedrejeb.calf.ui.sheet.BottomSheetControllerDelegate
import com.mohamedrejeb.calf.ui.utils.applyTheme
import com.mohamedrejeb.calf.ui.utils.isDark
import com.mohamedrejeb.calf.ui.utils.toUIColor
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.UIKit.UIAdaptivePresentationControllerDelegateProtocol
import platform.UIKit.UIImage
import platform.UIKit.UIModalPresentationPageSheet
import platform.UIKit.UIModalTransitionStyleCoverVertical
import platform.UIKit.UIPresentationController
import platform.UIKit.UISheetPresentationControllerDetent
import platform.UIKit.UITabBar
import platform.UIKit.UITabBarDelegateProtocol
import platform.UIKit.UITabBarItem
import platform.UIKit.UIViewController
import platform.UIKit.presentationController
import platform.UIKit.sheetPresentationController
import platform.darwin.NSObject
import platform.posix.index

/**
 * A helper class that is used to present UIKit bottom sheet with Compose content.
 *
 * @param onDismiss The callback that is called when the bottom sheet is dismissed.
 * @param content The Compose content of the bottom sheet.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalForeignApi::class)
internal class TabBarManager(
    private val tabBar: UITabBar,
    onItemChanged: (String) -> Unit,
) {
    /**
     * The presentation controller delegate that is used to detect when the bottom sheet is dismissed.
     */
    private val uiTabBarDelegate by lazy {
        UITabBarDelegate(
            onItemChanged = onItemChanged,
        )
    }

    private var currentItems = emptyList<UIKitUITabBarItem>()

    init {
        initTabBar()
    }

    private fun initTabBar() {
        tabBar.delegate = uiTabBarDelegate
        tabBar.translatesAutoresizingMaskIntoConstraints = false
    }

    fun setItems(
        items: List<UIKitUITabBarItem>,
        selectedIndex: Int,
    ) {
        if (items.isEmpty())
            return

        if (items != currentItems) {
            val uiKitItems = items.mapIndexed { index, item ->
                item.toUITabBarItem(index.toLong())
            }
            tabBar.setItems(uiKitItems)
            currentItems = items

            val safeSelectedIndex = selectedIndex.coerceIn(uiKitItems.indices)
            tabBar.selectedItem = uiKitItems[safeSelectedIndex]
        } else {
            val safeSelectedIndex = selectedIndex.coerceIn(items.indices)
            tabBar.selectedItem = items[safeSelectedIndex].toUITabBarItem(safeSelectedIndex.toLong())
        }
    }
}

private fun UIKitUITabBarItem.toUITabBarItem(tag: Long) =
    UITabBarItem(
        title = title,
        image = image?.let { UIImage.systemImageNamed(it) },
        tag = tag
    )

class UITabBarDelegate(
    val onItemChanged: (String) -> Unit,
) : NSObject(),  UITabBarDelegateProtocol {
    override fun tabBar(tabBar: UITabBar, didSelectItem: UITabBarItem) {
        onItemChanged(didSelectItem.title.toString())
        tabBar.selectedItem = didSelectItem
    }
}
