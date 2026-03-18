package com.kyant.shapes

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.lerp

@Immutable
data class RectangleCornerRadii(
    val topStart: Dp,
    val topEnd: Dp,
    val bottomEnd: Dp,
    val bottomStart: Dp
)

@Stable
fun lerp(
    start: RectangleCornerRadii,
    stop: RectangleCornerRadii,
    fraction: Float
): RectangleCornerRadii {
    return RectangleCornerRadii(
        topStart = lerp(start.topStart, stop.topStart, fraction),
        topEnd = lerp(start.topEnd, stop.topEnd, fraction),
        bottomEnd = lerp(start.bottomEnd, stop.bottomEnd, fraction),
        bottomStart = lerp(start.bottomStart, stop.bottomStart, fraction)
    )
}
