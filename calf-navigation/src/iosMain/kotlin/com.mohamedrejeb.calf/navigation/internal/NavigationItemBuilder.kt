@file:OptIn(ExperimentalForeignApi::class)

package com.mohamedrejeb.calf.navigation.internal

import com.mohamedrejeb.calf.ui.navigation.BarItemsUpdate
import com.mohamedrejeb.calf.ui.navigation.UIBarButtonItemsBinding
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonItem
import com.mohamedrejeb.calf.ui.navigation.barItemsUpdate
import com.mohamedrejeb.calf.ui.navigation.applyTitleAppearance
import com.mohamedrejeb.calf.ui.navigation.toUIBarButtonItems
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIBarButtonItem
import platform.UIKit.UIBarButtonItemStyle
import platform.UIKit.UINavigationItem
import platform.UIKit.UINavigationItemLargeTitleDisplayMode

/**
 * Keeps the bar-button action handlers of one entry alive. UIKit holds
 * bar-button targets weakly, so the hosting controller must retain this for
 * as long as its `navigationItem` shows the items.
 */
internal class NavBarItemBindings(
    val leading: UIBarButtonItemsBinding,
    val trailing: UIBarButtonItemsBinding,
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
    item.applyLargeTitleMode(metadata.largeTitle)
    val leading = metadata.iosLeadingItems.toUIBarButtonItems(density)
    val trailing = metadata.iosTrailingItems.toUIBarButtonItems(density)
    item.leftBarButtonItems = leading.items
    item.rightBarButtonItems = trailing.items
    item.applyBackTitle(metadata.iosBackTitle)
    item.applyTitleAppearance(metadata.iosConfiguration)
    return NavBarItemBindings(leading, trailing)
}

/**
 * Brings [item] from [previous] to [next] after the entry was composed again, touching only what
 * changed: a title that is the same is not re-set, and items whose look is the same keep their
 * native objects and are only pointed at the new callbacks ([barItemsUpdate]), so nothing in the
 * bar flickers on an ordinary recomposition.
 */
internal fun updateEntryMetadataOnItem(
    item: UINavigationItem,
    previous: AdaptiveNavEntryMetadata,
    next: AdaptiveNavEntryMetadata,
    bindings: NavBarItemBindings,
    density: Float,
): NavBarItemBindings {
    if (next.iosTitle != previous.iosTitle) item.title = next.iosTitle
    if (next.largeTitle != previous.largeTitle) item.applyLargeTitleMode(next.largeTitle)
    val leading = bindings.leading.updatedTo(previous.iosLeadingItems, next.iosLeadingItems, density) {
        item.leftBarButtonItems = it
    }
    val trailing = bindings.trailing.updatedTo(previous.iosTrailingItems, next.iosTrailingItems, density) {
        item.rightBarButtonItems = it
    }
    if (next.iosBackTitle != previous.iosBackTitle) item.applyBackTitle(next.iosBackTitle)
    if (next.iosConfiguration != previous.iosConfiguration) item.applyTitleAppearance(next.iosConfiguration)
    return NavBarItemBindings(leading, trailing)
}

private fun UIBarButtonItemsBinding.updatedTo(
    old: List<UIKitUIBarButtonItem>,
    new: List<UIKitUIBarButtonItem>,
    density: Float,
    install: (List<UIBarButtonItem>) -> Unit,
): UIBarButtonItemsBinding =
    when (barItemsUpdate(old, new)) {
        BarItemsUpdate.None -> this
        BarItemsUpdate.Rebind -> also { it.rebind(new, density) }
        BarItemsUpdate.Replace -> new.toUIBarButtonItems(density).also { install(it.items) }
    }

/**
 * The bar keeps `prefersLargeTitles = true`; the per-entry display mode is what lets UIKit
 * animate the shrink between a large-title screen and a regular one.
 */
private fun UINavigationItem.applyLargeTitleMode(largeTitle: Boolean) {
    largeTitleDisplayMode = if (largeTitle) {
        UINavigationItemLargeTitleDisplayMode.UINavigationItemLargeTitleDisplayModeAlways
    } else {
        UINavigationItemLargeTitleDisplayMode.UINavigationItemLargeTitleDisplayModeNever
    }
}

/** The label of the back button on the *next* pushed screen; `null` restores UIKit's own. */
private fun UINavigationItem.applyBackTitle(backTitle: String?) {
    backBarButtonItem = backTitle?.let {
        UIBarButtonItem(
            title = it,
            style = UIBarButtonItemStyle.UIBarButtonItemStylePlain,
            target = null,
            action = null,
        )
    }
}
