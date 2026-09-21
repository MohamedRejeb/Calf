package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.mohamedrejeb.calf.core.CalfIosOnlyApi
import com.mohamedrejeb.calf.foldable.ExperimentalCalfFoldableApi
import com.mohamedrejeb.calf.foldable.FoldRegion
import com.mohamedrejeb.calf.foldable.FoldState
import com.mohamedrejeb.calf.foldable.rememberFoldState
import com.mohamedrejeb.calf.sample.components.SampleScreenScaffold

private val RegionOutlineWidth = 2.dp
private const val REGION_FILL_ALPHA = 0.25f

@OptIn(ExperimentalCalfFoldableApi::class)
@Composable
fun FoldableScreen(navigateBack: () -> Unit) {
    val foldState = rememberFoldState()

    SampleScreenScaffold(title = "Foldable", navigateBack = navigateBack) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            FoldValues(foldState = foldState, contentPadding = padding)
            FoldRegionsOverlay(foldState = foldState)
        }
    }
}

@OptIn(ExperimentalCalfFoldableApi::class, CalfIosOnlyApi::class)
@Composable
private fun FoldValues(foldState: FoldState, contentPadding: PaddingValues) {
    val windowSize = LocalWindowInfo.current.containerSize.toSize()
    val panes = foldState.calculatePanes(windowSize)

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(16.dp),
    ) {
        ValueCard(
            title = "Shared (Android + iOS)",
            lines = listOf(
                "posture" to foldState.posture.name,
                "fold" to (foldState.fold?.let { "${it.orientation} ${it.bounds.describe()}" } ?: "none"),
                "isSeparating" to foldState.isSeparating.toString(),
                "panes" to panes.count.toString(),
                "first pane" to panes.first.describe(),
                "second pane" to (panes.second?.describe() ?: "none"),
            ),
        )
        ValueCard(
            title = "iOS only",
            lines = listOf(
                "size classes" to "${foldState.iosHorizontalSizeClass} x ${foldState.iosVerticalSizeClass}",
                "fold region" to (foldState.iosFoldRegion?.describe() ?: "none"),
                "occlusion regions" to foldState.iosOcclusionRegions.describe(),
                "hinge API" to foldState.iosCapabilities.isHingeApiAvailable.toString(),
                "regions API" to foldState.iosCapabilities.isRegionsApiAvailable.toString(),
                "has hinge" to (foldState.iosCapabilities.hasHinge?.toString() ?: "unknown"),
            ),
        )
        HingeAngleCard(foldState)
        DebugDumpCard(foldState)
    }
}

/** The only part of the screen that follows the hinge frame by frame. */
@OptIn(ExperimentalCalfFoldableApi::class, CalfIosOnlyApi::class)
@Composable
private fun HingeAngleCard(foldState: FoldState) {
    val angle by foldState.iosHingeAngle.collectAsState(initial = null)

    ValueCard(
        title = "Hinge angle (iOS only, a Flow)",
        lines = listOf("degrees" to (angle?.let { "${it.toInt()}°" } ?: "no reading")),
    )
}

@OptIn(ExperimentalCalfFoldableApi::class, CalfIosOnlyApi::class)
@Composable
private fun DebugDumpCard(foldState: FoldState) {
    // Reading the snapshot here refreshes the dump whenever a value changes.
    val dump = remember(foldState.snapshot) { foldState.iosDebugDump() }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Debug dump", style = MaterialTheme.typography.titleMedium)
            Text(dump, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun ValueCard(title: String, lines: List<Pair<String, String>>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            lines.forEach { (label, value) ->
                Text("$label: $value", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/**
 * Draws the fold and the occlusion regions where they really are. Their bounds are in the
 * window's coordinate space, so they are shifted by this overlay's own position in the root.
 */
@OptIn(ExperimentalCalfFoldableApi::class, CalfIosOnlyApi::class)
@Composable
private fun FoldRegionsOverlay(foldState: FoldState) {
    var positionInRoot by remember { mutableStateOf(Offset.Zero) }
    val foldColor = MaterialTheme.colorScheme.primary
    val occlusionColor = MaterialTheme.colorScheme.error

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .onPlaced { positionInRoot = it.positionInRoot() },
    ) {
        val foldBounds = foldState.iosFoldRegion?.bounds ?: foldState.fold?.bounds
        foldBounds?.let { drawRegion(it.translate(-positionInRoot), foldColor) }
        foldState.iosOcclusionRegions.forEach { region ->
            drawRegion(region.bounds.translate(-positionInRoot), occlusionColor)
        }
    }
}

private fun DrawScope.drawRegion(bounds: Rect, color: Color) {
    // A seamless fold has no width: widen it to the outline so it stays visible.
    val outline = RegionOutlineWidth.toPx()
    val visibleBounds = Rect(
        left = bounds.left,
        top = bounds.top,
        right = maxOf(bounds.right, bounds.left + outline),
        bottom = maxOf(bounds.bottom, bounds.top + outline),
    )
    drawRect(color.copy(alpha = REGION_FILL_ALPHA), visibleBounds.topLeft, visibleBounds.size)
    drawRect(color, visibleBounds.topLeft, visibleBounds.size, style = Stroke(outline))
}

private fun Rect.describe(): String =
    "(${left.toInt()}, ${top.toInt()}) ${width.toInt()}x${height.toInt()} px"

@OptIn(ExperimentalCalfFoldableApi::class)
private fun FoldRegion.describe(): String =
    "${bounds.describe()}, margins l=${margins.left.toInt()} t=${margins.top.toInt()} " +
        "r=${margins.right.toInt()} b=${margins.bottom.toInt()}, ${if (isActive) "active" else "inactive"}"

@OptIn(ExperimentalCalfFoldableApi::class)
private fun List<FoldRegion>.describe(): String =
    if (isEmpty()) "none" else joinToString(separator = "\n") { it.describe() }
