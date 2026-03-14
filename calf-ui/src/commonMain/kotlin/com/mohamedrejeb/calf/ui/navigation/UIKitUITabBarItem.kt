package com.mohamedrejeb.calf.ui.navigation

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents the image for a [UIKitUITabBarItem].
 *
 * Supports different image types:
 * - [SystemName]: An iOS SF Symbol system image name.
 * - [Vector]: A Compose [ImageVector].
 * - [Bitmap]: A Compose [ImageBitmap].
 */
sealed interface UIKitUITabBarItemImage {
    /**
     * An iOS SF Symbol system image name.
     *
     * @param name The SF Symbol name (e.g., "house.fill", "heart.fill").
     */
    data class SystemName(val name: String) : UIKitUITabBarItemImage

    /**
     * A Compose [ImageVector].
     *
     * On iOS, this will be converted to a UIImage using rasterization.
     *
     * @param imageVector The Compose [ImageVector] to use.
     * @param width The width in pixels for rasterization on iOS. Default is 24.
     * @param height The height in pixels for rasterization on iOS. Default is 24.
     */
    data class Vector(
        val imageVector: ImageVector,
        val width: Int = 24,
        val height: Int = 24,
    ) : UIKitUITabBarItemImage

    /**
     * A Compose [ImageBitmap].
     *
     * On iOS, this will be converted to a UIImage.
     *
     * @param imageBitmap The Compose [ImageBitmap] to use.
     */
    data class Bitmap(val imageBitmap: ImageBitmap) : UIKitUITabBarItemImage
}

/**
 * Represents a tab bar item for the iOS UITabBar used in [AdaptiveNavigationBar].
 *
 * @param title The title of the tab bar item.
 * @param image The image for the tab bar item. See [UIKitUITabBarItemImage] for supported types.
 * @param selectedImage Optional image to show when the item is selected.
 */
data class UIKitUITabBarItem(
    val title: String,
    val image: UIKitUITabBarItemImage? = null,
    val selectedImage: UIKitUITabBarItemImage? = null,
)
