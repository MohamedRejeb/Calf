package com.mohamedrejeb.calf.ui.progress

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

/**
 * @param modifier indicator modifier
 * @param color color of the indicator
 * */
@Composable
fun CupertinoActivityIndicator(
    color: Color = Color.Gray,
    modifier: Modifier = Modifier,
) {
    // number of paths of the activity indicator
    val pathCount = 8
    // duration of a single animation cycle
    val durationMillis = 1000
    val minAlpha = .1f
    val animatedPathCount = (pathCount / 2).coerceIn(1, pathCount)

    val coefficient = 360f / pathCount

    val infiniteTransition = rememberInfiniteTransition()
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = pathCount.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
    )

    Canvas(
        modifier = modifier
            .progressSemantics()
            .size(ActivityIndicatorDiameter),
    ) {
        var canvasWidth = size.width
        var canvasHeight = size.height

        if (canvasHeight < canvasWidth) {
            canvasWidth = canvasHeight
        } else {
            canvasHeight = canvasWidth
        }

        val itemWidth = canvasWidth / 3f
        val itemHeight = canvasHeight / pathCount

        val cornerRadius = itemWidth.coerceAtMost(itemHeight) / 2

        val horizontalOffset = (size.width - size.height).coerceAtLeast(0f) / 2
        val verticalOffset = (size.height - size.width).coerceAtLeast(0f) / 2

        val topLeftOffset = Offset(
            x = horizontalOffset + canvasWidth - itemWidth,
            y = verticalOffset + (canvasHeight - itemHeight) / 2,
        )

        val size = Size(itemWidth, itemHeight)

        for (i in 0..360 step 360 / pathCount) {
            rotate(i.toFloat()) {
                drawRoundRect(
                    color = color.copy(alpha = minAlpha.coerceIn(0f,1f)),
                    topLeft = topLeftOffset,
                    size = size,
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                )
            }
        }

        for (i in 0..animatedPathCount) {
            rotate((angle.toInt() + i) * coefficient) {
                drawRoundRect(
                    color = color.copy(
                        alpha = (1f / (pathCount / 2) * i).coerceIn(0f, 1f),
                    ),
                    topLeft = topLeftOffset,
                    size = size,
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                )
            }
        }
    }
}

internal val ActivityIndicatorDiameter =
    ActivityIndicatorTokens.Size - ActivityIndicatorTokens.ActiveIndicatorWidth * 2

internal object ActivityIndicatorTokens {
    val ActiveIndicatorWidth = 4.0.dp
    val Size = 38.0.dp
}
