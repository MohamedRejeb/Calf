package com.mohamedrejeb.calf.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay

/**
 * Material actual: delegates wholesale to upstream [NavDisplay] and exposes
 * the back-press as [LocalNavBackPress] so screen content can pop without
 * holding a back-stack reference.
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
    CompositionLocalProvider(LocalNavBackPress provides onBack) {
        NavDisplay(
            backStack = backStack,
            modifier = modifier,
            onBack = onBack,
            entryDecorators = entryDecorators,
            entryProvider = entryProvider,
        )
    }
}
