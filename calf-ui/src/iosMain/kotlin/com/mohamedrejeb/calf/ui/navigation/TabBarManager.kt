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

    private val configurationApplier = TabBarConfigurationApplier(tabBar)

    private var currentItems = emptyList<UIKitUITabBarItem>()
    private var currentRendering: ImageRendering? = null
    private var currentConfiguration = UIKitTabBarConfiguration()

    init {
        tabBar.delegate = uiTabBarDelegate
    }

    /**
     * Shows [items] and selects [selectedIndex].
     *
     * When only the content of the items changed (titles, images) and their count did not, the
     * existing native items are updated in place, so the tab bar does not rebuild and animate
     * its items, e.g. when an asynchronously loaded image arrives. A new [density] or
     * [layoutDirection] draws every image again: the ones the bar holds were made for the old one.
     */
    fun setItems(
        items: List<UIKitUITabBarItem>,
        selectedIndex: Int,
        density: Float,
        layoutDirection: LayoutDirection = LayoutDirection.Ltr,
    ) {
        if (items.isEmpty()) return

        val rendering = ImageRendering(density, layoutDirection)
        val imagesAreStale = rendering != currentRendering
        val existingItems = tabBar.items?.filterIsInstance<UITabBarItem>().orEmpty()
        val uiKitItems =
            when {
                existingItems.size != items.size -> {
                    val newItems = items.mapIndexed { index, item ->
                        item.toUITabBarItem(index.toLong(), density, layoutDirection)
                    }
                    tabBar.setItems(newItems)
                    newItems
                }
                items == currentItems && !imagesAreStale -> existingItems
                else -> {
                    items.forEachIndexed { index, item ->
                        if (imagesAreStale || item != currentItems.getOrNull(index)) {
                            existingItems[index].update(item, density, layoutDirection)
                        }
                    }
                    existingItems
                }
            }
        currentItems = items
        currentRendering = rendering
        val safeSelectedIndex = selectedIndex.coerceIn(uiKitItems.indices)
        tabBar.selectedItem = uiKitItems[safeSelectedIndex]
    }

    fun updateConfiguration(
        configuration: UIKitTabBarConfiguration,
    ) {
        if (configuration == currentConfiguration) return
        configurationApplier.apply(configuration)
        currentConfiguration = configuration
    }
}

/** What the item images were drawn with. */
private data class ImageRendering(val density: Float, val layoutDirection: LayoutDirection)

/**
 * Applies a [UIKitTabBarConfiguration] to one native tab bar. Shared by the standalone bar, the
 * bar host and `calf-navigation`'s `UITabBarController`, so the three style a tab bar alike.
 *
 * Create it before anything configures [tabBar]: the tint colors and appearances the bar has at
 * that moment are what a configuration going back to its defaults restores.
 */
@InternalCalfApi
public class TabBarConfigurationApplier(private val tabBar: UITabBar) {
    private val defaultTintColor: UIColor = tabBar.tintColor
    private val defaultUnselectedItemTintColor: UIColor? = tabBar.unselectedItemTintColor
    private val defaultStandardAppearance: UITabBarAppearance = tabBar.standardAppearance
    private val defaultScrollEdgeAppearance: UITabBarAppearance? = tabBar.scrollEdgeAppearance

    private var hasCustomAppearance = false

    public fun apply(configuration: UIKitTabBarConfiguration) {
        tabBar.translucent = configuration.isTranslucent
        tabBar.tintColor =
            if (configuration.selectedItemColor.isSpecified) configuration.selectedItemColor.toUIColor()
            else defaultTintColor
        // Liquid Glass (iOS 26+) ignores `unselectedItemTintColor`.
        tabBar.unselectedItemTintColor =
            if (configuration.unselectedItemColor.isSpecified) configuration.unselectedItemColor.toUIColor()
            else defaultUnselectedItemTintColor
        applyAppearance(configuration)
    }

    /**
     * The appearances are only replaced for a configuration that asks for one, and only put back
     * after Calf replaced them: a bar that never had a custom appearance keeps whatever the
     * system or the app gave it.
     */
    private fun applyAppearance(configuration: UIKitTabBarConfiguration) {
        if (configuration.usesCustomAppearance) {
            val appearance = configuration.toUITabBarAppearance()
            tabBar.standardAppearance = appearance
            tabBar.scrollEdgeAppearance = appearance
            hasCustomAppearance = true
        } else if (hasCustomAppearance) {
            tabBar.standardAppearance = defaultStandardAppearance
            tabBar.scrollEdgeAppearance = defaultScrollEdgeAppearance
            hasCustomAppearance = false
        }
    }
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
