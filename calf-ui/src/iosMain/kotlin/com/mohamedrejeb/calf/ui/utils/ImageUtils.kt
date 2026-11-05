package com.mohamedrejeb.calf.ui.utils

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.asSkiaPath
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorNode
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.graphics.vector.toPath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import com.mohamedrejeb.calf.ui.uikit.UIKitImage
import com.mohamedrejeb.calf.ui.uikit.UIKitImageRenderingMode
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.Color
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.Surface
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIImage
import platform.UIKit.UIImageRenderingMode

/**
 * Converts a [UIKitImage] to a native iOS [UIImage].
 *
 * @param density The screen density (pixels per dp) used for rasterization of vector images
 *   and painters.
 * @param layoutDirection The layout direction painters are drawn in.
 * @return The converted [UIImage], or null if the conversion fails.
 */
internal fun UIKitImage.toUIImage(
    density: Float,
    layoutDirection: LayoutDirection = LayoutDirection.Ltr,
): UIImage? =
    when (this) {
        is UIKitImage.SystemName -> UIImage.systemImageNamed(name)
        is UIKitImage.Vector -> imageVector.toUIImage(width, height, density)
        is UIKitImage.Bitmap ->
            imageBitmap.toUIImage(scale = scale.toDouble())
                ?.withRenderingMode(renderingMode)
        is UIKitImage.Painter ->
            painter.toUIImage(size, density, layoutDirection)
                ?.withRenderingMode(renderingMode)
    }

private fun UIImage.withRenderingMode(renderingMode: UIKitImageRenderingMode): UIImage =
    when (renderingMode) {
        UIKitImageRenderingMode.Automatic -> this
        UIKitImageRenderingMode.Template ->
            imageWithRenderingMode(UIImageRenderingMode.UIImageRenderingModeAlwaysTemplate)
        UIKitImageRenderingMode.Original ->
            imageWithRenderingMode(UIImageRenderingMode.UIImageRenderingModeAlwaysOriginal)
    }

/**
 * Draws the painter offscreen into a bitmap of [size] times [density] pixels and wraps it in a
 * [UIImage] of [size] points, so the result is sharp on the current display scale.
 */
private fun Painter.toUIImage(
    size: DpSize,
    density: Float,
    layoutDirection: LayoutDirection,
): UIImage? {
    val composeDensity = Density(density)
    val pixelWidth = with(composeDensity) { size.width.roundToPx() }
    val pixelHeight = with(composeDensity) { size.height.roundToPx() }
    if (pixelWidth <= 0 || pixelHeight <= 0) return null

    val bitmap = ImageBitmap(pixelWidth, pixelHeight)
    val pixelSize = Size(pixelWidth.toFloat(), pixelHeight.toFloat())
    CanvasDrawScope().draw(composeDensity, layoutDirection, Canvas(bitmap), pixelSize) {
        with(this@toUIImage) { draw(this@draw.size) }
    }
    return bitmap.toUIImage(scale = density.toDouble())
}

private fun ImageVector.toUIImage(width: Dp, height: Dp, density: Float): UIImage? {
    val pixelWidth = (width.value * density).toInt()
    val pixelHeight = (height.value * density).toInt()
    return this.toImageBitmap(pixelWidth, pixelHeight).toUIImage(scale = density.toDouble())
}

private fun ImageVector.toImageBitmap(width: Int, height: Int): ImageBitmap {
    val surface = Surface.makeRasterN32Premul(width, height)
    val canvas = surface.canvas

    canvas.clear(Color.TRANSPARENT)

    // Scale from viewport coordinates to pixel dimensions
    val scaleX = width.toFloat() / viewportWidth
    val scaleY = height.toFloat() / viewportHeight
    canvas.scale(scaleX, scaleY)

    with(canvas) {
        val paint = org.jetbrains.skia.Paint().apply {
            color = Color.BLACK
        }
        drawPath(toPath().asSkiaPath(), paint)
    }

    return surface.makeImageSnapshot().toComposeImageBitmap()
}

/**
 * Converts the bitmap to a [UIImage] through PNG encoding, which keeps the premultiplied alpha
 * and channel order correct for colored content.
 *
 * @param scale How many bitmap pixels make up one point of the resulting image.
 */
@OptIn(ExperimentalForeignApi::class)
internal fun ImageBitmap.toUIImage(scale: Double = 1.0): UIImage? {
    val bytes = Image.makeFromBitmap(asSkiaBitmap())
        .encodeToData(EncodedImageFormat.PNG)
        ?.bytes
        ?: return null
    val data = bytes.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
    }
    return UIImage(data = data, scale = scale)
}

private fun ImageVector.toPath(): Path = Path().apply {
    traverseNodes(root, this)
}

private fun traverseNodes(node: VectorNode, path: Path) {
    when (node) {
        is VectorGroup -> {
            node.iterator().forEach { childNode ->
                traverseNodes(childNode, path)
            }
        }
        is VectorPath -> {
            path.addPath(node.pathData.toPath())
        }
    }
}
