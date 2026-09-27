package com.mohamedrejeb.calf.sample.screens

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.mohamedrejeb.calf.foldable.ExperimentalCalfFoldableApi
import com.mohamedrejeb.calf.foldable.FoldPanes

/** Insets in pixels, one value per edge. */
internal data class EdgeInsets(
    val left: Float = 0f,
    val top: Float = 0f,
    val right: Float = 0f,
    val bottom: Float = 0f,
) {
    companion object {
        val Zero = EdgeInsets()
    }
}

/**
 * Where one pane's content goes, in the screen's own coordinates (pixels).
 *
 * @property bounds the pane.
 * @property padding the part of the pane that is outside the safe area, per edge: the content is
 *   inset by it so nothing sits under the status bar, the home indicator or the Duo's strip.
 */
internal data class PaneSlot(
    val bounds: Rect,
    val padding: EdgeInsets,
) {
    /** The part of the pane content can use. */
    val safeBounds: Rect
        get() = Rect(
            left = bounds.left + padding.left,
            top = bounds.top + padding.top,
            right = bounds.right - padding.right,
            bottom = bounds.bottom - padding.bottom,
        )
}

/**
 * Turns [panes] (window coordinates) into the slots the screen lays its content out in.
 *
 * @param screenOrigin where the screen starts in the window: the fold is reported in the window's
 *   space, the screen draws in its own.
 * @param screenSize the screen's size.
 * @param safeInsets the window's unsafe edges, as `WindowInsets.safeDrawing` reports them.
 */
@OptIn(ExperimentalCalfFoldableApi::class)
internal fun paneSlots(
    panes: FoldPanes,
    screenOrigin: Offset,
    screenSize: Rect,
    safeInsets: EdgeInsets,
): List<PaneSlot> {
    val safe = Rect(
        left = screenSize.left + safeInsets.left,
        top = screenSize.top + safeInsets.top,
        right = screenSize.right - safeInsets.right,
        bottom = screenSize.bottom - safeInsets.bottom,
    )
    return listOfNotNull(panes.first, panes.second).map { windowPane ->
        val pane = windowPane.translate(-screenOrigin).intersect(screenSize)
        PaneSlot(
            bounds = pane,
            padding = EdgeInsets(
                left = (safe.left - pane.left).coerceAtLeast(0f),
                top = (safe.top - pane.top).coerceAtLeast(0f),
                right = (pane.right - safe.right).coerceAtLeast(0f),
                bottom = (pane.bottom - safe.bottom).coerceAtLeast(0f),
            ),
        )
    }
}
