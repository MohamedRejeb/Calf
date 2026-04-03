package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.runtime.Immutable
import com.mohamedrejeb.calf.ui.dropdown.AdaptiveDropDownItem
import com.mohamedrejeb.calf.ui.dropdown.AdaptiveDropDownSection
import com.mohamedrejeb.calf.ui.uikit.UIKitImage

/**
 * Represents a bar button item for iOS UIToolbar / UINavigationBar.
 *
 * Use the companion factory functions to create items matching iOS constructor styles.
 *
 * @param title The title of the bar button item. If null, [image] or [systemItem] is used.
 * @param image The image for the bar button item. See [UIKitImage] for supported types.
 * @param systemItem A system bar button item type. If provided, [title] and [image] are ignored.
 * @param enabled Whether the bar button item is enabled. Defaults to true.
 * @param selected Whether the bar button item is selected. Defaults to false.
 * @param width The width for a [UIKitUIBarButtonSystemItem.FixedSpace] item. Defaults to null.
 * @param onClick Callback invoked when the bar button item is clicked.
 */
@Immutable
data class UIKitUIBarButtonItem(
    val title: String? = null,
    val image: UIKitImage? = null,
    val systemItem: UIKitUIBarButtonSystemItem? = null,
    val enabled: Boolean = true,
    val selected: Boolean = false,
    val width: Double? = null,
    val onClick: () -> Unit = {},
    val menuItems: List<AdaptiveDropDownItem> = emptyList(),
    val menuSections: List<AdaptiveDropDownSection> = emptyList(),
) {
    internal val hasMenu: Boolean
        get() = menuItems.isNotEmpty() || menuSections.isNotEmpty()
    companion object {

        /** Creates a bar button item with a title. Matches UIBarButtonItem(title:style:target:action:). */
        fun title(
            title: String,
            enabled: Boolean = true,
            onClick: () -> Unit = {},
        ) = UIKitUIBarButtonItem(
            title = title,
            enabled = enabled,
            onClick = onClick,
        )

        /** Creates a bar button item with an image. Matches UIBarButtonItem(image:style:target:action:). */
        fun image(
            image: UIKitImage,
            enabled: Boolean = true,
            onClick: () -> Unit = {},
        ) = UIKitUIBarButtonItem(
            image = image,
            enabled = enabled,
            onClick = onClick,
        )

        /** Creates a system bar button item. Matches UIBarButtonItem(barButtonSystemItem:target:action:). */
        fun systemItem(
            systemItem: UIKitUIBarButtonSystemItem,
            enabled: Boolean = true,
            onClick: () -> Unit = {},
        ) = UIKitUIBarButtonItem(
            systemItem = systemItem,
            enabled = enabled,
            onClick = onClick,
        )

        /** Creates a flexible space that expands to fill available width. */
        fun flexibleSpace() = UIKitUIBarButtonItem(
            systemItem = UIKitUIBarButtonSystemItem.FlexibleSpace,
        )

        /** Creates a fixed-width space. */
        fun fixedSpace(width: Double = 16.0) = UIKitUIBarButtonItem(
            systemItem = UIKitUIBarButtonSystemItem.FixedSpace,
            width = width,
        )

        /** Creates a bar button item with a native dropdown menu (iOS 14+). */
        fun withMenu(
            title: String? = null,
            image: UIKitImage? = null,
            systemItem: UIKitUIBarButtonSystemItem? = null,
            menuItems: List<AdaptiveDropDownItem> = emptyList(),
            menuSections: List<AdaptiveDropDownSection> = emptyList(),
            enabled: Boolean = true,
        ) = UIKitUIBarButtonItem(
            title = title,
            image = image,
            systemItem = systemItem,
            enabled = enabled,
            menuItems = menuItems,
            menuSections = menuSections,
        )
    }
}

/**
 * Represents the system bar button item types available on iOS.
 *
 * Maps to `UIBarButtonItem.SystemItem` on iOS.
 */
enum class UIKitUIBarButtonSystemItem {
    Done,
    Cancel,
    Edit,
    Save,
    Add,
    Compose,
    Reply,
    Action,
    Organize,
    Trash,
    Search,
    Close,
    /** Expands to fill available space — used for spacing between items. */
    FlexibleSpace,
    /** A fixed-width space between items. Set width via [UIKitUIBarButtonItem.width]. */
    FixedSpace,
}
