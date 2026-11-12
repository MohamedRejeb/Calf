@file:OptIn(com.mohamedrejeb.calf.navigation.ExperimentalCalfNavigationApi::class)

package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.VerticalAlignTop
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.mohamedrejeb.calf.sample.components.DemoCard
import com.mohamedrejeb.calf.sample.components.DemoDivider
import com.mohamedrejeb.calf.sample.components.DemoListCard
import com.mohamedrejeb.calf.sample.components.DemoPage
import com.mohamedrejeb.calf.sample.components.DemoRow
import com.mohamedrejeb.calf.sample.components.DemoSection
import com.mohamedrejeb.calf.sample.components.DividerIndentWithIcon
import com.mohamedrejeb.calf.sample.components.IconTile
import com.mohamedrejeb.calf.sample.ui.theme.Spacing
import com.mohamedrejeb.calf.sample.ui.theme.TileAmber
import com.mohamedrejeb.calf.sample.ui.theme.TileBlue
import com.mohamedrejeb.calf.sample.ui.theme.TileIndigo
import com.mohamedrejeb.calf.sample.ui.theme.TileTeal

/** Scoped to a single back-stack entry; cleared exactly when the entry is popped. */
internal class NavDemoCounterViewModel : ViewModel() {
    var count by mutableIntStateOf(0)
        private set

    fun increment() {
        count += 1
    }
}

/** The Home tab: the saved places. The bar's + adds one more. */
@Composable
internal fun NavDemoHomeContent(
    contentPadding: PaddingValues,
    itemCount: () -> Int,
    onOpenItem: (Int) -> Unit,
) {
    DemoPage(contentPadding = contentPadding) {
        NavDemoHintCard(
            icon = Icons.Outlined.Layers,
            tint = TileIndigo,
            title = "One stack per tab",
            body = "Open a place, switch tabs and come back: the stack, the scroll position and the " +
                "place's ViewModel are still here. Tap Home again to pop to the root.",
        )
        DemoSection(
            title = "Saved places",
            footer = "Tap + in the bar to save another place. A place opens without the tab bar.",
        ) {
            DemoListCard {
                repeat(itemCount()) { index ->
                    if (index > 0) DemoDivider(startIndent = NavDemoThumbnailIndent)
                    NavDemoPlaceRow(place = navDemoPlace(index), onClick = { onOpenItem(index) })
                }
            }
        }
    }
}

@Composable
private fun NavDemoPlaceRow(place: NavDemoPlace, onClick: () -> Unit) {
    DemoRow(
        title = place.name,
        subtitle = "${place.country} · ${place.tagline}",
        leading = { NavDemoThumbnail(icon = place.icon, color = place.tint) },
        onClick = onClick,
    )
}

/**
 * A place pushed from Home, with the tab bar hidden. Below the place itself, the "Navigation"
 * section shows what this entry owns: its ViewModel, the back stack it sits on, and pushes
 * and pops from code.
 */
@Composable
internal fun NavDemoDetailContent(
    itemId: Int,
    contentPadding: PaddingValues,
    backStack: NavBackStack<NavKey>,
    onPopToRoot: () -> Unit,
) {
    val place = navDemoPlace(itemId)

    DemoPage(contentPadding = contentPadding) {
        NavDemoPlaceHeader(place)
        DemoSection(title = "About") {
            DemoCard {
                Text(
                    text = place.about,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            DemoListCard {
                NavDemoInfoRow(label = "Country", value = place.country)
                DemoDivider()
                NavDemoInfoRow(label = "Best time", value = place.bestTime)
                DemoDivider()
                NavDemoInfoRow(label = "Suggested stay", value = place.stay)
            }
        }
        NavDemoEntrySection(itemId = itemId, backStack = backStack, onPopToRoot = onPopToRoot)
    }
}

@Composable
private fun NavDemoPlaceHeader(place: NavDemoPlace) {
    NavDemoHero(icon = place.icon, color = place.tint)
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        modifier = Modifier.padding(horizontal = Spacing.xs),
    ) {
        Text(
            text = place.name,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = place.tagline,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** The entry-scoped ViewModel, the back stack this entry is on, and pushes and pops from code. */
@Composable
private fun NavDemoEntrySection(
    itemId: Int,
    backStack: NavBackStack<NavKey>,
    onPopToRoot: () -> Unit,
) {
    val counterViewModel = viewModel(key = "nav-demo-counter") { NavDemoCounterViewModel() }

    DemoSection(
        title = "Navigation",
        footer = "The counter lives in a ViewModel that belongs to this entry. It survives screens " +
            "pushed on top and tab switches, and is cleared when you pop back: open the place again " +
            "and it starts at 0. A duplicate entry gets a counter of its own.",
    ) {
        DemoListCard {
            NavDemoInfoRow(
                label = "Counter",
                value = counterViewModel.count.toString(),
                monospace = true,
                action = { NavDemoRowButton(label = "Increment", onClick = counterViewModel::increment) },
            )
            DemoDivider()
            NavDemoInfoRow(
                label = "Back stack",
                value = backStack.demoLabels(),
                monospace = true,
                valueMaxLines = Int.MAX_VALUE,
            )
        }
        NavDemoStackActions(itemId = itemId, backStack = backStack, onPopToRoot = onPopToRoot)
    }
}

@Composable
private fun NavDemoStackActions(
    itemId: Int,
    backStack: NavBackStack<NavKey>,
    onPopToRoot: () -> Unit,
) {
    val place = navDemoPlace(itemId)
    val next = navDemoPlace(itemId + 1)

    DemoListCard {
        DemoRow(
            title = "Open ${next.name}",
            subtitle = "Pushes the next place",
            leading = { IconTile(icon = Icons.AutoMirrored.Outlined.ArrowForward, color = TileBlue) },
            onClick = { backStack.add(NavDemoDetail(itemId + 1)) },
        )
        DemoDivider(startIndent = DividerIndentWithIcon)
        DemoRow(
            title = "Open ${place.name} again",
            subtitle = "A duplicate entry, with a ViewModel of its own",
            leading = { IconTile(icon = Icons.Outlined.ContentCopy, color = TileTeal) },
            onClick = { backStack.add(NavDemoDetail(itemId)) },
        )
        DemoDivider(startIndent = DividerIndentWithIcon)
        DemoRow(
            title = "Back to Home",
            subtitle = "Pops to the root of the tab",
            leading = { IconTile(icon = Icons.Outlined.VerticalAlignTop, color = TileAmber) },
            onClick = onPopToRoot,
        )
    }
}
