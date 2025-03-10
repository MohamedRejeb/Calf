package com.mohamedrejeb.calf.sample.navigation

import androidx.compose.runtime.Composable
import com.mohamedrejeb.calf.navigation.AdaptiveNavHost
import com.mohamedrejeb.calf.navigation.AdaptiveNavHostController
import com.mohamedrejeb.calf.sample.screens.AdaptiveClickableScreen
import com.mohamedrejeb.calf.sample.screens.AlertDialogScreen
import com.mohamedrejeb.calf.sample.screens.BottomSheetScreen
import com.mohamedrejeb.calf.sample.screens.CameraPickerScreen
import com.mohamedrejeb.calf.sample.screens.DatePickerScreen
import com.mohamedrejeb.calf.sample.screens.DropDownMenuScreen
import com.mohamedrejeb.calf.sample.screens.FilePickerScreen
import com.mohamedrejeb.calf.sample.screens.HomeScreen
import com.mohamedrejeb.calf.sample.screens.ImagePickerScreen
import com.mohamedrejeb.calf.sample.screens.MapScreen
import com.mohamedrejeb.calf.sample.screens.PermissionScreen
import com.mohamedrejeb.calf.sample.screens.ProgressBarScreen
import com.mohamedrejeb.calf.sample.screens.SwitchScreen
import com.mohamedrejeb.calf.sample.screens.TimePickerScreen
import com.mohamedrejeb.calf.sample.screens.WebViewScreen

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
        composable(Screen.AdaptiveClickable.name) {
            AdaptiveClickableScreen(
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
        composable(Screen.ImagePicker.name) {
            ImagePickerScreen(
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.CameraPickerScreen.name) {
            CameraPickerScreen(
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