@file:OptIn(com.mohamedrejeb.calf.navigation.ExperimentalCalfNavigationApi::class)

package com.mohamedrejeb.calf.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.mohamedrejeb.calf.navigation.AdaptiveTabState

/** Scoped to a single back-stack entry; cleared exactly when the entry is popped. */
internal class NavDemoCounterViewModel : ViewModel() {
    var count by mutableIntStateOf(0)
        private set

    fun increment() {
        count += 1
    }
}

@Composable
internal fun NavDemoHomeContent(
    contentPadding: PaddingValues,
    itemCount: () -> Int,
    onOpenItem: (Int) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
    ) {
        item {
            NavDemoCard(
                title = "One stack per tab",
                body = "Push an item, switch tabs and come back: the stack, the scroll " +
                    "position and the item's ViewModel are still here. Tap the Home " +
                    "tab again to pop to the root.",
            )
        }
        item { NavDemoSectionHeader("Items — tap + to add more") }
        items(itemCount()) { index ->
            ListItem(
                headlineContent = { Text("Item $index") },
                supportingContent = { Text("Pushes a detail that hides the tab bar") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenItem(index) },
            )
            HorizontalDivider()
        }
    }
}

@Composable
internal fun NavDemoExploreContent(
    contentPadding: PaddingValues,
    onOpenCover: () -> Unit,
    onOpenLocked: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
    ) {
        item { NavDemoSectionHeader("Per-entry options") }
        item {
            NavDemoFeatureRow(
                icon = { Icon(Icons.Outlined.Fullscreen, contentDescription = null) },
                title = "Edge-to-edge cover",
                subtitle = "barShown = false and tabBarShown = false",
                onClick = onOpenCover,
            )
        }
        item {
            NavDemoFeatureRow(
                icon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                title = "Locked screen",
                subtitle = "iosGestureEnabled = false blocks iOS swipe-back",
                onClick = onOpenLocked,
            )
        }
        item {
            NavDemoCard(
                title = "Badges",
                body = "This tab shows a badge on both platforms: a Material badge here, " +
                    "the native badgeValue on iOS.",
            )
        }
    }
}

@Composable
internal fun NavDemoDetailContent(
    itemId: Int,
    contentPadding: PaddingValues,
    backStack: NavBackStack<NavKey>,
    onPopToRoot: () -> Unit,
) {
    val counterViewModel = viewModel(key = "nav-demo-counter") { NavDemoCounterViewModel() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Entry-scoped ViewModel", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Counter: ${counterViewModel.count}",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Button(onClick = counterViewModel::increment) { Text("Increment") }
                Text(
                    "This ViewModel belongs to this entry only. It survives screens " +
                        "pushed on top and tab switches, and is cleared when you pop " +
                        "back — reopen the item and the counter starts at 0.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        NavDemoCard(title = "Back stack", body = backStack.demoLabels())

        Button(
            onClick = { backStack.add(NavDemoDetail(itemId + 1)) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Push Item ${itemId + 1}")
        }
        OutlinedButton(
            onClick = { backStack.add(NavDemoDetail(itemId)) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Push this screen again (duplicate entry)")
        }
        OutlinedButton(
            onClick = onPopToRoot,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Pop to root")
        }
    }
}

@Composable
internal fun NavDemoCoverContent(
    contentPadding: PaddingValues,
    onClose: () -> Unit,
) {
    val gradient = Brush.verticalGradient(
        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary),
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                "Edge-to-edge",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "This entry hides both the navigation bar and the tab bar, so the " +
                    "content owns the full screen. On iOS you can still swipe from " +
                    "the left edge to pop.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onClose) { Text("Close") }
        }
    }
}

@Composable
internal fun NavDemoLockedContent(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Outlined.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(16.dp))
        Text("Swipe-back is disabled", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Text(
            "This entry sets iosGestureEnabled = false — the iOS edge-swipe gesture " +
                "won't pop it, which is useful for forms that shouldn't be dismissed " +
                "accidentally. Use the back button instead.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onBack) { Text("Go back") }
    }
}

@Composable
internal fun NavDemoSettingsContent(
    contentPadding: PaddingValues,
    tabs: AdaptiveTabState<NavKey>,
    onSignOut: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        NavDemoCard(
            title = "Tab state",
            body = tabs.tabs.joinToString("\n") { tab ->
                val marker = if (tab == tabs.selectedTab) "▶ " else "   "
                marker + tabs.backStackOf(tab).demoLabels()
            },
        )
        OutlinedButton(
            onClick = { tabs.select(NavDemoHome) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Select the Home tab from code")
        }
        NavDemoCard(
            title = "Wide displays",
            body = "iosSidebarAdaptable = true lets the tab bar become a sidebar on " +
                "iPad and on the inner display of iPhone Duo. On Material the " +
                "navigation suite switches to a rail on wide windows.",
        )
        NavDemoCard(
            title = "Sign out",
            body = "Signing out pushes a screen over the tabs from the outer stack. " +
                "The tab bar leaves with the transition and comes back with the " +
                "back swipe, on iOS as well.",
        )
        Button(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null)
            Spacer(Modifier.padding(4.dp))
            Text("Sign out")
        }
    }
}

@Composable
internal fun NavDemoSignedOutContent(
    contentPadding: PaddingValues,
    onSignIn: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("You are signed out", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Text(
            "This screen lives on the outer stack, above the tabs. Swipe back on " +
                "iOS and watch the tab bar return with the gesture.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onSignIn) { Text("Sign in again") }
    }
}

@Composable
private fun NavDemoCard(title: String, body: String) {
    Card(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun NavDemoSectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
    )
}

@Composable
private fun NavDemoFeatureRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    ListItem(
        leadingContent = icon,
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    )
    HorizontalDivider()
}
