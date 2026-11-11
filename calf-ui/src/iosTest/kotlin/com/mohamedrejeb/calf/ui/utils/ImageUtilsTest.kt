@file:OptIn(ExperimentalForeignApi::class)

package com.mohamedrejeb.calf.ui.utils

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.ui.uikit.UIKitImage
import com.mohamedrejeb.calf.ui.uikit.UIKitImageRenderingMode
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGColorSpaceRelease
import platform.CoreGraphics.CGContextDrawImage
import platform.CoreGraphics.CGContextRelease
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.CGImageGetHeight
import platform.CoreGraphics.CGImageGetWidth
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIImage
import platform.UIKit.UIImageRenderingMode
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val DENSITY = 3f
private const val BITMAP_SIZE = 4
private const val BYTES_PER_PIXEL = 4
private const val BITS_PER_COMPONENT = 8
private const val CHANNEL_TOLERANCE = 2
private const val OPAQUE = 255
private const val HALF = 128
private const val VIEWPORT = 24f

/** A painter that notes the layout direction it was drawn in. */
private class DirectionPainter : Painter() {
    var drawnIn: LayoutDirection? = null

    override val intrinsicSize: Size = Size.Unspecified

    override fun DrawScope.onDraw() {
        drawnIn = layoutDirection
    }
}

/**
 * [UIKitImage] to `UIImage`: the size, scale and rendering mode UIKit gets, and the pixels of
 * colored images read back through Core Graphics. Runs on the iOS simulator.
 */
class ImageUtilsTest {
    private fun bitmapOf(color: Color): ImageBitmap =
        ImageBitmap(BITMAP_SIZE, BITMAP_SIZE).also { bitmap ->
            val area = Rect(0f, 0f, BITMAP_SIZE.toFloat(), BITMAP_SIZE.toFloat())
            Canvas(bitmap).drawRect(area, Paint().apply { this.color = color })
        }

    private val filledSquare: ImageVector =
        ImageVector.Builder(
            defaultWidth = VIEWPORT.dp,
            defaultHeight = VIEWPORT.dp,
            viewportWidth = VIEWPORT,
            viewportHeight = VIEWPORT,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(0f, 0f)
                lineTo(VIEWPORT, 0f)
                lineTo(VIEWPORT, VIEWPORT)
                lineTo(0f, VIEWPORT)
                close()
            }
        }.build()

    /** The RGBA bytes of the first pixel, alpha premultiplied, as Core Graphics draws the image. */
    private fun UIImage.firstPixel(): List<Int> {
        val cgImage = assertNotNull(CGImage, "the image has no bitmap")
        val width = CGImageGetWidth(cgImage).toInt()
        val height = CGImageGetHeight(cgImage).toInt()
        val bytes = UByteArray(width * height * BYTES_PER_PIXEL)
        bytes.usePinned { pinned ->
            val colorSpace = CGColorSpaceCreateDeviceRGB()
            val context = CGBitmapContextCreate(
                data = pinned.addressOf(0),
                width = width.convert(),
                height = height.convert(),
                bitsPerComponent = BITS_PER_COMPONENT.convert(),
                bytesPerRow = (width * BYTES_PER_PIXEL).convert(),
                space = colorSpace,
                bitmapInfo = CGImageAlphaInfo.kCGImageAlphaPremultipliedLast.value,
            )
            CGContextDrawImage(context, CGRectMake(0.0, 0.0, width.toDouble(), height.toDouble()), cgImage)
            CGContextRelease(context)
            CGColorSpaceRelease(colorSpace)
        }
        return (0 until BYTES_PER_PIXEL).map { bytes[it].toInt() }
    }

    private fun assertPixel(expected: List<Int>, actual: List<Int>) {
        val close = expected.zip(actual).all { (want, got) -> abs(want - got) <= CHANNEL_TOLERANCE }
        assertTrue(close, "expected RGBA $expected but was $actual")
    }

    @Test
    fun `a painter becomes an image of its size in points at the screen scale`() {
        val image = UIKitImage.Painter(ColorPainter(Color.Red), size = DpSize(30.dp, 20.dp)).toUIImage(DENSITY)

        val (width, height) = assertNotNull(image).size.useContents { width to height }
        assertEquals(30.0, width)
        assertEquals(20.0, height)
        assertEquals(DENSITY.toDouble(), image.scale)
    }

    @Test
    fun `a painter is a template unless it asks to keep its colors`() {
        val template = UIKitImage.Painter(ColorPainter(Color.Red)).toUIImage(DENSITY)
        val original = UIKitImage.Painter(ColorPainter(Color.Red), renderingMode = UIKitImageRenderingMode.Original)
            .toUIImage(DENSITY)

        assertEquals(UIImageRenderingMode.UIImageRenderingModeAlwaysTemplate, template?.renderingMode)
        assertEquals(UIImageRenderingMode.UIImageRenderingModeAlwaysOriginal, original?.renderingMode)
    }

    @Test
    fun `a painter keeps the colors it draws`() {
        val image = UIKitImage.Painter(ColorPainter(Color.Red), renderingMode = UIKitImageRenderingMode.Original)
            .toUIImage(DENSITY)

        assertPixel(listOf(OPAQUE, 0, 0, OPAQUE), assertNotNull(image).firstPixel())
    }

    @Test
    fun `a painter is drawn in the given layout direction`() {
        val painter = DirectionPainter()

        UIKitImage.Painter(painter).toUIImage(DENSITY, LayoutDirection.Rtl)

        assertEquals(LayoutDirection.Rtl, painter.drawnIn)
    }

    @Test
    fun `a painter without an area gives no image`() {
        assertNull(UIKitImage.Painter(ColorPainter(Color.Red), size = DpSize(0.dp, 24.dp)).toUIImage(DENSITY))
    }

    @Test
    fun `a bitmap keeps its red and blue channels apart`() {
        val red = UIKitImage.Bitmap(bitmapOf(Color.Red)).toUIImage(DENSITY)
        val blue = UIKitImage.Bitmap(bitmapOf(Color.Blue)).toUIImage(DENSITY)

        assertPixel(listOf(OPAQUE, 0, 0, OPAQUE), assertNotNull(red).firstPixel())
        assertPixel(listOf(0, 0, OPAQUE, OPAQUE), assertNotNull(blue).firstPixel())
    }

    @Test
    fun `a half transparent bitmap keeps its alpha`() {
        val image = UIKitImage.Bitmap(bitmapOf(Color.Blue.copy(alpha = 0.5f))).toUIImage(DENSITY)

        assertPixel(listOf(0, 0, HALF, HALF), assertNotNull(image).firstPixel())
    }

    @Test
    fun `a bitmap is one point per pixel unless it names its scale`() {
        val plain = UIKitImage.Bitmap(bitmapOf(Color.Red)).toUIImage(DENSITY)
        val scaled = UIKitImage.Bitmap(bitmapOf(Color.Red), scale = 2f).toUIImage(DENSITY)

        assertEquals(BITMAP_SIZE.toDouble(), assertNotNull(plain).size.useContents { width })
        assertEquals(BITMAP_SIZE / 2.0, assertNotNull(scaled).size.useContents { width })
        assertEquals(2.0, scaled.scale)
    }

    @Test
    fun `a bitmap leaves the rendering mode to the control unless it names one`() {
        val automatic = UIKitImage.Bitmap(bitmapOf(Color.Red)).toUIImage(DENSITY)
        val template = UIKitImage.Bitmap(bitmapOf(Color.Red), renderingMode = UIKitImageRenderingMode.Template)
            .toUIImage(DENSITY)

        assertEquals(UIImageRenderingMode.UIImageRenderingModeAutomatic, automatic?.renderingMode)
        assertEquals(UIImageRenderingMode.UIImageRenderingModeAlwaysTemplate, template?.renderingMode)
    }

    @Test
    fun `a vector is filled and sized in points at the screen scale`() {
        val image = UIKitImage.Vector(filledSquare).toUIImage(DENSITY)

        val width = assertNotNull(image).size.useContents { width }
        assertEquals(VIEWPORT.toDouble(), width)
        assertEquals(DENSITY.toDouble(), image.scale)
        assertEquals(OPAQUE, image.firstPixel().last(), "the shape is filled")
    }

    @Test
    fun `a system name gives the SF Symbol`() {
        assertNotNull(UIKitImage.SystemName("house").toUIImage(DENSITY))
        assertNull(UIKitImage.SystemName("calf.no.such.symbol").toUIImage(DENSITY))
    }
}
