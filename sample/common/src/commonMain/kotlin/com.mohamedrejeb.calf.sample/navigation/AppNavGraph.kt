package com.mohamedrejeb.calf.sample.navigation

import androidx.compose.runtime.Composable
import com.mohamedrejeb.calf.navigation.AdaptiveNavHost
import com.mohamedrejeb.calf.navigation.AdaptiveNavHostController
import com.mohamedrejeb.calf.sample.screens.AdaptiveClickableScreen
import com.mohamedrejeb.calf.sample.screens.AlertDialogScreen
import com.mohamedrejeb.calf.sample.screens.BottomSheetScreen
import com.mohamedrejeb.calf.sample.screens.ButtonScreen
import com.mohamedrejeb.calf.sample.screens.CameraPickerScreen
import com.mohamedrejeb.calf.sample.screens.DropDownScreen
//import com.mohamedrejeb.calf.sample.screens.ExpandableFABScreen
import com.mohamedrejeb.calf.sample.screens.DatePickerScreen
import com.mohamedrejeb.calf.sample.screens.FilePickerScreen
import com.mohamedrejeb.calf.sample.screens.HomeScreen
import com.mohamedrejeb.calf.sample.screens.ImagePickerScreen
import com.mohamedrejeb.calf.sample.screens.FileSaverScreen
import com.mohamedrejeb.calf.sample.screens.ShowcaseScreen
//import com.mohamedrejeb.calf.sample.screens.MapScreen
import com.mohamedrejeb.calf.sample.screens.NavigationBarScreen
import com.mohamedrejeb.calf.sample.screens.PermissionScreen
import com.mohamedrejeb.calf.sample.screens.ProgressBarScreen
import com.mohamedrejeb.calf.sample.screens.ScaffoldDemoScreen
import com.mohamedrejeb.calf.sample.screens.SliderScreen
import com.mohamedrejeb.calf.sample.screens.SwitchScreen
import com.mohamedrejeb.calf.sample.screens.TimePickerScreen
import com.mohamedrejeb.calf.sample.screens.ToolbarScreen
import com.mohamedrejeb.calf.sample.screens.TopBarDemoScreen
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
        composable(Screen.Showcase.name) {
            ShowcaseScreen(
                navigateBack = {
                    navController.popBackStack()
                },
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
        composable(Screen.Slider.name) {
            SliderScreen(
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Button.name) {
            ButtonScreen(
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.DropDown.name) {
            DropDownScreen(
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
        composable(Screen.ProfilePicture.name) {
            FileSaverScreen(
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
//        composable(Screen.Map.name) {
//            MapScreen(
//                navigateBack = {
//                    navController.popBackStack()
//                }
//            )
//        }
        composable(Screen.NavigationBar.name) {
            NavigationBarScreen(
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.ScaffoldDemo.name) {
            ScaffoldDemoScreen(
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.TopBarDemo.name) {
            TopBarDemoScreen(
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
//        composable(Screen.ExpandableFAB.name) {
//            ExpandableFABScreen(
//                navigateBack = {
//                    navController.popBackStack()
//                }
//            )
//        }
        composable(Screen.Toolbar.name) {
            ToolbarScreen(
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
