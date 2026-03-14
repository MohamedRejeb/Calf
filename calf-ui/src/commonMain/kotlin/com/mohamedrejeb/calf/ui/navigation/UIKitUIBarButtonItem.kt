package com.mohamedrejeb.calf.ui.navigation

import com.mohamedrejeb.calf.ui.uikit.UIKitImage

/**
 * Represents a bar button item for the iOS UINavigationBar used in [AdaptiveTopBar].
 *
 * @param title The title of the bar button item. If null, [image] or [systemItem] is used.
 * @param image The image for the bar button item. See [UIKitImage] for supported types.
 * @param systemItem A system bar button item type. If provided, [title] and [image] are ignored.
 * @param onClick Callback invoked when the bar button item is clicked.
 */
data class UIKitUIBarButtonItem(
    val title: String? = null,
    val image: UIKitImage? = null,
    val systemItem: UIKitUIBarButtonSystemItem? = null,
    val onClick: () -> Unit = {},
)

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
}
