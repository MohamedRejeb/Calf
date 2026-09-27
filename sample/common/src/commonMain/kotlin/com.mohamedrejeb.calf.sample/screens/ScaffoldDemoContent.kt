package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ScreenRotation
import androidx.compose.material.icons.outlined.Tab
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.sample.components.CodeSnippet
import com.mohamedrejeb.calf.sample.components.DemoCard
import com.mohamedrejeb.calf.sample.components.DemoDivider
import com.mohamedrejeb.calf.sample.components.DemoHeader
import com.mohamedrejeb.calf.sample.components.DemoListCard
import com.mohamedrejeb.calf.sample.components.DemoPage
import com.mohamedrejeb.calf.sample.components.DemoRow
import com.mohamedrejeb.calf.sample.components.DemoSection
import com.mohamedrejeb.calf.sample.components.DividerIndentWithIcon
import com.mohamedrejeb.calf.sample.components.IconTile
import com.mohamedrejeb.calf.sample.components.ValueRow
import com.mohamedrejeb.calf.sample.navigation.Screen
import com.mohamedrejeb.calf.sample.ui.theme.Spacing
import com.mohamedrejeb.calf.sample.ui.theme.TileBlue
import com.mohamedrejeb.calf.sample.ui.theme.TileViolet
import kotlin.math.roundToInt

private const val SCAFFOLD_USAGE = """
AdaptiveScaffold(
    topBar = {
        AdaptiveTopBar(title = { Text("Home") }, iosTitle = "Home")
    },
    bottomBar = {
        AdaptiveNavigationBar(
            iosItems = tabs,
            iosSelectedIndex = selected,
            iosOnItemSelected = { selected = it },
        ) { /* NavigationBarItems */ }
    },
) { padding ->
    // All four edges: in landscape, start and end keep clear of the cutout.
    HomeContent(Modifier.padding(padding))
}
"""

private val GuideThickness = 1.dp
private val GuideLabelCorner = 6.dp

/** Keeps the page's first and last lines clear of the edge guides' labels. */
private val GuideClearance = 28.dp

/** Which edge of the content area a [ContentEdgeGuide] marks. */
internal enum class ContentEdge { Top, Bottom }

/**
 * The page of the scaffold demo: the padding the scaffold gives its content, live, and how to
 * check it. Shown between the two edge guides.
 */
@Composable
internal fun ScaffoldInsetsPage(tabTitle: String, padding: PaddingValues) {
    DemoPage(contentPadding = PaddingValues(vertical = GuideClearance)) {
        DemoHeader(Screen.ScaffoldDemo)
        ContentPaddingSection(tabTitle = tabTitle, padding = padding)
        HowToCheckSection()
        DemoSection(title = "Usage") {
            CodeSnippet(SCAFFOLD_USAGE.trimIndent())
        }
    }
}

@Composable
private fun ContentPaddingSection(tabTitle: String, padding: PaddingValues) {
    val layoutDirection = LocalLayoutDirection.current
    DemoSection(
        title = "Content padding",
        footer = "What AdaptiveScaffold passes to its content. On iOS it is measured from the " +
            "native UINavigationBar and UITabBar.",
    ) {
        DemoCard {
            ValueRow(label = "Selected tab", value = tabTitle)
            ValueRow(label = "Top", value = padding.calculateTopPadding().toInsetLabel())
            ValueRow(label = "Bottom", value = padding.calculateBottomPadding().toInsetLabel())
            ValueRow(label = "Start", value = padding.calculateStartPadding(layoutDirection).toInsetLabel())
            ValueRow(label = "End", value = padding.calculateEndPadding(layoutDirection).toInsetLabel())
        }
    }
}

@Composable
private fun HowToCheckSection() {
    DemoSection(title = "How to check") {
        DemoListCard {
            DemoRow(
                title = "Rotate the device",
                subtitle = "Each guide stays on the edge of its bar, with no gap and no overlap",
                leading = { IconTile(icon = Icons.Outlined.ScreenRotation, color = TileBlue) },
            )
            DemoDivider(startIndent = DividerIndentWithIcon)
            DemoRow(
                title = "Switch tabs",
                subtitle = "The title changes and the padding stays the same",
                leading = { IconTile(icon = Icons.Outlined.Tab, color = TileViolet) },
            )
        }
    }
}

/**
 * A thin line on one edge of the content area, with a small label hanging inward from it. It is
 * placed by the caller exactly on the edge, so it touches its bar only when the padding is right.
 */
@Composable
internal fun ContentEdgeGuide(
    edge: ContentEdge,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (edge == ContentEdge.Bottom) GuideLabel(label = label, edge = edge)
        HorizontalDivider(thickness = GuideThickness, color = MaterialTheme.colorScheme.primary)
        if (edge == ContentEdge.Top) GuideLabel(label = label, edge = edge)
    }
}

@Composable
private fun GuideLabel(label: String, edge: ContentEdge) {
    // Square on the side that touches the line, so the label reads as hanging from it.
    val shape = when (edge) {
        ContentEdge.Top -> RoundedCornerShape(bottomStart = GuideLabelCorner, bottomEnd = GuideLabelCorner)
        ContentEdge.Bottom -> RoundedCornerShape(topStart = GuideLabelCorner, topEnd = GuideLabelCorner)
    }
    Surface(
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = shape,
        modifier = Modifier.padding(start = Spacing.pageMargin),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = Spacing.s, vertical = Spacing.xs / 2),
        )
    }
}

/** "64 dp": a padding as the demo prints it. */
internal fun Dp.toInsetLabel(): String = "${value.roundToInt()} dp"
