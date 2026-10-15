package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
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
import com.mohamedrejeb.calf.sample.components.SampleScreenScaffold
import com.mohamedrejeb.calf.sample.components.ValueRow
import com.mohamedrejeb.calf.sample.currentPlatform
import com.mohamedrejeb.calf.sample.navigation.Screen
import com.mohamedrejeb.calf.sample.ui.theme.Spacing
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.button.AdaptiveIconButton
import com.mohamedrejeb.calf.ui.toolbar.AdaptiveToolbar

private const val USAGE = """
AdaptiveToolbar(
    expanded = true,
    // iOS 26: a native UIToolbar with Liquid Glass
    iosItems = listOf(
        UIKitUIBarButtonItem.image(UIKitImage.SystemName(SFSymbol.bold), label = "Bold") { toggleBold() },
        UIKitUIBarButtonItem.flexibleSpace(),
        UIKitUIBarButtonItem.image(UIKitImage.SystemName(SFSymbol.squareAndArrowUp), label = "Share") { share() },
    ),
    // Elsewhere: Material 3 Expressive floating toolbar
    content = {
        AdaptiveIconButton(onClick = { toggleBold() }) {
            Icon(Icons.Filled.FormatBold, contentDescription = "Bold")
        }
    },
    // Material: slides away while the page under it scrolls down
    scrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(FloatingToolbarExitDirection.Bottom),
)
"""

private const val DRAFT_BODY =
    "We took the 28 tram up to Graça before the crowds, then walked down through Alfama to the " +
        "river. Lunch was grilled sardines at a place with six tables and no menu."

/** Room left under the page so its last section scrolls clear of the floating toolbar. */
private val ToolbarClearance = 96.dp

@OptIn(ExperimentalCalfUiApi::class)
@Composable
fun ToolbarScreen(navigateBack: () -> Unit) {
    var format by remember { mutableStateOf(DraftFormat()) }
    var lastAction by rememberSaveable { mutableStateOf("None yet") }
    var spacing by rememberSaveable { mutableStateOf(ToolbarSpacing.Default) }

    // Reads the state when called, so the remembered items below never act on a stale format.
    val actions = remember {
        DraftActions(
            toggleBold = {
                format = format.copy(bold = !format.bold)
                lastAction = if (format.bold) "Bold on" else "Bold off"
            },
            toggleItalic = {
                format = format.copy(italic = !format.italic)
                lastAction = if (format.italic) "Italic on" else "Italic off"
            },
            toggleUnderline = {
                format = format.copy(underline = !format.underline)
                lastAction = if (format.underline) "Underline on" else "Underline off"
            },
            share = { lastAction = "Share sheet opened" },
        )
    }
    val iosItems = remember(format, spacing) { draftToolbarItems(format, actions, spacing) }

    // Material only: the floating toolbar slides out below the screen while the page scrolls down
    // and back in when it scrolls up. The page's scroll reaches it through the nested scroll chain.
    val scrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
        exitDirection = FloatingToolbarExitDirection.Bottom,
    )

    SampleScreenScaffold(title = Screen.Toolbar.title, navigateBack = navigateBack) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            DemoPage(
                contentPadding = padding.plusBottom(ToolbarClearance),
                modifier = Modifier.nestedScroll(scrollBehavior),
            ) {
                DemoHeader(Screen.Toolbar)
                DraftSection(format = format)
                DemoSection(
                    title = "Toolbar",
                    footer = if (currentPlatform.isIOS) {
                        null
                    } else {
                        "Scroll down and the toolbar slides away through scrollBehavior; scroll up to bring it back."
                    },
                ) {
                    DemoCard {
                        ValueRow(label = "Last action", value = lastAction)
                        ValueRow(label = "Style", value = format.description)
                    }
                }
                if (currentPlatform.isIOS) {
                    SpacingSection(selected = spacing, onSelect = { spacing = it })
                }
                DemoSection(title = "Usage") {
                    CodeSnippet(USAGE.trimIndent())
                }
            }
            AdaptiveToolbar(
                expanded = true,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = padding.calculateBottomPadding() + Spacing.l),
                scrollBehavior = scrollBehavior,
                iosItems = iosItems,
                content = { MaterialToolbarItems(format = format, actions = actions) },
            )
        }
    }
}

@Composable
private fun DraftSection(format: DraftFormat) {
    DemoSection(
        title = "Draft",
        footer = "The toolbar floats over the page. Bold, italic and underline change the text above.",
    ) {
        DemoCard(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            Text(
                text = "Lisbon, day two",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Sep 14 · Travel notes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = DRAFT_BODY,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = if (format.bold) FontWeight.Bold else FontWeight.Normal,
                fontStyle = if (format.italic) FontStyle.Italic else FontStyle.Normal,
                textDecoration = if (format.underline) TextDecoration.Underline else TextDecoration.None,
            )
        }
    }
}

/** iOS only: the spacing patterns change how the native toolbar lays out its items. */
@Composable
private fun SpacingSection(
    selected: ToolbarSpacing,
    onSelect: (ToolbarSpacing) -> Unit,
) {
    DemoSection(
        title = "Spacing",
        footer = "Flexible and fixed spaces between the UIBarButtonItems of the native toolbar.",
    ) {
        DemoListCard {
            ToolbarSpacing.entries.forEachIndexed { index, pattern ->
                if (index > 0) DemoDivider()
                DemoRow(
                    title = pattern.title,
                    subtitle = pattern.subtitle,
                    onClick = { onSelect(pattern) },
                    trailing = {
                        if (pattern == selected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun RowScope.MaterialToolbarItems(format: DraftFormat, actions: DraftActions) {
    ToggleItem(Icons.Filled.FormatBold, "Bold", checked = format.bold, onClick = actions.toggleBold)
    ToggleItem(Icons.Filled.FormatItalic, "Italic", checked = format.italic, onClick = actions.toggleItalic)
    ToggleItem(
        Icons.Filled.FormatUnderlined,
        "Underline",
        checked = format.underline,
        onClick = actions.toggleUnderline,
    )
    AdaptiveIconButton(onClick = actions.share) {
        Icon(Icons.Outlined.Share, contentDescription = "Share")
    }
}

/** A formatting button that shows a filled container while its style is on. */
@Composable
private fun ToggleItem(
    icon: ImageVector,
    contentDescription: String,
    checked: Boolean,
    onClick: () -> Unit,
) {
    val colors = if (checked) {
        IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    } else {
        IconButtonDefaults.iconButtonColors()
    }
    AdaptiveIconButton(onClick = onClick, colors = colors) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}

@Composable
private fun PaddingValues.plusBottom(extra: Dp): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current
    return PaddingValues(
        start = calculateStartPadding(layoutDirection),
        top = calculateTopPadding(),
        end = calculateEndPadding(layoutDirection),
        bottom = calculateBottomPadding() + extra,
    )
}
