package com.mohamedrejeb.calf.sample.navigation

import androidx.compose.runtime.Composable
import com.mohamedrejeb.calf.navigation.AdaptiveNavHost
import com.mohamedrejeb.calf.navigation.AdaptiveNavHostController
import com.mohamedrejeb.calf.sample.ui.AlertDialogScreen
import com.mohamedrejeb.calf.sample.ui.BottomSheetScreen
import com.mohamedrejeb.calf.sample.ui.DatePickerScreen
import com.mohamedrejeb.calf.sample.ui.DropDownMenuScreen
import com.mohamedrejeb.calf.sample.ui.FilePickerScreen
import com.mohamedrejeb.calf.sample.ui.HomeScreen
import com.mohamedrejeb.calf.sample.ui.MapScreen
import com.mohamedrejeb.calf.sample.ui.PermissionScreen
import com.mohamedrejeb.calf.sample.ui.ProgressBarScreen
import com.mohamedrejeb.calf.sample.ui.SwitchScreen
import com.mohamedrejeb.calf.sample.ui.TimePickerScreen
import com.mohamedrejeb.calf.sample.ui.WebViewScreen

@Composable
fun AppNavGraph(
    navController: AdaptiveNavHostController
) {
    AdaptiveNavHost(
        navController = navController,
        startDestination = Screen.Home.name
    ) {
        composable(Screen.Home.name) {
            HomeScreen(
                navigate = {
                    navController.navigate(it)
                }
            )
        }
        composable(Screen.Dialog.name) {
            AlertDialogScreen(
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.BottomSheet.name) {
            BottomSheetScreen(
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.ProgressBar.name) {
            ProgressBarScreen(
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Switch.name) {
            SwitchScreen(
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.DropDownMenu.name) {
            DropDownMenuScreen(
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.DatePicker.name) {
            DatePickerScreen(
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.TimePicker.name) {
            TimePickerScreen(
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.FilePicker.name) {
            FilePickerScreen(
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.WebView.name) {
            WebViewScreen(
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Permission.name) {
            PermissionScreen(
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Map.name) {
            MapScreen(
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}