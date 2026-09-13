package com.mohamedrejeb.calf.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.mohamedrejeb.calf.navigation.internal.LocalNavTransitionTracker
import com.mohamedrejeb.calf.navigation.internal.NavTransitionGuardEffect
import com.mohamedrejeb.calf.navigation.internal.NavTransitionTracker

/**
 * Material actual: Nav3's [NavDisplay], plus a guard that drops pushes made
 * while a transition is still animating (a double tap yields one push).
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
    val tracker = remember { NavTransitionTracker() }
    NavTransitionGuardEffect(backStack, tracker)
    CompositionLocalProvider(LocalNavTransitionTracker provides tracker) {
        NavDisplay(
            backStack = backStack,
            modifier = modifier,
            onBack = onBack,
            entryDecorators = entryDecorators,
            entryProvider = entryProvider,
        )
    }
}
