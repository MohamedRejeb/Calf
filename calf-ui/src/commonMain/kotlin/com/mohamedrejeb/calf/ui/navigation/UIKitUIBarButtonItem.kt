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
 * @param label The item's name for VoiceOver and for the bar's overflow menu, where an
 *   image-only item is otherwise listed without text. The bar keeps showing the image.
 *   When null, an image item is named after its [title] if it has one, and every other
 *   item keeps UIKit's own name: system items and SF Symbols have one, a Compose vector or
 *   bitmap has none until you give it one.
 * @param visibilityPriority Which items leave the bar first when there is no room for all
 *   of them (iOS 27+, for example the vertical bar of an iPhone Duo): [UIKitUIBarButtonItemVisibilityPriority.Low]
 *   items move to the overflow menu first, [UIKitUIBarButtonItemVisibilityPriority.High] items last.
 *   Ignored before iOS 27.
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
    val label: String? = null,
    val visibilityPriority: UIKitUIBarButtonItemVisibilityPriority = UIKitUIBarButtonItemVisibilityPriority.Standard,
) {
    internal val hasMenu: Boolean
        get() = menuItems.isNotEmpty() || menuSections.isNotEmpty()
    companion object {

        /** Creates a bar button item with a title. Matches UIBarButtonItem(title:style:target:action:). */
        fun title(
            title: String,
            enabled: Boolean = true,
            label: String? = null,
            visibilityPriority: UIKitUIBarButtonItemVisibilityPriority = UIKitUIBarButtonItemVisibilityPriority.Standard,
            onClick: () -> Unit = {},
        ) = UIKitUIBarButtonItem(
            title = title,
            enabled = enabled,
            onClick = onClick,
            label = label,
            visibilityPriority = visibilityPriority,
        )

        /**
         * Creates a bar button item with an image. Matches UIBarButtonItem(image:style:target:action:).
         * Give a Compose vector or bitmap a [label]: it is what VoiceOver reads and what the
         * overflow menu shows next to the image. SF Symbols carry their own description.
         */
        fun image(
            image: UIKitImage,
            enabled: Boolean = true,
            label: String? = null,
            visibilityPriority: UIKitUIBarButtonItemVisibilityPriority = UIKitUIBarButtonItemVisibilityPriority.Standard,
            onClick: () -> Unit = {},
        ) = UIKitUIBarButtonItem(
            image = image,
            enabled = enabled,
            onClick = onClick,
            label = label,
            visibilityPriority = visibilityPriority,
        )

        /** Creates a system bar button item. Matches UIBarButtonItem(barButtonSystemItem:target:action:). */
        fun systemItem(
            systemItem: UIKitUIBarButtonSystemItem,
            enabled: Boolean = true,
            label: String? = null,
            visibilityPriority: UIKitUIBarButtonItemVisibilityPriority = UIKitUIBarButtonItemVisibilityPriority.Standard,
            onClick: () -> Unit = {},
        ) = UIKitUIBarButtonItem(
            systemItem = systemItem,
            enabled = enabled,
            onClick = onClick,
            label = label,
            visibilityPriority = visibilityPriority,
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
            label: String? = null,
            visibilityPriority: UIKitUIBarButtonItemVisibilityPriority = UIKitUIBarButtonItemVisibilityPriority.Standard,
        ) = UIKitUIBarButtonItem(
            title = title,
            image = image,
            systemItem = systemItem,
            enabled = enabled,
            menuItems = menuItems,
            menuSections = menuSections,
            label = label,
            visibilityPriority = visibilityPriority,
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

/**
 * How long a bar button item is kept in the bar when there is not enough room for every
 * item (iOS 27+). Maps to `UIBarButtonItem.visibilityPriority`; UIKit moves [Low] items to
 * the overflow menu first and [High] items last. Before iOS 27 every item is [Standard].
 */
enum class UIKitUIBarButtonItemVisibilityPriority {
    Low,
    Standard,
    High,
}
