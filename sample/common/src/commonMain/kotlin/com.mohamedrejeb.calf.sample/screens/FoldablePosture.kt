package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.core.CalfIosOnlyApi
import com.mohamedrejeb.calf.foldable.ExperimentalCalfFoldableApi
import com.mohamedrejeb.calf.foldable.FoldOrientation
import com.mohamedrejeb.calf.foldable.FoldPanes
import com.mohamedrejeb.calf.foldable.FoldPosture
import com.mohamedrejeb.calf.foldable.FoldState
import com.mohamedrejeb.calf.sample.components.CodePill
import com.mohamedrejeb.calf.sample.components.DemoCard
import com.mohamedrejeb.calf.sample.navigation.Screen
import com.mohamedrejeb.calf.sample.ui.theme.Spacing
import com.mohamedrejeb.calf.ui.button.AdaptiveIconButton
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val HingeDiagramSize = 120.dp
private val HingePanelStroke = 6.dp
private val HingeArcStroke = 2.dp
private const val FLAT_DEGREES = 180f
private const val HINGE_ARC_ALPHA = 0.35f
private const val PANEL_LENGTH_FRACTION = 0.42f

/** The title row: a close button (the demo is full screen, with no bar) and the demo's name. */
@Composable
internal fun FoldableTitleRow(onClose: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
        modifier = modifier.fillMaxWidth(),
    ) {
        AdaptiveIconButton(onClick = onClose) {
            Icon(Icons.Filled.Close, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
        }
        Text(
            text = Screen.Foldable.title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

/**
 * The posture in large type, the hinge drawn at its live angle, and what the layout makes of it.
 * The hinge angle follows the hinge frame by frame, so it is collected here and nowhere else.
 */
@OptIn(ExperimentalCalfFoldableApi::class, CalfIosOnlyApi::class, ExperimentalLayoutApi::class)
@Composable
internal fun PostureCard(foldState: FoldState, panes: FoldPanes, modifier: Modifier = Modifier) {
    val angle by foldState.iosHingeAngle.collectAsState(initial = null)

    DemoCard(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        HingeDiagram(
            angle = angle ?: foldState.posture.nominalAngle(),
            showOpening = foldState.posture != FoldPosture.NoFold,
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = foldState.posture.label,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = angle?.let { "Hinge at ${it.toInt()}°" } ?: foldState.posture.explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.s, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
            modifier = Modifier.fillMaxWidth(),
        ) {
            CodePill(if (panes.count == 2) "2 panes" else "1 pane")
            foldState.fold?.let { fold ->
                CodePill(if (fold.orientation == FoldOrientation.Vertical) "vertical fold" else "horizontal fold")
                CodePill(if (fold.isSeparating) "separating" else "not separating")
            }
        }
    }
}

/**
 * Two panels meeting at a hinge, opened by [angle] degrees (0 shut, 180 flat), like looking at
 * the device from its side. Where there is no live angle, the posture's usual angle is drawn;
 * without a fold there is no opening to mark.
 */
@Composable
private fun HingeDiagram(angle: Float, showOpening: Boolean, modifier: Modifier = Modifier) {
    val panelColor = MaterialTheme.colorScheme.primary
    val arcColor = MaterialTheme.colorScheme.tertiary

    Box(contentAlignment = Alignment.Center, modifier = modifier.size(HingeDiagramSize)) {
        Canvas(modifier = Modifier.size(HingeDiagramSize)) {
            val hinge = Offset(size.width / 2f, size.height * 2f / 3f)
            val length = size.width * PANEL_LENGTH_FRACTION
            if (showOpening) drawOpeningArc(hinge, length / 2f, angle, arcColor.copy(alpha = HINGE_ARC_ALPHA))
            // The fixed half lies to the right; the moving half turns anticlockwise from it.
            drawPanel(hinge, Offset(hinge.x + length, hinge.y), panelColor)
            val radians = angle * PI / FLAT_DEGREES
            val tip = Offset(
                x = hinge.x + (length * cos(radians)).toFloat(),
                y = hinge.y - (length * sin(radians)).toFloat(),
            )
            drawPanel(hinge, tip, panelColor)
        }
    }
}

private fun DrawScope.drawPanel(from: Offset, to: Offset, color: Color) {
    drawLine(color, from, to, strokeWidth = HingePanelStroke.toPx(), cap = StrokeCap.Round)
}

private fun DrawScope.drawOpeningArc(hinge: Offset, radius: Float, angle: Float, color: Color) {
    drawArc(
        color = color,
        startAngle = -angle,
        sweepAngle = angle,
        useCenter = false,
        topLeft = Offset(hinge.x - radius, hinge.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(HingeArcStroke.toPx()),
    )
}

@OptIn(ExperimentalCalfFoldableApi::class)
private val FoldPosture.label: String
    get() = when (this) {
        FoldPosture.NoFold -> "No fold"
        FoldPosture.Closed -> "Closed"
        FoldPosture.HalfOpen -> "Half open"
        FoldPosture.Flat -> "Flat"
    }

@OptIn(ExperimentalCalfFoldableApi::class)
private val FoldPosture.explanation: String
    get() = when (this) {
        FoldPosture.NoFold -> "This window is not on a folding display."
        FoldPosture.Closed -> "Folded shut, running on the cover display."
        FoldPosture.HalfOpen -> "Partly open, like a book or a laptop."
        FoldPosture.Flat -> "Opened all the way."
    }

/** The angle drawn when the platform gives no live reading (Android, desktop, web). */
@OptIn(ExperimentalCalfFoldableApi::class)
private fun FoldPosture.nominalAngle(): Float =
    when (this) {
        FoldPosture.Closed -> CLOSED_DEGREES
        FoldPosture.HalfOpen -> HALF_OPEN_DEGREES
        FoldPosture.NoFold, FoldPosture.Flat -> FLAT_DEGREES
    }

private const val CLOSED_DEGREES = 8f
private const val HALF_OPEN_DEGREES = 110f
