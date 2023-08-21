package com.mohamedrejeb.calf.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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

    Box(modifier = modifier) {
        navController.currentDestination?.let { currentDestination ->
            graphBuilder.destinations.find { it.route == currentDestination }?.let { destination ->
                destination.content(destination.arguments)
            }
        }
    }
}