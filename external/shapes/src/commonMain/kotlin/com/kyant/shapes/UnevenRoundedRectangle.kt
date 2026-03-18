package com.kyant.shapes

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn

@Immutable
class UnevenRoundedRectangle(
    val cornerRadii: RectangleCornerRadii,
    override val style: RoundedCornerStyle = RoundedCornerStyle.Continuous
) : RoundedRectangularShape {

    constructor(
        topStart: Dp = 0f.dp,
        topEnd: Dp = 0f.dp,
        bottomEnd: Dp = 0f.dp,
        bottomStart: Dp = 0f.dp,
        style: RoundedCornerStyle = RoundedCornerStyle.Continuous
    ) : this(
        cornerRadii = RectangleCornerRadii(
            topStart = topStart,
            topEnd = topEnd,
            bottomEnd = bottomEnd,
            bottomStart = bottomStart
        ),
        style = style
    )

    override fun corners(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): RoundedRectangularShape.Corners {
        val maxRadius = size.minDimension * 0.5f
        val topStart = with(density) { cornerRadii.topStart.toPx() }.fastCoerceIn(0f, maxRadius)
        val topEnd = with(density) { cornerRadii.topEnd.toPx() }.fastCoerceIn(0f, maxRadius)
        val bottomEnd = with(density) { cornerRadii.bottomEnd.toPx() }.fastCoerceIn(0f, maxRadius)
        val bottomStart = with(density) { cornerRadii.bottomStart.toPx() }.fastCoerceIn(0f, maxRadius)

        val topLeft: Float
        val topRight: Float
        val bottomRight: Float
        val bottomLeft: Float
        when (layoutDirection) {
            LayoutDirection.Ltr -> {
                topLeft = topStart
                topRight = topEnd
                bottomRight = bottomEnd
                bottomLeft = bottomStart
            }

            LayoutDirection.Rtl -> {
                topLeft = topEnd
                topRight = topStart
                bottomRight = bottomStart
                bottomLeft = bottomEnd
            }
        }

        return RoundedRectangularShape.Corners(
            topLeft = topLeft,
            topRight = topRight,
            bottomRight = bottomRight,
            bottomLeft = bottomLeft
        )
    }

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val corners = corners(size, layoutDirection, density)
        return roundedRectangleOutline(
            size = size,
            topLeft = corners.topLeft,
            topRight = corners.topRight,
            bottomRight = corners.bottomRight,
            bottomLeft = corners.bottomLeft,
            style = style
        )
    }

    override fun copy(style: RoundedCornerStyle) =
        UnevenRoundedRectangle(
            cornerRadii = cornerRadii,
            style = style
        )

    fun copy(
        cornerRadii: RectangleCornerRadii = this.cornerRadii,
        style: RoundedCornerStyle = this.style
    ): UnevenRoundedRectangle =
        UnevenRoundedRectangle(
            cornerRadii = cornerRadii,
            style = style
        )

    fun copy(
        topStart: Dp = cornerRadii.topStart,
        topEnd: Dp = cornerRadii.topEnd,
        bottomEnd: Dp = cornerRadii.bottomEnd,
        bottomStart: Dp = cornerRadii.bottomStart,
        style: RoundedCornerStyle = this.style
    ): UnevenRoundedRectangle =
        UnevenRoundedRectangle(
            topStart = topStart,
            topEnd = topEnd,
            bottomEnd = bottomEnd,
            bottomStart = bottomStart,
            style = style
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UnevenRoundedRectangle) return false

        if (cornerRadii != other.cornerRadii) return false
        if (style != other.style) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cornerRadii.hashCode()
        result = 31 * result + style.hashCode()
        return result
    }

    override fun toString(): String {
        return "RoundedRectangle(cornerRadii=$cornerRadii, style=$style)"
    }
}
