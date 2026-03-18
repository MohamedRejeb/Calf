package com.kyant.shapes

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

@Immutable
sealed interface RoundedRectangularShape : Shape {

    val style: RoundedCornerStyle?
        get() = null

    fun corners(size: Size, layoutDirection: LayoutDirection, density: Density): Corners

    fun copy(style: RoundedCornerStyle): RoundedRectangularShape

    data class Corners(
        val topLeft: Float,
        val topRight: Float,
        val bottomRight: Float,
        val bottomLeft: Float
    )
}
