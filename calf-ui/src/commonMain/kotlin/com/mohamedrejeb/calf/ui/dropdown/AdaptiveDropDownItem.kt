package com.mohamedrejeb.calf.ui.dropdown

import com.mohamedrejeb.calf.ui.uikit.UIKitImage

/**
 * Represents a single item in an adaptive drop-down menu.
 *
 * @param title The display title of the menu item.
 * @param iosIcon An optional [UIKitImage] used as the icon on iOS (SF Symbol, ImageVector, or ImageBitmap).
 * @param isDestructive Whether the item should be styled as destructive (red on iOS).
 * @param isDisabled Whether the item is disabled and non-interactive.
 * @param onClick The action to perform when the item is tapped.
 */
data class AdaptiveDropDownItem(
    val title: String,
    val iosIcon: UIKitImage? = null,
    val isDestructive: Boolean = false,
    val isDisabled: Boolean = false,
    val onClick: () -> Unit,
)

/**
 * Represents a group/section of drop-down menu items.
 *
 * On iOS, sections are rendered as inline sub-menus with visual separators.
 * On Material platforms, sections are separated by horizontal dividers.
 *
 * @param title An optional title for the section.
 * @param items The list of [AdaptiveDropDownItem]s in this section.
 */
data class AdaptiveDropDownSection(
    val title: String = "",
    val items: List<AdaptiveDropDownItem>,
)
