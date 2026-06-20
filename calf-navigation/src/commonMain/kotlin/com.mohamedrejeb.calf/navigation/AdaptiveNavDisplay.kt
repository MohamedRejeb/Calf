package com.mohamedrejeb.calf.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay

/**
 * Renders a navigation stack adaptively per platform.
 *
 *  - On Material platforms (Android / Desktop / Web), this delegates to
 *    Compose Multiplatform's [NavDisplay] with default predictive-back
 *    transitions.
 *  - On iOS, this hosts a real `UINavigationController` *inside* a Compose
 *    composable, so each backstack entry becomes a child `UIViewController`
 *    and you get native push/pop animations, large-title shrink, swipe-back
 *    from edge, and bar-tint transitions for free.
 *
 * Mutating [backStack] from a screen (`backStack.add(...)`,
 * `backStack.removeLastOrNull()`) is what triggers navigation on every
 * platform. The same Kotlin call-site works everywhere.
 *
 * Each entry gets its own `ViewModelStoreOwner`: `viewModel()` calls inside
 * an entry's content are scoped to that entry and cleared when it leaves the
 * back stack — via the default decorators on Material platforms, and via the
 * per-entry UIViewController scope on iOS.
 *
 * Use [adaptiveNavEntry] to define each route's bar (Material composable
 * slots + iOS structured data + cross-platform behaviour).
 */
@ExperimentalCalfNavigationApi
@Composable
expect fun AdaptiveNavDisplay(
    backStack: NavBackStack<NavKey>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = { backStack.removeLastOrNull() },
    entryDecorators: List<NavEntryDecorator<NavKey>> = listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator(),
    ),
    entryProvider: (NavKey) -> NavEntry<NavKey>,
)
