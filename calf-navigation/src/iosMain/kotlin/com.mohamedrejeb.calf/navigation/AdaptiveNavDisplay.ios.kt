@file:OptIn(ExperimentalForeignApi::class, ExperimentalComposeApi::class)

package com.mohamedrejeb.calf.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
 * iOS actual: hosts a real [platform.UIKit.UINavigationController] *inside*
 * a Compose composable via [UIKitViewController]. Each entry in [backStack]
 * becomes a child UIViewController; mutating the back stack (push/pop)
 * triggers UIKit's native push/pop animation, swipe-back, and large-title
 * shrink-on-scroll.
 *
 * The `entryDecorators` parameter is currently **not** applied on iOS — the
 * built-in lifecycle / `ViewModelStore` / `SavedStateRegistry` providers
 * cover the 90% case. Custom decorator support is a tracked follow-up
 * (see `docs/superpowers/specs/2026-05-09-adaptive-navigation-design.md`
 * § 11).
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
    val updatedOnBack = rememberUpdatedState(onBack)
    val updatedEntryProvider = rememberUpdatedState(entryProvider)
    val density = LocalDensity.current.density

    // Provide LocalNavBackPress *before* capturing currentCompositionLocalContext
    // — the captured context is what each child Compose surface re-applies to
    // propagate theme, density, and these locals across the UIKit boundary.
    CompositionLocalProvider(LocalNavBackPress provides { updatedOnBack.value() }) {
        val parentLocals = rememberUpdatedState(currentCompositionLocalContext)

        val nav = remember(backStack) {
            IosNavController(
                backStack = backStack,
                onBack = { updatedOnBack.value() },
                parentLocals = parentLocals,
                entryProvider = { updatedEntryProvider.value(it) },
                density = density,
            )
        }

        LaunchedEffect(nav, backStack) {
            snapshotFlow { backStack.toList() }
                .collect { snapshot -> nav.syncToUiStack(snapshot) }
        }

        UIKitViewController(
            factory = { nav.uiNavigationController },
            modifier = modifier,
            properties = UIKitInteropProperties(
                isInteractive = true,
                isNativeAccessibilityEnabled = true,
            ),
        )

        DisposableEffect(nav) { onDispose { nav.release() } }
    }
}
