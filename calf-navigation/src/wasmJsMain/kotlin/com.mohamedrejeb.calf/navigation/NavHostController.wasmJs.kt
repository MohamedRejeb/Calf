package com.mohamedrejeb.calf.navigation

actual typealias NavHostController = AdaptiveNavHostController

actual fun NavHostController.navigate(route: String) {
    navigate(route)
}

actual fun NavHostController.popBackStack(): Boolean {
    return popBackStack()
}
