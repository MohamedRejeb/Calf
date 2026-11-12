package com.mohamedrejeb.calf.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.savedstate.serialization.SavedStateConfiguration

/**
 * Remembers a [NavBackStack] seeded with [startEntries].
 *
 * With a [configuration], the stack is persisted through saved state, so it
 * survives Android configuration changes and process death. The configuration
 * must register every [NavKey] subtype the stack can hold, exactly as Nav3's
 * own `rememberNavBackStack(configuration, …)` requires:
 *
 * ```kotlin
 * val config = SavedStateConfiguration {
 *     serializersModule = SerializersModule {
 *         polymorphic(NavKey::class) {
 *             subclass(Home::class)
 *             subclass(Detail::class)
 *         }
 *     }
 * }
 * val backStack = rememberAdaptiveNavBackStack(Home, configuration = config)
 * ```
 *
 * Without a configuration the stack only survives recomposition. Pass the
 * same value on every call: switching between `null` and a configuration
 * recreates the stack.
 */
@ExperimentalCalfNavigationApi
@Composable
fun rememberAdaptiveNavBackStack(
    vararg startEntries: NavKey,
    configuration: SavedStateConfiguration? = null,
): NavBackStack<NavKey> =
    if (configuration != null) {
        rememberNavBackStack(configuration, *startEntries)
    } else {
        remember { NavBackStack(*startEntries) }
    }
