package com.mohamedrejeb.calf.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/**
 * Remembers a [NavBackStack] seeded with [startEntries] across recomposition.
 *
 * **State persistence:** the upstream multiplatform `rememberNavBackStack(configuration, *elements)`
 * requires an explicitly-configured `SavedStateConfiguration` with a serializers
 * module that registers every [NavKey] subtype, which is necessary on non-Android
 * platforms because there's no reflection-based discovery. To keep the walking-skeleton
 * prototype call-site simple, [rememberAdaptiveNavBackStack] uses a plain `remember`
 * that survives recompositions but **not** process death or configuration changes
 * on Android. For full process-death survival, call upstream's
 * `rememberNavBackStack(configuration, *startEntries)` directly with your serializers
 * module — its return type is `NavBackStack<NavKey>`, the same as ours.
 */
@ExperimentalCalfNavigationApi
@Composable
fun rememberAdaptiveNavBackStack(vararg startEntries: NavKey): NavBackStack<NavKey> =
    remember { NavBackStack(*startEntries) }
