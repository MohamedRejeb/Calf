package com.kyant.shapes

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.lerp

@Stable
fun lerp(
    start: RoundedRectangularShape,
    stop: RoundedRectangularShape,
    fraction: Float,
    style: RoundedCornerStyle
): RoundedRectangularShape {
    return when (fraction) {
        0f -> start
        1f -> stop
        else -> LerpRoundedRectangle(start, stop, fraction, style)
    }
}

@Stable
fun lerp(
    start: RoundedRectangularShape,
    stop: RoundedRectangularShape,
    fraction: Float
): RoundedRectangularShape {
    return when (fraction) {
        0f -> start
        1f -> stop
        else -> {
            val startStyle = start.style
            val stopStyle = stop.style
            val style = when {
                startStyle != null && stopStyle != null -> if (fraction < 0.5f) startStyle else stopStyle
                startStyle != null -> startStyle
                stopStyle != null -> stopStyle
                else -> null
            }
            LerpRoundedRectangle(start, stop, fraction, style)
        }
    }
}

@Immutable
private data class LerpRoundedRectangle(
    val start: RoundedRectangularShape,
    val stop: RoundedRectangularShape,
    val fraction: Float,
    override val style: RoundedCornerStyle?
) : RoundedRectangularShape {

    override fun corners(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): RoundedRectangularShape.Corners {
        val startCorners = start.corners(size, layoutDirection, density)
        val stopCorners = stop.corners(size, layoutDirection, density)
        return if (startCorners == stopCorners) {
            startCorners
        } else {
            val maxRadius = size.minDimension * 0.5f
            RoundedRectangularShape.Corners(
                topLeft =
                    lerp(startCorners.topLeft, stopCorners.topLeft, fraction).fastCoerceIn(0f, maxRadius),
                topRight =
                    lerp(startCorners.topRight, stopCorners.topRight, fraction).fastCoerceIn(0f, maxRadius),
                bottomRight =
                    lerp(startCorners.bottomRight, stopCorners.bottomRight, fraction).fastCoerceIn(0f, maxRadius),
                bottomLeft =
                    lerp(startCorners.bottomLeft, stopCorners.bottomLeft, fraction).fastCoerceIn(0f, maxRadius)
            )
        }
    }

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val corners = corners(size, layoutDirection, density)
        if (style == null) {
            return Outline.Rectangle(Rect(0f, 0f, size.width, size.height))
        }
        return if (corners.topLeft == corners.topRight && corners.topRight == corners.bottomRight && corners.bottomRight == corners.bottomLeft) {
            roundedRectangleOutline(
                size = size,
                radius = corners.topLeft,
                style = style
            )
        } else {
            roundedRectangleOutline(
                size = size,
                topLeft = corners.topLeft,
                topRight = corners.topRight,
                bottomRight = corners.bottomRight,
                bottomLeft = corners.bottomLeft,
                style = style
            )
        }
    }

    override fun copy(style: RoundedCornerStyle) =
        LerpRoundedRectangle(
            start = start,
            stop = stop,
            fraction = fraction,
            style = style
        )
}
