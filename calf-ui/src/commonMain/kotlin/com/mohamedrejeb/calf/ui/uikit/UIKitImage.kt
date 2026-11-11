package com.mohamedrejeb.calf.ui.uikit

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/**
 * Represents an image that can be converted to a UIImage on iOS.
 *
 * Supports different image types:
 * - [SystemName]: An iOS SF Symbol system image name.
 * - [Vector]: A Compose [ImageVector].
 * - [Bitmap]: A Compose [ImageBitmap].
 * - [Painter]: A Compose [androidx.compose.ui.graphics.painter.Painter], e.g. from
 *   `painterResource(...)`.
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
     * Note: only the path geometry is rasterized, as a single filled shape. Fill rules
     * (even-odd), strokes, group transforms, clip paths and alpha are ignored. For vectors that
     * use any of those, wrap them in a painter (`rememberVectorPainter`, `painterResource`) and
     * use [Painter] instead.
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
     * @param scale The scale factor of the bitmap, i.e. how many bitmap pixels make up one
     *   point on screen. Default is 1, which shows the bitmap at one point per pixel. Pass the
     *   screen density for a bitmap that was rendered at that density.
     * @param renderingMode How UIKit colors the image. See [UIKitImageRenderingMode].
     */
    data class Bitmap(
        val imageBitmap: ImageBitmap,
        val scale: Float = 1f,
        val renderingMode: UIKitImageRenderingMode = UIKitImageRenderingMode.Automatic,
    ) : UIKitImage

    /**
     * A Compose [androidx.compose.ui.graphics.painter.Painter].
     *
     * On iOS, the painter is drawn offscreen at [size] times the screen density, so it stays
     * sharp on any display scale. Everything a painter can draw is preserved: even-odd fills,
     * strokes, group transforms, clip paths, alpha, bitmaps. Use this for images loaded with
     * `painterResource(...)` or `rememberVectorPainter(...)`.
     *
     * The painter is drawn once, when the item it belongs to is set, and stretched to [size]. A
     * painter whose content arrives later, like an image loader's, is not drawn again: give the
     * item its image once it has loaded, as a [Bitmap] or as another painter.
     *
     * The painter is compared by reference, so keep it stable across recompositions (the
     * `painterResource` / `rememberVectorPainter` results already are).
     *
     * @param painter The Compose painter to draw.
     * @param size The size in dp the painter is drawn at. Default is 24 x 24 dp.
     * @param renderingMode How UIKit colors the image. See [UIKitImageRenderingMode]. Icons
     *   that should take the tint color use [UIKitImageRenderingMode.Template]; photos and
     *   other full-color images use [UIKitImageRenderingMode.Original].
     */
    data class Painter(
        val painter: androidx.compose.ui.graphics.painter.Painter,
        val size: DpSize = DpSize(24.dp, 24.dp),
        val renderingMode: UIKitImageRenderingMode = UIKitImageRenderingMode.Template,
    ) : UIKitImage
}

/**
 * How UIKit colors a [UIKitImage]. Maps to `UIImage.RenderingMode` on iOS.
 */
enum class UIKitImageRenderingMode {
    /** Let the hosting control decide. Tab bar and navigation bar items treat images as templates. */
    Automatic,

    /** Only the alpha channel is used; the image is drawn in the control's tint color. */
    Template,

    /** The image is drawn with its own colors, never tinted. */
    Original,
}
