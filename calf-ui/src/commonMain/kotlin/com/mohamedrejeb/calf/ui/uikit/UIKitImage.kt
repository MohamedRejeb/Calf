package com.mohamedrejeb.calf.ui.uikit

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Represents an image that can be converted to a UIImage on iOS.
 *
 * Supports different image types:
 * - [SystemName]: An iOS SF Symbol system image name.
 * - [Vector]: A Compose [ImageVector].
 * - [Bitmap]: A Compose [ImageBitmap].
 */
@Immutable
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
     * @param width The width in dp for rasterization on iOS. Default is 24.dp.
     * @param height The height in dp for rasterization on iOS. Default is 24.dp.
     */
    data class Vector(
        val imageVector: ImageVector,
        val width: Dp = 24.dp,
        val height: Dp = 24.dp,
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
