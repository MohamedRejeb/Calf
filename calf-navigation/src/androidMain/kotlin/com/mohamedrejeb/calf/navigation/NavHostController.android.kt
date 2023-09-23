package com.mohamedrejeb.calf.navigation

import androidx.navigation.NavHostController

actual typealias NavHostController = NavHostController

actual fun NavHostController.navigate(route: String) {
    navigate(route)
}

actual fun NavHostController.popBackStack(): Boolean {
    return popBackStack()
}