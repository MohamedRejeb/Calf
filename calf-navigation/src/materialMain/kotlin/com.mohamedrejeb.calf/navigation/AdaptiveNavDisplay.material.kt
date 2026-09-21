package com.mohamedrejeb.calf.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay

/**
 * Material actual: Nav3's [NavDisplay], unchanged. Back-stack changes made
 * while a transition is animating behave as they do in any Nav3 app.
 */
@ExperimentalCalfNavigationApi
@Composable
actual fun AdaptiveNavDisplay(
    backStack: NavBackStack<NavKey>,
    modifier: Modifier,
    onBack: () -> Unit,
    entryDecorators: List<NavEntryDecorator<NavKey>>,
    entryProvider: (NavKey) -> NavEntry<NavKey>,
) {
    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = onBack,
        entryDecorators = entryDecorators,
        entryProvider = entryProvider,
    )
}
