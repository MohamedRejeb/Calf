package com.mohamedrejeb.calf.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun rememberNavController(): AdaptiveNavHostController {
    return rememberSaveable(saver = AdaptiveNavHostController.Saver()) {
        AdaptiveNavHostController()
    }
}

class AdaptiveNavHostController {
    val backStack = mutableStateListOf<String>()
    val currentDestination: String?
        get() = backStack.lastOrNull()

    fun navigate(
        route: String,
        arguments: Map<String, NavArgument> = emptyMap()
    ) {
        backStack.add(route)
    }

    fun popBackStack(): Boolean {
        return backStack.removeLastOrNull() != null
    }

    companion object {
        fun Saver(): Saver<AdaptiveNavHostController, *> {
            return Saver(
                save = {
                    it.backStack.toTypedArray()
               },
                restore = {
                    AdaptiveNavHostController().apply { backStack.addAll(it) }
                }
            )
        }
    }
}