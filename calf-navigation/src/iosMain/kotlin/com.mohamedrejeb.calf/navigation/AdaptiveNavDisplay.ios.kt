@file:OptIn(ExperimentalForeignApi::class, ExperimentalComposeApi::class)

package com.mohamedrejeb.calf.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.currentCompositionLocalContext
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitViewController
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import com.mohamedrejeb.calf.navigation.internal.IosNavController
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * iOS actual: hosts a real `UINavigationController` inside the composable via
 * [UIKitViewController]. Each entry in [backStack] becomes a child view
 * controller; mutating the back stack triggers UIKit's native push/pop.
 *
 * [onBack] is unused here: UIKit performs pops itself and the host trims the
 * back stack to match. [entryDecorators] are not applied either; the native
 * per-entry scopes provide lifecycle, ViewModel and saved-state owners.
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
    val updatedEntryProvider = rememberUpdatedState(entryProvider)
    val density = LocalDensity.current.density
    // Each child Compose surface re-applies this context so theme, density and
    // any app-provided locals cross the UIKit boundary.
    val parentLocals = rememberUpdatedState(currentCompositionLocalContext)

    val host = remember(backStack) {
        IosNavController(
            backStack = backStack,
            parentLocals = parentLocals,
            entryProvider = { updatedEntryProvider.value(it) },
            density = density,
        )
    }

    LaunchedEffect(host) {
        snapshotFlow { backStack.toList() }.collect { snapshot -> host.syncToUiStack(snapshot) }
    }

    UIKitViewController(
        factory = { host.uiNavigationController },
        modifier = modifier,
        properties = UIKitInteropProperties(
            isInteractive = true,
            isNativeAccessibilityEnabled = true,
        ),
    )

    DisposableEffect(host) { onDispose { host.release() } }
}
