package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.core.CalfIosOnlyApi
import com.mohamedrejeb.calf.foldable.ExperimentalCalfFoldableApi
import com.mohamedrejeb.calf.foldable.FoldState

private val HatchSpacing = 10.dp
private val HatchStroke = 1.dp
private val PaneOutlineInset = 6.dp
private val PaneOutlineStroke = 1.5.dp
private val PaneCorner = 20.dp
private val DashLength = 6.dp
private val FoldLineWidth = 2.dp
private val RegionStroke = 2.dp
private val LabelMargin = 4.dp
private const val UNSAFE_FILL_ALPHA = 0.10f
private const val HATCH_ALPHA = 0.30f
private const val PANE_OUTLINE_ALPHA = 0.45f
private const val FOLD_FILL_ALPHA = 0.30f
private const val REGION_FILL_ALPHA = 0.35f

/** The colours of the drawing, also used by the legend so the two always match. */
internal data class GeometryColors(
    val unsafe: Color,
    val pane: Color,
    val fold: Color,
    val camera: Color,
)

@Composable
internal fun geometryColors(): GeometryColors =
    GeometryColors(
        unsafe = MaterialTheme.colorScheme.tertiary,
        pane = MaterialTheme.colorScheme.outline,
        fold = MaterialTheme.colorScheme.primary,
        camera = MaterialTheme.colorScheme.error,
    )

/**
 * Drawn behind the content: the unsafe edges of the window, hatched and labelled with their size,
 * and the outline of the safe part of each pane.
 */
@Composable
internal fun WindowGeometryBackdrop(
    safeInsets: EdgeInsets,
    slots: List<PaneSlot>,
    colors: GeometryColors,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = colors.unsafe)

    Canvas(modifier = modifier.fillMaxSize()) {
        unsafeBands(safeInsets).forEach { band ->
            drawRect(colors.unsafe.copy(alpha = UNSAFE_FILL_ALPHA), band.rect.topLeft, band.rect.size)
            drawHatch(band.rect, colors.unsafe.copy(alpha = HATCH_ALPHA))
            drawBandLabel(textMeasurer, band, labelStyle)
        }
        slots.forEach { slot -> drawPaneOutline(slot.safeBounds, colors.pane.copy(alpha = PANE_OUTLINE_ALPHA)) }
    }
}

/**
 * Drawn over the content, so it is visible wherever it is: the fold, and on iPhone Duo the areas
 * the camera and sensors cover. Both come in the window's space and are moved by [screenOrigin].
 */
@OptIn(ExperimentalCalfFoldableApi::class, CalfIosOnlyApi::class)
@Composable
internal fun FoldAndCameraOverlay(
    foldState: FoldState,
    screenOrigin: Offset,
    colors: GeometryColors,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val foldBounds = foldState.iosFoldRegion?.bounds ?: foldState.fold?.bounds
        foldBounds?.let { drawFold(it.translate(-screenOrigin), colors.fold) }
        foldState.iosOcclusionRegions.forEach { region ->
            drawRegion(region.bounds.translate(-screenOrigin), colors.camera)
        }
    }
}

private data class UnsafeBand(val rect: Rect, val sizePx: Float, val isVertical: Boolean)

private fun DrawScope.unsafeBands(insets: EdgeInsets): List<UnsafeBand> =
    listOf(
        UnsafeBand(Rect(0f, 0f, size.width, insets.top), insets.top, isVertical = false),
        UnsafeBand(Rect(0f, size.height - insets.bottom, size.width, size.height), insets.bottom, isVertical = false),
        UnsafeBand(Rect(0f, insets.top, insets.left, size.height - insets.bottom), insets.left, isVertical = true),
        UnsafeBand(
            Rect(size.width - insets.right, insets.top, size.width, size.height - insets.bottom),
            insets.right,
            isVertical = true,
        ),
    ).filter { it.sizePx > 0f }

private fun DrawScope.drawHatch(rect: Rect, color: Color) {
    val spacing = HatchSpacing.toPx()
    clipRect(rect.left, rect.top, rect.right, rect.bottom) {
        var x = rect.left - rect.height
        while (x < rect.right) {
            drawLine(color, Offset(x, rect.bottom), Offset(x + rect.height, rect.top), HatchStroke.toPx())
            x += spacing
        }
    }
}

/**
 * "59 dp" inside a band, when the band is thick enough to hold it. Along a horizontal band it sits
 * a quarter of the way in, clear of a vertical fold, which usually runs down the middle.
 */
private fun DrawScope.drawBandLabel(textMeasurer: TextMeasurer, band: UnsafeBand, style: TextStyle) {
    val label = textMeasurer.measure("${band.sizePx.toDp().value.toInt()} dp", style)
    val thickness = if (band.isVertical) band.rect.width else band.rect.height
    val labelThickness = if (band.isVertical) label.size.width else label.size.height
    if (labelThickness + LabelMargin.toPx() * 2 > thickness) return
    drawText(
        textLayoutResult = label,
        topLeft = Offset(
            x = if (band.isVertical) band.rect.center.x - label.size.width / 2f else band.rect.left + band.rect.width / 4f,
            y = band.rect.center.y - label.size.height / 2f,
        ),
    )
}

private fun DrawScope.drawPaneOutline(bounds: Rect, color: Color) {
    val inset = PaneOutlineInset.toPx()
    val outline = bounds.deflate(inset)
    if (outline.width <= 0f || outline.height <= 0f) return
    val dash = DashLength.toPx()
    drawRoundRect(
        color = color,
        topLeft = outline.topLeft,
        size = outline.size,
        cornerRadius = CornerRadius(PaneCorner.toPx()),
        style = Stroke(width = PaneOutlineStroke.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash))),
    )
}

/** The fold, widened to a visible line when it is seamless, with its margins shaded around it. */
private fun DrawScope.drawFold(bounds: Rect, color: Color) {
    val lineWidth = FoldLineWidth.toPx()
    drawRect(color.copy(alpha = FOLD_FILL_ALPHA), bounds.topLeft, bounds.size)
    val isVertical = bounds.height >= bounds.width
    if (isVertical) {
        drawLine(color, Offset(bounds.center.x, bounds.top), Offset(bounds.center.x, bounds.bottom), lineWidth)
    } else {
        drawLine(color, Offset(bounds.left, bounds.center.y), Offset(bounds.right, bounds.center.y), lineWidth)
    }
}

private fun DrawScope.drawRegion(bounds: Rect, color: Color) {
    val corner = CornerRadius(RegionStroke.toPx() * 2)
    drawRoundRect(color.copy(alpha = REGION_FILL_ALPHA), bounds.topLeft, bounds.size, corner)
    drawRoundRect(color, bounds.topLeft, bounds.size, corner, style = Stroke(RegionStroke.toPx()))
}
