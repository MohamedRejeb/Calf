@file:OptIn(ExperimentalForeignApi::class)

package com.mohamedrejeb.calf.navigation.internal

import androidx.compose.ui.graphics.isSpecified
import com.mohamedrejeb.calf.ui.navigation.UIBarButtonItemsBinding
import com.mohamedrejeb.calf.ui.navigation.UIKitNavigationBarConfiguration
import com.mohamedrejeb.calf.ui.navigation.toNavigationBarAppearance
import com.mohamedrejeb.calf.ui.navigation.toUIBarButtonItems
import com.mohamedrejeb.calf.ui.utils.isIOS26OrAbove
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIBarButtonItem
import platform.UIKit.UIBarButtonItemStyle
import platform.UIKit.UINavigationBarAppearance
import platform.UIKit.UINavigationItem
import platform.UIKit.UINavigationItemLargeTitleDisplayMode

/**
 * Keeps the bar-button action handlers of one entry alive. UIKit holds
 * bar-button targets weakly, so the hosting controller must retain this for
 * as long as its `navigationItem` shows the items.
 */
internal class NavBarItemBindings(
    @Suppress("unused") private val leading: UIBarButtonItemsBinding,
    @Suppress("unused") private val trailing: UIBarButtonItemsBinding,
)

/**
 * Populates [item] from the entry's metadata. Call it from the hosting
 * controller's `init`, before the controller is pushed: UIKit reads the item
 * during `setViewControllers(_:animated:)` to animate the title cross-fade.
 *
 * Bar items go through calf-ui's converters, so SF Symbols, Compose vectors,
 * bitmaps and dropdown menus all work exactly as in `AdaptiveTopBar`.
 */
internal fun applyEntryMetadataToItem(
    item: UINavigationItem,
    metadata: AdaptiveNavEntryMetadata,
    density: Float,
): NavBarItemBindings {
    item.title = metadata.iosTitle
    // The bar keeps `prefersLargeTitles = true`; the per-entry display mode is
    // what lets UIKit animate the shrink between a large-title screen and a
    // regular one.
    item.largeTitleDisplayMode = if (metadata.largeTitle) {
        UINavigationItemLargeTitleDisplayMode.UINavigationItemLargeTitleDisplayModeAlways
    } else {
        UINavigationItemLargeTitleDisplayMode.UINavigationItemLargeTitleDisplayModeNever
    }
    val leading = metadata.iosLeadingItems.toUIBarButtonItems(density)
    val trailing = metadata.iosTrailingItems.toUIBarButtonItems(density)
    item.leftBarButtonItems = leading.items
    item.rightBarButtonItems = trailing.items
    metadata.iosBackTitle?.let { backTitle ->
        item.backBarButtonItem = UIBarButtonItem(
            title = backTitle,
            style = UIBarButtonItemStyle.UIBarButtonItemStylePlain,
            target = null,
            action = null,
        )
    }
    item.applyAppearance(metadata.iosConfiguration)
    return NavBarItemBindings(leading, trailing)
}

/**
 * Per-item bar appearance, which UIKit animates together with the push
 * transition (unlike mutating the bar's own appearance from `viewWillAppear`,
 * which snaps). Only a specified title color needs one; otherwise the item
 * inherits the bar's system appearance, keeping the transparent scroll-edge
 * look large titles rely on.
 */
private fun UINavigationItem.applyAppearance(configuration: UIKitNavigationBarConfiguration) {
    if (!configuration.titleColor.isSpecified) return
    val standard = configuration.toNavigationBarAppearance(isLiquidGlassEnabled = isIOS26OrAbove())
    val scrollEdge = UINavigationBarAppearance().apply {
        configureWithTransparentBackground()
        titleTextAttributes = standard.titleTextAttributes
        largeTitleTextAttributes = standard.largeTitleTextAttributes
    }
    standardAppearance = standard
    compactAppearance = standard
    scrollEdgeAppearance = scrollEdge
}
