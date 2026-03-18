package com.kyant.shapes

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.util.fastCoerceIn

internal fun roundedRectangleOutline(
    size: Size,
    radius: Float,
    style: RoundedCornerStyle
): Outline {
    val width = size.width
    val height = size.height
    val maxRadius = size.minDimension * 0.5f
    return when {
        radius == 0f -> {
            Outline.Rectangle(Rect(0f, 0f, width, height))
        }

        style == RoundedCornerStyle.Circular || (width == height && radius >= maxRadius) -> {
            val cornerRadius = CornerRadius(radius)
            Outline.Rounded(
                RoundRect(
                    left = 0f,
                    top = 0f,
                    right = width,
                    bottom = height,
                    topLeftCornerRadius = cornerRadius,
                    topRightCornerRadius = cornerRadius,
                    bottomRightCornerRadius = cornerRadius,
                    bottomLeftCornerRadius = cornerRadius
                )
            )
        }

        else -> {
            Outline.Generic(continuousCurvatureRoundedRectanglePath(size, radius))
        }
    }
}

internal fun roundedRectangleOutline(
    size: Size,
    topLeft: Float,
    topRight: Float,
    bottomRight: Float,
    bottomLeft: Float,
    style: RoundedCornerStyle
): Outline {
    val width = size.width
    val height = size.height
    val maxRadius = size.minDimension * 0.5f
    return when {
        topLeft == 0f && topRight == 0f && bottomRight == 0f && bottomLeft == 0f -> {
            Outline.Rectangle(Rect(0f, 0f, width, height))
        }

        style == RoundedCornerStyle.Circular ||
                (width == height && topLeft >= maxRadius && topRight >= maxRadius && bottomRight >= maxRadius && bottomLeft >= maxRadius) -> {
            Outline.Rounded(
                RoundRect(
                    left = 0f,
                    top = 0f,
                    right = width,
                    bottom = height,
                    topLeftCornerRadius = CornerRadius(topLeft),
                    topRightCornerRadius = CornerRadius(topRight),
                    bottomRightCornerRadius = CornerRadius(bottomRight),
                    bottomLeftCornerRadius = CornerRadius(bottomLeft)
                )
            )
        }

        else -> {
            Outline.Generic(
                continuousCurvatureRoundedRectanglePath(
                    size = size,
                    topLeft = topLeft,
                    topRight = topRight,
                    bottomRight = bottomRight,
                    bottomLeft = bottomLeft
                )
            )
        }
    }
}

private fun continuousCurvatureRoundedRectanglePath(
    size: Size,
    radius: Float,
    path: Path? = null
): Path {
    val width = size.width
    val height = size.height
    val path = path?.apply { rewind() } ?: Path()
    return path.apply {
        val cornerBuilder = ContinuousCurvatureRoundedRectangleCornerBuilder.Default
        val w = width.toDouble()
        val h = height.toDouble()

        val r = radius.toDouble()
        val tW = ((width * 0.5 - r) / r).fastCoerceIn(0.0, 1.0)
        val tH = ((height * 0.5 - r) / r).fastCoerceIn(0.0, 1.0)
        val p = cornerBuilder.getCornerBezierPoints(tW, tH)
        if (p.size < 20) return@apply

        var x = w - r
        var y = 0.0
        moveTo((x + p[0] * r).toFloat(), (y + p[1] * r).toFloat())
        cubicTo(
            (x + p[2] * r).toFloat(), (y + p[3] * r).toFloat(),
            (x + p[4] * r).toFloat(), (y + p[5] * r).toFloat(),
            (x + p[6] * r).toFloat(), (y + p[7] * r).toFloat()
        )
        cubicTo(
            (x + p[8] * r).toFloat(), (y + p[9] * r).toFloat(),
            (x + p[10] * r).toFloat(), (y + p[11] * r).toFloat(),
            (x + p[12] * r).toFloat(), (y + p[13] * r).toFloat()
        )
        cubicTo(
            (x + p[14] * r).toFloat(), (y + p[15] * r).toFloat(),
            (x + p[16] * r).toFloat(), (y + p[17] * r).toFloat(),
            (x + p[18] * r).toFloat(), (y + p[19] * r).toFloat()
        )

        x = w - r
        y = h
        lineTo((x + p[18] * r).toFloat(), (y - p[19] * r).toFloat())
        cubicTo(
            (x + p[16] * r).toFloat(), (y - p[17] * r).toFloat(),
            (x + p[14] * r).toFloat(), (y - p[15] * r).toFloat(),
            (x + p[12] * r).toFloat(), (y - p[13] * r).toFloat()
        )
        cubicTo(
            (x + p[10] * r).toFloat(), (y - p[11] * r).toFloat(),
            (x + p[8] * r).toFloat(), (y - p[9] * r).toFloat(),
            (x + p[6] * r).toFloat(), (y - p[7] * r).toFloat()
        )
        cubicTo(
            (x + p[4] * r).toFloat(), (y - p[5] * r).toFloat(),
            (x + p[2] * r).toFloat(), (y - p[3] * r).toFloat(),
            (x + p[0] * r).toFloat(), (y - p[1] * r).toFloat()
        )

        x = r
        y = h
        lineTo((x - p[0] * r).toFloat(), (y - p[1] * r).toFloat())
        cubicTo(
            (x - p[2] * r).toFloat(), (y - p[3] * r).toFloat(),
            (x - p[4] * r).toFloat(), (y - p[5] * r).toFloat(),
            (x - p[6] * r).toFloat(), (y - p[7] * r).toFloat()
        )
        cubicTo(
            (x - p[8] * r).toFloat(), (y - p[9] * r).toFloat(),
            (x - p[10] * r).toFloat(), (y - p[11] * r).toFloat(),
            (x - p[12] * r).toFloat(), (y - p[13] * r).toFloat()
        )
        cubicTo(
            (x - p[14] * r).toFloat(), (y - p[15] * r).toFloat(),
            (x - p[16] * r).toFloat(), (y - p[17] * r).toFloat(),
            (x - p[18] * r).toFloat(), (y - p[19] * r).toFloat()
        )

        x = r
        y = 0.0
        lineTo((x - p[18] * r).toFloat(), (y + p[19] * r).toFloat())
        cubicTo(
            (x - p[16] * r).toFloat(), (y + p[17] * r).toFloat(),
            (x - p[14] * r).toFloat(), (y + p[15] * r).toFloat(),
            (x - p[12] * r).toFloat(), (y + p[13] * r).toFloat()
        )
        cubicTo(
            (x - p[10] * r).toFloat(), (y + p[11] * r).toFloat(),
            (x - p[8] * r).toFloat(), (y + p[9] * r).toFloat(),
            (x - p[6] * r).toFloat(), (y + p[7] * r).toFloat()
        )
        cubicTo(
            (x - p[4] * r).toFloat(), (y + p[5] * r).toFloat(),
            (x - p[2] * r).toFloat(), (y + p[3] * r).toFloat(),
            (x - p[0] * r).toFloat(), (y + p[1] * r).toFloat()
        )

        close()
    }
}

private fun continuousCurvatureRoundedRectanglePath(
    size: Size,
    topLeft: Float,
    topRight: Float,
    bottomRight: Float,
    bottomLeft: Float,
    path: Path? = null
): Path {
    val width = size.width
    val height = size.height
    val path = path?.apply { rewind() } ?: Path()
    return path.apply {
        val cornerBuilder = ContinuousCurvatureRoundedRectangleCornerBuilder.Default
        val w = width.toDouble()
        val h = height.toDouble()

        var r = topRight.toDouble()
        var tW = ((width * 0.5 - r) / r).fastCoerceIn(0.0, 1.0)
        var tH = ((height * 0.5 - r) / r).fastCoerceIn(0.0, 1.0)
        var p = cornerBuilder.getCornerBezierPoints(tW, tH)
        if (p.size < 20) return@apply

        var x = w - r
        var y = 0.0
        moveTo((x + p[0] * r).toFloat(), (y + p[1] * r).toFloat())
        cubicTo(
            (x + p[2] * r).toFloat(), (y + p[3] * r).toFloat(),
            (x + p[4] * r).toFloat(), (y + p[5] * r).toFloat(),
            (x + p[6] * r).toFloat(), (y + p[7] * r).toFloat()
        )
        cubicTo(
            (x + p[8] * r).toFloat(), (y + p[9] * r).toFloat(),
            (x + p[10] * r).toFloat(), (y + p[11] * r).toFloat(),
            (x + p[12] * r).toFloat(), (y + p[13] * r).toFloat()
        )
        cubicTo(
            (x + p[14] * r).toFloat(), (y + p[15] * r).toFloat(),
            (x + p[16] * r).toFloat(), (y + p[17] * r).toFloat(),
            (x + p[18] * r).toFloat(), (y + p[19] * r).toFloat()
        )

        r = bottomRight.toDouble()
        tW = ((width * 0.5 - r) / r).fastCoerceIn(0.0, 1.0)
        tH = ((height * 0.5 - r) / r).fastCoerceIn(0.0, 1.0)
        p = cornerBuilder.getCornerBezierPoints(tW, tH)
        if (p.size < 20) return@apply

        x = w - r
        y = h
        lineTo((x + p[18] * r).toFloat(), (y - p[19] * r).toFloat())
        cubicTo(
            (x + p[16] * r).toFloat(), (y - p[17] * r).toFloat(),
            (x + p[14] * r).toFloat(), (y - p[15] * r).toFloat(),
            (x + p[12] * r).toFloat(), (y - p[13] * r).toFloat()
        )
        cubicTo(
            (x + p[10] * r).toFloat(), (y - p[11] * r).toFloat(),
            (x + p[8] * r).toFloat(), (y - p[9] * r).toFloat(),
            (x + p[6] * r).toFloat(), (y - p[7] * r).toFloat()
        )
        cubicTo(
            (x + p[4] * r).toFloat(), (y - p[5] * r).toFloat(),
            (x + p[2] * r).toFloat(), (y - p[3] * r).toFloat(),
            (x + p[0] * r).toFloat(), (y - p[1] * r).toFloat()
        )

        r = bottomLeft.toDouble()
        tW = ((width * 0.5 - r) / r).fastCoerceIn(0.0, 1.0)
        tH = ((height * 0.5 - r) / r).fastCoerceIn(0.0, 1.0)
        p = cornerBuilder.getCornerBezierPoints(tW, tH)
        if (p.size < 20) return@apply

        x = r
        y = h
        lineTo((x - p[0] * r).toFloat(), (y - p[1] * r).toFloat())
        cubicTo(
            (x - p[2] * r).toFloat(), (y - p[3] * r).toFloat(),
            (x - p[4] * r).toFloat(), (y - p[5] * r).toFloat(),
            (x - p[6] * r).toFloat(), (y - p[7] * r).toFloat()
        )
        cubicTo(
            (x - p[8] * r).toFloat(), (y - p[9] * r).toFloat(),
            (x - p[10] * r).toFloat(), (y - p[11] * r).toFloat(),
            (x - p[12] * r).toFloat(), (y - p[13] * r).toFloat()
        )
        cubicTo(
            (x - p[14] * r).toFloat(), (y - p[15] * r).toFloat(),
            (x - p[16] * r).toFloat(), (y - p[17] * r).toFloat(),
            (x - p[18] * r).toFloat(), (y - p[19] * r).toFloat()
        )

        r = topLeft.toDouble()
        tW = ((width * 0.5 - r) / r).fastCoerceIn(0.0, 1.0)
        tH = ((height * 0.5 - r) / r).fastCoerceIn(0.0, 1.0)
        p = cornerBuilder.getCornerBezierPoints(tW, tH)
        if (p.size < 20) return@apply

        x = r
        y = 0.0
        lineTo((x - p[18] * r).toFloat(), (y + p[19] * r).toFloat())
        cubicTo(
            (x - p[16] * r).toFloat(), (y + p[17] * r).toFloat(),
            (x - p[14] * r).toFloat(), (y + p[15] * r).toFloat(),
            (x - p[12] * r).toFloat(), (y + p[13] * r).toFloat()
        )
        cubicTo(
            (x - p[10] * r).toFloat(), (y + p[11] * r).toFloat(),
            (x - p[8] * r).toFloat(), (y + p[9] * r).toFloat(),
            (x - p[6] * r).toFloat(), (y + p[7] * r).toFloat()
        )
        cubicTo(
            (x - p[4] * r).toFloat(), (y + p[5] * r).toFloat(),
            (x - p[2] * r).toFloat(), (y + p[3] * r).toFloat(),
            (x - p[0] * r).toFloat(), (y + p[1] * r).toFloat()
        )

        close()
    }
}
