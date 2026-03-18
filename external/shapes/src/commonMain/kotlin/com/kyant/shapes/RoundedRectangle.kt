package com.kyant.shapes

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.util.fastCoerceIn

@Immutable
class RoundedRectangle(
    val cornerRadius: Dp,
    override val style: RoundedCornerStyle = RoundedCornerStyle.Continuous
) : RoundedRectangularShape {

    override fun corners(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): RoundedRectangularShape.Corners {
        val radius = with(density) { cornerRadius.toPx() }.fastCoerceIn(0f, size.minDimension * 0.5f)
        return RoundedRectangularShape.Corners(
            topLeft = radius,
            topRight = radius,
            bottomRight = radius,
            bottomLeft = radius
        )
    }

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val radius = with(density) { cornerRadius.toPx() }.fastCoerceIn(0f, size.minDimension * 0.5f)
        return roundedRectangleOutline(
            size = size,
            radius = radius,
            style = style
        )
    }

    override fun copy(style: RoundedCornerStyle) =
        RoundedRectangle(
            cornerRadius = cornerRadius,
            style = style
        )

    fun copy(
        cornerRadius: Dp = this.cornerRadius,
        style: RoundedCornerStyle = this.style
    ) =
        RoundedRectangle(
            cornerRadius = cornerRadius,
            style = style
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RoundedRectangle) return false

        if (cornerRadius != other.cornerRadius) return false
        if (style != other.style) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cornerRadius.hashCode()
        result = 31 * result + style.hashCode()
        return result
    }

    override fun toString(): String {
        return "RoundedRectangle(cornerRadius=$cornerRadius, style=$style)"
    }
}
