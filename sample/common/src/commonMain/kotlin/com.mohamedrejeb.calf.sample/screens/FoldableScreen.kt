package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.mohamedrejeb.calf.core.CalfIosOnlyApi
import com.mohamedrejeb.calf.foldable.ExperimentalCalfFoldableApi
import com.mohamedrejeb.calf.foldable.FoldPanes
import com.mohamedrejeb.calf.foldable.FoldRegion
import com.mohamedrejeb.calf.foldable.FoldState
import com.mohamedrejeb.calf.foldable.rememberFoldState
import com.mohamedrejeb.calf.sample.components.SampleScreenScaffold
import com.mohamedrejeb.calf.sample.components.tabletopPanes
import kotlinx.coroutines.launch

private val RegionOutlineWidth = 2.dp
private const val REGION_FILL_ALPHA = 0.25f

/**
 * Everything here uses calf-foldable's public API only: the live values, the fold and camera
 * regions drawn where they are, and a tabletop layout (content above the fold, controls below)
 * built from [FoldState.calculatePanes].
 */
@OptIn(ExperimentalCalfFoldableApi::class)
@Composable
fun FoldableScreen(navigateBack: () -> Unit) {
    val foldState = rememberFoldState()
    // The fold's bounds are in the window's space; both the layout and the overlay shift them by this.
    var positionInRoot by remember { mutableStateOf<Offset?>(null) }

    SampleScreenScaffold(title = "Foldable", navigateBack = navigateBack) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onPlaced { positionInRoot = it.positionInRoot() },
        ) {
            FoldableContent(foldState = foldState, contentPadding = padding, positionInRoot = positionInRoot)
            FoldRegionsOverlay(foldState = foldState, positionInRoot = positionInRoot)
        }
    }
}

/**
 * Half open with the fold running across the screen: the values above the fold, the controls
 * below it. Any other pose, and the first frame before the screen is placed: one scrolling column
 * with the controls at the end.
 */
@OptIn(ExperimentalCalfFoldableApi::class)
@Composable
private fun FoldableContent(foldState: FoldState, contentPadding: PaddingValues, positionInRoot: Offset?) {
    val windowSize = LocalWindowInfo.current.containerSize.toSize()
    val panes = foldState.calculatePanes(windowSize)
    val tabletop = positionInRoot?.let { tabletopPanes(foldState.posture, panes) }
    var showDebugDump by rememberSaveable { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (tabletop == null || positionInRoot == null) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(contentPadding)
                    .padding(16.dp),
            ) {
                FoldValues(foldState = foldState, panes = panes, showDebugDump = showDebugDump)
                TabletopControls(
                    foldState = foldState,
                    showDebugDump = showDebugDump,
                    onShowDebugDumpChange = { showDebugDump = it },
                    onScrollToTop = { scrollState.animateScrollTo(0) },
                    hint = "Half open the device with the fold running across the screen for the tabletop layout.",
                )
            }
        } else {
            TabletopLayout(
                // The panes are in the window's space; this layout starts at positionInRoot.
                gap = requireNotNull(tabletop.gap).translate(-positionInRoot),
                contentPadding = contentPadding,
                above = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(16.dp),
                    ) {
                        FoldValues(foldState = foldState, panes = panes, showDebugDump = showDebugDump)
                    }
                },
                below = {
                    TabletopControls(
                        foldState = foldState,
                        showDebugDump = showDebugDump,
                        onShowDebugDumpChange = { showDebugDump = it },
                        onScrollToTop = { scrollState.animateScrollTo(0) },
                        hint = "Tabletop: the values stay above the fold, these controls below it.",
                        modifier = Modifier.padding(16.dp),
                    )
                },
            )
        }
    }
}

/** Splits the screen at [gap] (in this layout's coordinates): [above] the fold, [below] it. */
@Composable
private fun TabletopLayout(
    gap: Rect,
    contentPadding: PaddingValues,
    above: @Composable () -> Unit,
    below: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val aboveHeight = with(density) { gap.top.toDp() } - contentPadding.calculateTopPadding()
    val gapHeight = with(density) { gap.height.toDp() }

    Column(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
        Box(modifier = Modifier.fillMaxWidth().height(aboveHeight.coerceAtLeast(0.dp))) { above() }
        Spacer(modifier = Modifier.height(gapHeight))
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) { below() }
    }
}

@OptIn(ExperimentalCalfFoldableApi::class, CalfIosOnlyApi::class)
@Composable
private fun FoldValues(foldState: FoldState, panes: FoldPanes, showDebugDump: Boolean) {
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
        title = "iOS only (empty values elsewhere)",
        lines = listOf(
            "size classes" to "${foldState.iosHorizontalSizeClass} x ${foldState.iosVerticalSizeClass}",
            "fold region" to (foldState.iosFoldRegion?.describe() ?: "none"),
            "occlusion regions" to foldState.iosOcclusionRegions.describe(),
            "hinge API" to foldState.iosCapabilities.isHingeApiAvailable.toString(),
            "regions API" to foldState.iosCapabilities.isRegionsApiAvailable.toString(),
            "has hinge" to (foldState.iosCapabilities.hasHinge?.toString() ?: "unknown"),
        ),
    )
    if (showDebugDump) DebugDumpCard(foldState)
}

/**
 * What sits below the fold in tabletop. The hinge angle is the only value that follows the
 * hinge frame by frame, so it is read here, as a Flow, and not with the other values.
 */
@OptIn(ExperimentalCalfFoldableApi::class, CalfIosOnlyApi::class)
@Composable
private fun TabletopControls(
    foldState: FoldState,
    showDebugDump: Boolean,
    onShowDebugDumpChange: (Boolean) -> Unit,
    onScrollToTop: suspend () -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
) {
    val angle by foldState.iosHingeAngle.collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Controls", style = MaterialTheme.typography.titleMedium)
            Text(hint, style = MaterialTheme.typography.bodySmall)
            Text("hinge angle (iOS only, a Flow): ${angle?.let { "${it.toInt()}°" } ?: "no reading"}")
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Switch(checked = showDebugDump, onCheckedChange = onShowDebugDumpChange)
                Text("Show the iOS debug dump")
            }
            Button(onClick = { scope.launch { onScrollToTop() } }) { Text("Scroll the values to the top") }
        }
    }
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
 * window's coordinate space, so they are shifted by the screen's [positionInRoot]; nothing is
 * drawn until the screen has been placed.
 */
@OptIn(ExperimentalCalfFoldableApi::class, CalfIosOnlyApi::class)
@Composable
private fun FoldRegionsOverlay(foldState: FoldState, positionInRoot: Offset?) {
    val foldColor = MaterialTheme.colorScheme.primary
    val occlusionColor = MaterialTheme.colorScheme.error

    Canvas(modifier = Modifier.fillMaxSize()) {
        positionInRoot ?: return@Canvas
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
