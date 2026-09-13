package com.mohamedrejeb.calf.navigation.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.LocalNavAnimatedContentScope

/**
 * Knows whether the hosting `NavDisplay` is animating. Entries report their
 * transition through [ReportNavTransition]; the display consults
 * [isTransitioning] to drop navigation requested mid-animation.
 */
@Stable
internal class NavTransitionTracker {
    private var running: Set<Any> by mutableStateOf(emptySet())

    val isTransitioning: Boolean
        get() = running.isNotEmpty()

    fun update(token: Any, isRunning: Boolean) {
        running = if (isRunning) running + token else running - token
    }
}

/** Provided by the Material displays around their `NavDisplay`; `null` outside them. */
internal val LocalNavTransitionTracker = staticCompositionLocalOf<NavTransitionTracker?> { null }

/**
 * Reports the enclosing Nav3 transition to the display's tracker. Composed
 * inside every `adaptiveNavEntry`, so both the entering and the exiting entry
 * report while a push or pop animates. A no-op outside Calf's displays.
 */
@Composable
internal fun ReportNavTransition() {
    val tracker = LocalNavTransitionTracker.current ?: return
    val scope = LocalNavAnimatedContentScope.current
    val token = remember { Any() }
    DisposableEffect(tracker, token) {
        onDispose { tracker.update(token, false) }
    }
    LaunchedEffect(tracker, scope, token) {
        snapshotFlow { scope.transition.isRunning }.collect { tracker.update(token, it) }
    }
}

/**
 * Drops pushes made while the display is animating, restoring the back
 * stack to the last accepted state. Pops stay allowed because predictive
 * back pops while its own transition is still running.
 */
@Composable
internal fun NavTransitionGuardEffect(backStack: NavBackStack<NavKey>, tracker: NavTransitionTracker) {
    LaunchedEffect(backStack, tracker) {
        var accepted = backStack.toList()
        snapshotFlow { backStack.toList() }.collect { snapshot ->
            when (val decision = decideNavChange(accepted, snapshot, tracker.isTransitioning, allowPops = true)) {
                NavChangeDecision.Accept -> accepted = snapshot
                is NavChangeDecision.Revert -> {
                    backStack.clear()
                    backStack.addAll(decision.stack)
                }
            }
        }
    }
}
