package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.core.CalfIosOnlyApi
import com.mohamedrejeb.calf.foldable.ExperimentalCalfFoldableApi
import com.mohamedrejeb.calf.foldable.FoldPanes
import com.mohamedrejeb.calf.foldable.FoldRegion
import com.mohamedrejeb.calf.foldable.FoldState
import com.mohamedrejeb.calf.sample.components.CodeSnippet
import com.mohamedrejeb.calf.sample.components.DemoCard
import com.mohamedrejeb.calf.sample.components.DemoDivider
import com.mohamedrejeb.calf.sample.components.DemoListCard
import com.mohamedrejeb.calf.sample.components.DemoRow
import com.mohamedrejeb.calf.sample.components.DemoSection
import com.mohamedrejeb.calf.sample.components.ValueRow
import com.mohamedrejeb.calf.ui.toggle.AdaptiveSwitch

private val LegendSwatchSize = 16.dp

private const val USAGE = """
val foldState = rememberFoldState()
val windowSize = LocalWindowInfo.current.containerSize.toSize()
val panes = foldState.calculatePanes(windowSize)

if (panes.count == 2) {
    TwoPanes(first = panes.first, second = panes.second)
} else {
    OnePane()
}
"""

/** Everything calf-foldable reports, in the units a layout uses (dp). */
@OptIn(ExperimentalCalfFoldableApi::class)
@Composable
internal fun FoldableDetails(
    foldState: FoldState,
    panes: FoldPanes,
    windowSizePx: Rect,
    safeInsets: EdgeInsets,
    showDebugDump: Boolean,
    onShowDebugDumpChange: (Boolean) -> Unit,
) {
    val density = LocalDensity.current
    DemoSection(title = "Fold") {
        DemoCard {
            val fold = foldState.fold
            if (fold == null) {
                ValueRow(label = "fold", value = "none")
            } else {
                ValueRow(label = "orientation", value = fold.orientation.name)
                ValueRow(label = "bounds", value = fold.bounds.describe(density))
                ValueRow(label = "isSeparating", value = fold.isSeparating.toString())
            }
        }
    }
    DemoSection(title = "Panes", footer = "From foldState.calculatePanes(windowSize). The dashed outlines are these rectangles.") {
        DemoCard {
            ValueRow(label = "count", value = panes.count.toString())
            ValueRow(label = "first", value = panes.first.describe(density))
            ValueRow(label = "second", value = panes.second?.describe(density) ?: "none")
            ValueRow(label = "gap", value = panes.gap?.describe(density) ?: "none")
        }
    }
    DemoSection(title = "Window", footer = "The hatched edges are WindowInsets.safeDrawing: status bar, home indicator, and on iPhone Duo the strip beside the fold.") {
        DemoCard {
            ValueRow(label = "size", value = windowSizePx.describeSize(density))
            ValueRow(label = "unsafe edges", value = safeInsets.describe(density))
        }
    }
    IosFoldSection(foldState, density, showDebugDump, onShowDebugDumpChange)
    LegendSection()
    DemoSection(title = "Usage") { CodeSnippet(USAGE.trimIndent()) }
}

@OptIn(ExperimentalCalfFoldableApi::class, CalfIosOnlyApi::class)
@Composable
private fun IosFoldSection(
    foldState: FoldState,
    density: Density,
    showDebugDump: Boolean,
    onShowDebugDumpChange: (Boolean) -> Unit,
) {
    val capabilities = foldState.iosCapabilities
    DemoSection(title = "iPhone Duo", footer = "iOS only (@CalfIosOnlyApi). Other platforms report the empty values.") {
        DemoCard {
            ValueRow(label = "size classes", value = "${foldState.iosHorizontalSizeClass} × ${foldState.iosVerticalSizeClass}")
            ValueRow(label = "fold region", value = foldState.iosFoldRegion?.describe(density) ?: "none")
            foldState.iosFoldRegion?.margins?.let { margins ->
                ValueRow(
                    label = "fold margins",
                    value = EdgeInsets(margins.left, margins.top, margins.right, margins.bottom).describe(density),
                )
            }
            ValueRow(label = "camera regions", value = foldState.iosOcclusionRegions.describe(density))
            ValueRow(label = "hinge API", value = capabilities.isHingeApiAvailable.toString())
            ValueRow(label = "regions API", value = capabilities.isRegionsApiAvailable.toString())
            ValueRow(label = "has hinge", value = capabilities.hasHinge?.toString() ?: "unknown")
        }
        DemoListCard {
            DemoRow(
                title = "Show the debug dump",
                subtitle = "Everything the running iOS exposes",
                trailing = { AdaptiveSwitch(checked = showDebugDump, onCheckedChange = onShowDebugDumpChange) },
            )
        }
        if (showDebugDump) DebugDump(foldState)
    }
}

@OptIn(ExperimentalCalfFoldableApi::class, CalfIosOnlyApi::class)
@Composable
private fun DebugDump(foldState: FoldState) {
    // Reading the snapshot here refreshes the dump whenever a value changes.
    val dump = remember(foldState.snapshot) { foldState.iosDebugDump() }
    DemoCard {
        Text(
            text = dump,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun LegendSection() {
    val colors = geometryColors()
    DemoSection(title = "Legend") {
        DemoListCard {
            LegendRow("Unsafe area", "Keep content out; it is inset for you here", colors.unsafe)
            DemoDivider()
            LegendRow("Pane", "One per side of the fold", colors.pane)
            DemoDivider()
            LegendRow("Fold", "Its margins shaded around it", colors.fold)
            DemoDivider()
            LegendRow("Camera and sensors", "iPhone Duo only", colors.camera)
        }
    }
}

@Composable
private fun LegendRow(title: String, subtitle: String, color: Color) {
    DemoRow(
        title = title,
        subtitle = subtitle,
        leading = {
            Box(Modifier.size(LegendSwatchSize).background(color, MaterialTheme.shapes.extraSmall))
        },
    )
}

private fun Float.toDpText(density: Density): String = with(density) { toDp().value.toInt().toString() }

private fun Rect.describe(density: Density): String =
    "${left.toDpText(density)}, ${top.toDpText(density)} · ${describeSize(density)}"

private fun Rect.describeSize(density: Density): String =
    "${width.toDpText(density)} × ${height.toDpText(density)} dp"

private fun EdgeInsets.describe(density: Density): String =
    "t ${top.toDpText(density)} r ${right.toDpText(density)} b ${bottom.toDpText(density)} l ${left.toDpText(density)}"

@OptIn(ExperimentalCalfFoldableApi::class)
private fun FoldRegion.describe(density: Density): String =
    bounds.describe(density) + if (isActive) "" else " (inactive)"

@OptIn(ExperimentalCalfFoldableApi::class)
private fun List<FoldRegion>.describe(density: Density): String =
    if (isEmpty()) "none" else joinToString(separator = "\n") { it.describe(density) }
