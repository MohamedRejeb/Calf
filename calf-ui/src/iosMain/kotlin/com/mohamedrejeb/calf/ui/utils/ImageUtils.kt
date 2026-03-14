package com.mohamedrejeb.calf.ui.utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asSkiaPath
import androidx.compose.ui.graphics.toComposeImageBitmap
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

/**
 * Converts a [UIKitImage] to a native iOS [UIImage].
 *
 * @return The converted [UIImage], or null if the conversion fails.
 */
fun UIKitImage.toUIImage(): UIImage? =
    when (this) {
        is UIKitImage.SystemName -> UIImage.systemImageNamed(name)
        is UIKitImage.Vector -> imageVector.toUIImage(width, height)
        is UIKitImage.Bitmap -> imageBitmap.toUIImage()
    }

fun ImageVector.toUIImage(width: Int, height: Int): UIImage? =
    this.toImageBitmap(width, height).toUIImage()

private fun ImageVector.toImageBitmap(width: Int, height: Int): ImageBitmap {
    val surface = Surface.makeRasterN32Premul(width, height)
    val canvas = surface.canvas

    canvas.clear(Color.TRANSPARENT) // clear the canvas
    with(canvas) { // draw the ImageVector
        val paint = org.jetbrains.skia.Paint().apply {
            color = Color.BLACK // Change color as needed
        }
        drawPath(toPath().asSkiaPath(), paint)
    }

    return surface.makeImageSnapshot().toComposeImageBitmap()
}

@OptIn(ExperimentalForeignApi::class)
fun ImageBitmap.toUIImage(): UIImage? {
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
    val image = UIImage.imageWithCGImage(cgImage)

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
