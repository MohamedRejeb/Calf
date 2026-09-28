@file:OptIn(ExperimentalForeignApi::class)

package com.mohamedrejeb.calf.ui.datepicker

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UILayoutPriorityFittingSizeLevel
import platform.UIKit.UILayoutPriorityRequired
import platform.UIKit.UIView
import kotlin.math.roundToInt

/**
 * Sizes an embedded UIKit [view] the way UIKit would: the width Compose gives it, and the height
 * the view needs at that width. A calendar does not grow taller when it gets wider (UIKit keeps an
 * inline calendar about 450pt tall on an iPhone and 470pt in regular width, from about 400pt wide
 * on); scaling its height with its width spread the day rows apart on a wide screen (an iPad, an
 * open iPhone Duo: 644pt instead of 490pt).
 *
 * Without a bounded width the view gets its own preferred width.
 */
internal fun Modifier.uikitFittingHeight(view: UIView): Modifier {
    val fitting = FittingHeightCache(view)
    return layout { measurable, constraints ->
        val width = if (constraints.hasBoundedWidth)
            constraints.maxWidth
        else
            (view.preferredWidthPoints() * density).roundToInt().coerceAtLeast(constraints.minWidth)
        val height = (fitting.heightPoints(width / density) * density).roundToInt()
            .coerceIn(constraints.minHeight, constraints.maxHeight)
        val placeable = measurable.measure(Constraints.fixed(width, height))
        layout(width, height) { placeable.place(0, 0) }
    }
}

/** The fitting height of the last width asked for; UIKit's fitting pass is not free. */
private class FittingHeightCache(private val view: UIView) {
    private var width = Double.NaN
    private var height = 0.0

    fun heightPoints(widthPoints: Float): Double {
        val requested = widthPoints.toDouble()
        if (requested != width) {
            width = requested
            height = view.systemLayoutSizeFittingSize(
                // UILayoutFittingCompressedSize's height: the smallest height the view accepts.
                CGSizeMake(requested, 0.0),
                withHorizontalFittingPriority = UILayoutPriorityRequired,
                verticalFittingPriority = UILayoutPriorityFittingSizeLevel,
            ).useContents { height }
        }
        return height
    }
}

private fun UIView.preferredWidthPoints(): Double =
    sizeThatFits(CGSizeMake(Double.MAX_VALUE, Double.MAX_VALUE)).useContents { width }
