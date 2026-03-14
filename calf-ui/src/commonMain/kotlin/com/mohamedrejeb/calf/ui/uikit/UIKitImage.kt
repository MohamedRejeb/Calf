package com.mohamedrejeb.calf.ui.uikit

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents an image that can be converted to a UIImage on iOS.
 *
 * Supports different image types:
 * - [SystemName]: An iOS SF Symbol system image name.
 * - [Vector]: A Compose [ImageVector].
 * - [Bitmap]: A Compose [ImageBitmap].
 */
sealed interface UIKitImage {
    /**
     * An iOS SF Symbol system image name.
     *
     * @param name The SF Symbol name (e.g., "house.fill", "heart.fill").
     */
    data class SystemName(val name: String) : UIKitImage

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
    ) : UIKitImage

    /**
     * A Compose [ImageBitmap].
     *
     * On iOS, this will be converted to a UIImage.
     *
     * @param imageBitmap The Compose [ImageBitmap] to use.
     */
    data class Bitmap(val imageBitmap: ImageBitmap) : UIKitImage
}
