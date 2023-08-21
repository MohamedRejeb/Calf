package com.mohamedrejeb.calf.navigation

import androidx.compose.runtime.Composable

public open class NavGraphBuilder {
    internal val destinations = mutableListOf<NavDestination>()

    /**
     * Add the destination to the [NavGraphBuilder]
     */
    public fun addDestination(destination: NavDestination) {
        destinations += destination
    }

    public fun composable(
        route: String,
        arguments: Map<String, NavArgument> = emptyMap(),
        content: @Composable (arguments: Map<String, NavArgument>) -> Unit
    ) {
        addDestination(NavDestination(route, arguments, content))
    }
}