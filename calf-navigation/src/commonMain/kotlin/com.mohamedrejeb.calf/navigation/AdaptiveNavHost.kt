package com.mohamedrejeb.calf.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier

@Composable
fun AdaptiveNavHost(
    navController: AdaptiveNavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
    builder: NavGraphBuilder.() -> Unit
) {
    AndroidBackHandler(enabled = navController.backStack.size > 1) {
        navController.popBackStack()
    }

    val graphBuilder = remember {
        NavGraphBuilder().also {
            it.builder()
        }
    }

    LaunchedEffect(Unit) {
        if (navController.currentDestination == null)
            navController.navigate(startDestination)
    }

    // Keeps each destination's `rememberSaveable` state (list positions, form input) while the
    // destination stays on the back stack, so coming back restores it.
    val saveableStateHolder = rememberSaveableStateHolder()
    ForgetPoppedDestinations(navController, saveableStateHolder)

    Box(modifier = modifier) {
        navController.currentDestination?.let { currentDestination ->
            graphBuilder.destinations.find { it.route == currentDestination }?.let { destination ->
                saveableStateHolder.SaveableStateProvider(key = currentDestination) {
                    destination.content(destination.arguments)
                }
            }
        }
    }
}

/** Drops the saved state of destinations that left the back stack, so a later visit starts fresh. */
@Composable
private fun ForgetPoppedDestinations(
    navController: AdaptiveNavHostController,
    saveableStateHolder: SaveableStateHolder,
) {
    val routesOnStack = navController.backStack.toSet()
    val retainedRoutes = remember { mutableSetOf<String>() }

    SideEffect {
        (retainedRoutes - routesOnStack).forEach(saveableStateHolder::removeState)
        retainedRoutes.clear()
        retainedRoutes.addAll(routesOnStack)
    }
}
