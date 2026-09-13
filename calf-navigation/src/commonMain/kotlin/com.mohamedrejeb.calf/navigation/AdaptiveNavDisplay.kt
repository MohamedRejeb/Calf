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
 * Renders a navigation stack natively per platform.
 *
 *  - On Material platforms (Android / Desktop / Web) this delegates to Nav3's
 *    [NavDisplay] with its default predictive-back transitions.
 *  - On iOS it hosts a real `UINavigationController` inside the composable:
 *    each back-stack entry becomes a child `UIViewController`, so push/pop
 *    animations, edge swipe-back and large-title shrink are the system's.
 *
 * [backStack] is the single source of truth everywhere: `backStack.add(...)`
 * pushes and `backStack.removeLastOrNull()` pops, on every platform. When the
 * user pops natively on iOS (back button, swipe, pop-to-root), the back stack
 * is trimmed to match UIKit, so it never disagrees with what is on screen.
 *
 * Each entry gets its own `ViewModelStoreOwner`: `viewModel()` inside an
 * entry is scoped to that entry and cleared when it leaves the back stack,
 * through [entryDecorators] on Material and the per-entry controller scope
 * on iOS.
 *
 * Use [adaptiveNavEntry] to define each route's bar.
 *
 * @param backStack the stack to render; see [rememberAdaptiveNavBackStack].
 * @param onBack handles Android's system back and predictive back on Material
 *   platforms. Not used on iOS, where the system performs the pop itself;
 *   veto swipe-back per entry with `iosGestureEnabled` instead.
 * @param entryDecorators Nav3 decorators applied on Material platforms. Not
 *   applied on iOS, whose native per-entry scopes provide lifecycle,
 *   ViewModel and saved-state owners.
 * @param entryProvider builds the entry for a key; use [adaptiveNavEntry].
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
