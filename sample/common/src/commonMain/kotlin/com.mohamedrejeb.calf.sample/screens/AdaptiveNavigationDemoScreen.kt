@file:OptIn(
    com.mohamedrejeb.calf.navigation.ExperimentalCalfNavigationApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
)

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
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
import com.mohamedrejeb.calf.navigation.AdaptiveNavDisplay
import com.mohamedrejeb.calf.navigation.adaptiveNavEntry
import com.mohamedrejeb.calf.navigation.rememberAdaptiveNavBackStack
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonItem
import com.mohamedrejeb.calf.ui.navigation.UIKitUIBarButtonSystemItem
import kotlinx.serialization.Serializable

@Serializable
internal data object NavDemoHome : NavKey

@Serializable
internal data class NavDemoDetail(val itemId: Int) : NavKey

@Serializable
internal data object NavDemoCover : NavKey

@Serializable
internal data object NavDemoLocked : NavKey

/** Scoped to a single back-stack entry; cleared exactly when the entry is popped. */
internal class NavDemoCounterViewModel : ViewModel() {
    var count by mutableIntStateOf(0)
        private set

    fun increment() {
        count += 1
    }
}

/**
 * Full-screen showcase for [AdaptiveNavDisplay]. Owns the whole screen — it
 * is *not* nested inside the sample app's scaffold, so the navigator's bars
 * are the only bars (nesting two top bars hid the inner one behind the outer
 * and broke both back buttons and content padding).
 *
 * What it demonstrates:
 *  - Large title that collapses on scroll (native shrink on iOS,
 *    `LargeTopAppBar` + scroll behavior on Material).
 *  - Native bar items: leading Close exits the demo, trailing + adds rows.
 *  - Push / pop, deep stacks, duplicate entries, and multi-pop ("pop to
 *    root") with the platform's native transitions.
 *  - Per-entry ViewModel scoping (the detail counter resets after pop).
 *  - `barShown = false` — edge-to-edge content with no bar.
 *  - `iosGestureEnabled = false` — a screen that blocks iOS swipe-back.
 */
@Composable
fun AdaptiveNavigationDemoScreen(navigateBack: () -> Unit) {
    val backStack = rememberAdaptiveNavBackStack(NavDemoHome)
    var itemCount by remember { mutableIntStateOf(5) }

    AdaptiveNavDisplay(
        backStack = backStack,
        modifier = Modifier.fillMaxSize(),
    ) { key ->
        when (key) {
            is NavDemoHome -> adaptiveNavEntry(
                key = key,
                title = { Text("Native Stack") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.Filled.Close, contentDescription = "Exit demo")
                    }
                },
                actions = {
                    IconButton(onClick = { itemCount += 1 }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add item")
                    }
                },
                largeTitle = true,
                iosTitle = "Native Stack",
                iosBackTitle = "Stack",
                iosLeadingItems = listOf(
                    UIKitUIBarButtonItem.systemItem(
                        systemItem = UIKitUIBarButtonSystemItem.Close,
                        onClick = navigateBack,
                    ),
                ),
                iosTrailingItems = listOf(
                    UIKitUIBarButtonItem.systemItem(
                        systemItem = UIKitUIBarButtonSystemItem.Add,
                        onClick = { itemCount += 1 },
                    ),
                ),
            ) { padding ->
                NavDemoHomeContent(
                    contentPadding = padding,
                    itemCount = { itemCount },
                    onOpenItem = { backStack.add(NavDemoDetail(it)) },
                    onOpenCover = { backStack.add(NavDemoCover) },
                    onOpenLocked = { backStack.add(NavDemoLocked) },
                )
            }

            is NavDemoDetail -> adaptiveNavEntry(
                key = key,
                title = { Text("Item ${key.itemId}") },
                navigationIcon = {
                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                iosTitle = "Item ${key.itemId}",
                iosBackTitle = "Item ${key.itemId}",
            ) { padding ->
                NavDemoDetailContent(
                    itemId = key.itemId,
                    contentPadding = padding,
                    backStack = backStack,
                )
            }

            is NavDemoCover -> adaptiveNavEntry(
                key = key,
                barShown = false,
                iosTitle = "Cover",
            ) { padding ->
                NavDemoCoverContent(
                    contentPadding = padding,
                    onClose = { backStack.removeLastOrNull() },
                )
            }

            is NavDemoLocked -> adaptiveNavEntry(
                key = key,
                title = { Text("No Swipe Back") },
                navigationIcon = {
                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                iosTitle = "No Swipe Back",
                iosGestureEnabled = false,
            ) { padding ->
                NavDemoLockedContent(
                    contentPadding = padding,
                    onBack = { backStack.removeLastOrNull() },
                )
            }

            else -> error("Unexpected nav key: $key")
        }
    }
}

@Composable
private fun NavDemoHomeContent(
    contentPadding: PaddingValues,
    itemCount: () -> Int,
    onOpenItem: (Int) -> Unit,
    onOpenCover: () -> Unit,
    onOpenLocked: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
    ) {
        item { NavDemoSectionHeader("Native features") }
        item {
            NavDemoFeatureRow(
                icon = { Icon(Icons.Outlined.Fullscreen, contentDescription = null) },
                title = "Edge-to-edge cover",
                subtitle = "barShown = false hides the navigation bar",
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
        item { NavDemoSectionHeader("Items — tap + to add more") }
        items(itemCount()) { index ->
            ListItem(
                headlineContent = { Text("Item $index") },
                supportingContent = { Text("Push a detail screen with its own ViewModel") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenItem(index) },
            )
            HorizontalDivider()
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

@Composable
private fun NavDemoDetailContent(
    itemId: Int,
    contentPadding: PaddingValues,
    backStack: NavBackStack<NavKey>,
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
                    "This ViewModel belongs to this entry only. It survives " +
                        "screens pushed on top, and is cleared when you pop back — " +
                        "reopen the item and the counter starts at 0.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Back stack", style = MaterialTheme.typography.titleMedium)
                Text(
                    backStack.joinToString(" › ") { it.demoLabel() },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

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
            onClick = { repeat(backStack.size - 1) { backStack.removeLastOrNull() } },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Pop to root")
        }
    }
}

@Composable
private fun NavDemoCoverContent(
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
                "This entry sets barShown = false, so the navigation bar is " +
                    "hidden and the content owns the full screen. On iOS you can " +
                    "still swipe from the left edge to pop.",
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
private fun NavDemoLockedContent(
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
        Text(
            "Swipe-back is disabled",
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "This entry sets iosGestureEnabled = false — the iOS edge-swipe " +
                "gesture won't pop it, which is useful for forms that shouldn't " +
                "be dismissed accidentally. Use the back button instead.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onBack) { Text("Go back") }
    }
}

private fun NavKey.demoLabel(): String =
    when (this) {
        is NavDemoHome -> "Home"
        is NavDemoDetail -> "Item $itemId"
        is NavDemoCover -> "Cover"
        is NavDemoLocked -> "Locked"
        else -> toString()
    }
