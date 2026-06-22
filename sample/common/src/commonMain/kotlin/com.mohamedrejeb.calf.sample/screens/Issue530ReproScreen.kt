package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.navigation.AdaptiveNavigationBar
import com.mohamedrejeb.calf.ui.navigation.AdaptiveScaffold
import com.mohamedrejeb.calf.ui.navigation.UIKitUITabBarItem

/**
 * Reproduction for https://github.com/MohamedRejeb/Calf/issues/530
 *
 * Mirrors the reporter's setup: a Navigation 3 [NavDisplay] hosting two routes.
 *  - [ReproRoute.Tabs] shows an [AdaptiveNavigationBar] (a native `UITabBar` on iOS).
 *  - [ReproRoute.Detail] has no bottom bar.
 *
 * Observe on an iOS device/simulator (the issue does NOT happen on Material platforms):
 *  1. Tap "Open detail": the native bottom bar lingers during the forward transition and only
 *     disappears once the animation has finished.
 *  2. From the detail screen, swipe from the left edge to go back: the native bottom bar reappears
 *     immediately, before the back gesture is committed.
 *
 * Root cause (for reference, not fixed here): the native `UITabBar` is a UIKit view pinned to the
 * hosting controller's bottom. It is composited on top of the Compose surface and positioned by
 * Auto Layout, so it cannot follow a Compose transition; and its add/remove is tied to the
 * composable entering/leaving composition, which lags behind the animated transition.
 *
 * This screen changes nothing in the library — it only exercises the existing public API so the
 * behaviour can be reproduced on device against the local `calf-ui` source.
 */
private sealed interface ReproRoute : NavKey {
    data object Tabs : ReproRoute
    data object Detail : ReproRoute
}

@OptIn(ExperimentalCalfUiApi::class)
@Composable
fun Issue530ReproScreen(
    navigateBack: () -> Unit,
) {
    val backStack = remember { mutableStateListOf<NavKey>(ReproRoute.Tabs) }

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
        },
        entryProvider = entryProvider {
            entry<ReproRoute.Tabs> {
                ReproTabsScreen(
                    onOpenDetail = { backStack.add(ReproRoute.Detail) },
                    onExitRepro = navigateBack,
                )
            }
            entry<ReproRoute.Detail> {
                ReproDetailScreen()
            }
        },
    )
}

@OptIn(ExperimentalCalfUiApi::class)
@Composable
private fun ReproTabsScreen(
    onOpenDetail: () -> Unit,
    onExitRepro: () -> Unit,
) {
    var selectedIndex by remember { mutableStateOf(0) }
    val titles = listOf("Tab 1", "Tab 2")
    val icons = listOf(Icons.Outlined.Home, Icons.Outlined.Person)

    AdaptiveScaffold(
        bottomBar = {
            AdaptiveNavigationBar(
                modifier = Modifier.fillMaxWidth(),
                iosItems = listOf(
                    UIKitUITabBarItem(title = titles[0]),
                    UIKitUITabBarItem(title = titles[1]),
                ),
                iosSelectedIndex = selectedIndex,
                iosOnItemSelected = { selectedIndex = it },
            ) {
                titles.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = { Icon(icons[index], contentDescription = title) },
                        label = { Text(title) },
                    )
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Tabs screen",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "On iOS, watch the native bottom bar while the screen transitions.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onOpenDetail) {
                Text("Open detail (no bottom bar)")
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onExitRepro) {
                Text("Exit repro")
            }
        }
    }
}

@OptIn(ExperimentalCalfUiApi::class)
@Composable
private fun ReproDetailScreen() {
    AdaptiveScaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Detail screen",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "No bottom bar here. Swipe from the left edge to go back " +
                        "and watch the bar reappear before the gesture completes (iOS).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
