package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import com.mohamedrejeb.calf.foldable.ExperimentalCalfFoldableApi
import com.mohamedrejeb.calf.foldable.FoldPanes
import com.mohamedrejeb.calf.foldable.FoldState
import com.mohamedrejeb.calf.foldable.rememberFoldState
import com.mohamedrejeb.calf.sample.components.DemoPage
import kotlin.math.roundToInt

/**
 * The fold, drawn where it really is. The demo owns the whole window, edge to edge: the unsafe
 * edges are hatched, the fold and (on iPhone Duo) the camera area are drawn over everything, and the
 * content is laid out in the panes `calculatePanes` returns, each inset by its own share of the
 * safe area. Half open with the fold across (tabletop), the posture stays above the fold and the
 * details go below it; opened like a book, they sit side by side.
 *
 * Everything here uses calf-foldable's public API only.
 */
@OptIn(ExperimentalCalfFoldableApi::class)
@Composable
fun FoldableScreen(navigateBack: () -> Unit) {
    val foldState = rememberFoldState()
    val panes = foldState.calculatePanes(LocalWindowInfo.current.containerSize.toSize())
    val safeInsets = safeDrawingInsets()
    val colors = geometryColors()
    // The fold is reported in the window's space; the screen draws in its own. Null until placed,
    // so nothing is drawn at a wrong position on the first frame.
    var screenOrigin by remember { mutableStateOf<Offset?>(null) }
    var screenSize by remember { mutableStateOf(IntSize.Zero) }
    var showDebugDump by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .onPlaced {
                screenOrigin = it.positionInRoot()
                screenSize = it.size
            },
    ) {
        val origin = screenOrigin ?: return@Box
        if (screenSize == IntSize.Zero) return@Box
        val slots = paneSlots(panes, origin, Rect(Offset.Zero, screenSize.toSize()), safeInsets)

        WindowGeometryBackdrop(safeInsets = safeInsets, slots = slots, colors = colors)
        slots.forEachIndexed { index, slot ->
            PaneContent(slot) {
                FoldablePane(
                    index = index,
                    paneCount = slots.size,
                    foldState = foldState,
                    panes = panes,
                    windowSize = Rect(Offset.Zero, screenSize.toSize()),
                    safeInsets = safeInsets,
                    showDebugDump = showDebugDump,
                    onShowDebugDumpChange = { showDebugDump = it },
                    onClose = navigateBack,
                )
            }
        }
        FoldAndCameraOverlay(foldState = foldState, screenOrigin = origin, colors = colors)
    }
}

/** One pane: the posture in the first, the details in the second, or both in a single pane. */
@OptIn(ExperimentalCalfFoldableApi::class)
@Composable
private fun ColumnScope.FoldablePane(
    index: Int,
    paneCount: Int,
    foldState: FoldState,
    panes: FoldPanes,
    windowSize: Rect,
    safeInsets: EdgeInsets,
    showDebugDump: Boolean,
    onShowDebugDumpChange: (Boolean) -> Unit,
    onClose: () -> Unit,
) {
    val showsPosture = index == 0
    val showsDetails = index == 1 || paneCount == 1

    if (showsPosture) {
        FoldableTitleRow(onClose = onClose)
        PostureCard(foldState = foldState, panes = panes)
        LegendSection()
    }
    if (showsDetails) {
        FoldableDetails(
            foldState = foldState,
            panes = panes,
            windowSizePx = windowSize,
            safeInsets = safeInsets,
            showDebugDump = showDebugDump,
            onShowDebugDumpChange = onShowDebugDumpChange,
        )
    }
}

/**
 * A scrolling page placed on the safe part of [slot] and clipped to it, so content never scrolls
 * into the hatched unsafe edges the screen is showing.
 */
@Composable
private fun PaneContent(slot: PaneSlot, content: @Composable ColumnScope.() -> Unit) {
    val bounds = slot.safeBounds
    if (bounds.width <= 0f || bounds.height <= 0f) return
    with(LocalDensity.current) {
        Box(
            modifier = Modifier
                .offset { IntOffset(bounds.left.roundToInt(), bounds.top.roundToInt()) }
                .size(bounds.width.toDp(), bounds.height.toDp())
                .clipToBounds(),
        ) {
            DemoPage(
                contentPadding = PaddingValues(),
                containerColor = Color.Transparent,
                content = content,
            )
        }
    }
}

/** `WindowInsets.safeDrawing` in pixels, left and right as on screen. */
@Composable
private fun safeDrawingInsets(): EdgeInsets {
    val insets = WindowInsets.safeDrawing
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    return EdgeInsets(
        left = insets.getLeft(density, layoutDirection).toFloat(),
        top = insets.getTop(density).toFloat(),
        right = insets.getRight(density, layoutDirection).toFloat(),
        bottom = insets.getBottom(density).toFloat(),
    )
}
