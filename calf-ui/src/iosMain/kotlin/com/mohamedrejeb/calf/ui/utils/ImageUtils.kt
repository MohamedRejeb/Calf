package com.mohamedrejeb.calf.ui.utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asSkiaPath
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorNode
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.graphics.vector.toPath
import com.mohamedrejeb.calf.ui.uikit.UIKitImage
import platform.UIKit.UIImage
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import kotlinx.cinterop.refTo
import org.jetbrains.skia.Color
import org.jetbrains.skia.Surface
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGBitmapContextCreateImage
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGColorSpaceRelease
import platform.CoreGraphics.CGContextRelease
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.CGImageRelease
import platform.UIKit.UIImageOrientation

/**
 * Converts a [UIKitImage] to a native iOS [UIImage].
 *
 * @param density The screen density (pixels per dp) used for rasterization of vector images.
 * @return The converted [UIImage], or null if the conversion fails.
 */
internal fun UIKitImage.toUIImage(density: Float): UIImage? =
    when (this) {
        is UIKitImage.SystemName -> UIImage.systemImageNamed(name)
        is UIKitImage.Vector -> imageVector.toUIImage(width, height, density)
        is UIKitImage.Bitmap -> imageBitmap.toUIImage()
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

@OptIn(ExperimentalForeignApi::class)
internal fun ImageBitmap.toUIImage(scale: Double = 1.0): UIImage? {
    val pixelMap = toPixelMap()
    val width = pixelMap.width
    val height = pixelMap.height

    val colorSpace = CGColorSpaceCreateDeviceRGB()
    val data = pixelMap.buffer.toUIntArray()

    val rowBytes = 4 * width

    val context = CGBitmapContextCreate(
        data = data.refTo(0),
        width = width.convert(),
        height = height.convert(),
        bitsPerComponent = 8.convert(),
        bytesPerRow = rowBytes.convert(),
        space = colorSpace,
        bitmapInfo = CGImageAlphaInfo.kCGImageAlphaPremultipliedLast.value
    )

    val cgImage = CGBitmapContextCreateImage(context) ?: return null
    @Suppress("DEPRECATION")
    val orientation = UIImageOrientation.UIImageOrientationUp
    val image = UIImage(cGImage = cgImage, scale = scale, orientation = orientation)

    CGContextRelease(context)
    CGColorSpaceRelease(colorSpace)
    CGImageRelease(cgImage)

    return image
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
