package com.mohamedrejeb.calf.ui.navigation

/**
 * Configuration for the iOS UINavigationBar appearance in [AdaptiveTopBar].
 *
 * @param prefersLargeTitles Whether the navigation bar should display large titles.
 * @param isTranslucent Whether the navigation bar is translucent.
 */
data class UIKitNavigationBarConfiguration(
    val prefersLargeTitles: Boolean = false,
    val isTranslucent: Boolean = true,
)
