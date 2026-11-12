package com.mohamedrejeb.calf.foldable

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Rect

/** The direction the fold line runs in. */
@ExperimentalCalfFoldableApi
enum class FoldOrientation {

    /** The fold runs from top to bottom, with content to its left and right. */
    Vertical,

    /** The fold runs from side to side, with content above and below it. */
    Horizontal,
}

/**
 * The fold of the display the window is on.
 *
 * @property bounds the hinge or crease itself, in pixels, in the window's coordinate space. A
 *   seamless fold has no thickness, so the rectangle is zero wide (or zero high).
 * @property orientation the direction the fold line runs in.
 * @property isSeparating whether the fold splits the content into two logical areas: the device is
 *   half open, or the hinge hides part of the window.
 */
@ExperimentalCalfFoldableApi
@Immutable
data class FoldInfo(
    val bounds: Rect,
    val orientation: FoldOrientation,
    val isSeparating: Boolean,
)

/** Distances in pixels, measured inwards from the edges of a rectangle. */
@ExperimentalCalfFoldableApi
@Immutable
data class FoldMargins(
    val left: Float = 0f,
    val top: Float = 0f,
    val right: Float = 0f,
    val bottom: Float = 0f,
) {
    companion object {
        val Zero: FoldMargins = FoldMargins()
    }
}

/**
 * A part of the window that iOS reserves: the fold, or something that covers the display such as
 * a camera.
 *
 * @property bounds the reserved area **including** [margins], in pixels, in the window's
 *   coordinate space. Keep interactive content out of it while [isActive].
 * @property margins how much of [bounds] is breathing room around the reserved area itself.
 * @property isActive whether iOS currently asks content to stay out. The fold's region is inactive
 *   while the device is flat but keeps its bounds.
 */
@ExperimentalCalfFoldableApi
@Immutable
data class FoldRegion(
    val bounds: Rect,
    val margins: FoldMargins = FoldMargins.Zero,
    val isActive: Boolean = true,
) {
    /** [bounds] without [margins]: the fold line or the covered pixels themselves. */
    val reservedBounds: Rect
        get() = Rect(
            left = bounds.left + margins.left,
            top = bounds.top + margins.top,
            right = bounds.right - margins.right,
            bottom = bounds.bottom - margins.bottom,
        )
}
