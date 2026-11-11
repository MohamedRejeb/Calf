package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import com.mohamedrejeb.calf.ui.uikit.UIKitFont

/**
 * Configuration for the iOS UITabBar appearance in [AdaptiveNavigationBar].
 *
 * All color properties default to [Color.Unspecified], which means the native iOS default
 * colors will be used. Only properties explicitly set by the caller are applied.
 *
 * [selectedItemColor] and [unselectedItemColor] are the simple way to color the items and map
 * to the tab bar's tint colors. The per-state colors ([selectedIconColor], [selectedTitleColor],
 * [unselectedIconColor], [unselectedTitleColor]), [titleFont] and [backgroundColor] are applied
 * through `UITabBarAppearance` and take precedence over the tint colors for the state they
 * cover. Setting any of them switches the tab bar to a custom appearance.
 *
 * @param selectedItemColor The tint color for selected tab bar items (icon + label).
 *   Maps to [UITabBar.tintColor] on iOS.
 * @param unselectedItemColor The color for unselected tab bar items (icon + label).
 *   Maps to [UITabBar.unselectedItemTintColor] on iOS.
 *   Note: Liquid Glass (iOS 26+) ignores this property; use [unselectedIconColor] and
 *   [unselectedTitleColor] instead.
 * @param isTranslucent Whether the tab bar is translucent.
 * @param backgroundColor The background color of the tab bar. Applied through
 *   `UITabBarAppearance.backgroundColor`, so it is drawn over the translucent material when
 *   [isTranslucent] is `true`.
 * @param selectedIconColor The icon color of the selected item.
 * @param selectedTitleColor The title color of the selected item.
 * @param unselectedIconColor The icon color of unselected items.
 * @param unselectedTitleColor The title color of unselected items.
 * @param titleFont The font of the item titles, for every state. `null` keeps the system font.
 */
@Immutable
data class UIKitTabBarConfiguration(
    val selectedItemColor: Color = Color.Unspecified,
    val unselectedItemColor: Color = Color.Unspecified,
    val isTranslucent: Boolean = true,
    val backgroundColor: Color = Color.Unspecified,
    val selectedIconColor: Color = Color.Unspecified,
    val selectedTitleColor: Color = Color.Unspecified,
    val unselectedIconColor: Color = Color.Unspecified,
    val unselectedTitleColor: Color = Color.Unspecified,
    val titleFont: UIKitFont? = null,
) {
    /** `true` when any property that needs a `UITabBarAppearance` is set. */
    internal val usesCustomAppearance: Boolean
        get() = backgroundColor.isSpecified ||
            selectedIconColor.isSpecified ||
            selectedTitleColor.isSpecified ||
            unselectedIconColor.isSpecified ||
            unselectedTitleColor.isSpecified ||
            titleFont != null
}
