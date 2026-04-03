package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Configuration for the iOS UITabBar appearance in [AdaptiveNavigationBar].
 *
 * All color properties default to [Color.Unspecified], which means the native iOS default
 * colors will be used. Only properties explicitly set by the caller are applied.
 *
 * @param selectedItemColor The tint color for selected tab bar items (icon + label).
 *   Maps to [UITabBar.tintColor] on iOS.
 * @param unselectedItemColor The color for unselected tab bar items (icon + label).
 *   Maps to [UITabBar.unselectedItemTintColor] on iOS.
 *   Note: Liquid Glass (iOS 26+) ignores this property; not yet supported.
 * @param isTranslucent Whether the tab bar is translucent.
 */
@Immutable
data class UIKitTabBarConfiguration(
    val selectedItemColor: Color = Color.Unspecified,
    val unselectedItemColor: Color = Color.Unspecified,
    val isTranslucent: Boolean = true,
)
