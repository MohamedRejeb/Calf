package com.mohamedrejeb.calf.sample

import androidx.compose.runtime.Composable
import com.mohamedrejeb.calf.navigation.AdaptiveNavHost
import com.mohamedrejeb.calf.navigation.AdaptiveNavHostController

enum class Screen {
    Home,
    Dialog,
    BottomSheet,
    DatePicker,
    TimePicker,
    ProgressBar,
    Switch,
    FilePicker,
    WebView,
    DropDownMenu,
}

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
    }
}