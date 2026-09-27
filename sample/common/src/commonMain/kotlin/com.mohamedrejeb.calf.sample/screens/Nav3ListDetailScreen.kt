@file:OptIn(
    androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi::class,
    com.mohamedrejeb.calf.foldable.ExperimentalCalfFoldableApi::class,
)

package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.VerticalSplit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.mohamedrejeb.calf.foldable.navigation3.rememberCalfListDetailSceneStrategy
import com.mohamedrejeb.calf.sample.components.DemoCard
import com.mohamedrejeb.calf.sample.components.DemoDivider
import com.mohamedrejeb.calf.sample.components.DemoListCard
import com.mohamedrejeb.calf.sample.components.DemoPage
import com.mohamedrejeb.calf.sample.components.DemoRow
import com.mohamedrejeb.calf.sample.components.DemoSection
import com.mohamedrejeb.calf.sample.components.EmptyState
import com.mohamedrejeb.calf.sample.components.SampleScreenScaffold
import com.mohamedrejeb.calf.sample.navigation.Screen
import com.mohamedrejeb.calf.sample.ui.theme.Spacing
import com.mohamedrejeb.calf.sample.ui.theme.TileIndigo
import com.mohamedrejeb.calf.ui.button.AdaptiveIconButton
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

// A list and its detail on one Navigation 3 back stack, without calf-navigation.
@Serializable
internal data object Nav3Inbox : NavKey

@Serializable
internal data class Nav3Item(val id: Int) : NavKey

/** Nav3's own back stack needs the keys registered to survive process death and Android folds. */
private val Nav3SavedState: SavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Nav3Inbox::class)
            subclass(Nav3Item::class)
        }
    }
}

private val ListSwatchSize = 36.dp
private val DetailSwatchHeight = 120.dp
private val StripHeight = 40.dp

/**
 * Plain Navigation 3 with Calf's fold-aware list-detail strategy: `NavDisplay` and upstream's
 * `listPane()` / `detailPane()` metadata, with Navigation 3's own transitions and predictive back.
 * The one line that comes from Calf is `rememberCalfListDetailSceneStrategy()`: it gives two panes
 * on an open iPhone Duo and lays the detail out after the fold when the Duo is half open.
 *
 * The content is a colour palette: the list of named colours, and a colour's detail.
 */
@Composable
fun Nav3ListDetailScreen(navigateBack: () -> Unit) {
    SampleScreenScaffold(title = Screen.Nav3ListDetail.title, navigateBack = navigateBack) { padding ->
        Nav3ListDetail(modifier = Modifier.fillMaxSize().padding(padding))
    }
}

@Composable
private fun Nav3ListDetail(modifier: Modifier) {
    val backStack = rememberNavBackStack(Nav3SavedState, Nav3Inbox)

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        sceneStrategies = listOf(rememberCalfListDetailSceneStrategy()),
        entryProvider = entryProvider {
            entry<Nav3Inbox>(
                metadata = ListDetailSceneStrategy.listPane(detailPlaceholder = { Nav3NoItemSelected() }),
            ) {
                Nav3ColorList(
                    // Read in the composition, so the highlight follows the back stack.
                    selectedId = backStack.filterIsInstance<Nav3Item>().lastOrNull()?.id,
                    onOpen = backStack::openItem,
                )
            }
            entry<Nav3Item>(metadata = ListDetailSceneStrategy.detailPane()) { key ->
                Nav3ColorDetail(id = key.id, onClose = { backStack.removeLastOrNull() })
            }
        },
    )
}

/**
 * Shows [id] as the one open item. In two panes the list stays where it is, and a tap replaces
 * the open item instead of stacking one per tap; in one pane it is a push.
 */
private fun NavBackStack<NavKey>.openItem(id: Int) {
    retainAll { it is Nav3Inbox }
    add(Nav3Item(id))
}

@Composable
private fun Nav3ColorList(selectedId: Int?, onOpen: (Int) -> Unit) {
    DemoPage(contentPadding = PaddingValues()) {
        NavDemoHintCard(
            icon = Icons.Outlined.VerticalSplit,
            tint = TileIndigo,
            title = "One back stack, two panes",
            body = "A color opens beside the list when there is room and over it when there is not. " +
                "On an open iPhone Duo the two panes meet at the hinge.",
        )
        DemoSection(title = "Palette") {
            DemoListCard {
                nav3ColorIds.forEach { id ->
                    if (id != nav3ColorIds.first) DemoDivider(startIndent = Nav3SwatchIndent)
                    val color = nav3Color(id)
                    DemoRow(
                        title = color.name,
                        subtitle = color.hex,
                        leading = { Nav3Swatch(color.color, Modifier.size(ListSwatchSize)) },
                        selected = id == selectedId,
                        onClick = { onOpen(id) },
                    )
                }
            }
        }
    }
}

private val Nav3SwatchIndent = Spacing.l + ListSwatchSize + Spacing.m

/** What has to survive a fold: the saved taps and the scroll position of this page. */
@Composable
internal fun Nav3ColorDetail(id: Int, onClose: () -> Unit) {
    var taps by rememberSaveable { mutableIntStateOf(0) }
    val color = nav3Color(id)

    DemoPage(contentPadding = PaddingValues()) {
        Nav3DetailHeader(color = color, onClose = onClose)
        DemoCard {
            Nav3Swatch(color.color, Modifier.fillMaxWidth().height(DetailSwatchHeight))
            Nav3TintStrip(color)
        }
        DemoSection(title = "Values") {
            DemoListCard {
                NavDemoInfoRow(label = "Hex", value = color.hex, monospace = true)
                DemoDivider()
                NavDemoInfoRow(label = "RGB", value = color.rgb, monospace = true)
                DemoDivider()
                NavDemoInfoRow(label = "HSL", value = color.hsl, monospace = true)
                DemoDivider()
                NavDemoInfoRow(label = "Text on it", value = if (color.prefersDarkText) "Black" else "White")
            }
        }
        NavDemoStateCheck(
            taps = taps,
            onTap = { taps += 1 },
            footer = "Scroll, then open or close the fold, rotate, or resize the window. The color " +
                "stays open with its taps and scroll position.",
        )
    }
}

@Composable
private fun Nav3DetailHeader(color: Nav3Color, onClose: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(start = Spacing.xs),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = color.name,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = color.hex,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        AdaptiveIconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close item",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** The tints and shades of [color], lightest first, as one rounded strip. */
@Composable
private fun Nav3TintStrip(color: Nav3Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(StripHeight)
            .clip(MaterialTheme.shapes.small),
    ) {
        color.tintsAndShades.forEach { step ->
            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(step))
        }
    }
    Row {
        Text(
            text = "Tints",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "Shades",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Nav3Swatch(color: Color, modifier: Modifier) {
    Box(modifier = modifier.background(color, MaterialTheme.shapes.medium))
}

@Composable
private fun Nav3NoItemSelected() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        EmptyState(
            icon = Icons.Outlined.Palette,
            title = "No color selected",
            message = "Pick a color from the palette to see its values.",
        )
    }
}
