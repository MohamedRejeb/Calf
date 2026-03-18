package com.kyant.shapes

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

@Immutable
class Capsule(
    override val style: RoundedCornerStyle = RoundedCornerStyle.Continuous
) : RoundedRectangularShape {

    override fun corners(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): RoundedRectangularShape.Corners {
        val radius = size.minDimension * 0.5f
        return RoundedRectangularShape.Corners(
            topLeft = radius,
            topRight = radius,
            bottomRight = radius,
            bottomLeft = radius
        )
    }

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val radius = size.minDimension * 0.5f
        return roundedRectangleOutline(
            size = size,
            radius = radius,
            style = style
        )
    }

    override fun copy(style: RoundedCornerStyle) =
        Capsule(style = style)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Capsule) return false

        if (style != other.style) return false

        return true
    }

    override fun hashCode(): Int {
        return style.hashCode()
    }

    override fun toString(): String {
        return "Capsule(style=$style)"
    }
}
