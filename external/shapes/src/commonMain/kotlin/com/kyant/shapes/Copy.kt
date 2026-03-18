package com.kyant.shapes

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.util.fastCoerceIn

@Stable
fun RoundedRectangularShape.copy(
    cornerRadius: Dp,
    style: RoundedCornerStyle = this.style ?: RoundedCornerStyle.Continuous
) =
    RoundedRectangle(
        cornerRadius = cornerRadius,
        style = style
    )

@Stable
fun RoundedRectangularShape.copy(
    cornerRadii: RectangleCornerRadii,
    style: RoundedCornerStyle = this.style ?: RoundedCornerStyle.Continuous
) =
    UnevenRoundedRectangle(
        cornerRadii = cornerRadii,
        style = style
    )

@Stable
fun RoundedRectangularShape.copy(
    topStart: Dp = Dp.Unspecified,
    topEnd: Dp = Dp.Unspecified,
    bottomEnd: Dp = Dp.Unspecified,
    bottomStart: Dp = Dp.Unspecified,
    style: RoundedCornerStyle = this.style ?: RoundedCornerStyle.Continuous
): RoundedRectangularShape =
    CopyRoundedRectangle(
        shape = this,
        topStart = topStart,
        topEnd = topEnd,
        bottomEnd = bottomEnd,
        bottomStart = bottomStart,
        style = style
    )

@Immutable
private data class CopyRoundedRectangle(
    val shape: RoundedRectangularShape,
    val topStart: Dp,
    val topEnd: Dp,
    val bottomEnd: Dp,
    val bottomStart: Dp,
    override val style: RoundedCornerStyle
) : RoundedRectangularShape {

    override fun corners(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): RoundedRectangularShape.Corners {
        val corners = shape.corners(size, layoutDirection, density)
        val maxRadius = size.minDimension * 0.5f

        var topLeft = corners.topLeft
        var topRight = corners.topRight
        var bottomRight = corners.bottomRight
        var bottomLeft = corners.bottomLeft
        with(density) {
            when (layoutDirection) {
                LayoutDirection.Ltr -> {
                    if (topStart.isSpecified) topLeft = topStart.toPx().fastCoerceIn(0f, maxRadius)
                    if (topEnd.isSpecified) topRight = topEnd.toPx().fastCoerceIn(0f, maxRadius)
                    if (bottomEnd.isSpecified) bottomRight = bottomEnd.toPx().fastCoerceIn(0f, maxRadius)
                    if (bottomStart.isSpecified) bottomLeft = bottomStart.toPx().fastCoerceIn(0f, maxRadius)
                }

                LayoutDirection.Rtl -> {
                    if (topStart.isSpecified) topRight = topStart.toPx().fastCoerceIn(0f, maxRadius)
                    if (topEnd.isSpecified) topLeft = topEnd.toPx().fastCoerceIn(0f, maxRadius)
                    if (bottomEnd.isSpecified) bottomLeft = bottomEnd.toPx().fastCoerceIn(0f, maxRadius)
                    if (bottomStart.isSpecified) bottomRight = bottomStart.toPx().fastCoerceIn(0f, maxRadius)
                }
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
        CopyRoundedRectangle(
            shape = shape,
            topStart = topStart,
            topEnd = topEnd,
            bottomEnd = bottomEnd,
            bottomStart = bottomStart,
            style = style
        )
}
