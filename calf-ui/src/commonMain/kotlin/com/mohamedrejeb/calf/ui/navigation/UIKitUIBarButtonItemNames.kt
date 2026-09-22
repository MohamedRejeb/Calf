package com.mohamedrejeb.calf.ui.navigation

import com.mohamedrejeb.calf.ui.uikit.UIKitImage

/**
 * What a [UIKitUIBarButtonItem] is called on the native side. `null` leaves UIKit's own
 * name in place (system items and SF Symbols have one).
 *
 * @property nativeTitle the `title` to set on the `UIBarButtonItem`. Image and system items
 *   get one from their label: the bar keeps showing the image, and the title names the
 *   item in the overflow menu and for VoiceOver. A text item already shows its title.
 * @property accessibilityLabel the `accessibilityLabel` to set, when the caller gave one.
 */
internal data class BarButtonItemNames(
    val nativeTitle: String?,
    val accessibilityLabel: String?,
)

/** Pure name resolution, shared by every place that builds a native bar button item. */
internal fun UIKitUIBarButtonItem.resolveNames(): BarButtonItemNames {
    if (isSpace) return BarButtonItemNames(nativeTitle = null, accessibilityLabel = null)
    val name = label ?: title
    return when {
        systemItem != null -> BarButtonItemNames(nativeTitle = label, accessibilityLabel = label)
        image != null -> BarButtonItemNames(nativeTitle = name, accessibilityLabel = name)
        else -> BarButtonItemNames(nativeTitle = title, accessibilityLabel = label)
    }
}

/**
 * An image item whose image is a Compose vector or bitmap and that has neither a label nor
 * a title: VoiceOver has nothing to read and the overflow menu lists it without text.
 */
internal val UIKitUIBarButtonItem.isUnnamedCustomImage: Boolean
    get() = systemItem == null && image != null && image !is UIKitImage.SystemName && label == null && title == null

private val UIKitUIBarButtonItem.isSpace: Boolean
    get() = systemItem == UIKitUIBarButtonSystemItem.FlexibleSpace || systemItem == UIKitUIBarButtonSystemItem.FixedSpace

/**
 * The raw `UIBarButtonItemVisibilityPriority` value, as measured on iOS 27.1. The iOS side
 * prefers the constants exported by the running UIKit and uses this when they cannot be
 * looked up.
 */
internal val UIKitUIBarButtonItemVisibilityPriority.uikitFallbackValue: Int
    get() = when (this) {
        UIKitUIBarButtonItemVisibilityPriority.Low -> -250
        UIKitUIBarButtonItemVisibilityPriority.Standard -> 0
        UIKitUIBarButtonItemVisibilityPriority.High -> 250
    }
