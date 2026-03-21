package com.mohamedrejeb.calf.ui.fab

import com.mohamedrejeb.calf.ui.uikit.UIKitImage

/**
 * Represents an item in the expandable FAB menu for iOS.
 *
 * On iOS, each item is rendered as a [UIButton] with Liquid Glass effect inside a
 * [UIGlassEffectContainerView].
 *
 * @param title The label for the FAB item.
 * @param image The image for the FAB item. See [UIKitImage] for supported types.
 */
internal data class UIKitExpandableFABItem(
    val title: String,
    val image: UIKitImage? = null,
)
