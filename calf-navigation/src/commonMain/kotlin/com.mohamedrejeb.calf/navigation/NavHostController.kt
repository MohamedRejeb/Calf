package com.mohamedrejeb.calf.navigation

expect class NavHostController

expect fun NavHostController.navigate(route: String)

expect fun NavHostController.popBackStack(): Boolean